# 生产部署手册（MVP）

## 1. 前置条件

1. 服务器安装：`java/graalvm-native-image`、`maven`、`node/npm`、`nginx`。
2. 可访问项目目录：`/home/workroom/items/app-note`。
3. 目标部署目录：`/home/app/app-note`。

## 2. 后端部署

1. 构建 native：

```bash
scripts/build-native-linux.sh --profile pro
```

2. 部署后端产物与配置：

```bash
scripts/deploy-app-note.sh --env pro
```

3. 启停管理：

```bash
bin/app start --env pro
bin/app stop
bin/app status
bin/app log
```

## 3. 前端部署

1. 构建前端：

```bash
scripts/build-note-ui-prod.sh
```

2. 发布到 nginx 静态目录并 reload：

```bash
scripts/deploy-note-ui-nginx.sh
```

## 4. 联动发布

```bash
scripts/release-all.sh --env pro
```

说明：当前版本失败即中断，不做自动回滚。

## 5. 发布后检查

1. 检查后端状态：

```bash
bin/app status
```

2. 打开页面：

- `http://<host>/app-note/`

3. 验证接口转发（按实际业务接口）：

- `http://<host>/app-note/auth/...`
- `http://<host>/app-note/api/...`
