#!/data/data/com.termux/files/usr/bin/bash
set -Eeuo pipefail
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PID_FILE="${ROOT_DIR}/data/panel.pid"
LOG_FILE="${ROOT_DIR}/../logs/admin-panel.log"
mkdir -p "${ROOT_DIR}/data" "$(dirname "${LOG_FILE}")"
if [ -f "${PID_FILE}" ] && kill -0 "$(cat "${PID_FILE}")" 2>/dev/null; then
  printf '%s\n' "Admin panel đang chạy với PID $(cat "${PID_FILE}")."
  exit 0
fi
command -v node >/dev/null 2>&1 || { printf '%s\n' 'Thiếu Node.js. Trên Termux: pkg install nodejs' >&2; exit 1; }
if [ ! -d "${ROOT_DIR}/node_modules/mysql2" ]; then
  (cd "${ROOT_DIR}" && npm install --omit=dev --no-audit --no-fund)
fi
nohup node "${ROOT_DIR}/server.mjs" >>"${LOG_FILE}" 2>&1 < /dev/null &
echo $! > "${PID_FILE}"
sleep 1
if kill -0 "$(cat "${PID_FILE}")" 2>/dev/null; then
  printf '%s\n' "Admin panel đã chạy: http://127.0.0.1:18080"
  printf '%s\n' "Log panel: ${LOG_FILE}"
else
  printf '%s\n' "Admin panel không khởi động được; xem ${LOG_FILE}" >&2
  rm -f "${PID_FILE}"
  exit 1
fi
