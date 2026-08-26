import { randomBytes } from "node:crypto";
import { createReadStream, createWriteStream, existsSync, mkdirSync, readFileSync, statSync, writeFileSync } from "node:fs";
import { createServer } from "node:http";
import { dirname, extname, join, resolve } from "node:path";
import { fileURLToPath } from "node:url";
import { execFile } from "node:child_process";
import net from "node:net";
import mysql from "mysql2/promise";
import { hashPassword, hasRole, randomToken, tokenHash, verifyPassword } from "./lib/security.mjs";
import { availableColumns } from "./lib/schema.mjs";

const __dirname = dirname(fileURLToPath(import.meta.url));
const ROOT_DIR = resolve(__dirname, "..");
const DATA_DIR = join(__dirname, "data");
const BACKUP_DIR = join(__dirname, "backups");
const PUBLIC_DIR = join(__dirname, "public");
const CONFIG_PATH = join(__dirname, "config.local.json");
const FIRST_LOGIN_PATH = join(DATA_DIR, "first-login.txt");
const MAX_BODY_SIZE = 1_000_000;
const SESSION_COOKIE = "nso_admin_session";
const tableColumnsCache = new Map();
const modules = [
  ["dashboard", "Tổng quan", "Vận hành", "Sức khỏe game, MariaDB, port 14444 và hoạt động gần đây", "analyst"],
  ["players", "Người chơi", "Người chơi", "Tìm kiếm nhân vật, trạng thái online, map, level và tài sản", "analyst"],
  ["accounts", "Tài khoản", "Người chơi", "Tra cứu, lock, unlock, ban và kích hoạt tài khoản", "moderator"],
  ["moderation", "Kiểm duyệt", "An toàn", "Ban có thời hạn và account status với audit; không giả lập kick/mute runtime", "moderator"],
  ["inventory", "Túi đồ", "Kinh tế", "Đọc JSON bag/box/equipped an toàn; ghi inventory bị chặn để tránh desync", "operator"],
  ["currency", "Tiền tệ", "Kinh tế", "Lượng, coin, xu, yên với transaction và xác nhận", "operator"],
  ["rewards", "Phần thưởng", "Kinh tế", "Gift code, reward delivery và theo dõi lịch sử", "operator"],
  ["reward-history", "Lịch sử reward", "Kinh tế", "Đối soát gift_code_histories theo dữ liệu game", "analyst"],
  ["custom-items", "Vật phẩm tùy biến", "Nội dung", "Chỉnh metadata item có validation", "operator"],
  ["shop", "Cửa hàng", "Nội dung", "Giá, nâng cấp, hệ và options shopcoin", "operator"],
  ["events", "Sự kiện", "Nội dung", "Đọc event point/reward state; lifecycle runtime không tự động hóa", "operator"],
  ["rates", "Tỷ lệ game", "Nội dung", "EXP, level rate và option có phê duyệt", "admin"],
  ["bosses", "Quái & Boss", "Nội dung", "Metadata quái, boss, HP và bản đồ spawn", "operator"],
  ["notices", "Thông báo", "Vận hành", "Cập nhật options.notify theo Java contract; broadcast runtime không tự động hóa", "moderator"],
  ["maintenance", "Bảo trì", "Vận hành", "Draft/approval window và runbook restart; không dừng Java chỉ bằng SQL", "admin"],
  ["health", "Sức khỏe server", "Vận hành", "Liveness check và resource snapshot", "analyst"],
  ["incidents", "Sự cố", "Vận hành", "Triage, acknowledgement và escalation", "operator"],
  ["audit", "Audit trail", "Kiểm soát", "Dấu vết chỉ-append cho thao tác nhạy cảm", "analyst"],
  ["backups", "Sao lưu", "Kiểm soát", "Tạo MariaDB dump local và retention", "admin"],
  ["leaderboards", "Bảng xếp hạng", "Phân tích", "Danh sách top và refresh reference", "analyst"],
  ["analytics", "Phân tích", "Phân tích", "KPI player, economy và báo cáo vận hành", "analyst"],
  ["jobs", "Tác vụ định kỳ", "Kiểm soát", "Health check, cleanup, report và transition đã phê duyệt", "admin"],
  ["security", "Bảo mật tài khoản", "Kiểm soát", "Đổi mật khẩu panel local và thu hồi session", "viewer"],
].map(([id, label, group, description, role]) => ({ id, label, group, description, role }));

function ensureLocalConfig() {
  mkdirSync(DATA_DIR, { recursive: true, mode: 0o700 });
  mkdirSync(BACKUP_DIR, { recursive: true, mode: 0o700 });
  if (!existsSync(CONFIG_PATH)) {
    const template = JSON.parse(readFileSync(join(__dirname, "config.example.json"), "utf8"));
    writeFileSync(CONFIG_PATH, `${JSON.stringify(template, null, 2)}\n`, { mode: 0o600 });
  }
  return JSON.parse(readFileSync(CONFIG_PATH, "utf8"));
}

const config = ensureLocalConfig();
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

async function ensureSchema() {
  const statements = [
    `CREATE TABLE IF NOT EXISTS panel_admin_users (
      id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
      username VARCHAR(64) NOT NULL UNIQUE,
      password_hash VARCHAR(255) NOT NULL,
      role ENUM('viewer','analyst','moderator','operator','admin') NOT NULL DEFAULT 'viewer',
      active TINYINT(1) NOT NULL DEFAULT 1,
      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4`,
    `CREATE TABLE IF NOT EXISTS panel_sessions (
      token_hash CHAR(64) NOT NULL PRIMARY KEY,
      user_id INT NOT NULL,
      csrf_token VARCHAR(96) NOT NULL,
      expires_at TIMESTAMP NOT NULL,
      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      INDEX panel_sessions_expiry_idx (expires_at)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4`,
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
  ];
  for (const statement of statements) await pool.query(statement);
  const [admins] = await pool.query("SELECT id FROM panel_admin_users WHERE role = 'admin' LIMIT 1");
  if (admins.length === 0) {
    const password = process.env.NSO_PANEL_ADMIN_PASSWORD || randomToken(15);
    await pool.query("INSERT INTO panel_admin_users (username, password_hash, role) VALUES (?, ?, 'admin')", ["admin", hashPassword(password)]);
    writeFileSync(FIRST_LOGIN_PATH, `Ninja School Offline panel\nUsername: admin\nPassword: ${password}\n\nĐổi mật khẩu sau lần đăng nhập đầu tiên.\n`, { mode: 0o600 });
    console.log(`Panel admin bootstrap created. Credentials saved to ${FIRST_LOGIN_PATH}`);
  }
}

function setCookie(res, value, maxAge = config.sessionHours * 3600) {
  res.setHeader("Set-Cookie", `${SESSION_COOKIE}=${value}; HttpOnly; SameSite=Strict; Path=/; Max-Age=${maxAge}`);
}

function clearCookie(res) {
  res.setHeader("Set-Cookie", `${SESSION_COOKIE}=; HttpOnly; SameSite=Strict; Path=/; Max-Age=0`);
}

function parseCookies(req) {
  return Object.fromEntries((req.headers.cookie || "").split(";").map(pair => pair.trim().split("=")).filter(pair => pair[0]));
}

async function getSessionUser(req) {
  const rawToken = parseCookies(req)[SESSION_COOKIE];
  if (!rawToken) return null;
  const [rows] = await pool.query(
    `SELECT u.id, u.username, u.role, s.csrf_token AS csrfToken
     FROM panel_sessions s JOIN panel_admin_users u ON u.id = s.user_id
     WHERE s.token_hash = ? AND s.expires_at > NOW() AND u.active = 1 LIMIT 1`,
    [tokenHash(rawToken)],
  );
  return rows[0] || null;
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
  if (req.headers["x-nso-csrf"] !== user.csrfToken) throw new Error("CSRF token không hợp lệ.");
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
  if (!user) { writeJson(res, 401, { error: "Bạn cần đăng nhập panel." }); return false; }
  if (!hasRole(user, role)) { writeJson(res, 403, { error: "Vai trò hiện tại không đủ quyền." }); return false; }
  return true;
}

async function api(req, res, url) {
  const user = await getSessionUser(req);
  if (req.method === "POST" && url.pathname !== "/api/auth/login") {
    if (!requireUser(user, res)) return;
    try { assertCsrf(req, user); } catch (error) { await audit(user, "security", "csrf.denied", "session", null, "denied"); writeJson(res, 403, { error: error.message }); return; }
  }
  if (req.method === "POST" && url.pathname === "/api/auth/login") {
    const body = await readJson(req);
    const username = String(body.username || "").trim();
    const password = String(body.password || "");
    const [rows] = await pool.query("SELECT id, username, password_hash, role FROM panel_admin_users WHERE username = ? AND active = 1 LIMIT 1", [username]);
    const admin = rows[0];
    if (!admin || !verifyPassword(password, admin.password_hash)) { writeJson(res, 401, { error: "Tên đăng nhập hoặc mật khẩu không đúng." }); return; }
    const token = randomToken();
    const csrf = randomToken();
    await pool.query("DELETE FROM panel_sessions WHERE expires_at <= NOW()");
    await pool.query("INSERT INTO panel_sessions (token_hash, user_id, csrf_token, expires_at) VALUES (?, ?, ?, DATE_ADD(NOW(), INTERVAL ? SECOND))", [tokenHash(token), admin.id, csrf, Number(config.sessionHours) * 3600]);
    setCookie(res, token);
    await audit({ id: admin.id, username: admin.username }, "security", "session.login", "admin_user", String(admin.id), "success");
    writeJson(res, 200, { user: { username: admin.username, role: admin.role }, csrf });
    return;
  }
  if (req.method === "POST" && url.pathname === "/api/auth/logout") {
    const token = parseCookies(req)[SESSION_COOKIE];
    if (token) await pool.query("DELETE FROM panel_sessions WHERE token_hash = ?", [tokenHash(token)]);
    await audit(user, "security", "session.logout", "session", null, "success");
    clearCookie(res); writeJson(res, 200, { ok: true }); return;
  }
  if (req.method === "GET" && url.pathname === "/api/auth/me") {
    if (!user) { writeJson(res, 401, { error: "unauthenticated" }); return; }
    writeJson(res, 200, { user: { username: user.username, role: user.role }, csrf: user.csrfToken }); return;
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
    const [rows] = await pool.query("SELECT id, name, type, gender, description, level, icon, part, fashion, isUpToUp FROM item WHERE name LIKE ? OR CAST(id AS CHAR) LIKE ? ORDER BY id LIMIT 100", [q, q]);
    writeJson(res, 200, { rows }); return;
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
  if (req.method === "GET" && url.pathname === "/api/shop") {
    if (!requireUser(user, res, "analyst")) return;
    const [rows] = await pool.query("SELECT s.id, s.idItem, i.name AS itemName, s.price, s.upgrade, s.system, s.options FROM shopcoin_tb1 s LEFT JOIN item i ON i.id = s.idItem ORDER BY s.id LIMIT 200");
    writeJson(res, 200, { rows }); return;
  }
  if (req.method === "GET" && url.pathname === "/api/monsters") {
    if (!requireUser(user, res, "analyst")) return;
    const boss = url.searchParams.get("boss") === "1" ? 1 : 0;
    const [rows] = await pool.query("SELECT id, name, level, boss, type, hp, range_move, speed FROM monster WHERE boss = ? ORDER BY level DESC LIMIT 250", [boss]);
    writeJson(res, 200, { rows }); return;
  }
  if (req.method === "GET" && url.pathname === "/api/gift-codes") {
    if (!requireUser(user, res, "operator")) return;
    const [rows] = await pool.query("SELECT id, server_id, type, code, coin, gold, yen, items, status, expires_at, created_at FROM gift_codes ORDER BY created_at DESC LIMIT 100");
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
  if (req.method === "POST" && url.pathname === "/api/actions/account-ban") {
    if (!requireUser(user, res, "moderator")) return;
    const body = await readJson(req); const id = Number(body.userId); const banUntil = new Date(body.banUntil);
    if (!Number.isInteger(id) || id < 1 || Number.isNaN(banUntil.getTime()) || banUntil.getTime() <= Date.now() || body.confirmation !== `BAN ACCOUNT ${id}`) throw new Error("Dữ liệu ban hoặc mã xác nhận không hợp lệ.");
    await pool.query("UPDATE users SET status = 2, ban_until = ? WHERE id = ? LIMIT 1", [banUntil, id]);
    await audit(user, "moderation", "account.banned", "user", String(id), "success", { banUntil: banUntil.toISOString() });
    writeJson(res, 200, { ok: true }); return;
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
  if (req.method === "POST" && url.pathname === "/api/actions/shop-update") {
    if (!requireUser(user, res, "operator")) return;
    const body = await readJson(req); const id = Number(body.id); const price = Number(body.price); const upgrade = Number(body.upgrade); const system = Number(body.system);
    if (![id, price, upgrade, system].every(Number.isInteger) || price < 0 || body.confirmation !== `APPLY SHOP ${id}`) throw new Error("Dữ liệu shop không hợp lệ.");
    let options = "[]"; try { options = JSON.stringify(JSON.parse(body.options || "[]")); } catch { throw new Error("Options phải là JSON hợp lệ."); }
    await pool.query("UPDATE shopcoin_tb1 SET price = ?, upgrade = ?, system = ?, options = ? WHERE id = ? LIMIT 1", [price, upgrade, system, options, id]);
    await audit(user, "shop", "shop.updated", "shop_item", String(id), "success", { price, upgrade, system });
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
    const body = await readJson(req); const id = Number(body.id); const level = Number(body.level); const icon = Number(body.icon);
    const name = String(body.name || "").trim().slice(0, 500); const description = String(body.description || "").trim().slice(0, 500);
    if (![id, level, icon].every(Number.isInteger) || !name || body.confirmation !== `APPLY ITEM ${id}`) throw new Error("Dữ liệu item không hợp lệ.");
    await pool.query("UPDATE item SET name = ?, description = ?, level = ?, icon = ? WHERE id = ? LIMIT 1", [name, description, level, icon, id]);
    await audit(user, "custom-items", "item.updated", "item", String(id), "success", { name, level, icon });
    writeJson(res, 200, { ok: true, reloadRequired: true }); return;
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
  if (req.method === "POST" && url.pathname === "/api/actions/gift-code-create") {
    if (!requireUser(user, res, "operator")) return;
    const body = await readJson(req); const code = String(body.code || "").trim().toUpperCase(); const coin = Number(body.coin || 0); const gold = Number(body.gold || 0); const yen = Number(body.yen || 0);
    let items = "[]"; try { items = JSON.stringify(JSON.parse(body.items || "[]")); } catch { throw new Error("Items phải là JSON hợp lệ."); }
    if (!/^[A-Z0-9_-]{4,48}$/.test(code) || ![coin, gold, yen].every(Number.isSafeInteger) || Math.min(coin, gold, yen) < 0 || body.confirmation !== `CREATE GIFT ${code}`) throw new Error("Gift code hoặc mã xác nhận không hợp lệ.");
    const [duplicate] = await pool.query("SELECT id FROM gift_codes WHERE code = ? LIMIT 1", [code]);
    if (duplicate[0]) throw new Error("Gift code đã tồn tại.");
    const expiresAt = body.expiresAt ? new Date(body.expiresAt) : null;
    if (expiresAt && Number.isNaN(expiresAt.getTime())) throw new Error("Ngày hết hạn không hợp lệ.");
    const [result] = await pool.query("INSERT INTO gift_codes (server_id, type, code, coin, gold, yen, items, status, expires_at, created_at) VALUES (1, ?, ?, ?, ?, ?, ?, 0, ?, NOW())", [Number(body.type) === 1 ? 1 : 0, code, coin, gold, yen, items, expiresAt]);
    await audit(user, "rewards", "gift_code.created", "gift_code", String(result.insertId), "success", { code, coin, gold, yen, expiresAt });
    writeJson(res, 200, { ok: true, id: result.insertId }); return;
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
  if (req.method === "POST" && url.pathname === "/api/auth/password") {
    if (!requireUser(user, res, "viewer")) return;
    const body = await readJson(req); const currentPassword = String(body.currentPassword || ""); const newPassword = String(body.newPassword || "");
    if (newPassword.length < 12 || newPassword.length > 128) throw new Error("Mật khẩu mới cần từ 12 đến 128 ký tự.");
    const [rows] = await pool.query("SELECT password_hash FROM panel_admin_users WHERE id = ? LIMIT 1", [user.id]);
    if (!rows[0] || !verifyPassword(currentPassword, rows[0].password_hash)) throw new Error("Mật khẩu hiện tại không đúng.");
    await pool.query("UPDATE panel_admin_users SET password_hash = ? WHERE id = ? LIMIT 1", [hashPassword(newPassword), user.id]);
    await pool.query("DELETE FROM panel_sessions WHERE user_id = ?", [user.id]);
    await audit(user, "security", "password.changed", "admin_user", String(user.id), "success");
    clearCookie(res); writeJson(res, 200, { ok: true, reloginRequired: true }); return;
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

server.listen(config.port, config.bindHost, () => console.log(`Ninja School offline panel: http://${config.bindHost}:${config.port}`));
