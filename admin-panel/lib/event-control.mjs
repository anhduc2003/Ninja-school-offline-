import { existsSync, mkdirSync, readFileSync, renameSync, writeFileSync } from "node:fs";
import { dirname, join, resolve } from "node:path";

export const EVENT_CATALOG = Object.freeze([
  { key: "off", label: "Không có sự kiện", className: "Exe_Z.event.OFF", eventId: 8, kind: "safe-off", assetPath: null, description: "Tắt gameplay event; chỉ hiện thông báo không có sự kiện." },
  { key: "gio_to", label: "Giỗ Tổ", className: "Exe_Z.event.GioTo", eventId: null, kind: "legacy", assetPath: null, description: "Event legacy có điểm tiêu xài/top; source không gán event ID riêng nên chỉ nên vận hành sau kiểm tra QA." },
  { key: "halloween", label: "Halloween", className: "Exe_Z.event.Halloween", eventId: 3, kind: "code-drop", assetPath: null, description: "Cơ chế hộp ma quỷ, quái và điểm/top từ code Java." },
  { key: "womens_international", label: "Quốc tế Phụ nữ", className: "Exe_Z.event.InternationalWomensDay", eventId: 6, kind: "code-drop", assetPath: null, description: "Event điểm tiêu xài và cửa hàng theo Java." },
  { key: "koro_king", label: "Koro King", className: "Exe_Z.event.KoroKing", eventId: 1, kind: "code-drop", assetPath: null, description: "Cơ chế Koro King/boss được điều khiển trong Java." },
  { key: "lunar_new_year", label: "Tết Nguyên Đán", className: "Exe_Z.event.LunarNewYear", eventId: 5, kind: "json-drop", assetPath: "item_roi/event_LunarNewYear/TET.json", description: "Bánh chưng/bánh tét, hộp may mắn và bảng top." },
  { key: "noel", label: "Noel", className: "Exe_Z.event.Noel", eventId: 4, kind: "json-drop", assetPath: "item_roi/event_Noel/NOEL.json", description: "Quà trang trí, tuần lộc/người tuyết và bảng top." },
  { key: "summer", label: "Mùa hè", className: "Exe_Z.event.SumMer", eventId: 7, kind: "json-drop", assetPath: "item_roi/event_SumMer/SUMMER.json", description: "Làm bánh, câu cá, hộp may mắn và bảng top." },
  { key: "trung_thu", label: "Trung Thu", className: "Exe_Z.event.TrungThu", eventId: 2, kind: "json-drop", assetPath: "item_roi/event_TrungThu/TRUNG_THU.json", description: "Lồng đèn, bánh trung thu, map effect và bảng top." },
  { key: "womens_vietnamese", label: "Phụ nữ Việt Nam", className: "Exe_Z.event.VietnameseWomensDay", eventId: 0, kind: "code-drop", assetPath: null, description: "Event điểm tiêu xài và cửa hàng theo Java." },
  { key: "star_festival", label: "Lễ hội Sao Đêm", className: "Exe_Z.event.StarFestival", eventId: 9, kind: "json-drop", assetPath: "item_roi/event_StarFestival/STAR_FESTIVAL.json", description: "Event mới: lồng đèn ngôi sao, đổi quà, điểm tiêu xài và top sao đêm." },
]);

const ROOT_DIR = resolve(dirname(new URL(import.meta.url).pathname), "../..");
export const EVENT_CONTROL_DIR = join(ROOT_DIR, "admin-panel", "data", "event-control");
export const PENDING_PLAN_PATH = join(EVENT_CONTROL_DIR, "pending.json");
export const HISTORY_DIR = join(EVENT_CONTROL_DIR, "history");
export const OVERRIDE_DIR = join(ROOT_DIR, "admin-panel", "data", "event-overrides");

export function findEvent(key) {
  return EVENT_CATALOG.find(event => event.key === key) || null;
}

export function parseProperties(raw) {
  const values = {};
  for (const line of String(raw).split(/\r?\n/)) {
    const trimmed = line.trim();
    if (!trimmed || trimmed.startsWith("#")) continue;
    const index = trimmed.indexOf("=");
    if (index > 0) values[trimmed.slice(0, index).trim()] = trimmed.slice(index + 1).trim();
  }
  return values;
}

export function updateProperties(raw, updates) {
  const seen = new Set();
  const lines = String(raw).split(/\r?\n/).map(line => {
    const match = line.match(/^\s*([^#=\s][^=]*)\s*=/);
    const key = match?.[1]?.trim();
    if (!key || !(key in updates)) return line;
    seen.add(key);
    return `${key}=${updates[key]}`;
  });
  for (const [key, value] of Object.entries(updates)) if (!seen.has(key)) lines.push(`${key}=${value}`);
  return `${lines.join("\n").replace(/\n+$/, "")}\n`;
}

export function validateDropTable(value) {
  let rows;
  try { rows = typeof value === "string" ? JSON.parse(value) : value; } catch { throw new Error("Drop JSON phải là một mảng JSON hợp lệ."); }
  if (!Array.isArray(rows) || rows.length === 0 || rows.length > 80) throw new Error("Drop JSON phải có từ 1 đến 80 vật phẩm.");
  const normalized = rows.map((row, index) => {
    const id = Number(row?.id);
    const percent = Number(row?.percent);
    if (!Number.isInteger(id) || id < 0 || id > 20000 || !Number.isFinite(percent) || percent <= 0 || percent > 100000) {
      throw new Error(`Dòng drop ${index + 1} cần id nguyên 0–20000 và percent lớn hơn 0, không quá 100000.`);
    }
    return { id, percent };
  });
  return normalized;
}

export function readJsonIfExists(path, fallback = null) {
  if (!existsSync(path)) return fallback;
  try { return JSON.parse(readFileSync(path, "utf8")); } catch { return fallback; }
}

export function atomicWriteJson(path, value) {
  mkdirSync(dirname(path), { recursive: true, mode: 0o700 });
  const tempPath = `${path}.${process.pid}.${Date.now()}.tmp`;
  writeFileSync(tempPath, `${JSON.stringify(value, null, 2)}\n`, { mode: 0o600 });
  renameSync(tempPath, path);
}

export function assetOverridePath(eventKey) {
  if (!/^[a-z_]+$/.test(eventKey)) throw new Error("Event key không hợp lệ.");
  return join(OVERRIDE_DIR, `${eventKey}.json`);
}

export function readEventAsset(event) {
  if (!event?.assetPath) return { source: "code", rows: [], path: null, error: null };
  const override = assetOverridePath(event.key);
  const selectedPath = existsSync(override) ? override : join(ROOT_DIR, event.assetPath);
  try {
    return { source: existsSync(override) ? "override" : "source", rows: validateDropTable(readFileSync(selectedPath, "utf8")), path: selectedPath, error: null };
  } catch (error) {
    return { source: "invalid", rows: [], path: selectedPath, error: error.message };
  }
}

export function readEventPlan() {
  return readJsonIfExists(PENDING_PLAN_PATH, null);
}

export function writePendingEventPlan(plan) {
  atomicWriteJson(PENDING_PLAN_PATH, plan);
}

export function eventConfigFromPlan(plan) {
  const end = new Date(plan.endAt);
  if (Number.isNaN(end.getTime())) throw new Error("Thời hạn event không hợp lệ.");
  return {
    "game.event": plan.className,
    "event.year": end.getFullYear(),
    "event.month": end.getMonth() + 1,
    "event.day": end.getDate(),
    "event.hour": end.getHours(),
    "event.minute": end.getMinutes(),
    "event.second": end.getSeconds(),
  };
}
