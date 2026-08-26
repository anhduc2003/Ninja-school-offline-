#!/data/data/com.termux/files/usr/bin/bash
set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PREFIX="${PREFIX:-/data/data/com.termux/files/usr}"
MYSQL_DATA="${PREFIX}/var/lib/mysql"
MYSQL_RUN="${PREFIX}/var/run/mysqld"

printf '%s\n' '==> Cập nhật package Termux'
pkg update -y
pkg upgrade -y

printf '%s\n' '==> Cài Git, OpenJDK, Maven và MariaDB'
pkg install -y git maven mariadb
if ! pkg install -y openjdk-21; then
  printf '%s\n' 'Không có openjdk-21 trên mirror hiện tại; thử cài openjdk-17.'
  pkg install -y openjdk-17
fi

mkdir -p "${MYSQL_DATA}" "${MYSQL_RUN}" "${ROOT_DIR}/.termux" "${ROOT_DIR}/logs"

if [ ! -d "${MYSQL_DATA}/mysql" ]; then
  printf '%s\n' '==> Khởi tạo thư mục dữ liệu MariaDB'
  mariadb-install-db \
    --basedir="${PREFIX}" \
    --datadir="${MYSQL_DATA}" \
    --auth-root-authentication-method=normal
fi

if [ ! -f "${ROOT_DIR}/config.properties" ]; then
  cp "${ROOT_DIR}/config.properties.example" "${ROOT_DIR}/config.properties"
  printf '%s\n' 'Đã tạo config.properties từ config.properties.example.'
fi

printf '%s\n' 'Hoàn tất cài đặt. Chạy scripts/start-db.sh, sau đó scripts/start-server.sh.'
