# 前后端打包与生产部署方案（boot + note-ui）

- 文档日期：2026-04-17
- 适用范围：`boot`（后端） + `note-ui`（前端） + 服务器 `nginx`
- 目标：形成可重复、可回滚、可交接的打包/部署/启动脚本体系
- 本地会话ID（用于续聊）：`019d9917-28ef-7b42-8a3d-55948bca693c`
- 续聊命令：`codex resume 019d9917-28ef-7b42-8a3d-55948bca693c`

## 1. 当前现状与主要问题

### 1.1 后端（boot）

已有：
1. 已有 native 构建脚本：`scripts/build-native-centos7.sh`
2. 已有 JVM jar 启动脚本：`scripts/start-boot-jar.sh`
3. 已有一个部署脚本：`scripts/deploy-app-noe.sh`

问题：
1. 脚本命名和职责不统一：`deploy-app-noe.sh` 名称疑似拼写问题，且与其他脚本风格不一致。
   答：这个命名缺失有问题，因此可以修改为 `deploy-app-note.sh`。
2. 启动方式分散：native 与 jar 脚本并存，但没有统一的 `start/stop/restart/status` 管理入口。
   答：那个只是构建脚本，没有启动脚本。所以这次需要可以补充启动脚本。
3. 进程守护缺失：没有标准 `systemd` 服务文件，重启策略、日志归集、开机自启都未标准化。
   答：启动脚本目前不需要那么复杂的逻辑，核心就一点：启动、停止、日志记录就行。
4. 配置策略不完整：部署脚本只复制部分配置文件，环境切换和配置覆盖策略不够清晰。
   答：确实缺少，需要补充，并且补充详细的说明文件。
5. 回滚机制不足：虽然有备份目录，但没有“版本化发布目录 + 一键回滚”。
   答：现在不考虑。
6. 安全风险：资源文件中包含数据库账号信息，生产环境建议彻底外置配置与密钥。
   答：暂时不考虑安全问题。

### 1.2 前端（note-ui）

已有：
1. 已有构建压缩脚本：`scripts/build-note-ui-prod.sh`
2. 已有 `VUE_APP_API_BASE_URL=/app-note` 配置，与后端 context-path 对齐。

问题：
1. 只有“构建 zip”，缺“部署到 nginx + 原子切换 + 回滚”脚本。
   答：补充部署脚本。构建脚本和部署脚本需要分开。回滚脚本目前不实现。
2. 没有 nginx 配置模板（history 路由、静态缓存、API 反代、gzip 等）沉淀在仓库。
   答：这个很关键，建议沉淀在仓库中。并且需要有详细的说明文件。
3. 构建流程可重复性一般：脚本使用 `npm install`，生产建议优先 `npm ci` 保证锁版本一致。
   答：生产环境只是部署构建好的包吗？如果你觉得构建脚本不够稳定，你可以优化构建脚本。
4. 没有前后端联动发布脚本，发布时容易出现“前后端版本不匹配”。
   答：这个目前确实没有，你可以补充一个。

### 1.3 前后端协同

问题：
1. 缺统一发布目录规范（release id、current 软链接、artifact 清单）。
2. 缺统一验收脚本（健康检查、关键接口探活、前端页面可达性检查）。
3. 缺统一回滚流程（指定 release 回退并快速恢复）。

答：这三个问题暂时不考虑。

## 2. 目标方案（建议落地形态）

### 2.1 服务器目录规范（建议）

```text
/home/app/app-note/
  releases/
    20260417-120001/
      backend/
        app-note            # native 可执行文件
        config/
      frontend/
        dist/
      meta/
        build-info.txt
  current -> releases/20260417-120001
  shared/
    backend-config/         # 生产配置（不入库）
    logs/
```

答：这个目录结构我不是很满意。就是对于生产环境来说，目录结构不稳定。基于你这我觉得可以创建一个历史版本记录，然后增加一个说明文件，记录当前的版本信息。
推荐的目录结构: bin、config、logs、app-note（程序）、meta（存放构建信息）、bak（历史版本备份）

核心原则：
1. 每次发布生成独立 release 目录。
2. 通过 `current` 软链接做原子切换。
3. 回滚只需切回旧 release 并重启服务。

### 2.2 后端运行方式（按你的偏好：native）

1. 构建：`mvn -pl boot -am -Pnative -DskipTests package`
2. 部署产物：`boot/target/app-note`
3. 进程管理：`systemd`（`app-note.service`）
4. 配置加载：统一通过 `--spring.config.additional-location` 指向 `shared/backend-config/`
5. 日志：stdout/stderr 交给 `journald`，业务日志输出到 `shared/logs/`

### 2.3 前端运行方式（nginx）

1. 构建：`npm ci && npm run build`
2. 部署目录：`current/frontend/dist`
3. nginx：
1. `location /app-note/` 提供前端静态资源
2. `try_files` 支持 Vue history 路由
3. `location /app-note/api|/app-note/auth|/app-note/common` 反代后端

## 3. 任务拆分（按阶段执行）

### 阶段 A：规范与脚手架（P0）

目标：先统一命名和目录，不改业务逻辑。

交付：
1. 新建目录：
1. `scripts/release/backend`
2. `scripts/release/frontend`
3. `scripts/release/common`
4. `deploy/nginx`
5. `deploy/systemd`
2. 统一脚本命名：
1. `build-backend-native.sh`
2. `deploy-backend-native.sh`
3. `build-frontend.sh`
4. `deploy-frontend-nginx.sh`
5. `release-all.sh`
6. `rollback-release.sh`
3. 补充 `.env.example`（部署变量模板）。

### 阶段 B：后端脚本体系（P0）

目标：后端可独立完成 build -> deploy -> start -> verify。

交付：
1. 构建脚本：固定产物路径、记录 git commit、记录构建时间。
2. 部署脚本：发布到 `releases/<id>/backend`，并创建 `current`。
3. systemd 服务文件模板：`deploy/systemd/app-note.service`
4. 运维脚本：
1. `backend-start.sh`
2. `backend-stop.sh`
3. `backend-restart.sh`
4. `backend-status.sh`
5. 验证脚本：`backend-healthcheck.sh`（检查 `/app-note/auth/login` 等基础接口可达性）。

### 阶段 C：前端脚本体系（P0）

目标：前端可独立完成 build -> deploy -> nginx reload -> verify。

交付：
1. 构建脚本：优先 `npm ci`，产出 `dist` + 可选 tar/zip。
2. 部署脚本：将 `dist` 发布到 `releases/<id>/frontend` 并切换 `current`。
3. nginx 配置模板：`deploy/nginx/app-note.conf`
4. 验证脚本：检查首页、静态资源、history 路由回退行为。

### 阶段 D：一键发布与回滚（P1）

目标：前后端联动上线，失败可快速回滚。

交付：
1. `release-all.sh`：
1. 执行后端构建/部署
2. 执行前端构建/部署
3. 运行健康检查
4. 任一步失败自动回滚到上一个 release
2. `rollback-release.sh <release-id>`：
1. 切换 `current`
2. 重启后端
3. reload nginx

### 阶段 E：文档与交接（P1）

目标：任何同事按文档可完成发布。

交付：
1. `docs/deploy/deploy-prod.md`（完整发布手册）。
2. `docs/deploy/rollback.md`（回滚手册）。
3. `docs/deploy/checklist.md`（发布前/后检查清单）。

## 4. 实现细节建议

1. 所有脚本统一 `set -euo pipefail`。
2. 脚本统一参数：
1. `--env dev|test|pro`
2. `--release-id <id>`
3. `--dry-run`
4. `--no-backup`
3. 脚本公共函数抽到 `scripts/release/common/lib.sh`（日志、错误处理、目录切换、软链接原子替换）。
4. 构建与部署解耦：CI 产物可直接喂给 deploy 脚本。
5. 保留旧脚本一段时间，但标注 `deprecated`，避免一次性替换导致生产风险。

## 5. 风险与规避

1. 风险：native 二进制与目标机 glibc 不兼容。
规避：在与生产同版本系统构建（你已倾向 native，这一条必须严格执行）。

2. 风险：前端 history 路由 404（nginx 未配置 `try_files`）。
规避：把 nginx 模板纳入仓库并强制走脚本部署配置。

3. 风险：前端 API 前缀或反代路径不一致，导致 401/404。
规避：以 `/app-note` 为统一前缀，脚本发布后自动做 API smoke test。

4. 风险：发布中断造成半发布状态。
规避：先发布到新 release 目录，健康检查通过后再切换 `current`。

5. 风险：配置泄漏或误提交。
规避：生产配置放 `shared/backend-config`，仓库仅保留模板，不存真实密钥。

6. 风险：脚本改动影响现网稳定性。
规避：先在预发环境跑全流程，再切生产；首批发布保留人工确认开关。

## 6. 验收标准

1. 后端可通过一组标准命令完成：构建、部署、启动、重启、状态、健康检查。
2. 前端可通过一组标准命令完成：构建、部署、切换、回滚、验证。
3. 发生发布失败时，10 分钟内可恢复到上一可用版本。
4. 新同事只看 `docs/deploy` 文档可独立完成上线。

## 7. 建议执行顺序（最小风险）

1. 先做阶段 A（统一规范，不动业务）。
2. 完成阶段 B（后端 native + systemd），在预发验证。
3. 完成阶段 C（前端 + nginx），在预发验证。
4. 最后做阶段 D（一键发布/回滚）。
5. 阶段 E 收口文档和 checklist。

## 8. 最新共识执行计划（按当前版本）

说明：本章节覆盖当前对范围的最新共识，优先按“先可用、再完善”执行。

### 8.1 范围重定义

先不做：
1. 回滚脚本与自动回滚流程。
2. release + current 软链接方案。
3. 安全治理专项（如密钥外置改造）。
4. 复杂 `systemd` 守护方案。

本轮必做：
1. 后端 native：构建、部署、启动、停止、状态、日志。
2. 前端：构建脚本和 nginx 部署脚本分离。
3. nginx 配置模板入库，并补充可执行说明文档。
4. 目录结构按 `bin/ config/ logs/ app-note/ meta/ bak/` 落地，并增加版本记录文件。

### 8.2 MVP 执行步骤

1. 脚本规范化
1. 将 `scripts/deploy-app-noe.sh` 更名为 `scripts/deploy-app-note.sh`（并兼容旧入口或标注废弃）。
2. 统一脚本风格（`set -euo pipefail`、统一参数和日志格式）。

2. 后端链路
1. 保留并优化 `scripts/build-native-centos7.sh`（产物校验、构建信息写入 `meta/`）。
2. 完成 `scripts/deploy-app-note.sh`，按目标目录部署并写入 `bak/` 备份。
3. 新增后端运行脚本：`start/stop/status/log`（基于 PID 文件避免重复启动）。

3. 前端链路
1. 优化 `scripts/build-note-ui-prod.sh`（优先 `npm ci`，保持构建可重复）。
2. 新增 `scripts/deploy-note-ui-nginx.sh`，负责部署 `dist` 并执行 nginx reload。
3. 新增 nginx 模板：`deploy/nginx/app-note.conf`（history 路由 + `/app-note/api|auth|common` 反代）。

4. 联动发布（无自动回滚）
1. 新增 `scripts/release-all.sh`：后端部署 -> 前端部署 -> 基础健康检查。
2. 任一步失败即中断并提示人工处理，不做自动回退。

5. 文档补齐
1. `docs/deploy/deploy-prod.md`：构建、部署、启动、验证、常见问题。
2. `docs/deploy/nginx.md`：配置安装与 reload 操作说明。
3. `docs/deploy/versioning.md`：`meta` 版本记录格式与维护规则。

### 8.3 本轮验收标准（MVP）

1. 后端可通过脚本完成：构建、部署、启动、停止、状态查看、日志查看。
2. 前端可通过脚本完成：构建、部署、nginx reload、可达性验证。
3. 发布目录满足：`bin/ config/ logs/ app-note/ meta/ bak/` 且版本记录可追溯。
4. 文档可指导他人独立完成一次完整发布。
