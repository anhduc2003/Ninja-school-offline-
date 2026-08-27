import { describe, expect, it } from "vitest";
import { readFileSync } from "node:fs";

const server = readFileSync(new URL("./server.mjs", import.meta.url), "utf8");
const app = readFileSync(new URL("./public/app.js", import.meta.url), "utf8");
const campaignUi = app.slice(app.indexOf("async function rewardCampaignModule"), app.indexOf("async function optionsModuleVisual"));
const java = readFileSync(new URL("../src/main/java/Exe_Z/reward/RewardCampaignService.java", import.meta.url), "utf8");
const char = readFileSync(new URL("../src/main/java/Exe_Z/model/Char.java", import.meta.url), "utf8");

describe("Reward Campaign Center contract", () => {
  it("stores normalized campaigns, item options and immutable claims", () => {
    expect(server).toContain("panel_reward_campaigns");
    expect(server).toContain("panel_reward_items");
    expect(server).toContain("panel_reward_item_options");
    expect(server).toContain("panel_reward_claims");
    expect(server).toContain("/api/actions/reward-campaign-save");
    expect(server).toContain("/api/actions/reward-campaign-state");
    expect(server).toContain("Campaign đã có người nhận");
  });

  it("exposes the four requested campaign types and visual item builder", () => {
    for (const type of ["fancung", "newbie", "topup", "event"]) {
      expect(server).toContain(`"${type}"`);
      expect(campaignUi).toContain(`value="${type}"`);
    }
    expect(campaignUi).toContain("reward-item-search");
    expect(campaignUi).toContain("reward-option-add");
    expect(campaignUi).toContain("Không cần sửa JSON");
    expect(campaignUi).not.toContain("Options JSON");
  });

  it("connects active campaigns to the two NPCs and reuses runtime state", () => {
    expect(java).toContain("hasActiveCampaign");
    expect(java).toContain("panel_reward_claims");
    expect(java).toContain("player.user.tongnap");
    expect(java).toContain("player.getEventPoint()");
    expect(char).toContain("RewardCampaignService.addCampaignMenus(this, \"hung_vuong\")");
    expect(char).toContain("RewardCampaignService.addCampaignMenus(this, \"admin\")");
  });
});
