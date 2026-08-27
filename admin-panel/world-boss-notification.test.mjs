import { describe, expect, it } from "vitest";
import { readFileSync } from "node:fs";

const server = readFileSync(new URL("./server.mjs", import.meta.url), "utf8");
const app = readFileSync(new URL("./public/app.js", import.meta.url), "utf8");
const runtime = readFileSync(new URL("../src/main/java/Exe_Z/api/RuntimeControlServer.java", import.meta.url), "utf8");
const service = readFileSync(new URL("../src/main/java/Exe_Z/server/WorldBossNotificationService.java", import.meta.url), "utf8");
const spawn = readFileSync(new URL("../src/main/java/Exe_Z/server/SpawnBoss.java", import.meta.url), "utf8");
const manager = readFileSync(new URL("../src/main/java/Exe_Z/server/SpawnBossManager.java", import.meta.url), "utf8");
const mob = readFileSync(new URL("../src/main/java/Exe_Z/mob/Mob.java", import.meta.url), "utf8");

describe("World boss notification system", () => {
  it("creates normalized notification rules and append-only send logs", () => {
    expect(server).toContain("panel_world_boss_notifications");
    expect(server).toContain("panel_world_boss_notification_logs");
    expect(server).toContain("world-boss-notifications");
    expect(server).toContain("world-boss-notification-save");
    expect(server).toContain("world-boss-notification-state");
    expect(server).toContain("world-boss-notification-test");
  });

  it("validates placeholders and supports per-server override/cooldown", () => {
    expect(server).toContain('const allowedPlaceholders = ["{boss}", "{map}", "{zone}", "{killer}", "{time}"];');
    expect(server).toContain("cooldownSeconds > 86400");
    expect(server).toContain("serverId > 255");
    expect(service).toContain("ORDER BY server_id DESC");
    expect(service).toContain("LAST_SENT_AT");
  });

  it("connects spawn and defeat events to GlobalService without affecting non-world bosses", () => {
    expect(spawn).toContain("WorldBossNotificationService.notifySpawn");
    expect(manager).toContain("setNotificationGroup(key)");
    expect(manager).toContain("findByMonster");
    expect(mob).toContain("WorldBossNotificationService.notifyDefeat(this, killer)");
    expect(service).toContain("GlobalService.getInstance().chat");
  });

  it("provides an admin UI for templates, test sending and history", () => {
    expect(app).toContain("Thông báo Boss thế giới");
    expect(app).toContain("world-boss-notification-test");
    expect(app).toContain("{boss}");
    expect(app).toContain("Lịch sử thông báo Boss thế giới");
    expect(runtime).toContain("WORLD_BOSS_TEST_PATH");
    expect(runtime).toContain("WorldBossNotificationService.sendTest");
  });
});
