import { describe, expect, it } from "vitest";
import { readFileSync } from "node:fs";

const server = readFileSync(new URL("./server.mjs", import.meta.url), "utf8");
const app = readFileSync(new URL("./public/app.js", import.meta.url), "utf8");
const stall = readFileSync(new URL("../src/main/java/Exe_Z/stall/Stall.java", import.meta.url), "utf8");
const manager = readFileSync(new URL("../src/main/java/Exe_Z/stall/StallManager.java", import.meta.url), "utf8");
const runtime = readFileSync(new URL("../src/main/java/Exe_Z/api/RuntimeControlServer.java", import.meta.url), "utf8");

describe("NPC Shinwa consignment management", () => {
  it("reads the real shinwa table and normalizes item data for the UI", () => {
    expect(server).toContain("async function shinwaData");
    expect(server).toContain("FROM shinwa s LEFT JOIN item i");
    expect(server).toContain("panel_audit_events");
    expect(server).toContain("parseShinwaItem");
    expect(server).toContain("optionNames");
    expect(server).not.toContain("body.itemJson");
  });

  it("filters by server, seller/item/id and all three runtime statuses", () => {
    expect(server).toContain('s.server_id = ?');
    expect(server).toContain('status = ?');
    expect(server).toContain('0: "Đang bán"');
    expect(server).toContain('1: "Đã bán"');
    expect(server).toContain('2: "Đã nhận lại"');
    expect(app).toContain("Tìm theo ID tin, tên người bán hoặc tên vật phẩm");
  });

  it("restricts mutations to active listings and uses admin confirmation", () => {
    expect(server).toContain("shinwa-update");
    expect(server).toContain('action === "update"');
    expect(server).toContain('action === "expire"');
    expect(app).toContain("UPDATE SHINWA");
    expect(app).toContain("EXPIRE SHINWA");
    expect(server).toContain("Chỉ được chỉnh tin đang bán");
    expect(app).toContain("Đánh dấu tin hết hạn");
  });

  it("synchronizes price/time/status into the running StallManager", () => {
    expect(stall).toContain("findByUniqueId");
    expect(manager).toContain("applyAdminUpdate");
    expect(manager).toContain("removeProductByUniqueId");
    expect(runtime).toContain("SHINWA_SYNC_PATH");
    expect(runtime).toContain("StallManager.getInstance().applyAdminUpdate");
    expect(server).toContain("/api/control/shinwa-sync");
  });

  it("renders item icon, options, seller, price, remaining time and audit-safe controls", () => {
    expect(app).toContain("Kí gửi Shinwa");
    expect(app).toContain("itemThumb(row.icon");
    expect(app).toContain("formatOptions");
    expect(app).toContain("Giá bán (xu)");
    expect(app).toContain("Thời hạn còn lại (giây)");
    expect(app).toContain("không cần nhập JSON");
  });
});
