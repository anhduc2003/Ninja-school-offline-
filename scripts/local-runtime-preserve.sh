#!/usr/bin/env bash
set -Eeuo pipefail

runtime_relative_paths() {
  printf '%s\n' \
    'config.properties' \
    '.termux' \
    'logs' \
    'admin-panel/config.local.json' \
    'admin-panel/data' \
    'admin-panel/backups' \
    'admin-panel/reports'
}

backup_local_runtime() {
  local source_dir="$1" backup_dir="$2" relative
  mkdir -p "${backup_dir}"
  while IFS= read -r relative; do
    [ -e "${source_dir}/${relative}" ] || continue
    mkdir -p "${backup_dir}/$(dirname "${relative}")"
    cp -a "${source_dir}/${relative}" "${backup_dir}/${relative}"
  done < <(runtime_relative_paths)
}

restore_local_runtime() {
  local backup_dir="$1" destination_dir="$2" relative
  [ -d "${backup_dir}" ] || return 0
  while IFS= read -r relative; do
    [ -e "${backup_dir}/${relative}" ] || continue
    mkdir -p "${destination_dir}/$(dirname "${relative}")"
    cp -a "${backup_dir}/${relative}" "${destination_dir}/${relative}"
  done < <(runtime_relative_paths)
  find "${destination_dir}/.termux" -maxdepth 1 -type f -name '*.pid' -delete 2>/dev/null || true
  find "${destination_dir}/admin-panel/data" -maxdepth 1 -type f -name '*.pid' -delete 2>/dev/null || true
}
