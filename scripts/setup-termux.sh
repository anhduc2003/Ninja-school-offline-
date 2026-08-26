#!/data/data/com.termux/files/usr/bin/bash
set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PREFIX="${PREFIX:-/data/data/com.termux/files/usr}"
MYSQL_DATA="${PREFIX}/var/lib/mysql"
MYSQL_RUN="${PREFIX}/var/run/mysqld"
MYSQL_CNF="${ROOT_DIR}/.termux/mariadb.cnf"
INIT_MARKER="${ROOT_DIR}/.termux/mariadb-initialized"

printf '%s\n' '==> Cập nhật package Termux'
pkg update -y
pkg upgrade -y

printf '%s\n' '==> Cài Git, OpenJDK, Maven và MariaDB'
pkg install -y git maven mariadb
if ! command -v java >/dev/null 2>&1; then
  if ! pkg install -y openjdk-21; then
    printf '%s\n' 'Không có openjdk-21 trên mirror hiện tại; thử cài openjdk-17.'
    pkg install -y openjdk-17
  fi
fi

mkdir -p "${MYSQL_DATA}" "${MYSQL_RUN}" "${ROOT_DIR}/.termux" "${ROOT_DIR}/logs"

# Termux/Android không luôn cung cấp MAC address cho feedback plugin của MariaDB.
# --defaults-file phải đứng trước các option khác để bỏ qua my.cnf bên ngoài.
cat > "${MYSQL_CNF}" <<EOF
[client]
socket=${MYSQL_RUN}/mysqld.sock

[mysqld]
basedir=${PREFIX}
datadir=${MYSQL_DATA}
socket=${MYSQL_RUN}/mysqld.sock
pid-file=${ROOT_DIR}/.termux/mariadb.pid
port=3306
bind-address=127.0.0.1
skip-name-resolve
skip-networking=0
feedback=OFF
feedback_url=
innodb_use_native_aio=0
EOF

if [ -d "${MYSQL_DATA}/mysql" ]; then
  USER_DB_COUNT="$(find "${MYSQL_DATA}" -mindepth 1 -maxdepth 1 -type d \
    ! -name mysql ! -name performance_schema ! -name sys ! -name test \
    -printf '.' 2>/dev/null | wc -c)"
  if [ "${USER_DB_COUNT}" -eq 0 ] && [ ! -f "${INIT_MARKER}" ]; then
    BACKUP_DIR="${MYSQL_DATA}.failed-init.$(date +%Y%m%d-%H%M%S)"
    printf '%s\n' "Phát hiện thư mục MariaDB khởi tạo dở dang; sao lưu tại ${BACKUP_DIR}."
    mv "${MYSQL_DATA}" "${BACKUP_DIR}"
    mkdir -p "${MYSQL_DATA}"
  fi
fi

if [ ! -d "${MYSQL_DATA}/mysql" ]; then
  printf '%s\n' '==> Khởi tạo thư mục dữ liệu MariaDB (feedback=OFF, native AIO=OFF)'
  mariadb-install-db \
    --defaults-file="${MYSQL_CNF}" \
    --basedir="${PREFIX}" \
    --datadir="${MYSQL_DATA}" \
    --auth-root-authentication-method=normal
  touch "${INIT_MARKER}"
else
  printf '%s\n' 'MariaDB đã có thư mục dữ liệu; giữ nguyên dữ liệu hiện tại.'
fi

if [ ! -f "${ROOT_DIR}/config.properties" ]; then
  cp "${ROOT_DIR}/config.properties.example" "${ROOT_DIR}/config.properties"
  printf '%s\n' 'Đã tạo config.properties từ config.properties.example.'
fi

printf '%s\n' 'Hoàn tất bước cài dependency và chuẩn bị MariaDB. Tiếp theo hãy chạy init-db.sh hoặc run-server.sh.'
