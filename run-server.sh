#!/data/data/com.termux/files/usr/bin/bash
set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

bash "${ROOT_DIR}/scripts/sync-source.sh"

if [ ! -f "${ROOT_DIR}/config.properties" ]; then
  cp "${ROOT_DIR}/config.properties.example" "${ROOT_DIR}/config.properties"
  printf '%s\n' 'Đã tạo config.properties từ file mẫu. Hãy kiểm tra cấu hình database rồi chạy lại.'
fi

node "${ROOT_DIR}/admin-panel/apply-event-plan.mjs"

bash "${ROOT_DIR}/scripts/start-db.sh"
if [ "${NSO_GIFT_CODE_MIGRATION:-1}" = "1" ]; then
  printf '%s\n' 'Kiểm tra migration Gift Code lifecycle trước khi mở Java/panel...'
  bash "${ROOT_DIR}/scripts/migrate-gift-code-lifecycle.sh"
else
  printf '%s\n' 'Bỏ qua migration Gift Code lifecycle theo NSO_GIFT_CODE_MIGRATION=0.'
fi
bash "${ROOT_DIR}/scripts/start-server.sh"
bash "${ROOT_DIR}/admin-panel/start-panel.sh"
bash "${ROOT_DIR}/admin-panel/start-scheduler.sh"

printf '%s\n' "Server game đang chạy. Game log: tail -f ${ROOT_DIR}/logs/server.log"
printf '%s\n' 'Web panel offline: http://127.0.0.1:18080'
