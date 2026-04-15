# Scripts 说明

本目录存放项目常用构建脚本。

## 1. 脚本列表

- `build-native-centos7.sh`：构建后端 Spring Boot Native Image（默认 `pro` 环境），可选构建后直接启动。
- `build-note-ui-prod.sh`：构建前端 `note-ui` 产物并打包为 zip。

## 2. build-native-centos7.sh

### 2.1 用途

用于在支持 GraalVM Native Image 的环境中执行：

- `boot` 模块 native 打包（`mvn -pl boot -am -Pnative -DskipTests package`）
- 默认按 `pro` 配置参与 AOT/native 构建
- 可选构建后直接运行二进制

### 2.2 依赖

- `java`（建议 GraalVM JDK 17）
- `native-image`
- `mvn`
- `rpm`（用于打印 `glibc` 版本，非强依赖，失败不影响构建）

### 2.3 用法

```bash
scripts/build-native-centos7.sh [options] [maven-args...]
```

#### 参数

- `--profile <name>`：指定 Spring profile（默认 `pro`）
- `--run`：构建完成后直接启动 native 可执行文件
- `--help`：查看帮助
- `--`：后续参数透传给运行阶段（仅在 `--run` 时生效）

#### 环境变量

- `SPRING_PROFILES_ACTIVE`：未传 `--profile` 时使用，默认 `pro`
- `SPRING_CONFIG_LOCATION`：构建和运行时传入 `spring.config.location`
- `SPRING_CONFIG_ADDITIONAL_LOCATION`：构建和运行时传入 `spring.config.additional-location`
- `NATIVE_BIN`：`--run` 时指定可执行文件路径，默认 `boot/target/app-note`

### 2.4 示例

```bash
# 默认 pro 构建
scripts/build-native-centos7.sh

# 按 dev 配置构建
scripts/build-native-centos7.sh --profile dev

# 追加 Maven 参数
scripts/build-native-centos7.sh -DskipTests=false

# 指定附加配置目录并构建后启动
SPRING_CONFIG_ADDITIONAL_LOCATION=./config/ \
  scripts/build-native-centos7.sh --profile pro --run -- --server.port=8081
```

### 2.5 产物

- Native 可执行文件：`boot/target/app-note`（或 `boot/target` 下匹配 `app-note*` 的可执行文件）

## 3. build-note-ui-prod.sh

### 3.1 用途

用于构建前端 `note-ui` 生产包并压缩为 zip，方便发布或分发。

### 3.2 依赖

- `npm`
- `zip`

### 3.3 用法

```bash
scripts/build-note-ui-prod.sh [zip-output]
```

- 不传 `zip-output`：默认输出为仓库根目录下 `note-ui-dist-YYYYMMDD-HHMMSS.zip`
- 传绝对路径：输出到指定绝对路径
- 传相对路径：相对仓库根目录解析

### 3.4 执行流程

1. 检查 `npm`/`zip` 是否存在
2. 检查 `note-ui/` 目录
3. 若无 `node_modules`，自动执行 `npm install`
4. 执行 `npm run build`
5. 压缩 `note-ui/dist` 到 zip

### 3.5 示例

```bash
# 默认输出文件名
scripts/build-note-ui-prod.sh

# 指定相对路径输出
scripts/build-note-ui-prod.sh artifacts/note-ui-prod.zip

# 指定绝对路径输出
scripts/build-note-ui-prod.sh /tmp/note-ui-prod.zip
```
