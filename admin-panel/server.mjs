import { randomBytes } from "node:crypto";
import { createReadStream, createWriteStream, existsSync, mkdirSync, readFileSync, statSync, writeFileSync } from "node:fs";
import { createServer } from "node:http";
import { dirname, extname, join, resolve } from "node:path";
import { fileURLToPath } from "node:url";
import { execFile } from "node:child_process";
import net from "node:net";
import mysql from "mysql2/promise";
import { randomToken } from "./lib/security.mjs";
import { availableColumns } from "./lib/schema.mjs";
import { hashGamePassword, validateGameUsername } from "./lib/game-account.mjs";
import { existingItemIds, validateInventory, validatePlayerStats } from "./lib/player-state.mjs";
import { resolveBootstrapPassword } from "./lib/bootstrap-password.mjs";
import { EVENT_CATALOG, findEvent, readEventAsset, readEventPlan, validateDropTable, writePendingEventPlan } from "./lib/event-control.mjs";
import { ECONOMY_RULES, detectEconomySignals, parseHistoryBalanceDelta } from "./lib/economy-monitor.mjs";

const __dirname = dirname(fileURLToPath(import.meta.url));
const ROOT_DIR = resolve(__dirname, "..");
const GAME_CONFIG_PATH = join(ROOT_DIR, "config.properties");
const DATA_DIR = join(__dirname, "data");
const BACKUP_DIR = join(__dirname, "backups");
const PUBLIC_DIR = join(__dirname, "public");
const ITEM_SPRITE_DIR = resolve(ROOT_DIR, "Data", "Img", "Small");
const MOB_SPRITE_DIR = resolve(ROOT_DIR, "Data", "Img", "Mob");
const CONFIG_PATH = join(__dirname, "config.local.json");
const FIRST_LOGIN_PATH = join(DATA_DIR, "first-login.txt");
const MAX_BODY_SIZE = 1_000_000;
const LOCAL_BIND_HOST = "127.0.0.1";
const LOCAL_CSRF_TOKEN = randomToken();
const LOCAL_ACTOR = Object.freeze({ id: 0, username: "local-only", role: "admin", authSource: "local-only", csrfToken: LOCAL_CSRF_TOKEN });
const tableColumnsCache = new Map();
const autoIncrementCache = new Map();
const modules = [
  ["dashboard", "Tổng quan", "Vận hành", "Sức khỏe game, MariaDB, port 14444 và hoạt động gần đây", "analyst"],
  ["players", "Người chơi", "Người chơi", "Tìm kiếm nhân vật, trạng thái online, map, level và tài sản", "analyst"],
  ["accounts", "Tài khoản", "Người chơi", "Tạo account game, tra cứu, lock, unlock, ban và kích hoạt", "moderator"],
  ["moderation", "Kiểm duyệt", "An toàn", "Ban có thời hạn và account status với audit; không giả lập kick/mute runtime", "moderator"],
  ["inventory", "Túi đồ", "Kinh tế", "Chỉnh bag/box/equipped/fashion offline với validator, snapshot và audit", "operator"],
  ["currency", "Tiền tệ", "Kinh tế", "Lượng, coin, xu, yên với transaction và xác nhận", "operator"],
  ["rewards", "Phần thưởng", "Kinh tế", "Gift code, reward delivery và theo dõi lịch sử", "operator"],
  ["reward-campaigns", "Reward Campaigns", "Kinh tế", "Fancung, quà tân thủ, nạp tích lũy và quà sự kiện bằng catalog", "admin"],
  ["reward-history", "Lịch sử reward", "Kinh tế", "Đối soát gift_code_histories theo dữ liệu game", "analyst"],
  ["custom-items", "Vật phẩm tùy biến", "Nội dung", "Tạo và chỉnh catalog item có validation", "operator"],
  ["shop", "Cửa hàng", "Nội dung", "Shopcoin và hàng hóa NPC từ stores/store_data", "operator"],
  ["consignment", "Kí gửi Shinwa", "Kinh tế", "Quản lý listing NPC Shinwa, giá, người bán, trạng thái, hạn và item detail", "admin"],
  ["events", "Event Control", "Nội dung", "Catalog, vật phẩm rơi, điểm/top và cấu hình chờ áp dụng sau restart Java", "operator"],
  ["rates", "Tỷ lệ game", "Nội dung", "EXP, level rate và option có phê duyệt", "admin"],
  ["bosses", "Quái & Boss", "Nội dung", "Metadata quái, boss, HP và bản đồ spawn", "operator"],
  ["bots", "Bot Player", "Vận hành", "Quản lý bot tự chơi: danh sách, bật/tắt, thêm/xoá và theo dõi trạng thái", "operator"],
  ["notices", "Thông báo", "Vận hành", "Cập nhật options.notify theo Java contract; broadcast runtime không tự động hóa", "moderator"],
  ["world-boss-notices", "Thông báo Boss thế giới", "Vận hành", "Cấu hình thông báo Boss xuất hiện/bị hạ theo group, server và cooldown", "admin"],
  ["maintenance", "Bảo trì", "Vận hành", "Draft/approval window và runbook restart; không dừng Java chỉ bằng SQL", "admin"],
  ["health", "Sức khỏe server", "Vận hành", "Liveness check và resource snapshot", "analyst"],
  ["incidents", "Sự cố", "Vận hành", "Triage, acknowledgement và escalation", "operator"],
  ["audit", "Audit trail", "Kiểm soát", "Dấu vết chỉ-append cho thao tác nhạy cảm", "analyst"],
  ["backups", "Sao lưu", "Kiểm soát", "Tạo MariaDB dump local và retention", "admin"],
  ["leaderboards", "Bảng xếp hạng", "Phân tích", "Danh sách top và refresh reference", "analyst"],
  ["analytics", "Phân tích", "Phân tích", "KPI player, economy và báo cáo vận hành", "analyst"],
  ["economy-monitor", "Kinh tế & cảnh báo", "Phân tích", "Dòng tiền, lịch sử giao dịch và tín hiệu bất thường cần GM review", "analyst"],
  ["inbox", "Hộp thư hệ thống", "Vận hành", "Theo dõi notification bền vững, seller nhận mail và trạng thái delivery", "analyst"],
  ["jobs", "Tác vụ định kỳ", "Kiểm soát", "Health check, cleanup, report và transition đã phê duyệt", "admin"],
  ["security", "Chế độ local-only", "Kiểm soát", "Không có đăng nhập; panel chỉ lắng nghe trên chính thiết bị", "viewer"],
].map(([id, label, group, description, role]) => ({ id, label, group, description, role }));
const WORLD_BOSS_GROUPS = [
  { key: "normal", label: "Boss thường" },
  { key: "vdmq", label: "Vùng đất ma quỷ" },
  { key: "ltt", label: "Làng truyền thuyết" },
  { key: "lc", label: "Làng cổ" },
  { key: "hangvithu", label: "Hang vĩ thú" },
  { key: "SK", label: "Boss sự kiện" },
];
const WORLD_BOSS_EVENT_TYPES = ["spawn", "defeat"];

function readGameProperties() {
  if (!existsSync(GAME_CONFIG_PATH)) return {};
  return Object.fromEntries(readFileSync(GAME_CONFIG_PATH, "utf8").split(/\r?\n/).map(line => line.trim()).filter(line => line && !line.startsWith("#") && line.includes("=")).map(line => {
    const index = line.indexOf("=");
    return [line.slice(0, index).trim(), line.slice(index + 1).trim()];
  }));
}

function worldBossNotificationConfig(body) {
  const id = Number(body.id || 0);
  const serverId = Number(body.serverId ?? 0);
  const bossGroup = String(body.bossGroup || "");
  const eventType = String(body.eventType || "spawn");
  const sender = String(body.sender || "Hệ thống").trim().slice(0, 40);
  const messageTemplate = String(body.messageTemplate || "").trim().slice(0, 500);
  const cooldownSeconds = Number(body.cooldownSeconds ?? 0);
  const parseDate = value => { if (!value) return null; const date = new Date(value); if (Number.isNaN(date.getTime())) throw new Error("Thời gian hiệu lực không hợp lệ."); return date; };
  const startsAt = parseDate(body.startsAt);
  const endsAt = parseDate(body.endsAt);
  if (!Number.isInteger(id) || id < 0 || !Number.isInteger(serverId) || serverId < 0 || serverId > 255) throw new Error("ID hoặc Server ID không hợp lệ.");
  if (!WORLD_BOSS_GROUPS.some(group => group.key === bossGroup) || !WORLD_BOSS_EVENT_TYPES.includes(eventType)) throw new Error("Nhóm Boss hoặc loại sự kiện không hợp lệ.");
  if (!sender || !messageTemplate || messageTemplate.length < 3) throw new Error("Người gửi và nội dung thông báo không được trống.");
  if (!Number.isInteger(cooldownSeconds) || cooldownSeconds < 0 || cooldownSeconds > 86400) throw new Error("Cooldown phải từ 0 đến 86.400 giây.");
  if (startsAt && endsAt && startsAt >= endsAt) throw new Error("Thời điểm kết thúc phải sau thời điểm bắt đầu.");
  const allowedPlaceholders = ["{boss}", "{map}", "{zone}", "{killer}", "{time}"];
  const unsupported = (messageTemplate.match(/\{[^}]+\}/g) || []).filter(token => !allowedPlaceholders.includes(token));
  if (unsupported.length) throw new Error(`Placeholder không hỗ trợ: ${unsupported.join(", ")}.`);
  return { id, serverId, bossGroup, eventType, sender, messageTemplate, cooldownSeconds, enabled: body.enabled === false || String(body.enabled) === "0" ? 0 : 1, startsAt, endsAt };
}

async function worldBossNotificationData() {
  const [configs] = await pool.query(`SELECT n.id, n.server_id, n.boss_group, n.event_type, n.sender, n.message_template, n.cooldown_seconds, n.enabled, n.starts_at, n.ends_at, n.created_at, n.updated_at, COALESCE(l.sent_count, 0) AS sent_count FROM panel_world_boss_notifications n LEFT JOIN (SELECT config_id, COUNT(*) AS sent_count FROM panel_world_boss_notification_logs GROUP BY config_id) l ON l.config_id = n.id ORDER BY n.enabled DESC, n.server_id, n.boss_group, n.event_type`);
  const [logs] = await pool.query(`SELECT l.id, l.config_id, n.boss_group, l.event_type, l.boss_name, l.map_name, l.zone_id, l.message, l.online_players, l.sent_at FROM panel_world_boss_notification_logs l LEFT JOIN panel_world_boss_notifications n ON n.id = l.config_id ORDER BY l.sent_at DESC LIMIT 200`);
  return { configs, logs, groups: WORLD_BOSS_GROUPS };
}

async function saveWorldBossNotification(user, body) {
  const config = worldBossNotificationConfig(body);
  let id = config.id;
  if (id) {
    const [result] = await pool.query(`UPDATE panel_world_boss_notifications SET server_id = ?, boss_group = ?, event_type = ?, sender = ?, message_template = ?, cooldown_seconds = ?, enabled = ?, starts_at = ?, ends_at = ?, updated_by = ? WHERE id = ? LIMIT 1`, [config.serverId, config.bossGroup, config.eventType, config.sender, config.messageTemplate, config.cooldownSeconds, config.enabled, config.startsAt, config.endsAt, user.id, id]);
    if (!result.affectedRows) throw new Error("Không tìm thấy cấu hình Boss notification.");
  } else {
    const [result] = await pool.query(`INSERT INTO panel_world_boss_notifications (server_id, boss_group, event_type, sender, message_template, cooldown_seconds, enabled, starts_at, ends_at, created_by, updated_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`, [config.serverId, config.bossGroup, config.eventType, config.sender, config.messageTemplate, config.cooldownSeconds, config.enabled, config.startsAt, config.endsAt, user.id, user.id]);
    id = result.insertId;
  }
  await audit(user, "world-boss-notices", "world_boss_notification.saved", "world_boss_notification", String(id), "success", { serverId: config.serverId, bossGroup: config.bossGroup, eventType: config.eventType, enabled: config.enabled });
  return id;
}

const SHINWA_STATUS_LABELS = { 0: "Đang bán", 1: "Đã bán", 2: "Đã nhận lại" };

function parseShinwaItem(serialized, optionNames) {
  try {
    const item = JSON.parse(serialized || "{}");
    const options = Array.isArray(item.options) ? item.options.map(option => {
      const id = Number(option?.[0]);
      const param = Number(option?.[1] || 0);
      return { id, name: optionNames.get(id) || `Option #${id}`, param };
    }).filter(option => Number.isInteger(option.id) && option.id >= 0) : [];
    return { itemId: Number(item.id || 0), quantity: Number(item.quantity || 1), isLock: Boolean(item.isLock), sys: Number(item.sys || 0), upgrade: Number(item.upgrade || 0), yen: Number(item.yen || 0), expire: Number(item.expire || -1), options, parseError: false };
  } catch (error) {
    return { itemId: 0, quantity: 0, isLock: false, sys: 0, upgrade: 0, yen: 0, expire: -1, options: [], parseError: true };
  }
}

async function shinwaData(searchParams) {
  const gameConfig = readGameProperties();
  const serverId = Number(gameConfig["server.id"] || 0);
  const page = Math.max(1, Number(searchParams.get("page") || 1));
  const limit = Math.min(100, Math.max(10, Number(searchParams.get("limit") || 50)));
  const offset = (page - 1) * limit;
  const q = String(searchParams.get("q") || "").trim().slice(0, 80);
  const status = searchParams.get("status");
  const params = [serverId];
  let where = "s.server_id = ?";
  if (q) { where += " AND (s.seller LIKE CONCAT('%', ?, '%') OR i.name LIKE CONCAT('%', ?, '%') OR CAST(s.id AS CHAR) = ?)"; params.push(q, q, q); }
  if (["0", "1", "2"].includes(status)) { where += " AND s.status = ?"; params.push(Number(status)); }
  const [rows] = await pool.query(`SELECT s.id, s.server_id, s.seller, s.price, s.status, s.time, s.item, n.notified_at AS expiry_notified_at, m.delivery_status AS expiry_delivery_status, m.delivered_at AS expiry_delivered_at, m.read_at AS expiry_read_at, JSON_UNQUOTE(JSON_EXTRACT(s.item, '$.id')) AS item_id, i.name AS item_name, i.icon, i.type AS item_type, i.level AS item_level FROM shinwa s LEFT JOIN item i ON i.id = CAST(JSON_UNQUOTE(JSON_EXTRACT(s.item, '$.id')) AS UNSIGNED) LEFT JOIN panel_shinwa_expiry_notifications n ON n.shinwa_id = s.id AND n.server_id = s.server_id LEFT JOIN panel_player_inbox m ON m.server_id = s.server_id AND m.source_type = 'shinwa_expiry' AND m.source_id = CAST(s.id AS CHAR) WHERE ${where} ORDER BY s.status ASC, s.id DESC LIMIT ? OFFSET ?`, [...params, limit, offset]);
  const [countRows] = await pool.query(`SELECT COUNT(*) AS total FROM shinwa s LEFT JOIN item i ON i.id = CAST(JSON_UNQUOTE(JSON_EXTRACT(s.item, '$.id')) AS UNSIGNED) WHERE ${where}`, params);
  const [optionRows] = await pool.query("SELECT id, name FROM item_option ORDER BY id");
  const optionNames = new Map(optionRows.map(row => [Number(row.id), row.name]));
  const normalized = rows.map(row => { const item = parseShinwaItem(row.item, optionNames); const deliveryLabel = row.expiry_delivery_status === "delivered" ? "Đã gửi hộp thư" : row.expiry_delivery_status === "pending" ? "Đã ghi hộp thư · Chờ delivery" : row.expiry_delivery_status === "failed" ? "Delivery lỗi" : "Chưa gửi hộp thư"; return { id: row.id, server_id: row.server_id, seller: row.seller, price: row.price, status: row.status, status_label: SHINWA_STATUS_LABELS[row.status] || `Trạng thái #${row.status}`, expiry_notified_at: row.expiry_notified_at || null, expiry_delivery_status: row.expiry_delivery_status || null, expiry_delivered_at: row.expiry_delivered_at || null, expiry_read_at: row.expiry_read_at || null, expiry_notification_label: deliveryLabel, time: row.time, item_id: item.itemId || Number(row.item_id || 0), item_name: row.item_name || `Vật phẩm #${item.itemId || row.item_id || "?"}`, icon: row.icon, item_type: row.item_type, item_level: row.item_level, quantity: item.quantity, is_lock: item.isLock, sys: item.sys, upgrade: item.upgrade, yen: item.yen, expire: item.expire, options: item.options, parse_error: item.parseError }; });
  return { rows: normalized, total: Number(countRows[0]?.total || 0), page, limit, statuses: SHINWA_STATUS_LABELS };
}

function boundedHours(searchParams) {
  const hours = Number(searchParams.get("hours") || 24);
  return Number.isInteger(hours) && hours >= 1 && hours <= 168 ? hours : 24;
}

async function upsertEconomySignals(signals) {
  for (const signal of signals) {
    await pool.query(`INSERT INTO panel_economy_alerts (dedupe_key, rule_key, severity, status, server_id, player_id, user_id, player_name, username, title, description, evidence) VALUES (?, ?, ?, 'open', ?, ?, ?, ?, ?, ?, ?, ?)
      ON DUPLICATE KEY UPDATE rule_key = VALUES(rule_key), severity = VALUES(severity), player_id = VALUES(player_id), user_id = VALUES(user_id), player_name = VALUES(player_name), username = VALUES(username), title = VALUES(title), description = VALUES(description), evidence = VALUES(evidence), updated_at = CURRENT_TIMESTAMP`, [signal.dedupe_key, signal.rule_key, signal.severity, signal.server_id, signal.player_id, signal.user_id, signal.player_name, signal.username, signal.title, signal.description, JSON.stringify(signal.evidence)]);
  }
}

async function economyMonitorData(searchParams) {
  const gameConfig = readGameProperties();
  const serverId = Number(gameConfig["server.id"] || 0);
  const hours = boundedHours(searchParams);
  const historyEnabled = String(gameConfig["open.historySQL"] || "false").toLowerCase() === "true";
  const sinceInterval = `INTERVAL ${hours} HOUR`;
  const [[summary], [highBalances], [negativeBalances], [giftBursts], [rewardBursts], [ipClusters], [shinwaConcentration], [historyRows], [shinwaSummary], [inboxSummary]] = await Promise.all([
    pool.query(`SELECT COUNT(*) AS players, COALESCE(SUM(p.online = 1), 0) AS online_players, CAST(COALESCE(SUM(p.xu), 0) AS CHAR) AS total_xu, CAST(COALESCE(SUM(p.xuInBox), 0) AS CHAR) AS total_xu_box, CAST(COALESCE(SUM(p.yen), 0) AS CHAR) AS total_yen FROM players p WHERE p.server_id = ?`, [serverId]),
    pool.query(`SELECT p.id AS player_id, p.user_id, p.server_id, p.name AS player_name, CAST(COALESCE(p.xu, 0) + COALESCE(p.xuInBox, 0) AS CHAR) AS total_xu, p.xu, p.xuInBox, p.yen, u.username, u.coin, u.luong, u.balance FROM players p LEFT JOIN users u ON u.id = p.user_id WHERE p.server_id = ? AND (COALESCE(p.xu, 0) + COALESCE(p.xuInBox, 0) >= ? OR COALESCE(u.coin, 0) >= ? OR COALESCE(u.luong, 0) >= ?) ORDER BY COALESCE(p.xu, 0) + COALESCE(p.xuInBox, 0) DESC LIMIT 100`, [serverId, ECONOMY_RULES.highBalanceXu, ECONOMY_RULES.highBalanceXu, ECONOMY_RULES.highBalanceXu]),
    pool.query(`SELECT p.id AS player_id, p.user_id, p.server_id, p.name AS player_name, p.xu, p.xuInBox, p.yen, u.username, u.coin, u.luong, u.balance FROM players p LEFT JOIN users u ON u.id = p.user_id WHERE p.server_id = ? AND (COALESCE(p.xu, 0) < 0 OR COALESCE(p.xuInBox, 0) < 0 OR COALESCE(p.yen, 0) < 0 OR COALESCE(u.coin, 0) < 0 OR COALESCE(u.luong, 0) < 0 OR COALESCE(u.balance, 0) < 0) LIMIT 100`, [serverId]),
    pool.query(`SELECT h.player_id, p.name AS player_name, p.server_id, COUNT(*) AS claims_24h FROM gift_code_histories h LEFT JOIN players p ON p.id = h.player_id WHERE h.created_at >= UTC_TIMESTAMP() - ${sinceInterval} AND (p.server_id = ? OR p.server_id IS NULL) GROUP BY h.player_id, p.name, p.server_id HAVING COUNT(*) >= ? ORDER BY claims_24h DESC LIMIT 100`, [serverId, ECONOMY_RULES.giftClaims24h]),
    pool.query(`SELECT c.player_id, p.name AS player_name, p.server_id, COUNT(*) AS claims_24h FROM panel_reward_claims c LEFT JOIN players p ON p.id = c.player_id WHERE c.claimed_at >= UTC_TIMESTAMP() - ${sinceInterval} AND (p.server_id = ? OR p.server_id IS NULL) GROUP BY c.player_id, p.name, p.server_id HAVING COUNT(*) >= ? ORDER BY claims_24h DESC LIMIT 100`, [serverId, ECONOMY_RULES.rewardClaims24h]),
    pool.query(`SELECT COALESCE(NULLIF(u.ip, ''), 'unknown') AS ip, COUNT(DISTINCT u.id) AS accounts, COUNT(DISTINCT CASE WHEN p.server_id = ? THEN p.id END) AS players, ? AS server_id FROM users u LEFT JOIN players p ON p.user_id = u.id WHERE u.ip IS NOT NULL AND u.ip NOT IN ('', '0') GROUP BY u.ip HAVING COUNT(DISTINCT u.id) >= ? ORDER BY accounts DESC LIMIT 100`, [serverId, serverId, ECONOMY_RULES.sameIpAccounts]),
    pool.query(`SELECT seller, server_id, COUNT(*) AS listings, CAST(COALESCE(SUM(price), 0) AS CHAR) AS total_value FROM shinwa WHERE server_id = ? AND status = 0 GROUP BY server_id, seller HAVING COUNT(*) >= ? OR SUM(price) >= ? ORDER BY listings DESC, total_value DESC LIMIT 100`, [serverId, ECONOMY_RULES.activeShinwaListings, ECONOMY_RULES.activeShinwaValue]),
    historyEnabled ? pool.query(`SELECT h.id, h.player_id, p.user_id, p.server_id, p.name AS player_name, h.type_name, h.truoc, h.sau, h.items, h.bo_sung, h.time FROM history_table h LEFT JOIN players p ON p.id = h.player_id WHERE h.time >= UTC_TIMESTAMP() - ${sinceInterval} AND (p.server_id = ? OR p.server_id IS NULL) ORDER BY h.time DESC LIMIT 500`, [serverId]) : Promise.resolve([[]]),
    pool.query(`SELECT COUNT(*) AS active_listings, CAST(COALESCE(SUM(price), 0) AS CHAR) AS active_value, COUNT(DISTINCT seller) AS active_sellers FROM shinwa WHERE server_id = ? AND status = 0`, [serverId]),
    pool.query(`SELECT COUNT(*) AS total, COALESCE(SUM(delivery_status = 'pending'), 0) AS pending, COALESCE(SUM(delivery_status = 'delivered'), 0) AS delivered FROM panel_player_inbox WHERE server_id = ?`, [serverId]),
  ]);
  const largeDeltas = historyRows.map(parseHistoryBalanceDelta).filter(row => row.maxAbsDelta >= ECONOMY_RULES.largeDeltaXu);
  const signals = detectEconomySignals({ negativeBalances, highBalances, largeDeltas, giftBursts, rewardBursts, ipClusters, shinwaConcentration }, new Date());
  await upsertEconomySignals(signals);
  const status = String(searchParams.get("status") || "");
  const statusFilter = ["open", "acknowledged", "resolved", "false_positive"].includes(status) ? " AND a.status = ?" : "";
  const alertParams = statusFilter ? [serverId, status] : [serverId];
  const [alerts] = await pool.query(`SELECT a.id, a.dedupe_key, a.rule_key, a.severity, a.status, a.server_id, a.player_id, a.user_id, a.player_name, a.username, a.title, a.description, a.evidence, a.note, a.created_at, a.updated_at FROM panel_economy_alerts a WHERE a.server_id = ?${statusFilter} ORDER BY FIELD(a.status, 'open', 'acknowledged', 'resolved', 'false_positive'), FIELD(a.severity, 'critical', 'warning', 'info'), a.created_at DESC LIMIT 300`, alertParams);
  return { serverId, hours, historyEnabled, summary: summary || {}, shinwa: shinwaSummary?.[0] || {}, inbox: inboxSummary?.[0] || {}, highBalances, negativeBalances, largeDeltas: largeDeltas.slice(0, 100), alerts, signalCount: signals.length, thresholds: ECONOMY_RULES };
}

async function inboxData(searchParams) {
  const serverId = Number(readGameProperties()["server.id"] || 0);
  const status = String(searchParams.get("status") || "");
  const playerId = Number(searchParams.get("player_id") || 0);
  const params = [serverId];
  let where = "i.server_id = ?";
  if (["pending", "delivered", "failed"].includes(status)) { where += " AND i.delivery_status = ?"; params.push(status); }
  if (Number.isInteger(playerId) && playerId > 0) { where += " AND i.player_id = ?"; params.push(playerId); }
  const [rows] = await pool.query(`SELECT i.id, i.server_id, i.player_id, i.user_id, p.name AS player_name, i.category, i.title, i.body, i.source_type, i.source_id, i.dedupe_key, i.delivery_status, i.read_at, i.delivered_at, i.created_at, i.updated_at FROM panel_player_inbox i LEFT JOIN players p ON p.id = i.player_id WHERE ${where} ORDER BY i.created_at DESC LIMIT 300`, params);
  return { rows, serverId, statuses: ["pending", "delivered", "failed"] };
}

async function eventControlData() {
  const game = readGameProperties();
  const activeClassName = game["game.event"] || "Exe_Z.event.OFF";
  const activeEvent = EVENT_CATALOG.find(event => event.className === activeClassName) || null;
  const eventColumns = await tableColumns("event_points");
  let rows = [];
  let summary = [];
  let unavailable = null;
  if (eventColumns.size === 0) {
    unavailable = "Schema game không có event_points; vẫn tạo được plan event nhưng chưa có dữ liệu điểm/top để xem.";
  } else {
    [summary] = await pool.query("SELECT event_id, COUNT(*) AS player_count FROM event_points GROUP BY event_id ORDER BY event_id");
    [rows] = await pool.query("SELECT ep.id, ep.event_id, ep.player_id, p.name AS player_name, ep.point FROM event_points ep LEFT JOIN players p ON p.id = ep.player_id ORDER BY ep.event_id, ep.id DESC LIMIT 200");
  }
  const hasEnd = ["event.year", "event.month", "event.day", "event.hour", "event.minute", "event.second"].every(key => game[key] !== undefined);
  return {
    active: {
      className: activeClassName,
      eventKey: activeEvent?.key || null,
      endAt: hasEnd ? `${game["event.year"]}-${String(game["event.month"]).padStart(2, "0")}-${String(game["event.day"]).padStart(2, "0")} ${String(game["event.hour"]).padStart(2, "0")}:${String(game["event.minute"]).padStart(2, "0")}:${String(game["event.second"]).padStart(2, "0")}` : null,
    },
    pending: readEventPlan(),
    catalog: EVENT_CATALOG.map(event => {
      const asset = readEventAsset(event);
      return { ...event, asset: { source: asset.source, rows: asset.rows, error: asset.error } };
    }),
    rows,
    summary,
    unavailable,
    applyRunbook: "Panel chỉ lưu kế hoạch pending. Dừng Java game, rồi chạy bash run-server.sh; launcher sao lưu config.properties, áp dụng plan và Java nạp event khi khởi động kế tiếp.",
  };
}

async function validateEventDropItems(dropTable) {
  const ids = [...new Set(dropTable.map(row => row.id))];
  const columns = await tableColumns("item");
  if (columns.size === 0) throw new Error("Schema game thiếu catalog item; không thể an toàn xác thực drop table event.");
  const [rows] = await pool.query(`SELECT id FROM item WHERE id IN (${ids.map(() => "?").join(",")})`, ids);
  const found = new Set(rows.map(row => Number(row.id)));
  const missing = ids.filter(id => !found.has(id));
  if (missing.length) throw new Error(`Item ID không tồn tại trong catalog: ${missing.join(", ")}.`);
}

const GIFT_LIFECYCLE_COLUMNS = ["starts_at", "max_redemptions", "redemption_count", "disabled"];

async function giftLifecycleStatus() {
  const columns = await tableColumns("gift_codes");
  const missing = GIFT_LIFECYCLE_COLUMNS.filter(column => !columns.has(column));
  return { ready: missing.length === 0, missing };
}

function parseGiftDate(value, label, required = false) {
  if (value === null || value === undefined || String(value).trim() === "") {
    if (required) throw new Error(`${label} là bắt buộc.`);
    return null;
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) throw new Error(`${label} không hợp lệ.`);
  return date;
}

async function normalizeGiftRewards(value) {
  let input;
  try { input = typeof value === "string" ? JSON.parse(value || "[]") : value; } catch { throw new Error("Reward items phải là JSON hợp lệ."); }
  if (!Array.isArray(input) || input.length > 30) throw new Error("Reward items phải là mảng gồm 0 đến 30 vật phẩm.");
  const ids = [...new Set(input.map(item => Number(item?.id)))];
  if (ids.some(id => !Number.isInteger(id) || id < 0)) throw new Error("Mỗi reward item cần item ID nguyên không âm.");
  if (ids.length === 0) return [];
  const [itemRows, optionRows] = await Promise.all([
    pool.query(`SELECT id, name, type, isUpToUp FROM item WHERE id IN (${ids.map(() => "?").join(",")})`, ids),
    pool.query("SELECT id FROM item_option"),
  ]);
  const itemsById = new Map(itemRows[0].map(item => [Number(item.id), item]));
  const optionIds = new Set(optionRows[0].map(option => Number(option.id)));
  const missing = ids.filter(id => !itemsById.has(id));
  if (missing.length) throw new Error(`Reward item ID không tồn tại: ${missing.join(", ")}.`);
  return input.map((raw, index) => {
    const item = itemsById.get(Number(raw.id));
    const quantity = Number(raw.quantity ?? 1);
    const sys = Number(raw.sys ?? 0);
    const upgrade = Number(raw.upgrade ?? 0);
    const yen = Number(raw.yen ?? 0);
    const expireDays = Number(raw.expire_days ?? raw.expireDays ?? 0);
    const isLock = raw.isLock !== false;
    const options = raw.options ?? [];
    if (!Number.isInteger(quantity) || quantity < 1 || quantity > 9999 || !Number.isInteger(sys) || sys < 0 || sys > 9 || !Number.isInteger(upgrade) || upgrade < 0 || upgrade > 30 || !Number.isSafeInteger(yen) || yen < 0 || !Number.isInteger(expireDays) || expireDays < 0 || expireDays > 3650 || !Array.isArray(options) || options.length > 20) {
      throw new Error(`Reward item dòng ${index + 1} có quantity, sys, upgrade, yên, hạn hoặc options không hợp lệ.`);
    }
    const normalizedOptions = options.map((option, optionIndex) => {
      if (!Array.isArray(option) || option.length !== 2) throw new Error(`Option ${optionIndex + 1} của reward item dòng ${index + 1} phải là [optionId, value].`);
      const optionId = Number(option[0]); const amount = Number(option[1]);
      if (!Number.isInteger(optionId) || !optionIds.has(optionId) || !Number.isInteger(amount) || amount < -2_000_000_000 || amount > 2_000_000_000) throw new Error(`Option ${optionIndex + 1} của reward item dòng ${index + 1} không tồn tại hoặc giá trị vượt giới hạn.`);
      return [optionId, amount];
    });
    return { id: item.id, quantity, isLock, yen, sys, upgrade, expire: -1, expire_days: expireDays, options: normalizedOptions, item_name: item.name, item_type: item.type };
  });
}

async function giftControlData() {
  const lifecycle = await giftLifecycleStatus();
  if (!lifecycle.ready) return { migrationRequired: `Thiếu cột ${lifecycle.missing.join(", ")}. Chạy: bash scripts/migrate-gift-code-lifecycle.sh`, rows: [], history: [], summary: {} };
  const [rows, history, summaryRows, options] = await Promise.all([
    pool.query(`SELECT g.id, g.server_id, g.type, g.code, g.coin, g.gold, g.yen, g.items, g.status, g.disabled, g.starts_at, g.expires_at, g.max_redemptions, g.redemption_count, g.created_at, g.updated_at,
      CASE WHEN g.disabled = 1 THEN 'disabled' WHEN g.status = 1 THEN 'consumed' WHEN g.starts_at IS NOT NULL AND g.starts_at > NOW() THEN 'scheduled' WHEN g.expires_at IS NOT NULL AND g.expires_at <= NOW() THEN 'expired' WHEN g.max_redemptions IS NOT NULL AND g.redemption_count >= g.max_redemptions THEN 'exhausted' ELSE 'active' END AS lifecycle,
      COUNT(h.id) AS history_count
      FROM gift_codes g LEFT JOIN gift_code_histories h ON h.gift_code = g.code GROUP BY g.id ORDER BY g.created_at DESC LIMIT 200`),
    pool.query(`SELECT h.id, h.gift_code, h.created_at, h.updated_at, p.name AS player_name, u.username FROM gift_code_histories h LEFT JOIN players p ON p.id = h.player_id LEFT JOIN users u ON u.id = h.user_id ORDER BY h.created_at DESC LIMIT 200`),
    pool.query(`SELECT COUNT(*) AS total_codes, SUM(disabled = 1) AS disabled_codes, SUM(status = 1) AS consumed_codes, SUM(starts_at IS NOT NULL AND starts_at > NOW()) AS scheduled_codes, SUM(expires_at IS NOT NULL AND expires_at <= NOW()) AS expired_codes, COALESCE(SUM(redemption_count), 0) AS total_redemptions FROM gift_codes`),
    pool.query("SELECT id, type, name FROM item_option ORDER BY id"),
  ]);
  return { rows: rows[0], history: history[0], summary: summaryRows[0][0] || {}, optionCatalog: options[0] };
}

function configFromGameProperties(template) {
  const game = readGameProperties();
  if (Object.keys(game).length === 0) return template;
  const port = Number(game["db.port"]);
  const controlPort = Number(game["server.control.port"]);
  return {
    ...template,
    runtimeControl: {
      ...template.runtimeControl,
      host: game["server.control.host"] || template.runtimeControl?.host || "127.0.0.1",
      port: Number.isInteger(controlPort) && controlPort > 0 && controlPort <= 65535 ? controlPort : (template.runtimeControl?.port || 18081),
      token: game["server.control.token"] || template.runtimeControl?.token || "",
    },
    database: {
      ...template.database,
      host: game["db.host"] || template.database.host,
      port: Number.isInteger(port) && port > 0 && port <= 65535 ? port : template.database.port,
      user: game["db.user"] || template.database.user,
      password: game["db.password"] ?? template.database.password,
      name: game["db.dbname"] || template.database.name,
    },
  };
}

function ensureLocalConfig() {
  mkdirSync(DATA_DIR, { recursive: true, mode: 0o700 });
  mkdirSync(BACKUP_DIR, { recursive: true, mode: 0o700 });
  if (!existsSync(CONFIG_PATH)) {
    const template = JSON.parse(readFileSync(join(__dirname, "config.example.json"), "utf8"));
    writeFileSync(CONFIG_PATH, `${JSON.stringify(configFromGameProperties(template), null, 2)}\n`, { mode: 0o600 });
  }
  return JSON.parse(readFileSync(CONFIG_PATH, "utf8"));
}

const config = ensureLocalConfig();
const runtimeControl = {
  host: "127.0.0.1",
  port: Number.isInteger(Number(config.runtimeControl?.port)) && Number(config.runtimeControl.port) > 0 && Number(config.runtimeControl.port) <= 65535 ? Number(config.runtimeControl.port) : 18081,
  token: String(config.runtimeControl?.token || "").trim(),
};

async function runtimeControlRequest(path, options = {}) {
  if (!runtimeControl.token) throw new Error("Runtime broadcast chưa được bật. Hãy đặt server.control.token trong config.properties rồi tạo hoặc cập nhật admin-panel/config.local.json với runtimeControl.token tương ứng.");
  const response = await fetch(`http://${runtimeControl.host}:${runtimeControl.port}${path}`, {
    ...options,
    headers: { Accept: "application/json", Authorization: `Bearer ${runtimeControl.token}`, ...(options.headers || {}) },
    signal: AbortSignal.timeout(3000),
  });
  const raw = await response.text();
  let body = {};
  try { body = raw ? JSON.parse(raw) : {}; } catch { body = {}; }
  if (!response.ok) throw new Error(body.error || `Runtime control trả về HTTP ${response.status}.`);
  return body;
}

async function runtimeControlStatus() {
  if (!runtimeControl.token) return { configured: false, online: false, endpoint: `127.0.0.1:${runtimeControl.port}`, error: "Chưa cấu hình token." };
  try {
    const body = await runtimeControlRequest("/api/control/health");
    return { configured: true, online: true, endpoint: `127.0.0.1:${runtimeControl.port}`, onlinePlayers: Number(body.onlinePlayers || 0) };
  } catch (error) {
    return { configured: true, online: false, endpoint: `127.0.0.1:${runtimeControl.port}`, error: error.message || "Không kết nối được Java runtime." };
  }
}

const pool = mysql.createPool({
  host: config.database.host,
  port: config.database.port,
  user: config.database.user,
  password: config.database.password,
  database: config.database.name,
  waitForConnections: true,
  connectionLimit: 6,
  namedPlaceholders: false,
});

async function tableColumns(tableName) {
  if (!/^[a-z_]+$/.test(tableName)) throw new Error("Tên bảng schema không hợp lệ.");
  if (!tableColumnsCache.has(tableName)) {
    const [rows] = await pool.query(
      "SELECT column_name FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ?",
      [tableName],
    );
    tableColumnsCache.set(tableName, new Set(rows.map(row => row.column_name)));
  }
  return tableColumnsCache.get(tableName);
}

async function hasAutoIncrementId(tableName) {
  if (!/^[a-z_]+$/.test(tableName)) throw new Error("Tên bảng schema không hợp lệ.");
  if (!autoIncrementCache.has(tableName)) {
    const [rows] = await pool.query(
      "SELECT extra FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ? AND column_name = 'id' LIMIT 1",
      [tableName],
    );
    autoIncrementCache.set(tableName, String(rows[0]?.extra || "").toLowerCase().includes("auto_increment"));
  }
  return autoIncrementCache.get(tableName);
}

async function ensureSchemaColumn(tableName, columnName, definition) {
  const columns = await tableColumns(tableName);
  if (!columns.has(columnName)) {
    await pool.query(`ALTER TABLE ${tableName} ADD COLUMN ${columnName} ${definition}`);
    tableColumnsCache.delete(tableName);
  }
}

async function ensureSchema() {
  const statements = [
    `CREATE TABLE IF NOT EXISTS panel_audit_events (
      id CHAR(36) NOT NULL PRIMARY KEY,
      actor_id INT NULL,
      actor_username VARCHAR(64) NULL,
      module_name VARCHAR(64) NOT NULL,
      action_name VARCHAR(100) NOT NULL,
      resource_type VARCHAR(64) NOT NULL,
      resource_id VARCHAR(96) NULL,
      outcome ENUM('success','denied','failed') NOT NULL,
      correlation_id CHAR(36) NOT NULL,
      metadata JSON NULL,
      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      INDEX panel_audit_created_idx (created_at),
      INDEX panel_audit_module_idx (module_name, created_at)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4`,
    `CREATE TABLE IF NOT EXISTS panel_jobs (
      id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
      name VARCHAR(120) NOT NULL,
      job_type VARCHAR(64) NOT NULL,
      cron_expression VARCHAR(64) NOT NULL,
      payload JSON NOT NULL,
      enabled TINYINT(1) NOT NULL DEFAULT 0,
      approved_by INT NULL,
      created_by INT NOT NULL,
      last_run_at TIMESTAMP NULL,
      last_result VARCHAR(500) NULL,
      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4`,
    `CREATE TABLE IF NOT EXISTS panel_job_runs (
      id CHAR(36) NOT NULL PRIMARY KEY,
      job_id INT NOT NULL,
      outcome ENUM('success','denied','failed') NOT NULL,
      details JSON NULL,
      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      INDEX panel_job_runs_job_created_idx (job_id, created_at)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4`,
    `CREATE TABLE IF NOT EXISTS panel_alerts (
      id CHAR(36) NOT NULL PRIMARY KEY,
      alert_key VARCHAR(80) NOT NULL,
      severity ENUM('warning','critical') NOT NULL,
      message VARCHAR(500) NOT NULL,
      status ENUM('open','acknowledged','resolved') NOT NULL DEFAULT 'open',
      acknowledged_by INT NULL,
      acknowledged_at TIMESTAMP NULL,
      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      INDEX panel_alerts_key_status_idx (alert_key, status),
      INDEX panel_alerts_status_created_idx (status, created_at)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4`,
    `CREATE TABLE IF NOT EXISTS panel_player_snapshots (
      id CHAR(36) NOT NULL PRIMARY KEY,
      player_id INT NOT NULL,
      snapshot_type ENUM('stats','inventory') NOT NULL,
      before_state JSON NOT NULL,
      after_state JSON NOT NULL,
      created_by INT NOT NULL,
      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      INDEX panel_player_snapshots_player_created_idx (player_id, created_at)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4`,
    `CREATE TABLE IF NOT EXISTS panel_maintenance_windows (
      id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
      starts_at DATETIME NOT NULL,
      ends_at DATETIME NOT NULL,
      message VARCHAR(500) NOT NULL,
      status ENUM('draft','scheduled','closed','cancelled') NOT NULL DEFAULT 'draft',
      created_by INT NOT NULL,
      approved_by INT NULL,
      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      INDEX panel_maintenance_time_idx (starts_at, ends_at),
      INDEX panel_maintenance_status_idx (status, starts_at)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4`,
          `CREATE TABLE IF NOT EXISTS panel_reward_campaigns (

      id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
      campaign_key VARCHAR(80) NOT NULL UNIQUE,
      title VARCHAR(180) NOT NULL,
      npc_key VARCHAR(32) NOT NULL,
      server_id INT NOT NULL DEFAULT 0,
      campaign_type VARCHAR(24) NOT NULL,
      requirement_key VARCHAR(100) NULL,
      requirement_value BIGINT NOT NULL DEFAULT 0,
      claim_scope ENUM('user','player') NOT NULL DEFAULT 'player',
      coin BIGINT NOT NULL DEFAULT 0,
      gold BIGINT NOT NULL DEFAULT 0,
      yen BIGINT NOT NULL DEFAULT 0,
      active TINYINT(1) NOT NULL DEFAULT 0,
      starts_at DATETIME NULL,
      ends_at DATETIME NULL,
      created_by INT NOT NULL,
      updated_by INT NULL,
      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      INDEX panel_reward_campaign_lookup_idx (npc_key, campaign_type, active, starts_at, ends_at)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4`,
    `CREATE TABLE IF NOT EXISTS panel_reward_items (
      id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
      campaign_id INT NOT NULL,
      item_id INT NOT NULL,
      quantity INT NOT NULL DEFAULT 1,
      is_lock TINYINT(1) NOT NULL DEFAULT 1,
      sys INT NOT NULL DEFAULT 0,
      upgrade INT NOT NULL DEFAULT 0,
      yen BIGINT NOT NULL DEFAULT 0,
      expire_days INT NOT NULL DEFAULT 0,
      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      INDEX panel_reward_items_campaign_idx (campaign_id, id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4`,
    `CREATE TABLE IF NOT EXISTS panel_reward_item_options (
      id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
      reward_item_id INT NOT NULL,
      option_id INT NOT NULL,
      min_value INT NOT NULL,
      max_value INT NOT NULL,
      INDEX panel_reward_item_options_item_idx (reward_item_id, id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4`,
    `CREATE TABLE IF NOT EXISTS panel_reward_claims (
      id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
      campaign_id INT NOT NULL,
      user_id INT NOT NULL,
      player_id INT NOT NULL,
      claim_key VARCHAR(120) NOT NULL UNIQUE,
      claimed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      UNIQUE KEY panel_reward_claim_unique (campaign_id, user_id, player_id),
      INDEX panel_reward_claim_campaign_idx (campaign_id, claimed_at),
      INDEX panel_reward_claim_user_idx (user_id, claimed_at)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4`,
    `CREATE TABLE IF NOT EXISTS panel_shinwa_expiry_notifications (
      shinwa_id INT NOT NULL,
      server_id INT NOT NULL,
      seller VARCHAR(100) NOT NULL,
      notified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      PRIMARY KEY (shinwa_id, server_id),
      INDEX panel_shinwa_expiry_seller_idx (seller, server_id, notified_at)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4`,
    `CREATE TABLE IF NOT EXISTS panel_player_inbox (
      id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
      server_id INT NOT NULL DEFAULT 0,
      player_id INT NOT NULL,
      user_id BIGINT NULL,
      category VARCHAR(40) NOT NULL DEFAULT 'system',
      title VARCHAR(160) NOT NULL,
      body TEXT NOT NULL,
      source_type VARCHAR(40) NOT NULL,
      source_id VARCHAR(120) NOT NULL,
      dedupe_key VARCHAR(180) NOT NULL,
      delivery_status ENUM('pending','delivered','failed') NOT NULL DEFAULT 'pending',
      read_at DATETIME NULL,
      delivered_at DATETIME NULL,
      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      UNIQUE KEY panel_player_inbox_dedupe_unique (dedupe_key),
      INDEX panel_player_inbox_player_idx (server_id, player_id, delivery_status, created_at),
      INDEX panel_player_inbox_source_idx (source_type, source_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4`,
    `CREATE TABLE IF NOT EXISTS panel_economy_alerts (
      id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
      dedupe_key VARCHAR(180) NOT NULL,
      rule_key VARCHAR(80) NOT NULL,
      severity ENUM('info','warning','critical') NOT NULL DEFAULT 'info',
      status ENUM('open','acknowledged','resolved','false_positive') NOT NULL DEFAULT 'open',
      server_id INT NOT NULL DEFAULT 0,
      player_id INT NULL,
      user_id BIGINT NULL,
      player_name VARCHAR(80) NULL,
      username VARCHAR(80) NULL,
      title VARCHAR(180) NOT NULL,
      description VARCHAR(500) NOT NULL,
      evidence JSON NULL,
      note VARCHAR(500) NULL,
      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      UNIQUE KEY panel_economy_alert_dedupe_unique (dedupe_key),
      INDEX panel_economy_alert_status_idx (status, severity, created_at),
      INDEX panel_economy_alert_player_idx (server_id, player_id, created_at),
      INDEX panel_economy_alert_rule_idx (rule_key, created_at)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4`,
    `CREATE TABLE IF NOT EXISTS panel_world_boss_notifications (
      id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
      server_id INT NOT NULL DEFAULT 0,
      boss_group VARCHAR(32) NOT NULL,
      event_type ENUM('spawn','defeat') NOT NULL,
      sender VARCHAR(40) NOT NULL DEFAULT 'Hệ thống',
      message_template VARCHAR(500) NOT NULL,
      cooldown_seconds INT NOT NULL DEFAULT 0,
      enabled TINYINT(1) NOT NULL DEFAULT 0,
      starts_at DATETIME NULL,
      ends_at DATETIME NULL,
      created_by INT NOT NULL,
      updated_by INT NOT NULL,
      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      UNIQUE KEY panel_world_boss_rule_unique (server_id, boss_group, event_type),
      INDEX panel_world_boss_rule_active_idx (server_id, boss_group, event_type, enabled),
      INDEX panel_world_boss_rule_time_idx (starts_at, ends_at)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4`,
    `CREATE TABLE IF NOT EXISTS panel_world_boss_notification_logs (
      id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
      config_id INT NOT NULL,
      event_type ENUM('spawn','defeat') NOT NULL,
      boss_name VARCHAR(150) NOT NULL,
      map_name VARCHAR(150) NOT NULL,
      zone_id INT NOT NULL,
      message VARCHAR(500) NOT NULL,
      online_players INT NOT NULL DEFAULT 0,
      sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      INDEX panel_world_boss_log_config_idx (config_id, sent_at),
      INDEX panel_world_boss_log_time_idx (sent_at)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4`,
    `CREATE TABLE IF NOT EXISTS panel_bots (
      id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
      char_id INT NOT NULL,
      name VARCHAR(80) NOT NULL,
      enabled TINYINT(1) NOT NULL DEFAULT 1,
      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      UNIQUE KEY panel_bots_char_id_unique (char_id),
      INDEX panel_bots_enabled_idx (enabled)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4`,
  ];
  for (const statement of statements) await pool.query(statement);
  await ensureSchemaColumn("panel_reward_campaigns", "server_id", "INT NOT NULL DEFAULT 0 AFTER npc_key");
  await ensureSchemaColumn("panel_reward_claims", "claim_key", "VARCHAR(120) NOT NULL UNIQUE AFTER player_id");
}

async function getSessionUser(req) {
  void req;
  return LOCAL_ACTOR;
}

function writeJson(res, status, body) {
  res.writeHead(status, { "Content-Type": "application/json; charset=utf-8", "Cache-Control": "no-store" });
  res.end(JSON.stringify(body));
}

async function readJson(req) {
  let body = "";
  for await (const chunk of req) {
    body += chunk;
    if (Buffer.byteLength(body) > MAX_BODY_SIZE) throw new Error("Request body quá lớn.");
  }
  return body ? JSON.parse(body) : {};
}

function assertCsrf(req, user) {
  void user;
  if (req.headers["x-nso-csrf"] !== LOCAL_CSRF_TOKEN) throw new Error("CSRF token local không hợp lệ. Hãy tải lại Control Room.");
}

async function audit(user, module, action, resourceType, resourceId, outcome, metadata = {}) {
  const id = randomUUID();
  await pool.query(
    `INSERT INTO panel_audit_events (id, actor_id, actor_username, module_name, action_name, resource_type, resource_id, outcome, correlation_id, metadata)
     VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
    [id, user?.id ?? null, user?.username ?? null, module, action, resourceType, resourceId ?? null, outcome, randomUUID(), JSON.stringify(metadata)],
  );
}

function randomUUID() {
  return `${randomBytes(4).toString("hex")}-${randomBytes(2).toString("hex")}-${randomBytes(2).toString("hex")}-${randomBytes(2).toString("hex")}-${randomBytes(6).toString("hex")}`;
}

function inventoryOptionIds(payload) {
  const ids = new Set();
  const collectItem = item => {
    for (const option of item?.options || []) ids.add(Number(option[0]));
    for (const gem of item?.gems || []) collectItem(gem);
  };
  for (const section of ["bag", "box", "equiped", "fashion"]) {
    for (const item of JSON.parse(payload[section] || "[]")) collectItem(item);
  }
  return [...ids];
}

function normalizeShopOptions(value) {
  let options;
  try { options = typeof value === "string" ? JSON.parse(value || "[]") : value; } catch { throw new Error("Option hàng hóa NPC phải là JSON hợp lệ."); }
  if (!Array.isArray(options) || options.length > 20) throw new Error("Hàng hóa NPC chỉ nhận tối đa 20 option.");
  return options.map((option, index) => {
    if (!Array.isArray(option) || option.length !== 2) throw new Error(`Option hàng hóa NPC dòng ${index + 1} phải là [optionId, value].`);
    const optionId = Number(option[0]); const value = Number(option[1]);
    if (!Number.isInteger(optionId) || optionId < 0 || !Number.isInteger(value) || value < -2_000_000_000 || value > 2_000_000_000) throw new Error(`Option hàng hóa NPC dòng ${index + 1} không hợp lệ.`);
    return [optionId, value];
  });
}

function tcpStatus(host, port, timeout = 800) {
  return new Promise(resolve => {
    const socket = net.createConnection({ host, port });
    const done = value => { socket.destroy(); resolve(value); };
    socket.setTimeout(timeout);
    socket.once("connect", () => done(true));
    socket.once("timeout", () => done(false));
    socket.once("error", () => done(false));
  });
}

async function reconcileHealthAlerts(dbOnline, gamePortOpen) {
  const conditions = [
    { key: "mariadb.unavailable", failed: !dbOnline, severity: "critical", message: "MariaDB nội bộ không phản hồi; panel không thể quản trị dữ liệu game." },
    { key: "game.port.14444.closed", failed: !gamePortOpen, severity: "critical", message: "Game server không lắng nghe TCP 14444 trên localhost." },
  ];
  for (const condition of conditions) {
    if (condition.failed) {
      const [existing] = await pool.query("SELECT id FROM panel_alerts WHERE alert_key = ? AND status IN ('open','acknowledged') LIMIT 1", [condition.key]);
      if (!existing[0]) await pool.query("INSERT INTO panel_alerts (id, alert_key, severity, message, status) VALUES (?, ?, ?, ?, 'open')", [randomUUID(), condition.key, condition.severity, condition.message]);
    } else {
      await pool.query("UPDATE panel_alerts SET status = 'resolved' WHERE alert_key = ? AND status IN ('open','acknowledged')", [condition.key]);
    }
  }
}

async function dashboardData() {
  let dbOnline = false;
  let onlinePlayers = 0;
  let totalPlayers = 0;
  let totalAccounts = 0;
  let queryError = null;
  try {
    await pool.query("SELECT 1");
    dbOnline = true;
    const [stats] = await pool.query("SELECT (SELECT COUNT(*) FROM players) AS players, (SELECT COUNT(*) FROM players WHERE online = 1) AS onlinePlayers, (SELECT COUNT(*) FROM users) AS accounts");
    totalPlayers = Number(stats[0]?.players || 0);
    onlinePlayers = Number(stats[0]?.onlinePlayers || 0);
    totalAccounts = Number(stats[0]?.accounts || 0);
  } catch (error) { queryError = error.message; }
  const gamePortOpen = await tcpStatus("127.0.0.1", 14444);
  let auditRows = [];
  let alerts = [];
  if (dbOnline) {
    await reconcileHealthAlerts(dbOnline, gamePortOpen);
    [auditRows] = await pool.query("SELECT actor_username, module_name, action_name, outcome, created_at FROM panel_audit_events ORDER BY created_at DESC LIMIT 10");
    [alerts] = await pool.query("SELECT id, alert_key, severity, message, status, created_at FROM panel_alerts WHERE status IN ('open','acknowledged') ORDER BY created_at DESC LIMIT 20");
  } else {
    alerts = [{ id: "local-db-offline", alert_key: "mariadb.unavailable", severity: "critical", message: "MariaDB nội bộ không phản hồi; panel không thể đọc audit hoặc ghi acknowledgement.", status: "open", created_at: new Date().toISOString() }];
    if (!gamePortOpen) alerts.push({ id: "local-game-port-closed", alert_key: "game.port.14444.closed", severity: "critical", message: "Game server không lắng nghe TCP 14444 trên localhost.", status: "open", created_at: new Date().toISOString() });
  }
  return { dbOnline, gamePortOpen, onlinePlayers, totalPlayers, totalAccounts, queryError, audit: auditRows, alerts, modules };
}

function requireUser(user, res, role = "viewer") {
  void user; void res; void role;
  return true;
}

const REWARD_CAMPAIGN_TYPES = new Set(["fancung", "newbie", "topup", "event"]);
const REWARD_NPCS = new Set(["hung_vuong", "admin"]);
const REWARD_CLAIM_SCOPES = new Set(["user", "player"]);

function rewardCampaignConfig(body) {
  const campaignType = String(body.campaignType || "");
  const npcKey = String(body.npcKey || "");
  const serverId = Number(body.serverId ?? 0);
  const claimScope = String(body.claimScope || "player");
  const title = String(body.title || "").trim().slice(0, 180);
  const campaignKey = String(body.campaignKey || "").trim().toLowerCase();
  const requirementKey = String(body.requirementKey || "").trim().slice(0, 100);
  const requirementValue = Number(body.requirementValue ?? 0);
  const currencies = Object.fromEntries(["coin", "gold", "yen"].map(key => [key, Number(body[key] ?? 0)]));
  if (!REWARD_CAMPAIGN_TYPES.has(campaignType) || !REWARD_NPCS.has(npcKey) || !REWARD_CLAIM_SCOPES.has(claimScope)) throw new Error("Loại campaign, NPC hoặc phạm vi claim không hợp lệ.");
  if ((campaignType === "newbie" && npcKey !== "admin") || ((campaignType === "fancung" || campaignType === "topup") && npcKey !== "hung_vuong")) throw new Error("NPC không đúng với loại campaign.");
  if (!title || !/^[a-z0-9][a-z0-9_-]{2,79}$/.test(campaignKey)) throw new Error("Tên campaign cần 3–80 ký tự chữ thường, số, _ hoặc -.");
  if (!Number.isInteger(serverId) || serverId < 0 || serverId > 255) throw new Error("Server ID không hợp lệ.");
  if (!Number.isSafeInteger(requirementValue) || requirementValue < 0) throw new Error("Mốc điều kiện không hợp lệ.");
  if (campaignType === "event" && !requirementKey) throw new Error("Campaign sự kiện cần chọn key điểm sự kiện.");
  if (Object.values(currencies).some(value => !Number.isSafeInteger(value) || value < 0 || value > 2_000_000_000)) throw new Error("Giá trị tiền thưởng không hợp lệ.");
  const parseDate = (value, label) => { if (!value) return null; const date = new Date(value); if (Number.isNaN(date.getTime())) throw new Error(`${label} không hợp lệ.`); return date; };
  const startsAt = parseDate(body.startsAt, "Thời điểm bắt đầu");
  const endsAt = parseDate(body.endsAt, "Thời điểm kết thúc");
  if (startsAt && endsAt && startsAt >= endsAt) throw new Error("Thời điểm kết thúc phải sau thời điểm bắt đầu.");
  return { campaignKey, title, npcKey, serverId, campaignType, requirementKey: campaignType === "event" ? requirementKey : null, requirementValue, claimScope, ...currencies, active: body.active === false || String(body.active) === "0" ? 0 : 1, startsAt, endsAt };
}

function rewardItemsConfig(items) {
  if (!Array.isArray(items) || items.length > 50) throw new Error("Campaign chỉ nhận tối đa 50 dòng vật phẩm.");
  return items.map((raw, index) => {
    const itemId = Number(raw?.itemId);
    const quantity = Number(raw?.quantity ?? 1);
    const sys = Number(raw?.sys ?? 0);
    const upgrade = Number(raw?.upgrade ?? 0);
    const yen = Number(raw?.yen ?? 0);
    const expireDays = Number(raw?.expireDays ?? 0);
    if (!Number.isInteger(itemId) || itemId < 0 || !Number.isInteger(quantity) || quantity < 1 || quantity > 9999 || !Number.isInteger(sys) || sys < 0 || sys > 9 || !Number.isInteger(upgrade) || upgrade < 0 || upgrade > 30 || !Number.isSafeInteger(yen) || yen < 0 || !Number.isSafeInteger(expireDays) || expireDays < 0 || expireDays > 3650) throw new Error(`Dòng vật phẩm ${index + 1} không hợp lệ.`);
    const options = Array.isArray(raw.options) ? raw.options : [];
    if (options.length > 20) throw new Error(`Dòng vật phẩm ${index + 1} vượt quá 20 option.`);
    return { itemId, quantity, isLock: raw.isLock !== false, sys, upgrade, yen, expireDays, options: options.map((option, optionIndex) => { const optionId = Number(option?.optionId); const minValue = Number(option?.minValue); const maxValue = Number(option?.maxValue); if (!Number.isInteger(optionId) || !Number.isInteger(minValue) || !Number.isInteger(maxValue) || minValue < -2_000_000_000 || maxValue > 2_000_000_000 || minValue > maxValue) throw new Error(`Option ${optionIndex + 1} của dòng ${index + 1} không hợp lệ.`); return { optionId, minValue, maxValue }; }) };
  });
}

async function rewardCampaignData() {
  const [campaignRows] = await pool.query(`SELECT c.id, c.campaign_key, c.title, c.npc_key, c.server_id, c.campaign_type, c.requirement_key, c.requirement_value, c.claim_scope, c.coin, c.gold, c.yen, c.active, c.starts_at, c.ends_at, c.created_at, c.updated_at, COALESCE(cl.claim_count, 0) AS claim_count FROM panel_reward_campaigns c LEFT JOIN (SELECT campaign_id, COUNT(*) AS claim_count FROM panel_reward_claims GROUP BY campaign_id) cl ON cl.campaign_id = c.id ORDER BY c.active DESC, c.updated_at DESC`);
  const [itemRows] = await pool.query(`SELECT ri.id, ri.campaign_id, ri.item_id, i.name AS item_name, i.icon, ri.quantity, ri.is_lock, ri.sys, ri.upgrade, ri.yen, ri.expire_days FROM panel_reward_items ri LEFT JOIN item i ON i.id = ri.item_id ORDER BY ri.campaign_id, ri.id`);
  const [optionRows] = await pool.query("SELECT id, type, name FROM item_option ORDER BY id");
  const [itemOptionRows] = await pool.query("SELECT reward_item_id, option_id, min_value, max_value FROM panel_reward_item_options ORDER BY reward_item_id, id");
  const [claimRows] = await pool.query(`SELECT cl.id, cl.campaign_id, c.title, cl.user_id, cl.player_id, p.name AS player_name, u.username, cl.claimed_at FROM panel_reward_claims cl JOIN panel_reward_campaigns c ON c.id = cl.campaign_id LEFT JOIN players p ON p.id = cl.player_id LEFT JOIN users u ON u.id = cl.user_id ORDER BY cl.claimed_at DESC LIMIT 300`);
  return { campaigns: campaignRows, items: itemRows, options: optionRows, itemOptions: itemOptionRows, claims: claimRows };
}

async function saveRewardCampaign(user, body) {
  const campaign = rewardCampaignConfig(body);
  const items = rewardItemsConfig(body.items);
  const itemIds = [...new Set(items.map(item => item.itemId))];
  const optionIds = [...new Set(items.flatMap(item => item.options.map(option => option.optionId)))];
  const connection = await pool.getConnection();
  try {
    await connection.beginTransaction();
    if (itemIds.length) { const [rows] = await connection.query(`SELECT id FROM item WHERE id IN (${itemIds.map(() => "?").join(",")})`, itemIds); if (rows.length !== itemIds.length) throw new Error("Có item không tồn tại trong catalog game."); }
    if (optionIds.length) { const [rows] = await connection.query(`SELECT id FROM item_option WHERE id IN (${optionIds.map(() => "?").join(",")})`, optionIds); if (rows.length !== optionIds.length) throw new Error("Có option không tồn tại trong catalog game."); }
    let campaignId = Number(body.id || 0);
    if (campaignId) {
      const [existing] = await connection.query("SELECT id, campaign_key FROM panel_reward_campaigns WHERE id = ? LIMIT 1 FOR UPDATE", [campaignId]);
      if (!existing[0]) throw new Error("Không tìm thấy campaign.");
      const [claimed] = await connection.query("SELECT id FROM panel_reward_claims WHERE campaign_id = ? LIMIT 1", [campaignId]);
      if (claimed[0]) throw new Error("Campaign đã có người nhận, không thể sửa cấu hình; hãy tắt và tạo campaign mới.");
      await connection.query(`UPDATE panel_reward_campaigns SET campaign_key = ?, title = ?, npc_key = ?, server_id = ?, campaign_type = ?, requirement_key = ?, requirement_value = ?, claim_scope = ?, coin = ?, gold = ?, yen = ?, active = ?, starts_at = ?, ends_at = ?, updated_by = ? WHERE id = ? LIMIT 1`, [campaign.campaignKey, campaign.title, campaign.npcKey, campaign.serverId, campaign.campaignType, campaign.requirementKey, campaign.requirementValue, campaign.claimScope, campaign.coin, campaign.gold, campaign.yen, campaign.active, campaign.startsAt, campaign.endsAt, user.id, campaignId]);
      await connection.query("DELETE FROM panel_reward_item_options WHERE reward_item_id IN (SELECT id FROM panel_reward_items WHERE campaign_id = ?)", [campaignId]);
      await connection.query("DELETE FROM panel_reward_items WHERE campaign_id = ?", [campaignId]);
    } else {
      const [result] = await connection.query(`INSERT INTO panel_reward_campaigns (campaign_key, title, npc_key, server_id, campaign_type, requirement_key, requirement_value, claim_scope, coin, gold, yen, active, starts_at, ends_at, created_by, updated_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`, [campaign.campaignKey, campaign.title, campaign.npcKey, campaign.serverId, campaign.campaignType, campaign.requirementKey, campaign.requirementValue, campaign.claimScope, campaign.coin, campaign.gold, campaign.yen, campaign.active, campaign.startsAt, campaign.endsAt, user.id, user.id]);
      campaignId = result.insertId;
    }
    for (const item of items) {
      const [result] = await connection.query(`INSERT INTO panel_reward_items (campaign_id, item_id, quantity, is_lock, sys, upgrade, yen, expire_days) VALUES (?, ?, ?, ?, ?, ?, ?, ?)`, [campaignId, item.itemId, item.quantity, item.isLock ? 1 : 0, item.sys, item.upgrade, item.yen, item.expireDays]);
      for (const option of item.options) await connection.query(`INSERT INTO panel_reward_item_options (reward_item_id, option_id, min_value, max_value) VALUES (?, ?, ?, ?)`, [result.insertId, option.optionId, option.minValue, option.maxValue]);
    }
    await connection.commit();
    await audit(user, "rewards", "reward_campaign.saved", "reward_campaign", String(campaignId), "success", { campaignKey: campaign.campaignKey, campaignType: campaign.campaignType, itemCount: items.length });
    return campaignId;
  } catch (error) { await connection.rollback(); throw error; } finally { connection.release(); }
}

async function sendMonsterIcon(res, url) {
  const rawZoom = url.searchParams.get("zoom");
  const zoom = rawZoom === null ? 1 : Number(rawZoom);
  const id = Number(url.searchParams.get("id"));
  if (!Number.isInteger(zoom) || zoom < 1 || zoom > 4 || !Number.isInteger(id) || id < 0 || id > 100_000) { res.writeHead(404, { "Cache-Control": "public, max-age=300" }); res.end(); return; }
  let file = null;
  for (const frame of [0, 1, 2, 3]) {
    const candidate = resolve(MOB_SPRITE_DIR, String(zoom), `${id}_${frame}.png`);
    if (candidate.startsWith(`${MOB_SPRITE_DIR}/`) && existsSync(candidate) && !statSync(candidate).isDirectory()) { file = candidate; break; }
  }
  if (!file) { res.writeHead(404, { "Cache-Control": "public, max-age=300" }); res.end(); return; }
  res.writeHead(200, { "Content-Type": "image/png", "Cache-Control": "public, max-age=86400", "X-Content-Type-Options": "nosniff" });
  createReadStream(file).pipe(res);
}

async function sendItemIcon(res, url) {
  const rawZoom = url.searchParams.get("zoom");
  const zoom = rawZoom === null ? 1 : Number(rawZoom);
  if (!Number.isInteger(zoom) || zoom < 1 || zoom > 4) { res.writeHead(400, { "Cache-Control": "no-store" }); res.end(); return; }
  const rawIcon = url.searchParams.get("icon");
  const rawItemId = url.searchParams.get("itemId");
  let icon = rawIcon === null ? null : Number(rawIcon);
  if (rawItemId !== null) {
    const itemId = Number(rawItemId);
    if (!Number.isInteger(itemId) || itemId < 0) { res.writeHead(400, { "Cache-Control": "no-store" }); res.end(); return; }
    const [rows] = await pool.query("SELECT icon FROM item WHERE id = ? LIMIT 1", [itemId]);
    icon = Number(rows[0]?.icon);
  }
  if (!Number.isInteger(icon) || icon < 0 || icon > 100_000) { res.writeHead(404, { "Cache-Control": "public, max-age=300" }); res.end(); return; }
  const file = resolve(ITEM_SPRITE_DIR, String(zoom), `Small${icon}.png`);
  if (!file.startsWith(`${ITEM_SPRITE_DIR}/`) || !existsSync(file) || statSync(file).isDirectory()) { res.writeHead(404, { "Cache-Control": "public, max-age=300" }); res.end(); return; }
  res.writeHead(200, { "Content-Type": "image/png", "Cache-Control": "public, max-age=86400", "X-Content-Type-Options": "nosniff" });
  createReadStream(file).pipe(res);
}

async function api(req, res, url) {
  if (req.method === "GET" && url.pathname === "/api/system/health") {
    let dbOnline = false; let dbError = null;
    try { await pool.query("SELECT 1"); dbOnline = true; } catch (error) { dbError = error.message; }
    writeJson(res, 200, { ok: true, service: "nso-offline-panel", access: "local-only-no-login", databaseOnline: dbOnline, database: dbOnline ? config.database.name : null, dbError, bindHost: LOCAL_BIND_HOST, port: config.port }); return;
  }
  const user = await getSessionUser(req);
  if (req.method === "GET" && url.pathname === "/api/item-icon") { await sendItemIcon(res, url); return; }
  if (req.method === "GET" && url.pathname === "/api/monster-icon") { await sendMonsterIcon(res, url); return; }
  if (req.method === "GET" && url.pathname === "/api/local/context") {
    writeJson(res, 200, { access: "local-only-no-login", actor: user.username, csrf: LOCAL_CSRF_TOKEN }); return;
  }
  if (url.pathname.startsWith("/api/auth/")) {
    writeJson(res, 410, { error: "Đăng nhập panel đã bị tắt. Control Room chỉ dùng local-only trên 127.0.0.1." }); return;
  }
  if (req.method === "POST") {
    try { assertCsrf(req, user); } catch (error) { await audit(user, "security", "csrf.denied", "local-request", null, "denied"); writeJson(res, 403, { error: error.message }); return; }
  }
  if (req.method === "GET" && url.pathname === "/api/dashboard") {
    if (!requireUser(user, res, "analyst")) return;
    writeJson(res, 200, await dashboardData()); return;
  }
  if (req.method === "GET" && url.pathname === "/api/modules") {
    if (!requireUser(user, res, "viewer")) return;
    writeJson(res, 200, { modules }); return;
  }
  if (req.method === "GET" && url.pathname === "/api/players") {
    if (!requireUser(user, res, "analyst")) return;
    const q = `%${String(url.searchParams.get("q") || "").slice(0, 30)}%`;
    const [rows] = await pool.query(
      `SELECT p.id, p.user_id, p.name, p.class, p.xu, p.yen, p.online, p.map, p.last_login_time, u.username, u.luong, u.coin, u.status
       FROM players p LEFT JOIN users u ON u.id = p.user_id
       WHERE p.name LIKE ? OR u.username LIKE ? ORDER BY p.online DESC, p.updated_at DESC LIMIT 50`, [q, q]);
    writeJson(res, 200, { rows }); return;
  }
  if (req.method === "GET" && url.pathname === "/api/items") {
    if (!requireUser(user, res, "analyst")) return;
    const q = `%${String(url.searchParams.get("q") || "").slice(0, 60)}%`;
    const rawType = url.searchParams.get("type");
    const rawGender = url.searchParams.get("gender");
    const type = rawType === null || rawType === "" || rawType === "all" ? null : Number(rawType);
    const gender = rawGender === null || rawGender === "" || rawGender === "all" ? null : Number(rawGender);
    if (type !== null && (!Number.isInteger(type) || type < 0 || type > 255)) throw new Error("Bộ lọc type không hợp lệ.");
    if (gender !== null && (!Number.isInteger(gender) || ![-1, 0, 1, 2].includes(gender))) throw new Error("Bộ lọc gender không hợp lệ.");
    const conditions = ["(name LIKE ? OR CAST(id AS CHAR) LIKE ?)"];
    const params = [q, q];
    if (type !== null) { conditions.push("type = ?"); params.push(type); }
    if (gender !== null) { conditions.push("gender = ?"); params.push(gender); }
    const [rows, types] = await Promise.all([
      pool.query(`SELECT id, name, type, gender, description, level, icon, part, fashion, isUpToUp FROM item WHERE ${conditions.join(" AND ")} ORDER BY type, level, id LIMIT 100`, params),
      pool.query("SELECT type, COUNT(*) AS total FROM item GROUP BY type ORDER BY type"),
    ]);
    writeJson(res, 200, { rows: rows[0], types: types[0], filters: { type, gender } }); return;
  }
  if (req.method === "GET" && url.pathname === "/api/accounts") {
    if (!requireUser(user, res, "moderator")) return;
    const q = `%${String(url.searchParams.get("q") || "").slice(0, 30)}%`;
    const [rows] = await pool.query("SELECT id, username, status, activated, online, luong, coin, ban_until, last_login_at FROM users WHERE username LIKE ? ORDER BY id DESC LIMIT 50", [q]);
    writeJson(res, 200, { rows }); return;
  }
  if (req.method === "GET" && url.pathname === "/api/inventory") {
    if (!requireUser(user, res, "analyst")) return;
    const playerId = Number(url.searchParams.get("playerId"));
    if (!Number.isInteger(playerId) || playerId < 1) throw new Error("playerId không hợp lệ.");
    const columns = await tableColumns("players");
    const selected = availableColumns(columns, ["id", "user_id", "name", "online", "numberCellBag", "numberCellBox", "bag", "box", "equiped", "fashion", "mount", "mask_box", "collection_box"]);
    if (!selected.includes("id")) throw new Error("Bảng players không có cột id bắt buộc.");
    const [rows] = await pool.query(
      `SELECT ${selected.map(column => `\`${column}\``).join(", ")} FROM players WHERE id = ? LIMIT 1`,
      [playerId],
    );
    writeJson(res, 200, { row: rows[0] || null }); return;
  }
  if (req.method === "GET" && url.pathname === "/api/player-state") {
    if (!requireUser(user, res, "analyst")) return;
    const playerId = Number(url.searchParams.get("playerId"));
    if (!Number.isInteger(playerId) || playerId < 1) throw new Error("playerId không hợp lệ.");
    const columns = await tableColumns("players");
    const selected = availableColumns(columns, ["id", "user_id", "name", "online", "point", "spoint", "potential", "numberCellBag", "numberCellBox", "bag", "box", "equiped", "fashion"]);
    if (!selected.includes("id") || !selected.includes("online")) throw new Error("Schema players không đủ id/online để chỉnh dữ liệu an toàn.");
    const [[rows], [inventoryCatalog], [optionCatalog]] = await Promise.all([
      pool.query(`SELECT ${selected.map(column => `\`${column}\``).join(", ")} FROM players WHERE id = ? LIMIT 1`, [playerId]),
      pool.query("SELECT id, name, type, gender, level, icon FROM item ORDER BY id LIMIT 200"),
      pool.query("SELECT id, type, name FROM item_option ORDER BY id"),
    ]);
    writeJson(res, 200, { row: rows[0] || null, editableStats: ["point", "spoint", "potential", "numberCellBag", "numberCellBox"].filter(column => columns.has(column)).concat(columns.has("data") ? ["exp"] : []), inventoryEditable: ["bag", "box", "equiped", "fashion"].every(column => columns.has(column)), inventoryCatalog, optionCatalog }); return;
  }
  if (req.method === "GET" && url.pathname === "/api/shop") {
    if (!requireUser(user, res, "analyst")) return;
    const [rows] = await pool.query("SELECT s.id, s.idItem, i.name AS itemName, i.icon AS itemIcon, s.price, s.upgrade, s.system, s.options FROM shopcoin_tb1 s LEFT JOIN item i ON i.id = s.idItem ORDER BY s.id LIMIT 200");
    writeJson(res, 200, { rows }); return;
  }
  if (req.method === "GET" && url.pathname === "/api/npc-shops") {
    if (!requireUser(user, res, "analyst")) return;
    const requestedStore = url.searchParams.get("storeId");
    const storeId = requestedStore ? Number(requestedStore) : null;
    if (storeId !== null && (!Number.isInteger(storeId) || storeId < 0)) throw new Error("storeId không hợp lệ.");
    const [stores, npcs, rows, optionCatalog] = await Promise.all([
      pool.query("SELECT id, name FROM stores ORDER BY id"),
      pool.query("SELECT id, name, head, body, leg, menu FROM npc ORDER BY id LIMIT 150"),
      pool.query(`SELECT sd.id, sd.item_id, i.name AS item_name, i.icon AS item_icon, sd.sys, sd.store, st.name AS store_name, sd.\`lock\`, sd.coin, sd.gold, sd.yen, sd.expire, sd.options FROM store_data sd JOIN stores st ON st.id = sd.store LEFT JOIN item i ON i.id = sd.item_id ${storeId === null ? "" : "WHERE sd.store = ?"} ORDER BY sd.store, sd.id LIMIT 400`, storeId === null ? [] : [storeId]),
      pool.query("SELECT id, type, name FROM item_option ORDER BY id"),
    ]);
    writeJson(res, 200, { stores: stores[0], npcs: npcs[0], rows: rows[0], optionCatalog: optionCatalog[0], reloadRequired: true }); return;
  }
  if (req.method === "GET" && url.pathname === "/api/world-boss-notifications") {
    if (!requireUser(user, res, "analyst")) return;
    writeJson(res, 200, await worldBossNotificationData()); return;
  }
  if (req.method === "GET" && url.pathname === "/api/shinwa") {
    if (!requireUser(user, res, "analyst")) return;
    writeJson(res, 200, await shinwaData(url.searchParams)); return;
  }
  if (req.method === "GET" && url.pathname === "/api/monsters") {
    if (!requireUser(user, res, "analyst")) return;
    const boss = url.searchParams.get("boss") === "1" ? 1 : 0;
    const [rows] = await pool.query("SELECT id, id AS mob_icon, name, level, boss, type, hp, range_move, speed FROM monster WHERE boss = ? ORDER BY level DESC LIMIT 250", [boss]);
    writeJson(res, 200, { rows }); return;
  }
  if (req.method === "GET" && url.pathname === "/api/gift-codes") {
    if (!requireUser(user, res, "operator")) return;
    const [rows] = await pool.query("SELECT id, server_id, type, code, coin, gold, yen, items, status, expires_at, created_at FROM gift_codes ORDER BY created_at DESC LIMIT 100");
    writeJson(res, 200, { rows }); return;
  }
  if (req.method === "GET" && url.pathname === "/api/gift-code-control") {
    if (!requireUser(user, res, "operator")) return;
    writeJson(res, 200, await giftControlData()); return;
  }
  if (req.method === "GET" && url.pathname === "/api/gift-code-item-search") {
    if (!requireUser(user, res, "operator")) return;
    const columns = await tableColumns("item");
    if (!columns.has("id") || !columns.has("name")) throw new Error("Schema item thiếu cột id/name.");
    const q = String(url.searchParams.get("q") || "").trim().slice(0, 80);
    const select = ["id", "name", ...(columns.has("type") ? ["type"] : []), ...(columns.has("icon") ? ["icon"] : [])];
    const [rows] = q ? await pool.query(`SELECT ${select.join(", ")} FROM item WHERE CAST(id AS CHAR) LIKE ? OR name LIKE ? ORDER BY id LIMIT 50`, [`%${q}%`, `%${q}%`]) : await pool.query(`SELECT ${select.join(", ")} FROM item ORDER BY id LIMIT 50`);
    writeJson(res, 200, { rows }); return;
  }
  if (req.method === "GET" && url.pathname === "/api/reward-history") {
    if (!requireUser(user, res, "analyst")) return;
    const historyColumns = await tableColumns("gift_code_histories");
    if (historyColumns.size === 0) { writeJson(res, 200, { rows: [], unavailable: "Schema game hiện tại không có gift_code_histories." }); return; }
    const [rows] = await pool.query(
      `SELECT h.id, h.gift_code, h.created_at, h.updated_at, p.name AS player_name, u.username
       FROM gift_code_histories h
       LEFT JOIN players p ON p.id = h.player_id
       LEFT JOIN users u ON u.id = h.user_id
       ORDER BY h.created_at DESC LIMIT 150`,
    );
    writeJson(res, 200, { rows }); return;
  }
  if (req.method === "GET" && url.pathname === "/api/events") {
    if (!requireUser(user, res, "analyst")) return;
    const eventColumns = await tableColumns("event_points");
    if (eventColumns.size === 0) { writeJson(res, 200, { rows: [], summary: [], unavailable: "Schema game hiện tại không có event_points." }); return; }
    const requestedEvent = url.searchParams.get("eventId");
    const eventId = requestedEvent === null || requestedEvent === "" ? null : Number(requestedEvent);
    if (eventId !== null && (!Number.isInteger(eventId) || eventId < 0)) throw new Error("eventId không hợp lệ.");
    const filter = eventId === null ? "" : "WHERE ep.event_id = ?";
    const params = eventId === null ? [] : [eventId];
    const [rows] = await pool.query(
      `SELECT ep.id, ep.event_id, ep.player_id, p.name AS player_name, ep.point
       FROM event_points ep LEFT JOIN players p ON p.id = ep.player_id
       ${filter} ORDER BY ep.event_id, ep.id DESC LIMIT 200`,
      params,
    );
    const [summary] = await pool.query(
      "SELECT event_id, COUNT(*) AS player_count FROM event_points GROUP BY event_id ORDER BY event_id",
    );
    writeJson(res, 200, { rows, summary }); return;
  }
  if (req.method === "GET" && url.pathname === "/api/event-control") {
    if (!requireUser(user, res, "operator")) return;
    writeJson(res, 200, await eventControlData()); return;
  }
  if (req.method === "GET" && url.pathname === "/api/runtime-control/status") {
    if (!requireUser(user, res, "analyst")) return;
    writeJson(res, 200, await runtimeControlStatus()); return;
  }
  if (req.method === "GET" && url.pathname === "/api/reward-campaigns") {
    if (!requireUser(user, res, "analyst")) return;
    writeJson(res, 200, await rewardCampaignData()); return;
  }
  if (req.method === "GET" && url.pathname === "/api/options") {
    if (!requireUser(user, res, "operator")) return;
    const [rows] = await pool.query("SELECT id, `key`, value FROM options WHERE `key` IN ('expserver','levelnjtl','notify') ORDER BY `key`");
    writeJson(res, 200, { rows, editableKeys: ["expserver", "levelnjtl", "notify"] }); return;
  }
  if (req.method === "GET" && url.pathname === "/api/leaderboards") {
    if (!requireUser(user, res, "analyst")) return;
    const now = new Date();
    const year = now.getUTCFullYear();
    const month = now.getUTCMonth() + 1;
    const [playersColumns, warColumns, rankingColumns] = await Promise.all([tableColumns("players"), tableColumns("top_war"), tableColumns("ranking_list")]);
    const [[boss], [vxmm], [war], [ranking]] = await Promise.all([
      playersColumns.has("topBoss") ? pool.query("SELECT id, name, topBoss, online FROM players WHERE topBoss > 0 ORDER BY topBoss DESC, id ASC LIMIT 30") : Promise.resolve([[]]),
      playersColumns.has("topvxmm") ? pool.query("SELECT id, name, topvxmm, online FROM players WHERE topvxmm > 0 ORDER BY topvxmm DESC, id ASC LIMIT 30") : Promise.resolve([[]]),
      warColumns.has("player_id") ? pool.query("SELECT player_id, name, SUM(point) AS points, type FROM top_war WHERE year = ? AND month = ? GROUP BY player_id, name, type ORDER BY points DESC LIMIT 30", [year, month]) : Promise.resolve([[]]),
      rankingColumns.has("player_id") ? pool.query("SELECT r.match_id, r.rank_at, p.name AS player_name, r.updated_at FROM ranking_list r LEFT JOIN players p ON p.id = r.player_id ORDER BY r.updated_at DESC LIMIT 30") : Promise.resolve([[]]),
    ]);
    writeJson(res, 200, { boss, vxmm, war, ranking, period: { year, month } }); return;
  }
  if (req.method === "GET" && url.pathname === "/api/analytics") {
    if (!requireUser(user, res, "analyst")) return;
    const columns = await tableColumns("players");
    const has = column => columns.has(column);
    const online = has("online") ? "SUM(online = 1)" : "0";
    const activated = has("activated") ? "SUM(activated = 1)" : "0";
    const playerClass = has("class") ? "`class`" : "0";
    const lastLogin = has("last_login_time") ? "last_login_time" : "0";
    const xu = has("xu") ? "xu" : "0";
    const yen = has("yen") ? "yen" : "0";
    const xuBox = has("xuInBox") ? "xuInBox" : "0";
    const [[totalsRows], [classes], [daily], [economyRows], [recent]] = await Promise.all([
      pool.query(`SELECT COUNT(*) AS players, ${online} AS online_players, ${activated} AS activated_players FROM players`),
      pool.query(`SELECT ${playerClass} AS class, COUNT(*) AS players, ${online} AS online_players FROM players GROUP BY ${playerClass} ORDER BY players DESC, class ASC`),
      has("last_login_time") ? pool.query("SELECT DATE(FROM_UNIXTIME(last_login_time / 1000)) AS day, COUNT(*) AS players FROM players WHERE last_login_time >= UNIX_TIMESTAMP(DATE_SUB(UTC_DATE(), INTERVAL 6 DAY)) * 1000 GROUP BY DATE(FROM_UNIXTIME(last_login_time / 1000)) ORDER BY day") : Promise.resolve([[]]),
      pool.query(`SELECT CAST(COALESCE(SUM(${xu}), 0) AS CHAR) AS total_xu, CAST(COALESCE(SUM(${yen}), 0) AS CHAR) AS total_yen, CAST(COALESCE(SUM(${xuBox}), 0) AS CHAR) AS total_xu_box FROM players`),
      pool.query(`SELECT id, ${has("name") ? "name" : "'' AS name"}, ${has("online") ? "online" : "0 AS online"}, ${lastLogin} AS last_login_time, ${xu} AS xu, ${yen} AS yen FROM players ORDER BY ${lastLogin} DESC LIMIT 20`),
    ]);
    writeJson(res, 200, { totals: totalsRows[0] || {}, classes, daily, economy: economyRows[0] || {}, recent }); return;
  }
  if (req.method === "GET" && url.pathname === "/api/economy-monitor") {
    if (!requireUser(user, res, "analyst")) return;
    writeJson(res, 200, await economyMonitorData(url.searchParams)); return;
  }
  if (req.method === "GET" && url.pathname === "/api/player-inbox") {
    if (!requireUser(user, res, "analyst")) return;
    writeJson(res, 200, await inboxData(url.searchParams)); return;
  }
  if (req.method === "GET" && url.pathname === "/api/maintenance") {
    if (!requireUser(user, res, "admin")) return;
    const [rows] = await pool.query("SELECT id, starts_at, ends_at, message, status, created_by, approved_by, created_at FROM panel_maintenance_windows ORDER BY starts_at DESC LIMIT 100");
    writeJson(res, 200, { rows, runtimeLimit: "Kế hoạch chỉ được lưu/audit trong panel. Game Java không có maintenance flag SQL để panel áp dụng live; cần thực hiện restart theo runbook đã duyệt." }); return;
  }
  if (req.method === "GET" && url.pathname === "/api/jobs") {
    if (!requireUser(user, res, "admin")) return;
    const [rows] = await pool.query("SELECT id, name, job_type, cron_expression, enabled, approved_by, last_run_at, last_result, created_at FROM panel_jobs ORDER BY created_at DESC LIMIT 100");
    writeJson(res, 200, { rows }); return;
  }
  if (req.method === "GET" && url.pathname === "/api/audit") {
    if (!requireUser(user, res, "analyst")) return;
    const [rows] = await pool.query("SELECT actor_username, module_name, action_name, resource_type, resource_id, outcome, created_at FROM panel_audit_events ORDER BY created_at DESC LIMIT 100");
    writeJson(res, 200, { rows }); return;
  }
  if (req.method === "GET" && url.pathname === "/api/alerts") {
    if (!requireUser(user, res, "analyst")) return;
    const [rows] = await pool.query("SELECT id, alert_key, severity, message, status, acknowledged_at, created_at FROM panel_alerts ORDER BY created_at DESC LIMIT 100");
    writeJson(res, 200, { rows }); return;
  }
  if (req.method === "POST" && url.pathname === "/api/actions/event-plan") {
    if (!requireUser(user, res, "admin")) return;
    const body = await readJson(req);
    const eventKey = String(body.eventKey || "");
    const event = findEvent(eventKey);
    const endAt = new Date(body.endAt);
    if (!event || Number.isNaN(endAt.getTime()) || endAt.getTime() <= Date.now() + 60_000 || body.confirmation !== `QUEUE EVENT ${eventKey.toUpperCase()}`) {
      throw new Error("Event, thời hạn hoặc mã xác nhận không hợp lệ. Thời hạn phải muộn hơn hiện tại ít nhất một phút.");
    }
    let dropTable = null;
    if (event.assetPath) {
      dropTable = validateDropTable(body.dropTable);
      await validateEventDropItems(dropTable);
    } else if (body.dropTable !== undefined && body.dropTable !== null && String(body.dropTable).trim() !== "") {
      throw new Error("Event này có drop table hard-code trong Java; panel chỉ preview, không ghi đè JSON.");
    }
    const plan = { id: randomUUID(), version: 1, status: "pending", eventKey: event.key, className: event.className, label: event.label, eventId: event.eventId, endAt: endAt.toISOString(), dropTable, createdAt: new Date().toISOString(), createdBy: user.username };
    writePendingEventPlan(plan);
    await audit(user, "events", "event.plan.queued", "event_plan", plan.id, "success", { eventKey: event.key, className: event.className, endAt: plan.endAt, dropRows: dropTable?.length || 0 });
    writeJson(res, 200, { ok: true, plan, restartRequired: true }); return;
  }
  if (req.method === "POST" && url.pathname === "/api/actions/event-plan-discard") {
    if (!requireUser(user, res, "admin")) return;
    const body = await readJson(req);
    const plan = readEventPlan();
    if (!plan || body.confirmation !== `DISCARD EVENT ${plan.id}`) throw new Error("Không có event pending hoặc mã xác nhận không hợp lệ.");
    const { rmSync } = await import("node:fs");
    const { PENDING_PLAN_PATH } = await import("./lib/event-control.mjs");
    rmSync(PENDING_PLAN_PATH, { force: true });
    await audit(user, "events", "event.plan.discarded", "event_plan", plan.id, "success", { eventKey: plan.eventKey });
    writeJson(res, 200, { ok: true }); return;
  }
  if (req.method === "POST" && url.pathname === "/api/actions/account-status") {
    if (!requireUser(user, res, "moderator")) return;
    const body = await readJson(req);
    const status = Number(body.status);
    const id = Number(body.userId);
    if (!Number.isInteger(id) || ![0, 1, 2].includes(status) || body.confirmation !== `APPLY ACCOUNT ${id}`) throw new Error("Xác nhận không hợp lệ.");
    await pool.query("UPDATE users SET status = ?, activated = CASE WHEN ? = 1 THEN 1 ELSE activated END, ban_until = CASE WHEN ? = 1 THEN NULL ELSE ban_until END WHERE id = ? LIMIT 1", [status, status, status, id]);
    await audit(user, "accounts", "account.status.updated", "user", String(id), "success", { status });
    writeJson(res, 200, { ok: true }); return;
  }
  if (req.method === "POST" && url.pathname === "/api/actions/account-create") {
    if (!requireUser(user, res, "moderator")) return;
    const body = await readJson(req); const username = String(body.username || "").trim(); const password = String(body.password || "");
    if (!validateGameUsername(username) || password.length < 8 || password.length > 100 || body.confirmation !== `CREATE ACCOUNT ${username}`) throw new Error("Username cần 3-30 ký tự chữ/số/gạch dưới; mật khẩu cần 8-100 ký tự; hoặc mã xác nhận không hợp lệ.");
    const [existing] = await pool.query("SELECT id FROM users WHERE username = ? LIMIT 1", [username]);
    if (existing[0]) throw new Error("Username đã tồn tại.");
    const passwordHash = await hashGamePassword(password);
    const [result] = await pool.query("INSERT INTO users (username, password, status, activated, online, luong, coin) VALUES (?, ?, 1, 0, 0, 0, 0)", [username, passwordHash]);
    await audit(user, "accounts", "account.created", "user", String(result.insertId), "success", { username });
    writeJson(res, 200, { ok: true, id: result.insertId, username, gamePasswordHash: "bcrypt-$2y$" }); return;
  }
  if (req.method === "POST" && url.pathname === "/api/actions/account-ban") {
    if (!requireUser(user, res, "moderator")) return;
    const body = await readJson(req); const id = Number(body.userId); const banUntil = new Date(body.banUntil);
    if (!Number.isInteger(id) || id < 1 || Number.isNaN(banUntil.getTime()) || banUntil.getTime() <= Date.now() || body.confirmation !== `BAN ACCOUNT ${id}`) throw new Error("Dữ liệu ban hoặc mã xác nhận không hợp lệ.");
    await pool.query("UPDATE users SET status = 2, ban_until = ? WHERE id = ? LIMIT 1", [banUntil, id]);
    await audit(user, "moderation", "account.banned", "user", String(id), "success", { banUntil: banUntil.toISOString() });
    writeJson(res, 200, { ok: true }); return;
  }
  if (req.method === "POST" && url.pathname === "/api/actions/economy-alert-state") {
    if (!requireUser(user, res, "operator")) return;
    const body = await readJson(req); const id = Number(body.id); const status = String(body.status || ""); const note = String(body.note || "").trim().slice(0, 500);
    if (!Number.isInteger(id) || id < 1 || !["acknowledged", "resolved", "false_positive", "open"].includes(status) || body.confirmation !== `UPDATE ECONOMY ALERT ${id}`) throw new Error("Cảnh báo kinh tế hoặc mã xác nhận không hợp lệ.");
    const [result] = await pool.query("UPDATE panel_economy_alerts SET status = ?, note = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ? LIMIT 1", [status, note || null, id]);
    if (!result.affectedRows) throw new Error("Không tìm thấy cảnh báo kinh tế.");
    await audit(user, "economy-monitor", `economy_alert.${status}`, "economy_alert", String(id), "success", { status, note: note || null });
    writeJson(res, 200, { ok: true, id, status }); return;
  }
  if (req.method === "POST" && url.pathname === "/api/actions/alert-ack") {
    if (!requireUser(user, res, "operator")) return;
    const body = await readJson(req); const id = String(body.id || "");
    if (!/^[a-f0-9-]{36}$/.test(id) || body.confirmation !== `ACK ALERT ${id}`) throw new Error("Alert hoặc mã xác nhận không hợp lệ.");
    const [result] = await pool.query("UPDATE panel_alerts SET status='acknowledged', acknowledged_by=?, acknowledged_at=NOW() WHERE id=? AND status='open'", [user.id, id]);
    if (!result.affectedRows) throw new Error("Alert không còn ở trạng thái open.");
    await audit(user, "incidents", "alert.acknowledged", "panel_alert", id, "success");
    writeJson(res, 200, { ok: true }); return;
  }
  if (req.method === "POST" && url.pathname === "/api/actions/currency-adjust") {
    if (!requireUser(user, res, "operator")) return;
    const body = await readJson(req);
    const id = Number(body.id); const amount = Number(body.amount); const currency = String(body.currency);
    const map = { luong: ["users", "luong"], coin: ["users", "coin"], xu: ["players", "xu"], yen: ["players", "yen"] };
    if (!Number.isInteger(id) || !Number.isSafeInteger(amount) || Math.abs(amount) > 1_000_000_000 || !map[currency] || body.confirmation !== `APPLY ${currency.toUpperCase()} ${id}`) throw new Error("Dữ liệu điều chỉnh tiền tệ không hợp lệ.");
    const [table, column] = map[currency];
    const conn = await pool.getConnection();
    try {
      await conn.beginTransaction();
      const [before] = await conn.query(`SELECT \`${column}\` AS value FROM \`${table}\` WHERE ${table === "users" ? "id" : "user_id"} = ? LIMIT 1 FOR UPDATE`, [id]);
      if (!before[0]) throw new Error("Không tìm thấy đối tượng cần điều chỉnh.");
      await conn.query(`UPDATE \`${table}\` SET \`${column}\` = GREATEST(0, \`${column}\` + ?) WHERE ${table === "users" ? "id" : "user_id"} = ? LIMIT 1`, [amount, id]);
      await conn.commit();
      await audit(user, "currency", "currency.adjusted", table === "users" ? "user" : "player", String(id), "success", { currency, amount, before: before[0].value });
      writeJson(res, 200, { ok: true });
    } catch (error) { await conn.rollback(); throw error; } finally { conn.release(); }
    return;
  }
  if (req.method === "POST" && url.pathname === "/api/actions/player-stats-update") {
    if (!requireUser(user, res, "operator")) return;
    const body = await readJson(req); const playerId = Number(body.playerId);
    if (!Number.isInteger(playerId) || playerId < 1 || body.confirmation !== `APPLY PLAYER STATS ${playerId}`) throw new Error("Player hoặc mã xác nhận không hợp lệ.");
    const columns = await tableColumns("players");
    const editable = availableColumns(columns, ["id", "online", "data", "point", "spoint", "potential", "numberCellBag", "numberCellBox"]);
    if (!editable.includes("online")) throw new Error("Schema không có cột online để chặn chỉnh live.");
    const patch = validatePlayerStats(body.stats || {}, columns);
    const conn = await pool.getConnection();
    try {
      await conn.beginTransaction();
      const [rows] = await conn.query(`SELECT ${editable.map(column => `\`${column}\``).join(", ")} FROM players WHERE id = ? LIMIT 1 FOR UPDATE`, [playerId]);
      const before = rows[0];
      if (!before) throw new Error("Không tìm thấy nhân vật.");
      if (Number(before.online) !== 0) throw new Error("Nhân vật đang online. Hãy yêu cầu người chơi thoát game hoàn toàn trước khi chỉnh chỉ số.");
      const changes = Object.entries(patch).map(([field, value]) => [field, Array.isArray(value) ? JSON.stringify(value) : value]);
      const expIndex = changes.findIndex(([field]) => field === "exp");
      if (expIndex >= 0) {
        let data;
        try { data = JSON.parse(before.data || "{}"); } catch { throw new Error("players.data hiện không phải JSON hợp lệ; không thể chỉnh EXP an toàn."); }
        if (!data || typeof data !== "object" || Array.isArray(data)) throw new Error("players.data không có object JSON hợp lệ.");
        data.exp = changes[expIndex][1];
        changes[expIndex] = ["data", JSON.stringify(data)];
      }
      await conn.query(`UPDATE players SET ${changes.map(([field]) => `\`${field}\` = ?`).join(", ")} WHERE id = ? LIMIT 1`, [...changes.map(([, value]) => value), playerId]);
      const after = { ...before, ...Object.fromEntries(changes) };
      const snapshotId = randomUUID();
      await conn.query("INSERT INTO panel_player_snapshots (id, player_id, snapshot_type, before_state, after_state, created_by) VALUES (?, ?, 'stats', ?, ?, ?)", [snapshotId, playerId, JSON.stringify(before), JSON.stringify(after), user.id]);
      await conn.commit();
      await audit(user, "players", "player.stats.updated", "player", String(playerId), "success", { fields: Object.keys(patch), snapshotId, offlineRequired: true });
      writeJson(res, 200, { ok: true, snapshotId, reloadRequired: true });
    } catch (error) { await conn.rollback(); throw error; } finally { conn.release(); }
    return;
  }
  if (req.method === "POST" && url.pathname === "/api/actions/player-inventory-update") {
    if (!requireUser(user, res, "operator")) return;
    const body = await readJson(req); const playerId = Number(body.playerId);
    if (!Number.isInteger(playerId) || playerId < 1 || body.confirmation !== `APPLY INVENTORY ${playerId}`) throw new Error("Player hoặc mã xác nhận inventory không hợp lệ.");
    const columns = await tableColumns("players");
    const selected = availableColumns(columns, ["id", "online", "numberCellBag", "numberCellBox", "bag", "box", "equiped", "fashion"]);
    if (!["id", "online", "bag", "box", "equiped", "fashion"].every(column => selected.includes(column))) throw new Error("Schema players không đủ cột hành trang để chỉnh an toàn.");
    const conn = await pool.getConnection();
    try {
      await conn.beginTransaction();
      const [rows] = await conn.query(`SELECT ${selected.map(column => `\`${column}\``).join(", ")} FROM players WHERE id = ? LIMIT 1 FOR UPDATE`, [playerId]);
      const before = rows[0];
      if (!before) throw new Error("Không tìm thấy nhân vật.");
      if (Number(before.online) !== 0) throw new Error("Nhân vật đang online. Hãy yêu cầu người chơi thoát game hoàn toàn trước khi chỉnh hành trang.");
      const { payload, itemIds } = validateInventory(body.inventory || {}, { bag: before.numberCellBag, box: before.numberCellBox });
      const oldIds = existingItemIds({ bag: before.bag, box: before.box, equiped: before.equiped, fashion: before.fashion });
      const ids = [...itemIds];
      if (ids.length > 1_000) throw new Error("Inventory chứa quá nhiều template item.");
      const [knownRows] = ids.length ? await conn.query(`SELECT id, type FROM item WHERE id IN (${ids.map(() => "?").join(",")})`, ids) : [[]];
      const known = new Set(knownRows.map(row => Number(row.id)));
      const missing = ids.filter(id => !known.has(id) && !oldIds.has(id));
      if (missing.length) throw new Error(`Item template không tồn tại trong catalog: ${missing.slice(0, 10).join(", ")}.`);
      const typeById = new Map(knownRows.map(row => [Number(row.id), Number(row.type)]));
      const optionIds = inventoryOptionIds(payload);
      if (optionIds.length) {
        const [knownOptionRows] = await conn.query(`SELECT id FROM item_option WHERE id IN (${optionIds.map(() => "?").join(",")})`, optionIds);
        const knownOptions = new Set(knownOptionRows.map(row => Number(row.id)));
        const missingOptions = optionIds.filter(id => !knownOptions.has(id));
        if (missingOptions.length) throw new Error(`Option item không tồn tại trong catalog: ${missingOptions.slice(0, 10).join(", ")}.`);
      }
      for (const section of ["equiped", "fashion"]) {
        const occupiedTypes = new Set();
        for (const entry of JSON.parse(payload[section])) {
          const itemType = typeById.get(Number(entry.id));
          if (!Number.isInteger(itemType) || itemType < 0 || itemType > 15) throw new Error(`${section} chỉ nhận item equipment có type 0-15 còn tồn tại trong catalog.`);
          if (occupiedTypes.has(itemType)) throw new Error(`${section} có hai item cùng equipment type ${itemType}.`);
          occupiedTypes.add(itemType);
        }
      }
      await conn.query("UPDATE players SET bag = ?, box = ?, equiped = ?, fashion = ? WHERE id = ? LIMIT 1", [payload.bag, payload.box, payload.equiped, payload.fashion, playerId]);
      const snapshotId = randomUUID();
      const after = { bag: payload.bag, box: payload.box, equiped: payload.equiped, fashion: payload.fashion };
      await conn.query("INSERT INTO panel_player_snapshots (id, player_id, snapshot_type, before_state, after_state, created_by) VALUES (?, ?, 'inventory', ?, ?, ?)", [snapshotId, playerId, JSON.stringify({ bag: before.bag, box: before.box, equiped: before.equiped, fashion: before.fashion }), JSON.stringify(after), user.id]);
      await conn.commit();
      await audit(user, "inventory", "player.inventory.updated", "player", String(playerId), "success", { snapshotId, sections: Object.fromEntries(Object.entries(payload).map(([section, raw]) => [section, JSON.parse(raw).length])), offlineRequired: true });
      writeJson(res, 200, { ok: true, snapshotId, reloadRequired: true });
    } catch (error) { await conn.rollback(); throw error; } finally { conn.release(); }
    return;
  }
  if (req.method === "POST" && url.pathname === "/api/actions/shop-update") {
    if (!requireUser(user, res, "operator")) return;
    const body = await readJson(req); const id = Number(body.id); const price = Number(body.price); const upgrade = Number(body.upgrade); const system = Number(body.system);
    if (![id, price, upgrade, system].every(Number.isInteger) || price < 0 || body.confirmation !== `APPLY SHOP ${id}`) throw new Error("Dữ liệu shop không hợp lệ.");
    let options = "[]"; try { options = JSON.stringify(JSON.parse(body.options || "[]")); } catch { throw new Error("Options phải là JSON hợp lệ."); }
    await pool.query("UPDATE shopcoin_tb1 SET price = ?, upgrade = ?, system = ?, options = ? WHERE id = ? LIMIT 1", [price, upgrade, system, options, id]);
    await audit(user, "shop", "shop.updated", "shop_item", String(id), "success", { price, upgrade, system });
    writeJson(res, 200, { ok: true, reloadRequired: true }); return;
  }
  if (req.method === "POST" && url.pathname === "/api/actions/store-create") {
    if (!requireUser(user, res, "operator")) return;
    const body = await readJson(req); const name = String(body.name || "").trim().slice(0, 500);
    if (!name || body.confirmation !== "CREATE NPC STORE") throw new Error("Tên store hoặc mã xác nhận không hợp lệ.");
    const [result] = await pool.query("INSERT INTO stores (name) VALUES (?)", [name]);
    await audit(user, "shop", "npc_store.created", "store", String(result.insertId), "success", { name });
    writeJson(res, 200, { ok: true, id: result.insertId, reloadRequired: true }); return;
  }
  if (req.method === "POST" && ["/api/actions/npc-shop-item-create", "/api/actions/npc-shop-item-update"].includes(url.pathname)) {
    if (!requireUser(user, res, "operator")) return;
    const body = await readJson(req); const id = Number(body.id); const itemId = Number(body.itemId); const storeId = Number(body.storeId); const sys = Number(body.sys); const lock = Number(body.lock); const coin = Number(body.coin); const gold = Number(body.gold); const yen = Number(body.yen); const expire = Number(body.expire);
    const normalizedOptions = normalizeShopOptions(body.options || []);
    const isUpdate = url.pathname.endsWith("-update"); const phrase = isUpdate ? `APPLY NPC SHOP ITEM ${id}` : `ADD NPC SHOP ITEM ${storeId}`;
    if (![itemId, storeId, sys, lock, coin, gold, yen, expire].every(Number.isSafeInteger) || (isUpdate && (!Number.isInteger(id) || id < 1)) || itemId < 0 || storeId < 0 || ![0, 1].includes(lock) || Math.min(coin, gold, yen) < 0 || expire < -1 || body.confirmation !== phrase) throw new Error("Dữ liệu hàng hóa NPC hoặc mã xác nhận không hợp lệ.");
    const optionIds = [...new Set(normalizedOptions.map(option => option[0]))];
    const [[itemRows], [storeRows], [optionRows]] = await Promise.all([
      pool.query("SELECT id FROM item WHERE id = ? LIMIT 1", [itemId]),
      pool.query("SELECT id FROM stores WHERE id = ? LIMIT 1", [storeId]),
      optionIds.length ? pool.query(`SELECT id FROM item_option WHERE id IN (${optionIds.map(() => "?").join(",")})`, optionIds) : Promise.resolve([[]]),
    ]);
    if (!itemRows[0] || !storeRows[0]) throw new Error("Item template hoặc store không tồn tại.");
    const knownOptionIds = new Set(optionRows.map(row => Number(row.id)));
    const missingOptions = optionIds.filter(id => !knownOptionIds.has(id));
    if (missingOptions.length) throw new Error(`Option hàng hóa NPC không tồn tại: ${missingOptions.join(", ")}.`);
    const options = JSON.stringify(normalizedOptions);
    if (!isUpdate) {
      const [result] = await pool.query("INSERT INTO store_data (item_id, sys, store, `lock`, coin, gold, yen, expire, options) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", [itemId, sys, storeId, lock, coin, gold, yen, expire, options]);
      await audit(user, "shop", "npc_store_item.created", "store_data", String(result.insertId), "success", { itemId, storeId, sys, lock, coin, gold, yen, expire });
      writeJson(res, 200, { ok: true, id: result.insertId, reloadRequired: true }); return;
    }
    const [result] = await pool.query("UPDATE store_data SET item_id = ?, sys = ?, store = ?, `lock` = ?, coin = ?, gold = ?, yen = ?, expire = ?, options = ? WHERE id = ? LIMIT 1", [itemId, sys, storeId, lock, coin, gold, yen, expire, options, id]);
    if (!result.affectedRows) throw new Error("Không tìm thấy hàng hóa NPC.");
    await audit(user, "shop", "npc_store_item.updated", "store_data", String(id), "success", { itemId, storeId, sys, lock, coin, gold, yen, expire });
    writeJson(res, 200, { ok: true, reloadRequired: true }); return;
  }
  if (req.method === "POST" && url.pathname === "/api/actions/npc-shop-item-delete") {
    if (!requireUser(user, res, "operator")) return;
    const body = await readJson(req); const id = Number(body.id);
    if (!Number.isInteger(id) || id < 1 || body.confirmation !== `DELETE NPC SHOP ITEM ${id}`) throw new Error("Hàng hóa NPC hoặc mã xác nhận không hợp lệ.");
    const [result] = await pool.query("DELETE FROM store_data WHERE id = ? LIMIT 1", [id]);
    if (!result.affectedRows) throw new Error("Không tìm thấy hàng hóa NPC.");
    await audit(user, "shop", "npc_store_item.deleted", "store_data", String(id), "success");
    writeJson(res, 200, { ok: true, reloadRequired: true }); return;
  }
  if (req.method === "POST" && url.pathname === "/api/actions/monster-update") {
    if (!requireUser(user, res, "operator")) return;
    const body = await readJson(req); const id = Number(body.id); const level = Number(body.level); const hp = Number(body.hp); const boss = Number(body.boss);
    if (![id, level, hp, boss].every(Number.isInteger) || hp < 1 || ![0, 1].includes(boss) || body.confirmation !== `APPLY MONSTER ${id}`) throw new Error("Dữ liệu monster không hợp lệ.");
    const name = String(body.name || "").trim().slice(0, 100); if (!name) throw new Error("Tên monster không được trống.");
    await pool.query("UPDATE monster SET name = ?, level = ?, hp = ?, boss = ? WHERE id = ? LIMIT 1", [name, level, hp, boss, id]);
    await audit(user, "bosses", "monster.updated", "monster", String(id), "success", { name, level, hp, boss });
    writeJson(res, 200, { ok: true, reloadRequired: true }); return;
  }
  if (req.method === "POST" && url.pathname === "/api/actions/item-update") {
    if (!requireUser(user, res, "operator")) return;
    const body = await readJson(req); const id = Number(body.id); const type = Number(body.type); const gender = Number(body.gender); const level = Number(body.level); const icon = Number(body.icon); const part = Number(body.part); const fashion = Number(body.fashion); const isUpToUp = Number(body.isUpToUp);
    const name = String(body.name || "").trim().slice(0, 500); const description = String(body.description || "").trim().slice(0, 500);
    if (![id, type, gender, level, icon, part, fashion, isUpToUp].every(Number.isInteger) || !name || type < 0 || type > 255 || ![-1, 0, 1, 2].includes(gender) || level < 0 || level > 1000 || icon < 0 || icon > 100000 || part < -1 || part > 100000 || fashion < -1 || fashion > 100000 || ![0, 1].includes(isUpToUp) || body.confirmation !== `APPLY ITEM ${id}`) throw new Error("Dữ liệu item không hợp lệ.");
    const [result] = await pool.query("UPDATE item SET name = ?, type = ?, gender = ?, description = ?, level = ?, icon = ?, part = ?, fashion = ?, isUpToUp = ? WHERE id = ? LIMIT 1", [name, type, gender, description, level, icon, part, fashion, isUpToUp, id]);
    if (!result.affectedRows) throw new Error("Không tìm thấy item cần cập nhật.");
    await audit(user, "custom-items", "item.updated", "item", String(id), "success", { name, type, gender, level, icon, part, fashion, isUpToUp });
    writeJson(res, 200, { ok: true, reloadRequired: true }); return;
  }
  if (req.method === "POST" && url.pathname === "/api/actions/item-create") {
    if (!requireUser(user, res, "operator")) return;
    const body = await readJson(req); const name = String(body.name || "").trim().slice(0, 500); const description = String(body.description || "").trim().slice(0, 500);
    const type = Number(body.type); const gender = Number(body.gender); const level = Number(body.level); const icon = Number(body.icon); const part = Number(body.part); const fashion = Number(body.fashion); const isUpToUp = Number(body.isUpToUp);
    if (!name || ![type, gender, level, icon, part, fashion, isUpToUp].every(Number.isInteger) || type < 0 || type > 255 || ![-1, 0, 1, 2].includes(gender) || level < 0 || level > 1000 || icon < 0 || icon > 100000 || part < -1 || part > 100000 || fashion < -1 || fashion > 100000 || ![0, 1].includes(isUpToUp) || body.confirmation !== "CREATE ITEM") throw new Error("Metadata item hoặc mã xác nhận không hợp lệ.");
    const conn = await pool.getConnection();
    try {
      await conn.beginTransaction();
      const autoIncrement = await hasAutoIncrementId("item");
      let id;
      if (autoIncrement) {
        const [result] = await conn.query("INSERT INTO item (name, type, gender, description, level, icon, part, fashion, isUpToUp) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", [name, type, gender, description, level, icon, part, fashion, isUpToUp]);
        id = result.insertId;
      } else {
        const [nextRows] = await conn.query("SELECT COALESCE(MAX(id), -1) + 1 AS next_id FROM item FOR UPDATE");
        id = Number(nextRows[0]?.next_id);
        await conn.query("INSERT INTO item (id, name, type, gender, description, level, icon, part, fashion, isUpToUp) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", [id, name, type, gender, description, level, icon, part, fashion, isUpToUp]);
      }
      await conn.commit();
      await audit(user, "custom-items", "item.created", "item", String(id), "success", { name, type, gender, level, icon, part, fashion, isUpToUp });
      writeJson(res, 200, { ok: true, id, reloadRequired: true }); return;
    } catch (error) { await conn.rollback(); throw error; } finally { conn.release(); }
  }
  if (req.method === "POST" && url.pathname === "/api/actions/reward-campaign-save") {
    if (!requireUser(user, res, "admin")) return;
    const body = await readJson(req);
    const id = Number(body.id || 0);
    const phrase = id ? `UPDATE REWARD ${id}` : `CREATE REWARD ${String(body.campaignKey || "").toUpperCase()}`;
    if (body.confirmation !== phrase) throw new Error(`Cần nhập ${phrase} để xác nhận.`);
    const campaignId = await saveRewardCampaign(user, body);
    writeJson(res, 200, { ok: true, id: campaignId }); return;
  }
  if (req.method === "POST" && url.pathname === "/api/actions/reward-campaign-state") {
    if (!requireUser(user, res, "admin")) return;
    const body = await readJson(req); const id = Number(body.id); const active = Number(body.active) ? 1 : 0;
    if (!Number.isInteger(id) || id < 1 || body.confirmation !== `${active ? "ENABLE" : "DISABLE"} REWARD ${id}`) throw new Error("Campaign hoặc mã xác nhận không hợp lệ.");
    const [result] = await pool.query("UPDATE panel_reward_campaigns SET active = ?, updated_by = ? WHERE id = ? LIMIT 1", [active, user.id, id]);
    if (!result.affectedRows) throw new Error("Không tìm thấy campaign.");
    await audit(user, "rewards", active ? "reward_campaign.enabled" : "reward_campaign.disabled", "reward_campaign", String(id), "success");
    writeJson(res, 200, { ok: true }); return;
  }
  if (req.method === "POST" && url.pathname === "/api/actions/reward-campaign-delete") {
    if (!requireUser(user, res, "admin")) return;
    const body = await readJson(req); const id = Number(body.id);
    if (!Number.isInteger(id) || id < 1 || body.confirmation !== `DELETE REWARD ${id}`) throw new Error("Campaign hoặc mã xác nhận không hợp lệ.");
    const [claims] = await pool.query("SELECT id FROM panel_reward_claims WHERE campaign_id = ? LIMIT 1", [id]);
    if (claims[0]) throw new Error("Campaign đã có người nhận, chỉ được tắt chứ không được xóa.");
    const connection = await pool.getConnection();
    try { await connection.beginTransaction(); await connection.query("DELETE FROM panel_reward_item_options WHERE reward_item_id IN (SELECT id FROM panel_reward_items WHERE campaign_id = ?)", [id]); await connection.query("DELETE FROM panel_reward_items WHERE campaign_id = ?", [id]); const [result] = await connection.query("DELETE FROM panel_reward_campaigns WHERE id = ? LIMIT 1", [id]); await connection.commit(); if (!result.affectedRows) throw new Error("Không tìm thấy campaign."); } catch (error) { await connection.rollback(); throw error; } finally { connection.release(); }
    await audit(user, "rewards", "reward_campaign.deleted", "reward_campaign", String(id), "success");
    writeJson(res, 200, { ok: true }); return;
  }
  if (req.method === "POST" && url.pathname === "/api/actions/world-boss-notification-save") {
    if (!requireUser(user, res, "admin")) return;
    const body = await readJson(req);
    if (body.confirmation !== `SAVE WORLD BOSS ${String(body.eventType || "").toUpperCase()}`) throw new Error("Mã xác nhận thông báo Boss không hợp lệ.");
    const id = await saveWorldBossNotification(user, body);
    writeJson(res, 200, { ok: true, id }); return;
  }
  if (req.method === "POST" && url.pathname === "/api/actions/world-boss-notification-state") {
    if (!requireUser(user, res, "admin")) return;
    const body = await readJson(req); const id = Number(body.id); const enabled = Number(body.enabled);
    if (!Number.isInteger(id) || id < 1 || ![0, 1].includes(enabled) || body.confirmation !== `${enabled ? "ENABLE" : "DISABLE"} WORLD BOSS ${id}`) throw new Error("Cấu hình hoặc mã xác nhận không hợp lệ.");
    const [result] = await pool.query("UPDATE panel_world_boss_notifications SET enabled = ?, updated_by = ? WHERE id = ? LIMIT 1", [enabled, user.id, id]);
    if (!result.affectedRows) throw new Error("Không tìm thấy cấu hình Boss notification.");
    await audit(user, "world-boss-notices", enabled ? "world_boss_notification.enabled" : "world_boss_notification.disabled", "world_boss_notification", String(id), "success");
    writeJson(res, 200, { ok: true }); return;
  }
  if (req.method === "POST" && url.pathname === "/api/actions/world-boss-notification-test") {
    if (!requireUser(user, res, "admin")) return;
    const body = await readJson(req); const id = Number(body.id);
    if (!Number.isInteger(id) || id < 1 || body.confirmation !== `TEST WORLD BOSS ${id}`) throw new Error("Cấu hình hoặc mã xác nhận không hợp lệ.");
    try {
      const onlinePlayers = await runtimeControlRequest("/api/control/world-boss-test", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ configId: id }) });
      await audit(user, "world-boss-notices", "world_boss_notification.tested", "world_boss_notification", String(id), "success", { onlinePlayers: Number(onlinePlayers.onlinePlayers || 0) });
      writeJson(res, 200, { ok: true, onlinePlayers: Number(onlinePlayers.onlinePlayers || 0) }); return;
    } catch (error) {
      await audit(user, "world-boss-notices", "world_boss_notification.tested", "world_boss_notification", String(id), "failed", { error: error.message || "unknown" });
      throw error;
    }
  }
  if (req.method === "POST" && url.pathname === "/api/actions/shinwa-update") {
    if (!requireUser(user, res, "admin")) return;
    const body = await readJson(req);
    const id = Number(body.id); const action = String(body.action || "");
    const price = Number(body.price); const time = Number(body.time);
    const confirmation = String(body.confirmation || "");
    if (!Number.isInteger(id) || id < 1 || !["update", "expire"].includes(action)) throw new Error("Kí gửi hoặc thao tác không hợp lệ.");
    if (action === "update" && (!Number.isInteger(price) || price < 1 || price > 2000000000 || !Number.isInteger(time) || time < 1 || time > 2592000)) throw new Error("Giá phải từ 1 đến 2.000.000.000 xu, thời hạn từ 1 giây đến 30 ngày.");
    if (confirmation !== `${action === "expire" ? "EXPIRE" : "UPDATE"} SHINWA ${id}`) throw new Error("Mã xác nhận kí gửi không hợp lệ.");
    const gameConfig = readGameProperties(); const serverId = Number(gameConfig["server.id"] || 0);
    const connection = await pool.getConnection();
    let listing;
    try {
      await connection.beginTransaction();
      const [rows] = await connection.query("SELECT id, seller, price, status, time FROM shinwa WHERE id = ? AND server_id = ? LIMIT 1 FOR UPDATE", [id, serverId]);
      if (!rows[0]) throw new Error("Không tìm thấy tin kí gửi trên server hiện tại.");
      if (Number(rows[0].status) !== 0) throw new Error("Chỉ được chỉnh tin đang bán; tin đã mua/nhận lại không thể sửa.");
      if (action === "expire") await connection.query("UPDATE shinwa SET time = 0 WHERE id = ? AND server_id = ? AND status = 0 LIMIT 1", [id, serverId]);
      else await connection.query("UPDATE shinwa SET price = ?, time = ? WHERE id = ? AND server_id = ? AND status = 0 LIMIT 1", [price, time, id, serverId]);
      listing = { ...rows[0], price: action === "expire" ? rows[0].price : price, time: action === "expire" ? 0 : time };
      await connection.commit();
    } catch (error) { await connection.rollback(); throw error; } finally { connection.release(); }
    let runtime = { synced: false, found: false };
    try {
      const response = await runtimeControlRequest("/api/control/shinwa-sync", { method:"POST", headers:{ "Content-Type":"application/json" }, body:JSON.stringify({ uniqueId:id, price:Number(listing.price), time:Number(listing.time), status:0 }) });
      runtime = { synced: true, found: Boolean(response.found) };
    } catch (error) {
      runtime = { synced: false, found: false, error: error.message || "Runtime chưa đồng bộ" };
    }
    await audit(user, "consignment", `shinwa.${action}`, "shinwa", String(id), "success", { seller: listing.seller, price: listing.price, time: listing.time, runtime });
    writeJson(res, 200, { ok: true, runtime }); return;
  }
  if (req.method === "POST" && url.pathname === "/api/actions/notice-broadcast") {
    if (!requireUser(user, res, "moderator")) return;
    const body = await readJson(req);
    const sender = String(body.sender || "Hệ thống").trim().slice(0, 40);
    const message = String(body.message || "").trim().slice(0, 500);
    const confirmation = String(body.confirmation || "").trim();
    const expectedConfirmation = `BROADCAST NOTICE ${sender.toUpperCase()}`;
    if (!sender || !message || confirmation !== expectedConfirmation) throw new Error("Thông tin thông báo hoặc mã xác nhận không hợp lệ.");
    try {
      const result = await runtimeControlRequest("/api/control/broadcast", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ sender, message }),
      });
      await audit(user, "notices", "notice.broadcast", "runtime-broadcast", sender, "success", { sender, message, onlinePlayers: Number(result.onlinePlayers || 0) });
      writeJson(res, 200, { ok: true, onlinePlayers: Number(result.onlinePlayers || 0) }); return;
    } catch (error) {
      await audit(user, "notices", "notice.broadcast", "runtime-broadcast", sender, "failed", { sender, message, error: error.message || "unknown" });
      throw error;
    }
  }
  if (req.method === "POST" && url.pathname === "/api/actions/option-update") {
    if (!requireUser(user, res, "admin")) return;
    const body = await readJson(req); const key = String(body.key || ""); const value = String(body.value ?? "").slice(0, 10000);
    if (!["expserver", "levelnjtl", "notify"].includes(key) || body.confirmation !== `APPLY OPTION ${key}`) throw new Error("Option hoặc mã xác nhận không hợp lệ.");
    const [existing] = await pool.query("SELECT id, value FROM options WHERE `key` = ? LIMIT 1", [key]);
    if (existing[0]) await pool.query("UPDATE options SET value = ? WHERE id = ? LIMIT 1", [value, existing[0].id]);
    else await pool.query("INSERT INTO options (`key`, value) VALUES (?, ?)", [key, value]);
    await audit(user, key === "notify" ? "notices" : "rates", "option.updated", "option", key, "success", { before: existing[0]?.value ?? null, after: value });
    writeJson(res, 200, { ok: true, reloadRequired: true }); return;
  }
  if (req.method === "POST" && url.pathname === "/api/actions/gift-code-save") {
    if (!requireUser(user, res, "operator")) return;
    const lifecycle = await giftLifecycleStatus();
    if (!lifecycle.ready) throw new Error(`Gift Code lifecycle chưa migration. Chạy bash scripts/migrate-gift-code-lifecycle.sh (thiếu: ${lifecycle.missing.join(", ")}).`);
    const body = await readJson(req);
    const id = body.id === null || body.id === undefined || body.id === "" ? null : Number(body.id);
    const code = String(body.code || "").trim().toUpperCase();
    const serverId = Number(body.serverId ?? 1); const type = Number(body.type) === 1 ? 1 : 0;
    const coin = Number(body.coin || 0); const gold = Number(body.gold || 0); const yen = Number(body.yen || 0);
    const startsAt = parseGiftDate(body.startsAt, "Thời gian bắt đầu"); const expiresAt = parseGiftDate(body.expiresAt, "Thời gian hết hạn");
    let maxRedemptions = body.maxRedemptions === null || body.maxRedemptions === undefined || String(body.maxRedemptions).trim() === "" || Number(body.maxRedemptions) === 0 ? null : Number(body.maxRedemptions);
    if (!/^[A-Z0-9_-]{4,30}$/.test(code) || !Number.isInteger(serverId) || serverId < 0 || serverId > 127 || ![coin, gold, yen].every(Number.isSafeInteger) || Math.min(coin, gold, yen) < 0 || (maxRedemptions !== null && (!Number.isInteger(maxRedemptions) || maxRedemptions < 1 || maxRedemptions > 10_000_000)) || (startsAt && expiresAt && expiresAt <= startsAt)) throw new Error("Code, server, tiền tệ, quota hoặc khoảng thời gian không hợp lệ.");
    if (type === 0) maxRedemptions = 1;
    const rewards = await normalizeGiftRewards(body.items || "[]");
    const items = JSON.stringify(rewards.map(({ item_name, item_type, ...item }) => item));
    const confirmation = id ? `UPDATE GIFT ${id}` : `CREATE GIFT ${code}`;
    if (body.confirmation !== confirmation) throw new Error("Mã xác nhận Gift Code không hợp lệ.");
    if (id !== null) {
      if (!Number.isInteger(id) || id < 1) throw new Error("Gift Code ID không hợp lệ.");
      const [existing] = await pool.query("SELECT id, code, status, redemption_count FROM gift_codes WHERE id = ? LIMIT 1", [id]);
      if (!existing[0]) throw new Error("Không tìm thấy Gift Code.");
      if (Number(existing[0].status) === 1 || Number(existing[0].redemption_count) > 0) throw new Error("Không sửa reward/lifecycle của Gift Code đã có lượt đổi; hãy disable code cũ và tạo code mới.");
      const [duplicate] = await pool.query("SELECT id FROM gift_codes WHERE code = ? AND id <> ? LIMIT 1", [code, id]);
      if (duplicate[0]) throw new Error("Gift Code đã tồn tại.");
      await pool.query("UPDATE gift_codes SET server_id=?, type=?, code=?, coin=?, gold=?, yen=?, items=?, starts_at=?, expires_at=?, max_redemptions=?, disabled=?, updated_at=NOW() WHERE id=? LIMIT 1", [serverId, type, code, coin, gold, yen, items, startsAt, expiresAt, maxRedemptions, body.disabled ? 1 : 0, id]);
      await audit(user, "rewards", "gift_code.updated", "gift_code", String(id), "success", { code, type, serverId, startsAt, expiresAt, maxRedemptions, rewardCount: rewards.length });
      writeJson(res, 200, { ok: true, id }); return;
    }
    const [duplicate] = await pool.query("SELECT id FROM gift_codes WHERE code = ? LIMIT 1", [code]);
    if (duplicate[0]) throw new Error("Gift Code đã tồn tại.");
    const [result] = await pool.query("INSERT INTO gift_codes (server_id, type, code, coin, gold, yen, items, status, disabled, starts_at, expires_at, max_redemptions, redemption_count, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, 0, ?, ?, ?, ?, 0, NOW())", [serverId, type, code, coin, gold, yen, items, body.disabled ? 1 : 0, startsAt, expiresAt, maxRedemptions]);
    await audit(user, "rewards", "gift_code.created", "gift_code", String(result.insertId), "success", { code, type, serverId, startsAt, expiresAt, maxRedemptions, rewardCount: rewards.length });
    writeJson(res, 200, { ok: true, id: result.insertId }); return;
  }
  if (req.method === "POST" && url.pathname === "/api/actions/gift-code-state") {
    if (!requireUser(user, res, "operator")) return;
    const lifecycle = await giftLifecycleStatus();
    if (!lifecycle.ready) throw new Error("Gift Code lifecycle chưa migration; chạy scripts/migrate-gift-code-lifecycle.sh.");
    const body = await readJson(req); const id = Number(body.id); const disabled = Boolean(body.disabled); const action = disabled ? "DISABLE" : "ENABLE";
    if (!Number.isInteger(id) || id < 1 || body.confirmation !== `${action} GIFT ${id}`) throw new Error("Gift Code state hoặc mã xác nhận không hợp lệ.");
    const [result] = await pool.query("UPDATE gift_codes SET disabled=?, updated_at=NOW() WHERE id=? LIMIT 1", [disabled ? 1 : 0, id]);
    if (!result.affectedRows) throw new Error("Không tìm thấy Gift Code.");
    await audit(user, "rewards", disabled ? "gift_code.disabled" : "gift_code.enabled", "gift_code", String(id), "success");
    writeJson(res, 200, { ok: true }); return;
  }
  if (req.method === "POST" && url.pathname === "/api/actions/gift-code-delete") {
    if (!requireUser(user, res, "admin")) return;
    const body = await readJson(req); const id = Number(body.id);
    if (!Number.isInteger(id) || id < 1 || body.confirmation !== `DELETE GIFT ${id}`) throw new Error("Gift Code ID hoặc mã xác nhận không hợp lệ.");
    const [rows] = await pool.query("SELECT code, redemption_count FROM gift_codes WHERE id=? LIMIT 1", [id]);
    if (!rows[0]) throw new Error("Không tìm thấy Gift Code.");
    if (Number(rows[0].redemption_count) > 0) throw new Error("Không xóa Gift Code đã có lượt đổi; hãy disable để bảo toàn đối soát.");
    await pool.query("DELETE FROM gift_codes WHERE id=? LIMIT 1", [id]);
    await audit(user, "rewards", "gift_code.deleted", "gift_code", String(id), "success", { code: rows[0].code });
    writeJson(res, 200, { ok: true }); return;
  }
  if (req.method === "POST" && url.pathname === "/api/actions/job-draft") {
    if (!requireUser(user, res, "admin")) return;
    const body = await readJson(req); const name = String(body.name || "").trim().slice(0, 120); const jobType = String(body.jobType || ""); const cronExpression = String(body.cronExpression || "").trim();
    if (!name || !["health_check", "daily_report", "cleanup", "event_transition", "maintenance_transition"].includes(jobType) || !/^\S+\s+\S+\s+\S+\s+\S+\s+\S+\s+\S+$/.test(cronExpression) || body.confirmation !== "CREATE JOB DRAFT") throw new Error("Draft job hoặc mã xác nhận không hợp lệ.");
    const [result] = await pool.query("INSERT INTO panel_jobs (name, job_type, cron_expression, payload, enabled, created_by) VALUES (?, ?, ?, ?, 0, ?)", [name, jobType, cronExpression, JSON.stringify(body.payload || {}), user.id]);
    await audit(user, "jobs", "job.drafted", "panel_job", String(result.insertId), "success", { name, jobType, cronExpression });
    writeJson(res, 200, { ok: true, id: result.insertId }); return;
  }
  if (req.method === "POST" && url.pathname === "/api/actions/job-enable") {
    if (!requireUser(user, res, "admin")) return;
    const body = await readJson(req); const id = Number(body.id);
    if (!Number.isInteger(id) || body.confirmation !== `ENABLE JOB ${id}`) throw new Error("Job hoặc mã xác nhận không hợp lệ.");
    await pool.query("UPDATE panel_jobs SET enabled = 1, approved_by = ? WHERE id = ? LIMIT 1", [user.id, id]);
    await audit(user, "jobs", "job.enabled", "panel_job", String(id), "success");
    writeJson(res, 200, { ok: true }); return;
  }
  if (req.method === "POST" && url.pathname === "/api/actions/maintenance-draft") {
    if (!requireUser(user, res, "admin")) return;
    const body = await readJson(req); const startsAt = new Date(body.startsAt); const endsAt = new Date(body.endsAt); const message = String(body.message || "").trim().slice(0, 500);
    if (Number.isNaN(startsAt.getTime()) || Number.isNaN(endsAt.getTime()) || startsAt >= endsAt || !message || body.confirmation !== "CREATE MAINTENANCE DRAFT") throw new Error("Kế hoạch bảo trì hoặc mã xác nhận không hợp lệ.");
    const [result] = await pool.query("INSERT INTO panel_maintenance_windows (starts_at, ends_at, message, status, created_by) VALUES (?, ?, ?, 'draft', ?)", [startsAt, endsAt, message, user.id]);
    await audit(user, "maintenance", "maintenance.drafted", "maintenance_window", String(result.insertId), "success", { startsAt: startsAt.toISOString(), endsAt: endsAt.toISOString() });
    writeJson(res, 200, { ok: true, id: result.insertId }); return;
  }
  if (req.method === "POST" && url.pathname === "/api/actions/maintenance-approve") {
    if (!requireUser(user, res, "admin")) return;
    const body = await readJson(req); const id = Number(body.id);
    if (!Number.isInteger(id) || id < 1 || body.confirmation !== `APPROVE MAINTENANCE ${id}`) throw new Error("Kế hoạch bảo trì hoặc mã xác nhận không hợp lệ.");
    const [result] = await pool.query("UPDATE panel_maintenance_windows SET status = 'scheduled', approved_by = ? WHERE id = ? AND status = 'draft' LIMIT 1", [user.id, id]);
    if (!result.affectedRows) throw new Error("Chỉ có thể phê duyệt maintenance ở trạng thái draft.");
    await audit(user, "maintenance", "maintenance.approved", "maintenance_window", String(id), "success");
    writeJson(res, 200, { ok: true, runtimeActionRequired: "Thực hiện dừng/khởi động game theo runbook khi tới giờ; panel không tự tác động runtime Java." }); return;
  }
  if (req.method === "POST" && url.pathname === "/api/actions/backup") {
    if (!requireUser(user, res, "admin")) return;
    const body = await readJson(req);
    if (body.confirmation !== "CREATE BACKUP") throw new Error("Cần nhập CREATE BACKUP để tạo sao lưu.");
    const filename = `nsoz-${new Date().toISOString().replace(/[:.]/g, "-")}.sql`;
    const output = join(BACKUP_DIR, filename);
    const args = ["-h", config.database.host, "-P", String(config.database.port), "-u", config.database.user, config.database.name];
    const env = { ...process.env, MYSQL_PWD: config.database.password || "" };
    await new Promise((resolvePromise, reject) => {
      const child = execFile("mariadb-dump", args, { env, maxBuffer: 8 * 1024 * 1024 }, error => error ? reject(error) : resolvePromise());
      child.stdout.pipe(createWriteStream(output, { mode: 0o600 }));
    });
    await audit(user, "backups", "backup.created", "database_backup", filename, "success");
    writeJson(res, 200, { ok: true, filename }); return;
  }
  if (req.method === "GET" && url.pathname === "/api/bots") {
    if (!requireUser(user, res, "operator")) return;
    const [rows] = await pool.query(`SELECT b.id, b.char_id, b.name, b.enabled, b.created_at, b.updated_at, p.name AS player_name, p.online, p.map FROM panel_bots b LEFT JOIN players p ON p.id = b.char_id ORDER BY b.id ASC`);
    writeJson(res, 200, { rows }); return;
  }
  if (req.method === "POST" && url.pathname === "/api/actions/bot-create") {
    if (!requireUser(user, res, "operator")) return;
    const body = await readJson(req);
    const charId = Number(body.charId);
    const name = String(body.name || "").trim().slice(0, 80);
    if (!Number.isInteger(charId) || charId < 1 || !name) throw new Error("charId hoặc tên bot không hợp lệ.");
    if (body.confirmation !== undefined && body.confirmation !== null && body.confirmation !== `CREATE BOT ${charId}`) throw new Error("Mã xác nhận không hợp lệ.");
    const [existing] = await pool.query("SELECT id FROM players WHERE id = ? LIMIT 1", [charId]);
    if (!existing[0]) throw new Error("Không tìm thấy nhân vật với charId đã chọn.");
    const [dup] = await pool.query("SELECT id FROM panel_bots WHERE char_id = ? LIMIT 1", [charId]);
    if (dup[0]) throw new Error("Nhân vật này đã là bot.");
    const [result] = await pool.query("INSERT INTO panel_bots (char_id, name, enabled) VALUES (?, ?, 1)", [charId, name]);
    await audit(user, "bots", "bot.created", "panel_bot", String(result.insertId), "success", { charId, name });
    try {
      const response = await runtimeControlRequest("/api/control/bot/add", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ charId }) });
      await audit(user, "bots", "bot.auto_started", "panel_bot", String(charId), "success", response);
    } catch (error) {
      await audit(user, "bots", "bot.auto_start.failed", "panel_bot", String(charId), "failed", { error: error.message || "unknown" });
    }
    writeJson(res, 200, { ok: true, id: result.insertId }); return;
  }
  if (req.method === "POST" && url.pathname === "/api/actions/bot-toggle") {
    if (!requireUser(user, res, "operator")) return;
    const body = await readJson(req);
    const id = Number(body.id);
    const enabled = Number(body.enabled) ? 1 : 0;
    if (!Number.isInteger(id) || id < 1 || ![0, 1].includes(enabled)) throw new Error("Bot không hợp lệ.");
    if (body.confirmation !== undefined && body.confirmation !== null && body.confirmation !== `${enabled ? "ENABLE" : "DISABLE"} BOT ${id}`) throw new Error("Mã xác nhận không hợp lệ.");
    const [result] = await pool.query("UPDATE panel_bots SET enabled = ? WHERE id = ? LIMIT 1", [enabled, id]);
    if (!result.affectedRows) throw new Error("Không tìm thấy bot.");
    await audit(user, "bots", enabled ? "bot.enabled" : "bot.disabled", "panel_bot", String(id), "success");
    writeJson(res, 200, { ok: true }); return;
  }
  if (req.method === "POST" && url.pathname === "/api/actions/bot-delete") {
    if (!requireUser(user, res, "operator")) return;
    const body = await readJson(req);
    const id = Number(body.id);
    if (!Number.isInteger(id) || id < 1) throw new Error("Bot không hợp lệ.");
    if (body.confirmation !== undefined && body.confirmation !== null && body.confirmation !== `DELETE BOT ${id}`) throw new Error("Mã xác nhận không hợp lệ.");
    const [result] = await pool.query("DELETE FROM panel_bots WHERE id = ? LIMIT 1", [id]);
    if (!result.affectedRows) throw new Error("Không tìm thấy bot.");
    await audit(user, "bots", "bot.deleted", "panel_bot", String(id), "success");
    writeJson(res, 200, { ok: true }); return;
  }
  if (req.method === "POST" && url.pathname === "/api/actions/bot-control") {
    if (!requireUser(user, res, "operator")) return;
    const body = await readJson(req);
    const action = String(body.action || "").trim().toLowerCase();
    const charId = Number(body.charId);
    if (!["start", "stop"].includes(action) || !Number.isInteger(charId) || charId < 1) throw new Error("Thao tác bot không hợp lệ.");
    if (body.confirmation !== undefined && body.confirmation !== null && body.confirmation !== `${action.toUpperCase()} BOT ${charId}`) throw new Error("Mã xác nhận không hợp lệ.");
    try {
      const response = await runtimeControlRequest(`/api/control/bot/${action}`, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ charId }) });
      await audit(user, "bots", `bot.${action}`, "panel_bot", String(charId), "success", response);
      writeJson(res, 200, { ok: true, ...response }); return;
    } catch (error) {
      await audit(user, "bots", `bot.${action}.failed`, "panel_bot", String(charId), "failed", { error: error.message || "unknown" });
      throw error;
    }
  }
  writeJson(res, 404, { error: "Không tìm thấy API." });
}

const mimeTypes = { ".html": "text/html; charset=utf-8", ".js": "application/javascript; charset=utf-8", ".css": "text/css; charset=utf-8", ".json": "application/json; charset=utf-8", ".svg": "image/svg+xml" };
function serveStatic(req, res, url) {
  const requested = url.pathname === "/" ? "index.html" : url.pathname.replace(/^\//, "");
  const file = resolve(PUBLIC_DIR, requested);
  if (!file.startsWith(PUBLIC_DIR) || !existsSync(file) || statSync(file).isDirectory()) { res.writeHead(404); res.end("Not found"); return; }
  res.writeHead(200, { "Content-Type": mimeTypes[extname(file)] || "application/octet-stream", "Cache-Control": "no-store" });
  createReadStream(file).pipe(res);
}

if (config.bootstrapSchema !== false) await ensureSchema();
const server = createServer(async (req, res) => {
  res.setHeader("X-Content-Type-Options", "nosniff");
  res.setHeader("X-Frame-Options", "DENY");
  res.setHeader("Referrer-Policy", "no-referrer");
  res.setHeader("Content-Security-Policy", "default-src 'self'; style-src 'self'; script-src 'self'; connect-src 'self'; img-src 'self' data:; base-uri 'none'; frame-ancestors 'none'");
  const url = new URL(req.url, `http://${req.headers.host || "127.0.0.1"}`);
  try { if (url.pathname.startsWith("/api/")) await api(req, res, url); else serveStatic(req, res, url); }
  catch (error) { console.error(error); writeJson(res, 400, { error: error.message || "Có lỗi không xác định." }); }
});

server.listen(config.port, LOCAL_BIND_HOST, () => console.log(`Ninja School offline panel (no login): http://${LOCAL_BIND_HOST}:${config.port}`));
