import { randomUUID } from "node:crypto";
import { existsSync, mkdirSync, readFileSync, writeFileSync } from "node:fs";
import net from "node:net";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";
import mysql from "mysql2/promise";
import { isCronDue, runKey } from "./lib/cron.mjs";

const __dirname = dirname(fileURLToPath(import.meta.url));
const config = JSON.parse(readFileSync(join(__dirname, "config.local.json"), "utf8"));
const reportDir = join(__dirname, "reports");
mkdirSync(reportDir, { recursive: true, mode: 0o700 });
const { name: database, ...databaseConfig } = config.database;
const pool = mysql.createPool({ ...databaseConfig, database, waitForConnections: true, connectionLimit: 3 });
const handledTicks = new Map();

function tcpStatus(host, port, timeout = 800) {
  return new Promise(resolve => {
    const socket = net.createConnection({ host, port });
    const done = value => { socket.destroy(); resolve(value); };
    socket.setTimeout(timeout); socket.once("connect", () => done(true)); socket.once("timeout", () => done(false)); socket.once("error", () => done(false));
  });
}

async function audit(job, outcome, details) {
  await pool.query(
    "INSERT INTO panel_audit_events (id, actor_username, module_name, action_name, resource_type, resource_id, outcome, correlation_id, metadata) VALUES (?, 'scheduler', 'jobs', 'job.executed', 'panel_job', ?, ?, ?, ?)",
    [randomUUID(), String(job.id), outcome, randomUUID(), JSON.stringify(details)],
  );
}

async function executeJob(job) {
  const startedAt = new Date();
  try {
    if (job.job_type === "health_check") {
      let dbOnline = true; try { await pool.query("SELECT 1"); } catch { dbOnline = false; }
      const gamePortOpen = await tcpStatus("127.0.0.1", 14444);
      const result = { dbOnline, gamePortOpen, checkedAt: startedAt.toISOString() };
      await audit(job, dbOnline && gamePortOpen ? "success" : "failed", result);
      await pool.query("UPDATE panel_jobs SET last_run_at = NOW(), last_result = ? WHERE id = ?", [JSON.stringify(result), job.id]);
      return;
    }
    if (job.job_type === "daily_report") {
      const [stats] = await pool.query("SELECT (SELECT COUNT(*) FROM users) accounts, (SELECT COUNT(*) FROM players) players, (SELECT COUNT(*) FROM players WHERE online=1) onlinePlayers");
      const report = { generatedAt: startedAt.toISOString(), ...stats[0] };
      const filename = `daily-${startedAt.toISOString().slice(0, 10)}.json`;
      writeFileSync(join(reportDir, filename), `${JSON.stringify(report, null, 2)}\n`, { mode: 0o600 });
      await audit(job, "success", { filename, ...report });
      await pool.query("UPDATE panel_jobs SET last_run_at = NOW(), last_result = ? WHERE id = ?", [`report:${filename}`, job.id]);
      return;
    }
    if (job.job_type === "cleanup") {
      await audit(job, "success", { localOnly: true, removedSessions: 0 });
      await pool.query("UPDATE panel_jobs SET last_run_at = NOW(), last_result = ? WHERE id = ?", ["local-only:no-sessions", job.id]);
      return;
    }
    const result = { blocked: true, reason: "Event và maintenance transition phải được triển khai riêng theo runbook trước khi bật tự động." };
    await audit(job, "denied", result);
    await pool.query("UPDATE panel_jobs SET last_run_at = NOW(), last_result = ? WHERE id = ?", [result.reason, job.id]);
  } catch (error) {
    await pool.query("UPDATE panel_jobs SET last_run_at = NOW(), last_result = ? WHERE id = ?", [String(error.message).slice(0, 500), job.id]);
    await audit(job, "failed", { error: String(error.message) });
  }
}

async function tick() {
  const now = new Date();
  const [jobs] = await pool.query("SELECT id, name, job_type, cron_expression FROM panel_jobs WHERE enabled = 1 AND approved_by IS NOT NULL");
  for (const job of jobs) {
    const key = `${job.id}:${runKey(now)}`;
    if (isCronDue(job.cron_expression, now) && !handledTicks.has(key)) {
      handledTicks.set(key, now.getTime());
      await executeJob(job);
    }
  }
  for (const [key, timestamp] of handledTicks) if (now.getTime() - timestamp > 120_000) handledTicks.delete(key);
}

console.log("Ninja Control Room scheduler started (offline, approved jobs only).");
setInterval(() => tick().catch(error => console.error("scheduler tick failed", error)), 1000);
tick().catch(error => console.error("scheduler bootstrap failed", error));
