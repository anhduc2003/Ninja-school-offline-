import { copyFileSync, existsSync, mkdirSync, readFileSync, renameSync, rmSync, writeFileSync } from "node:fs";
import { dirname, join, resolve } from "node:path";
import { EVENT_CONTROL_DIR, HISTORY_DIR, PENDING_PLAN_PATH, assetOverridePath, atomicWriteJson, eventConfigFromPlan, findEvent, readEventPlan, updateProperties, validateDropTable } from "./lib/event-control.mjs";

const ROOT_DIR = resolve(dirname(new URL(import.meta.url).pathname), "..");
const CONFIG_PATH = join(ROOT_DIR, "config.properties");
const PID_PATH = join(ROOT_DIR, ".termux", "server.pid");

function assertGameStopped() {
  if (!existsSync(PID_PATH)) return;
  const pid = Number(readFileSync(PID_PATH, "utf8").trim());
  if (!Number.isInteger(pid) || pid < 2) return;
  try { process.kill(pid, 0); } catch { return; }
  throw new Error(`Không áp dụng event pending khi Java game vẫn chạy (PID ${pid}). Hãy dừng game trước, rồi chạy lại run-server.sh.`);
}

function atomicWriteText(path, content) {
  const temp = `${path}.${process.pid}.${Date.now()}.tmp`;
  writeFileSync(temp, content, { mode: 0o600 });
  renameSync(temp, path);
}

const plan = readEventPlan();
if (!plan) {
  console.log("Không có event pending cần áp dụng.");
  process.exit(0);
}
const event = findEvent(plan.eventKey);
if (!event || event.className !== plan.className) throw new Error("Event pending không khớp catalog source hiện tại; hãy tạo lại plan trong panel.");
assertGameStopped();
if (!existsSync(CONFIG_PATH)) throw new Error("Thiếu config.properties; launcher sẽ tạo file này trước. Hãy chạy lại run-server.sh.");

const backupDir = join(EVENT_CONTROL_DIR, "config-backups");
mkdirSync(backupDir, { recursive: true, mode: 0o700 });
const backupPath = join(backupDir, `config-before-${plan.id}.properties`);
copyFileSync(CONFIG_PATH, backupPath);

if (event.assetPath && plan.dropTable) {
  atomicWriteJson(assetOverridePath(event.key), validateDropTable(plan.dropTable));
}
const config = updateProperties(readFileSync(CONFIG_PATH, "utf8"), eventConfigFromPlan(plan));
atomicWriteText(CONFIG_PATH, config);
mkdirSync(HISTORY_DIR, { recursive: true, mode: 0o700 });
atomicWriteJson(join(HISTORY_DIR, `${plan.id}.json`), { ...plan, status: "applied", appliedAt: new Date().toISOString(), configBackup: backupPath });
rmSync(PENDING_PLAN_PATH, { force: true });
console.log(`Đã áp dụng event pending: ${event.label}. Java sẽ nạp event khi khởi động.`);
