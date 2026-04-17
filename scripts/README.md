# Scripts 说明

本目录存放项目构建、部署、启动脚本。当前优先支持 MVP 生产部署链路。

## 1. 核心脚本

### 1.1 后端构建与部署

1. `build-native-linux.sh`
- Linux 通用 native 构建脚本，可选 `--target-glibc` 做兼容校验（默认不限制目标版本）。
2. `build-native-centos7.sh`
- 兼容旧入口，内部转发到 `build-native-linux.sh --target-glibc 2.17`。
3. `deploy-app-note.sh`
- 部署 native 产物和配置到目标目录（默认 `/home/app/app-note`）。
4. `deploy-app-noe.sh`
- 兼容旧入口，内部转发到 `deploy-app-note.sh`。

### 1.2 后端运行管理

1. `backend-start.sh`
2. `backend-stop.sh`
3. `backend-restart.sh`
4. `backend-status.sh`
5. `backend-log.sh`

默认目录结构：
1. `bin/`
2. `config/`
3. `logs/`
4. `app-note`（可执行文件）
5. `meta/`（版本信息）
6. `bak/`（备份）

### 1.3 前端构建与部署

1. `build-note-ui-prod.sh`
- 构建 `note-ui` 并打包 zip（构建依赖安装改为 `npm ci`）。
2. `deploy-note-ui-nginx.sh`
- 将 `note-ui/dist` 部署到 nginx 静态目录（默认 `/usr/share/nginx/html/app-note`），并 reload nginx。

### 1.4 联动发布

1. `release-all.sh`
- 串行执行后端构建部署 + 前端构建部署 + 可选健康检查。
- 当前版本失败即中断，不做自动回滚。

## 2. 常用命令

```bash
# 后端 native 构建 + 部署 + 启动
scripts/build-native-linux.sh --profile pro
scripts/deploy-app-note.sh --env pro
scripts/backend-start.sh --env pro

# 前端构建 + nginx 发布
scripts/build-note-ui-prod.sh
scripts/deploy-note-ui-nginx.sh

# 联动发布
scripts/release-all.sh --env pro
```

### 2.1 构建后直接启动（前台）

```bash
# 构建完成后直接前台启动
scripts/build-native-linux.sh --profile dev --run
scripts/build-native-linux.sh --profile test --run
scripts/build-native-linux.sh --profile pro --run
```

### 2.2 后台启动（deploy + start 脚本）

`backend-start.sh` 支持 `dev|test|pro`，默认 `pro`。

```bash
# 先部署，再后台启动
scripts/deploy-app-note.sh --env dev
scripts/backend-start.sh --env dev

scripts/deploy-app-note.sh --env test
scripts/backend-start.sh --env test

scripts/deploy-app-note.sh --env pro
scripts/backend-start.sh --env pro
```

## 3. 环境变量模板

可参考：

- `deploy/.env.example`

其中包含 `DEST_ROOT`、`NGINX_WEB_ROOT`、`ENABLE_NGINX_RELOAD` 等配置示例。
