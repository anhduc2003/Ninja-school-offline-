#!/data/data/com.termux/files/usr/bin/bash
set -Eeuo pipefail
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PID_FILE="${ROOT_DIR}/data/scheduler.pid"
LOG_FILE="${ROOT_DIR}/../logs/admin-scheduler.log"
mkdir -p "${ROOT_DIR}/data" "$(dirname "${LOG_FILE}")"
if [ -f "${PID_FILE}" ] && kill -0 "$(cat "${PID_FILE}")" 2>/dev/null; then
  printf '%s\n' "Admin scheduler đang chạy với PID $(cat "${PID_FILE}")."
  exit 0
fi
nohup node "${ROOT_DIR}/scheduler.mjs" >>"${LOG_FILE}" 2>&1 < /dev/null &
echo $! > "${PID_FILE}"
sleep 1
kill -0 "$(cat "${PID_FILE}")" 2>/dev/null || { printf '%s\n' "Scheduler không khởi động được; xem ${LOG_FILE}" >&2; exit 1; }
printf '%s\n' "Admin scheduler đã chạy. Log: ${LOG_FILE}"
