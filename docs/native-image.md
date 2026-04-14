# GraalVM Native Image 部署

本项目为 Spring Boot 3 应用（模块：`boot`）。已提供 `native` profile，用于构建 GraalVM Native Image 可执行文件。

## 1) 本机（macOS）验证构建

在仓库根目录执行：

```bash
mvn -pl boot -am -Pnative -DskipTests package
```

成功后可在 `boot/target/` 看到可执行文件（名称通常为 `app-note`）。

## 2) CentOS 7 构建与部署（glibc 2.17）

CentOS 7 的 `glibc` 通常为 `2.17`。为了保证可执行文件在 CentOS 7 上可运行，推荐**在 CentOS 7（或等价 glibc=2.17 的构建环境）里构建** native 可执行文件。

### 2.1 安装系统依赖

```bash
sudo yum install -y gcc gcc-c++ make zlib-devel
```

### 2.2 安装 GraalVM（Java 17）并确保 `native-image` 可用

要求：

- `java -version` 为 GraalVM Java 17
- `native-image --version` 可用

如果 `native-image` 不存在，通常需要执行（按你的 GraalVM 版本为准）：

```bash
gu install native-image
```

### 2.3 构建 native 可执行文件

在仓库根目录执行：

```bash
mvn -pl boot -am -Pnative -DskipTests package
```

产物输出目录：`boot/target/`（名称通常为 `app-note`）。

### 2.4 运行

将 `boot/target/app-note` 复制到 CentOS 7 目标机后运行：

```bash
./app-note
```

如果你依赖外部配置文件（如 `application.yaml`），建议按你当前 JVM 部署方式保持相同的配置挂载策略（例如：同目录 `config/`）。

## 3) 常见 native 运行时报错

### 3.1 `DruidDataSource` 无默认构造方法

如果看到类似报错：

`Failed to instantiate [com.alibaba.druid.pool.DruidDataSource]: No default constructor found`

通常是 native image 下反射元数据不完整导致。项目已在 `boot` 模块添加了 Druid 的 Spring AOT RuntimeHints（见 `org.zzt.note.boot.nativeimage.DruidRuntimeHints`），重新构建 native 可执行文件后即可生效。
