#!/usr/bin/env bash
set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SYNC_SCRIPT="${ROOT_DIR}/scripts/sync-source.sh"
WORK_DIR="$(mktemp -d)"
trap 'rm -rf "${WORK_DIR}"' EXIT

git_config() { git -c user.name='NSO test' -c user.email='nso-test@example.invalid' "$@"; }
REMOTE_DIR="${WORK_DIR}/remote.git"
SEED_DIR="${WORK_DIR}/seed"
LOCAL_DIR="${WORK_DIR}/local"
RELEASE_DIR="${WORK_DIR}/release-install"
UNSAFE_RELEASE_DIR="${WORK_DIR}/release-install-modified"

git init -q --bare "${REMOTE_DIR}"
git init -q -b main "${SEED_DIR}"
printf '%s\n' 'logs/' '.termux/' 'config.properties' 'target/' 'admin-panel/data/' > "${SEED_DIR}/.gitignore"
printf '%s\n' 'one' > "${SEED_DIR}/version.txt"
git_config -C "${SEED_DIR}" add .
git_config -C "${SEED_DIR}" commit -qm 'version one'
git -C "${SEED_DIR}" remote add origin "${REMOTE_DIR}"
git -C "${SEED_DIR}" push -q -u origin main
git -C "${REMOTE_DIR}" symbolic-ref HEAD refs/heads/main
git clone -q "${REMOTE_DIR}" "${LOCAL_DIR}"

printf '%s\n' 'two' > "${SEED_DIR}/version.txt"
git_config -C "${SEED_DIR}" add version.txt
git_config -C "${SEED_DIR}" commit -qm 'version two'
git -C "${SEED_DIR}" push -q origin main
printf '%s\n' 'local-db-config' > "${LOCAL_DIR}/config.properties"
NSO_SYNC_ROOT="${LOCAL_DIR}" NSO_SYNC_REMOTE=origin NSO_SYNC_BRANCH=main NSO_AUTO_SYNC=1 bash "${SYNC_SCRIPT}"
test "$(cat "${LOCAL_DIR}/version.txt")" = 'two'
test "$(cat "${LOCAL_DIR}/config.properties")" = 'local-db-config'
grep -q 'Đã fast-forward' "${LOCAL_DIR}/logs/sync.log"

printf '%s\n' 'three' > "${SEED_DIR}/version.txt"
git_config -C "${SEED_DIR}" add version.txt
git_config -C "${SEED_DIR}" commit -qm 'version three'
git -C "${SEED_DIR}" push -q origin main
git -C "${SEED_DIR}" tag -a v-test -m 'release test marker'
git -C "${SEED_DIR}" push -q origin v-test
printf '%s\n' 'local-manual-change' > "${LOCAL_DIR}/version.txt"
NSO_SYNC_ROOT="${LOCAL_DIR}" NSO_SYNC_REMOTE=origin NSO_SYNC_BRANCH=main NSO_AUTO_SYNC=1 bash "${SYNC_SCRIPT}"
test "$(cat "${LOCAL_DIR}/version.txt")" = 'local-manual-change'
grep -q 'thay đổi source local chưa commit' "${LOCAL_DIR}/logs/sync.log"

git clone -q "${REMOTE_DIR}" "${RELEASE_DIR}"
rm -rf "${RELEASE_DIR}/.git"
mkdir -p "${RELEASE_DIR}/.termux"
printf '%s\n' 'v-test' > "${RELEASE_DIR}/.termux/release-installed"
printf '%s\n' 'keep-this-config' > "${RELEASE_DIR}/config.properties"
printf '%s\n' 'four' > "${SEED_DIR}/version.txt"
git_config -C "${SEED_DIR}" add version.txt
git_config -C "${SEED_DIR}" commit -qm 'version four'
git -C "${SEED_DIR}" push -q origin main
NSO_SYNC_ROOT="${RELEASE_DIR}" NSO_SYNC_REMOTE=origin NSO_SYNC_BRANCH=main NSO_SYNC_REPOSITORY="${REMOTE_DIR}" NSO_AUTO_SYNC=1 NSO_SYNC_BOOTSTRAP=1 bash "${SYNC_SCRIPT}"
test -d "${RELEASE_DIR}/.git"
test "$(cat "${RELEASE_DIR}/version.txt")" = 'four'
test "$(cat "${RELEASE_DIR}/config.properties")" = 'keep-this-config'
grep -q 'Bootstrap thành công' "${RELEASE_DIR}/logs/sync.log"

git clone -q "${REMOTE_DIR}" "${UNSAFE_RELEASE_DIR}"
rm -rf "${UNSAFE_RELEASE_DIR}/.git"
mkdir -p "${UNSAFE_RELEASE_DIR}/.termux"
printf '%s\n' 'v-test' > "${UNSAFE_RELEASE_DIR}/.termux/release-installed"
NSO_SYNC_ROOT="${UNSAFE_RELEASE_DIR}" NSO_SYNC_REMOTE=origin NSO_SYNC_BRANCH=main NSO_SYNC_REPOSITORY="${REMOTE_DIR}" NSO_AUTO_SYNC=1 NSO_SYNC_BOOTSTRAP=1 bash "${SYNC_SCRIPT}"
test ! -d "${UNSAFE_RELEASE_DIR}/.git"
test "$(cat "${UNSAFE_RELEASE_DIR}/version.txt")" = 'four'
grep -q 'source hiện tại khác Release marker' "${UNSAFE_RELEASE_DIR}/logs/sync.log"

printf '%s\n' 'sync-source lifecycle tests passed.'
