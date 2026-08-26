#!/data/data/com.termux/files/usr/bin/bash
set -Eeuo pipefail
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PID_FILE="${ROOT_DIR}/data/panel.pid"
bash "${ROOT_DIR}/stop-scheduler.sh" || true
if [ -f "${PID_FILE}" ] && kill -0 "$(cat "${PID_FILE}")" 2>/dev/null; then
  kill "$(cat "${PID_FILE}")" || true
fi
rm -f "${PID_FILE}"
printf '%s\n' 'Admin panel đã dừng.'
