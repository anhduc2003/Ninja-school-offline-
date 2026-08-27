import { describe, expect, it } from "vitest";
import { readFileSync } from "node:fs";

const server = readFileSync(new URL("./server.mjs", import.meta.url), "utf8");
const stall = readFileSync(new URL("../src/main/java/Exe_Z/stall/Stall.java", import.meta.url), "utf8");
const manager = readFileSync(new URL("../src/main/java/Exe_Z/stall/StallManager.java", import.meta.url), "utf8");

describe("Shinwa expiry mailbox notification", () => {
  it("bootstraps a marker table keyed by listing and server", () => {
    expect(server).toContain("panel_shinwa_expiry_notifications");
    expect(server).toContain("PRIMARY KEY (shinwa_id, server_id)");
    expect(readFileSync(new URL("../SQL/nsoz.sql", import.meta.url), "utf8")).toContain("panel_shinwa_expiry_notifications");
  });

  it("claims expiry notification atomically before writing the seller mailbox", () => {
    expect(manager).toContain("INSERT IGNORE INTO panel_shinwa_expiry_notifications");
    expect(manager).toContain("claim.executeUpdate() != 1");
    expect(manager).toContain("SELECT message FROM players");
    expect(manager).toContain("UPDATE players SET message");
    expect(manager).toContain("appendMailbox");
    expect(manager).toContain("String combined = current.isEmpty() ? text : current + \"\\n\" + text;");
    expect(manager).toContain("return combined;");
  });

  it("notifies online seller immediately and keeps offline seller notification pending", () => {
    expect(manager).toContain("Char.findCharByName");
    expect(manager).toContain("onlineSeller.getService().showAlert");
    expect(manager).toContain("onlineSeller.message = mailbox");
    expect(manager).toContain("Hãy đến NPC Shinwa để nhận lại vật phẩm");
    expect(readFileSync(new URL("./public/app.js", import.meta.url), "utf8")).toContain("row.expiry_notification_label");
  });

  it("runs exactly when a listing leaves the active product list", () => {
    expect(stall).toContain("StallManager.getInstance().notifyExpiry(t)");
    expect(stall).toContain("expiredProductList.add(t)");
  });
});
