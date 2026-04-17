# 版本记录说明（MVP）

后端部署脚本 `scripts/deploy-app-note.sh` 会维护以下文件：

1. `meta/current-version.txt`
2. `meta/version-history.log`

## current-version.txt 字段

1. `build_time`：构建时间
2. `git_commit`：源码 commit
3. `env`：部署环境（dev/test/pro）
4. `binary_sha256`：二进制 hash
5. `binary_path`：部署后二进制路径

## version-history.log

每次部署追加一行，包含时间、commit、环境和 hash，用于追溯发布记录。
