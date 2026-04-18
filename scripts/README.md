# Scripts 说明

本目录存放项目构建、部署、启动脚本。当前优先支持 MVP 生产部署链路。

## 1. 核心脚本

### 1.1 后端构建与部署

1. `builder/build-native-linux.sh`
- Linux 通用 native 构建脚本，可选 `--target-glibc` 做兼容校验（默认不限制目标版本）。
2. `deploy/deploy-app-note.sh`
- 部署 native 产物和配置到目标目录（默认 `/home/app/app-note`）。

### 1.2 后端运行管理

1. `../note-project/bin/app`
- 统一后端管理入口：`start|stop|status|log`。

默认目录结构：
1. `bin/`
2. `config/`
3. `logs/`
4. `app-note`（可执行文件）
5. `meta/`（版本信息）
6. `bak/`（备份）

### 1.3 前端构建与部署

1. `builder/build-note-ui-prod.sh`
- 构建 `note-ui` 并打包 zip（构建依赖安装改为 `npm ci`）。
2. `deploy/deploy-note-ui-nginx.sh`
- 将 `note-ui/dist` 部署到 nginx 静态目录（默认 `/usr/share/nginx/html/app-note`），并 reload nginx。

### 1.4 联动发布

1. `release-all.sh`
- 串行执行后端构建部署 + 前端构建部署 + 可选健康检查。
- 当前版本失败即中断，不做自动回滚。

### 1.5 本地调试启动（JAR）

1. `start-boot-jar.sh`
- 用于本地临时调试：每次先执行 `mvn -pl note-project/boot -am package -DskipTests` 构建 jar，再把仓库根 `note-project/config/` 同步到 `note-project/boot/target/config/`，最后前台启动。
- JVM 参数风格：`-Dxxx=xxx`（如 `-Dspring.profiles.active=test`、`-Denv=local`）。
- 默认环境是 `dev`，只允许 `dev|test|local`，不允许 `pro`。

## 2. 常用命令

```bash
# 后端 native 构建 + 部署 + 启动
scripts/builder/build-native-linux.sh --profile pro
scripts/deploy/deploy-app-note.sh --env pro
note-project/bin/app start --no-tail

# 前端构建 + nginx 发布
scripts/builder/build-note-ui-prod.sh
scripts/deploy/deploy-note-ui-nginx.sh

# 联动发布
scripts/release-all.sh --env pro
```

### 2.1 本地调试启动（前台）

```bash
# 默认 dev
scripts/start-boot-jar.sh

# 使用本地环境（等价于 -Dspring.profiles.active=local）
scripts/start-boot-jar.sh -Denv=local

# 指定 test
scripts/start-boot-jar.sh -Dspring.profiles.active=test

# 透传 Spring Boot 参数
scripts/start-boot-jar.sh -Dspring.profiles.active=dev -- --server.port=19813
```

### 2.2 构建后直接启动（前台）

```bash
# 构建完成后直接前台启动
scripts/builder/build-native-linux.sh --profile dev --run
scripts/builder/build-native-linux.sh --profile test --run
scripts/builder/build-native-linux.sh --profile pro --run
```

### 2.3 后台启动（deploy + start 脚本）

`note-project/bin/app start` 固定 `pro` 环境启动；默认启动后自动 tail 应用日志，可用 `--no-tail` 关闭。

```bash
scripts/deploy/deploy-app-note.sh --env pro
note-project/bin/app start

# 不自动进入日志跟踪
note-project/bin/app start --no-tail
```

## 3. 环境变量模板

可参考：

- `deploy/.env.example`

其中包含 `DEST_ROOT`、`NGINX_WEB_ROOT`、`ENABLE_NGINX_RELOAD` 等配置示例。
