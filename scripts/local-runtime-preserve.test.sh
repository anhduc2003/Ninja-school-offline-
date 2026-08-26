#!/usr/bin/env bash
set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
source "${ROOT_DIR}/scripts/local-runtime-preserve.sh"

WORK_DIR="$(mktemp -d)"
trap 'rm -rf "${WORK_DIR}"' EXIT
OLD_DIR="${WORK_DIR}/old"
NEW_DIR="${WORK_DIR}/new"
BACKUP_DIR="${WORK_DIR}/backup"

mkdir -p "${OLD_DIR}/.termux/mariadb-data" "${OLD_DIR}/logs" "${OLD_DIR}/admin-panel/data" "${OLD_DIR}/admin-panel/backups" "${OLD_DIR}/admin-panel/reports"
printf '%s\n' 'db.host=127.0.0.1' > "${OLD_DIR}/config.properties"
printf '%s\n' 'database-stays' > "${OLD_DIR}/.termux/mariadb-data/marker"
printf '%s\n' 'old-log-stays' > "${OLD_DIR}/logs/server.log"
printf '%s\n' '{"port":18080}' > "${OLD_DIR}/admin-panel/config.local.json"
printf '%s\n' 'panel-state-stays' > "${OLD_DIR}/admin-panel/data/state.json"
printf '%s\n' 'backup-stays' > "${OLD_DIR}/admin-panel/backups/nsoz.sql.gz"
printf '%s\n' 'report-stays' > "${OLD_DIR}/admin-panel/reports/daily.md"
printf '%s\n' 'stale' > "${OLD_DIR}/.termux/server.pid"
printf '%s\n' 'stale' > "${OLD_DIR}/admin-panel/data/panel.pid"

backup_local_runtime "${OLD_DIR}" "${BACKUP_DIR}"
mkdir -p "${NEW_DIR}/admin-panel"
printf '%s\n' 'new-source-stays' > "${NEW_DIR}/new-source.txt"
restore_local_runtime "${BACKUP_DIR}" "${NEW_DIR}"

test "$(cat "${NEW_DIR}/config.properties")" = 'db.host=127.0.0.1'
test "$(cat "${NEW_DIR}/.termux/mariadb-data/marker")" = 'database-stays'
test "$(cat "${NEW_DIR}/logs/server.log")" = 'old-log-stays'
test "$(cat "${NEW_DIR}/admin-panel/config.local.json")" = '{"port":18080}'
test "$(cat "${NEW_DIR}/admin-panel/data/state.json")" = 'panel-state-stays'
test "$(cat "${NEW_DIR}/admin-panel/backups/nsoz.sql.gz")" = 'backup-stays'
test "$(cat "${NEW_DIR}/admin-panel/reports/daily.md")" = 'report-stays'
test "$(cat "${NEW_DIR}/new-source.txt")" = 'new-source-stays'
test ! -e "${NEW_DIR}/.termux/server.pid"
test ! -e "${NEW_DIR}/admin-panel/data/panel.pid"

printf '%s\n' 'local runtime preserve tests passed.'
