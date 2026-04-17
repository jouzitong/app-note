# Nginx 配置说明（app-note）

## 1. 模板位置

- `deploy/nginx/app-note.conf`

## 2. 配置要点

1. `/app-note/` 提供前端静态页面（Vue history 模式使用 `try_files` 回退）。
2. `/app-note/api|auth|common` 反代到后端 `127.0.0.1:19812`。

## 3. 安装步骤（示例）

```bash
cp deploy/nginx/app-note.conf /etc/nginx/conf.d/app-note.conf
nginx -t
systemctl reload nginx
```

## 4. 与部署脚本配合

`scripts/deploy-note-ui-nginx.sh` 默认将 `note-ui/dist` 发布到：

- `/usr/share/nginx/html/app-note`

可通过环境变量覆盖：

```bash
NGINX_WEB_ROOT=/data/nginx/html/app-note scripts/deploy-note-ui-nginx.sh
```
