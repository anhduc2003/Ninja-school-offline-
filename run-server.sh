#!/data/data/com.termux/files/usr/bin/bash
set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if [ ! -f "${ROOT_DIR}/config.properties" ]; then
  cp "${ROOT_DIR}/config.properties.example" "${ROOT_DIR}/config.properties"
  printf '%s\n' 'Đã tạo config.properties từ file mẫu. Hãy kiểm tra cấu hình database rồi chạy lại.'
fi

bash "${ROOT_DIR}/scripts/start-db.sh"
bash "${ROOT_DIR}/scripts/start-server.sh"

printf '%s\n' "Server game đang chạy. Xem log: tail -f ${ROOT_DIR}/logs/server.log"
