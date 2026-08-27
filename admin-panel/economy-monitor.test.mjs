import { describe, expect, it } from "vitest";
import { readFileSync } from "node:fs";
import { detectEconomySignals, parseHistoryBalanceDelta, ECONOMY_RULES } from "./lib/economy-monitor.mjs";

describe("Economy Monitor rules", () => {
  it("parses exact balance deltas without depending on floating point formatting", () => {
    const result = parseHistoryBalanceDelta({ id: 7, truoc: JSON.stringify({ coin: 1000000000, gold: 20, yen: 5 }), sau: JSON.stringify({ coin: 1500000000, gold: 18, yen: 5 }) });
    expect(result.delta).toEqual({ coin: 500000000, gold: -2, yen: 0 });
    expect(result.maxAbsDelta).toBe(500000000);
  });

  it("emits advisory signals for negative balances, bursts, shared IP and Shinwa concentration", () => {
    const signals = detectEconomySignals({
      negativeBalances: [{ server_id: 1, player_id: 10, player_name: "ninja", xu: -1 }],
      giftBursts: [{ server_id: 1, player_id: 10, player_name: "ninja", claims_24h: ECONOMY_RULES.giftClaims24h }],
      ipClusters: [{ server_id: 1, ip: "127.0.0.1", accounts: ECONOMY_RULES.sameIpAccounts, players: 3 }],
      shinwaConcentration: [{ server_id: 1, seller: "ninja", listings: ECONOMY_RULES.activeShinwaListings, total_value: "0" }],
    }, new Date("2026-08-27T00:00:00.000Z"));
    expect(signals.map(signal => signal.rule_key)).toEqual(expect.arrayContaining(["negative_balance", "gift_claim_burst", "shared_ip_cluster", "shinwa_concentration"]));
    expect(new Set(signals.map(signal => signal.dedupe_key)).size).toBe(signals.length);
  });

  it("uses server and UTC day in dedupe keys", () => {
    const first = detectEconomySignals({ negativeBalances: [{ server_id: 1, player_id: 10, xu: -1 }] }, new Date("2026-08-27T01:00:00.000Z"))[0];
    const second = detectEconomySignals({ negativeBalances: [{ server_id: 2, player_id: 10, xu: -1 }] }, new Date("2026-08-27T01:00:00.000Z"))[0];
    expect(first.dedupe_key).not.toBe(second.dedupe_key);
    expect(first.dedupe_key).toContain("2026-08-27");
  });

  it("keeps anti-fraud advisory and schema contracts in panel source", () => {
    const server = readFileSync(new URL("./server.mjs", import.meta.url), "utf8");
    const app = readFileSync(new URL("./public/app.js", import.meta.url), "utf8");
    expect(server).toContain("panel_economy_alerts");
    expect(server).toContain("UPDATE ECONOMY ALERT");
    expect(app).toContain("không tự động ban người chơi");
    expect(server).toContain("/api/economy-monitor");
  });
});
