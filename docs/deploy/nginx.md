# Nginx 配置说明（app-note）

## 1. 目标与原则

1. 前端静态目录使用：`/data/fe/app-note`
2. 前端入口统一走：`/app-note/`
3. 后端接口保留独立前缀（如 `/app-note/api/`、`/app-note/auth/`、`/app-note/common/`），不要把整个 `/app-note/` 都反代到后端

说明：
- 如果把 `/app-note/` 整体反代到后端，会和 Vue History 路由冲突（前端页面刷新会被转发到后端）。
- 推荐后端新增接口优先收敛到 `/app-note/api/` 下的子路径，减少 Nginx 顶层 `location` 变更。

## 2. 模板位置

- `deploy/nginx/app-note.conf`

## 3. 推荐配置（按 /data/fe/app-note）

```nginx
server {
    listen 80;
    server_name _;

    # Backend proxy - use explicit prefixes to avoid conflicting with SPA routes
    location ^~ /app-note/api/ {
        proxy_pass http://127.0.0.1:19812;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location ^~ /app-note/auth/ {
        proxy_pass http://127.0.0.1:19812;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location ^~ /app-note/common/ {
        proxy_pass http://127.0.0.1:19812;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # Frontend static files
    location /app-note/ {
        alias /data/fe/app-note/;
        index index.html;
        try_files $uri $uri/ /app-note/index.html;
    }
}
```

扩展约定：
1. 如果后端未来新增顶层前缀（例如 `/app-note/admin/`），增加一个对应的 `location ^~ /app-note/admin/` 即可。
2. 不建议改成“一个正则匹配所有 `/app-note/*` 并反代”，会破坏前端页面路由回退。

## 4. 安装步骤（示例）

```bash
cp deploy/nginx/app-note.conf /etc/nginx/conf.d/app-note.conf
# 按本文件第 3 节修改 alias 到 /data/fe/app-note，并校对后端 location 前缀
nginx -t
systemctl reload nginx
```

## 5. 与前端部署脚本配合

`scripts/deploy-note-ui-nginx.sh` 默认发布到 `/usr/share/nginx/html/app-note`，如果你使用 `/data/fe/app-note`，发布时显式指定：

```bash
NGINX_WEB_ROOT=/data/fe/app-note scripts/deploy-note-ui-nginx.sh
```
