import { readFileSync } from "node:fs";
import { describe, expect, it } from "vitest";

const app = readFileSync(new URL("./public/app.js", import.meta.url), "utf8");
const styles = readFileSync(new URL("./public/styles.css", import.meta.url), "utf8");

describe("Gift Code visual wizard", () => {
  const active = app.slice(app.lastIndexOf("async function giftModule(state = {})"));

  it("uses four guided steps and lifecycle/quota presets instead of exposing raw server/status controls", () => {
    expect(active).toContain("Mã & phạm vi");
    expect(active).toContain("Lịch & quota");
    expect(active).toContain("Phần thưởng");
    expect(active).toContain("Rà soát");
    expect(active).toContain("gift-schedule-mode");
    expect(active).toContain("gift-expiry-mode");
    expect(active).toContain("gift-quota-mode");
    expect(active).toContain("Tạo mã tự động");
    expect(app).toContain('!field.closest("#gift-reward-form")');
  });

  it("selects reward from catalog and builds option pairs without an Options JSON textarea", () => {
    expect(active).toContain("gift-wizard-item-search");
    expect(active).toContain("gift-wizard-option-add");
    expect(app).toContain("data-gift-wizard-option-id");
    expect(active).not.toContain("Options JSON");
    expect(active).toContain("items:current.rewards.map");
    expect(styles).toContain(".gift-stepper");
    expect(styles).toContain(".gift-option-row");
  });
});
