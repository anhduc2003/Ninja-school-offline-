export const ECONOMY_RULES = Object.freeze({
  highBalanceXu: 1_500_000_000,
  largeDeltaXu: 500_000_000,
  giftClaims24h: 10,
  rewardClaims24h: 5,
  sameIpAccounts: 5,
  activeShinwaListings: 20,
  activeShinwaValue: 500_000_000,
});

function numeric(value) {
  const number = Number(value);
  return Number.isFinite(number) ? number : 0;
}

function dayKey(now = new Date()) {
  return now.toISOString().slice(0, 10);
}

function addSignal(signals, ruleKey, severity, row, title, description, evidence, now) {
  const serverId = Number(row.server_id || 0);
  const playerId = row.player_id === null || row.player_id === undefined ? null : Number(row.player_id);
  const userId = row.user_id === null || row.user_id === undefined ? null : Number(row.user_id);
  const subject = playerId ?? userId ?? String(row.ip || row.seller || "global");
  signals.push({
    dedupe_key: `${ruleKey}:${serverId}:${subject}:${dayKey(now)}`,
    rule_key: ruleKey,
    severity,
    status: "open",
    server_id: serverId,
    player_id: Number.isInteger(playerId) ? playerId : null,
    user_id: Number.isInteger(userId) ? userId : null,
    player_name: row.player_name || row.name || null,
    username: row.username || null,
    title,
    description,
    evidence,
  });
}

export function parseHistoryBalanceDelta(row) {
  let before;
  let after;
  try { before = JSON.parse(row.truoc || "{}"); } catch { before = {}; }
  try { after = JSON.parse(row.sau || "{}"); } catch { after = {}; }
  const delta = {
    coin: numeric(after.coin) - numeric(before.coin),
    gold: numeric(after.gold) - numeric(before.gold),
    yen: numeric(after.yen) - numeric(before.yen),
  };
  return { ...row, delta, maxAbsDelta: Math.max(Math.abs(delta.coin), Math.abs(delta.gold), Math.abs(delta.yen)) };
}

export function detectEconomySignals(input = {}, now = new Date()) {
  const signals = [];
  for (const row of input.negativeBalances || []) {
    addSignal(signals, "negative_balance", "critical", row, "Số dư âm", `Phát hiện số dư âm ở ${row.player_name || row.username || "đối tượng không xác định"}.`, { xu: row.xu, xuInBox: row.xuInBox, yen: row.yen, coin: row.coin, luong: row.luong, balance: row.balance }, now);
  }
  for (const row of input.highBalances || []) {
    addSignal(signals, "high_balance_concentration", "warning", row, "Tập trung số dư lớn", `${row.player_name || row.username || "Người chơi"} đang giữ tổng xu rất lớn, cần kiểm tra nguồn phát sinh.`, { total_xu: row.total_xu, xu: row.xu, xuInBox: row.xuInBox }, now);
  }
  for (const row of input.largeDeltas || []) {
    const parsed = row.maxAbsDelta === undefined ? parseHistoryBalanceDelta(row) : row;
    addSignal(signals, "large_balance_delta", "warning", parsed, "Biến động số dư lớn", `Lịch sử ${parsed.type_name || "kinh tế"} có biến động lớn trong 24 giờ gần nhất.`, { history_id: parsed.id, type: parsed.type_name, delta: parsed.delta, time: parsed.time }, now);
  }
  for (const row of input.giftBursts || []) {
    addSignal(signals, "gift_claim_burst", "warning", row, "Đổi gift code dồn dập", `${row.player_name || "Người chơi"} có số lượt đổi gift code cao trong 24 giờ.`, { claims_24h: Number(row.claims_24h || 0) }, now);
  }
  for (const row of input.rewardBursts || []) {
    addSignal(signals, "reward_claim_burst", "warning", row, "Nhận reward dồn dập", `${row.player_name || "Người chơi"} có số lượt nhận campaign cao trong 24 giờ.`, { claims_24h: Number(row.claims_24h || 0) }, now);
  }
  for (const row of input.ipClusters || []) {
    addSignal(signals, "shared_ip_cluster", "info", row, "Cụm account dùng chung IP", `Có nhiều account cùng xuất hiện trên một IP; đây là tín hiệu cần review, không tự động kết luận gian lận.`, { ip: row.ip, accounts: Number(row.accounts || 0), players: Number(row.players || 0) }, now);
  }
  for (const row of input.shinwaConcentration || []) {
    addSignal(signals, "shinwa_concentration", "info", row, "Tập trung listing Shinwa", `${row.seller || "Seller"} có nhiều listing hoặc tổng giá trị kí gửi cao.`, { seller: row.seller, listings: Number(row.listings || 0), total_value: row.total_value }, now);
  }
  return signals;
}
