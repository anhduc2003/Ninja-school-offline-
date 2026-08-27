package Exe_Z.reward;

import Exe_Z.constants.CMDMenu;
import Exe_Z.db.jdbc.DbManager;
import Exe_Z.event.eventpoint.EventPoint;
import Exe_Z.item.Item;
import Exe_Z.item.ItemFactory;
import Exe_Z.model.Char;
import Exe_Z.model.Menu;
import Exe_Z.option.ItemOption;
import Exe_Z.server.Config;
import Exe_Z.util.Log;
import Exe_Z.util.NinjaUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data-driven rewards used by the admin panel. Reward definitions are stored in
 * normalized tables; the panel is responsible for the visual editor and this
 * class is the runtime source for NPC menus and claims.
 */
public final class RewardCampaignService {

    private static final String NPC_HUNG_VUONG = "hung_vuong";
    private static final String NPC_ADMIN = "admin";
    private static final String TYPE_FANCUNG = "fancung";
    private static final String TYPE_NEWBIE = "newbie";
    private static final String TYPE_TOPUP = "topup";
    private static final String TYPE_EVENT = "event";

    private RewardCampaignService() {
    }

    public static boolean hasActiveCampaign(String npcKey, String campaignType) {
        try (Connection connection = DbManager.getInstance().getConnection(DbManager.GAME);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id FROM panel_reward_campaigns WHERE npc_key = ? AND campaign_type = ? AND active = 1 "
                     + "AND (server_id = 0 OR server_id = ?) "
                     + "AND (starts_at IS NULL OR starts_at <= NOW()) AND (ends_at IS NULL OR ends_at > NOW()) LIMIT 1")) {
            statement.setString(1, npcKey);
            statement.setString(2, campaignType);
            statement.setInt(3, Config.getInstance().getServerID());
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        } catch (Exception exception) {
            return false;
        }
    }

    public static void addCampaignMenus(Char player, String npcKey) {
        if (player == null || player.user == null) {
            return;
        }
        try (Connection connection = DbManager.getInstance().getConnection(DbManager.GAME);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id, campaign_key, title, campaign_type, requirement_key, requirement_value, "
                     + "claim_scope, active, starts_at, ends_at FROM panel_reward_campaigns "
                     +                      "WHERE npc_key = ? AND active = 1 AND (server_id = 0 OR server_id = ?) "
                     + "AND (starts_at IS NULL OR starts_at <= NOW()) "
                     + "AND (ends_at IS NULL OR ends_at > NOW()) ORDER BY campaign_type, id")) {
            statement.setString(1, npcKey);
            statement.setInt(2, Config.getInstance().getServerID());

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    Campaign campaign = readCampaign(result, npcKey);
                    if (campaign != null && !isLegacyClaimed(player, campaign) && !hasClaimed(player, campaign)) {
                        player.menus.add(new Menu(CMDMenu.EXECUTE, campaign.title, () -> openCampaign(player, campaign)));
                    }
                }
            }
        } catch (Exception exception) {
            Log.error("reward campaign menu error: " + exception.getMessage(), exception);
        }
    }

    private static Campaign readCampaign(ResultSet result, String npcKey) throws SQLException {
        Campaign campaign = new Campaign();
        campaign.id = result.getInt("id");
        campaign.key = result.getString("campaign_key");
        campaign.title = result.getString("title");
        campaign.npcKey = npcKey;
        campaign.type = result.getString("campaign_type");
        campaign.requirementKey = result.getString("requirement_key");
        campaign.requirementValue = result.getLong("requirement_value");
        campaign.claimScope = result.getString("claim_scope");
        campaign.active = result.getBoolean("active");
        campaign.coin = result.getLong("coin");
        campaign.gold = result.getLong("gold");
        campaign.xu = result.getLong("xu");
        campaign.yen = result.getLong("yen");
        return campaign;
    }

    private static void openCampaign(Char player, Campaign campaign) {
        player.menus.clear();
        player.menus.add(new Menu(CMDMenu.EXECUTE, "Nhận: " + campaign.title, () -> claim(player, campaign)));
        player.menus.add(new Menu(CMDMenu.EXECUTE, "Xem điều kiện & phần thưởng", () -> showDetails(player, campaign)));
        player.menus.add(new Menu(CMDMenu.EXECUTE, "Quay lại", () -> {
            addCampaignMenus(player, campaign.npcKey);
            player.getService().openUIMenu();
        }));
        player.getService().openUIMenu();
    }

    private static void showDetails(Char player, Campaign campaign) {
        StringBuilder details = new StringBuilder();
        details.append(campaign.title).append("\n\n");
        details.append(requirementText(campaign)).append("\n");
        if (campaign.coin > 0) details.append("- Coin: ").append(campaign.coin).append("\n");
        if (campaign.gold > 0) details.append("- Lượng: ").append(campaign.gold).append("\n");
        if (campaign.xu > 0) details.append("- Xu: ").append(campaign.xu).append("\n");
        if (campaign.yen > 0) details.append("- Yên: ").append(campaign.yen).append("\n");
        details.append("\n");
        try (Connection connection = DbManager.getInstance().getConnection(DbManager.GAME);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT ri.item_id, i.name, ri.quantity, ri.is_lock, ri.expire_days "
                     + "FROM panel_reward_items ri LEFT JOIN item i ON i.id = ri.item_id "
                     + "WHERE ri.campaign_id = ? ORDER BY ri.id")) {
            statement.setInt(1, campaign.id);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    details.append("- ").append(result.getString("name"))
                            .append(" x").append(result.getInt("quantity"));
                    if (result.getBoolean("is_lock")) {
                        details.append(" (khóa)");
                    }
                    if (result.getInt("expire_days") > 0) {
                        details.append(" ").append(result.getInt("expire_days")).append(" ngày");
                    }
                    details.append("\n");
                }
            }
        } catch (SQLException exception) {
            details.append("Không đọc được danh sách phần thưởng.\n");
        }
        player.getService().showAlert(campaign.title, details.toString());
    }

    private static String requirementText(Campaign campaign) {
        if (TYPE_FANCUNG.equals(campaign.type)) {
            return "Điều kiện: đã hoàn thành Fancung.";
        }
        if (TYPE_NEWBIE.equals(campaign.type)) {
            return "Điều kiện: tài khoản chưa nhận quà tân thủ.";
        }
        if (TYPE_TOPUP.equals(campaign.type)) {
            return "Điều kiện: tổng nạp tối thiểu " + campaign.requirementValue + " Coin.";
        }
        return "Điều kiện: " + (campaign.requirementKey == null ? "điểm sự kiện" : campaign.requirementKey)
                + " đạt tối thiểu " + campaign.requirementValue + ".";
    }

    private static boolean isLegacyClaimed(Char player, Campaign campaign) {
        if (TYPE_NEWBIE.equals(campaign.type)) {
            return player.user.receivedFirstGift;
        }
        if (TYPE_FANCUNG.equals(campaign.type)) {
            return player.fancung > 0;
        }
        return false;
    }

    private static boolean hasClaimed(Char player, Campaign campaign) {
        try (Connection connection = DbManager.getInstance().getConnection(DbManager.GAME);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id FROM panel_reward_claims WHERE campaign_id = ? AND "
                     + (campaign.claimScope.equals("user") ? "user_id = ?" : "player_id = ?") + " LIMIT 1")) {
            statement.setInt(1, campaign.id);
            statement.setInt(2, campaign.claimScope.equals("user") ? player.user.id : player.id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        } catch (Exception exception) {
            return false;
        }
    }

    private static void claim(Char player, Campaign campaign) {
        synchronized (player) {
            try {
                if (!isEligible(player, campaign)) {
                    player.serverDialog("Bạn chưa đủ điều kiện nhận phần thưởng này.");
                    return;
                }
                List<RewardItem> rewards = loadRewards(campaign.id);
                if (player.getSlotNull() < rewards.size()) {
                    player.warningBagFull();
                    return;
                }
                if (hasClaimed(player, campaign) || isLegacyClaimed(player, campaign)) {
                    player.serverDialog("Bạn đã nhận phần thưởng này rồi.");
                    return;
                }
                if (!insertClaim(player, campaign)) {
                    player.serverDialog("Bạn đã nhận phần thưởng này rồi.");
                    return;
                }
                try {
                    applyCurrencies(player, campaign);
                    applyRewards(player, rewards);
                } catch (Exception exception) {
                    deleteClaim(player, campaign);
                    throw exception;
                }
                if (TYPE_NEWBIE.equals(campaign.type)) {
                    player.user.receivedFirstGift = true;
                } else if (TYPE_FANCUNG.equals(campaign.type)) {
                    player.fancung += 1;
                }
                player.serverDialog("Đã nhận phần thưởng: " + campaign.title);
            } catch (Exception exception) {
                Log.error("reward campaign claim error: " + exception.getMessage(), exception);
                player.serverDialog("Không thể nhận quà lúc này. Vui lòng thử lại sau.");
            }
        }
    }

    private static boolean isEligible(Char player, Campaign campaign) {
        if (TYPE_FANCUNG.equals(campaign.type)) {
            return player.user.fancung >= Math.max(1, campaign.requirementValue);
        }
        if (TYPE_NEWBIE.equals(campaign.type)) {
            return !player.user.receivedFirstGift;
        }
        if (TYPE_TOPUP.equals(campaign.type)) {
            return player.user.tongnap >= campaign.requirementValue;
        }
        if (TYPE_EVENT.equals(campaign.type)) {
            EventPoint eventPoint = player.getEventPoint();
            return eventPoint != null && eventPoint.getPoint(campaign.requirementKey) >= campaign.requirementValue;
        }
        return false;
    }

    private static boolean insertClaim(Char player, Campaign campaign) throws SQLException {
        String claimKey = claimKey(player, campaign);
        try (Connection connection = DbManager.getInstance().getConnection(DbManager.GAME);
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT IGNORE INTO panel_reward_claims (campaign_id, user_id, player_id, claim_key, claimed_at) VALUES (?, ?, ?, ?, NOW())")) {
            statement.setInt(1, campaign.id);
            statement.setInt(2, player.user.id);
            statement.setInt(3, player.id);
            statement.setString(4, claimKey);
            return statement.executeUpdate() > 0;
        }
    }

    private static void deleteClaim(Char player, Campaign campaign) throws SQLException {
        try (Connection connection = DbManager.getInstance().getConnection(DbManager.GAME);
             PreparedStatement statement = connection.prepareStatement("DELETE FROM panel_reward_claims WHERE claim_key = ? LIMIT 1")) {
            statement.setString(1, claimKey(player, campaign));
            statement.executeUpdate();
        }
    }

    private static String claimKey(Char player, Campaign campaign) {
        return campaign.id + ":" + (campaign.claimScope.equals("user") ? "U:" + player.user.id : "P:" + player.id);
    }

    private static List<RewardItem> loadRewards(int campaignId) throws SQLException {
        List<RewardItem> rewards = new ArrayList<>();
        try (Connection connection = DbManager.getInstance().getConnection(DbManager.GAME);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id, item_id, quantity, is_lock, sys, upgrade, yen, expire_days "
                     + "FROM panel_reward_items WHERE campaign_id = ? ORDER BY id")) {
            statement.setInt(1, campaignId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    RewardItem item = new RewardItem();
                    item.id = result.getInt("id");
                    item.itemId = result.getInt("item_id");
                    item.quantity = result.getInt("quantity");
                    item.locked = result.getBoolean("is_lock");
                    item.sys = result.getInt("sys");
                    item.upgrade = result.getInt("upgrade");
                    item.yen = result.getInt("yen");
                    item.expireDays = result.getInt("expire_days");
                    item.options = loadOptions(connection, item.id);
                    rewards.add(item);
                }
            }
        }
        return rewards;
    }

    private static List<RewardOption> loadOptions(Connection connection, int rewardItemId) throws SQLException {
        List<RewardOption> options = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT option_id, min_value, max_value FROM panel_reward_item_options WHERE reward_item_id = ? ORDER BY id")) {
            statement.setInt(1, rewardItemId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    RewardOption option = new RewardOption();
                    option.optionId = result.getInt("option_id");
                    option.minValue = result.getInt("min_value");
                    option.maxValue = result.getInt("max_value");
                    options.add(option);
                }
            }
        }
        return options;
    }

    private static void applyCurrencies(Char player, Campaign campaign) {
        if (campaign.coin > 0) player.addCoin(campaign.coin);
        if (campaign.gold > 0) player.addGold((int) campaign.gold);
        if (campaign.xu > 0) player.addCoin(campaign.xu);
        if (campaign.yen > 0) player.addYen(campaign.yen);
    }

    private static void applyRewards(Char player, List<RewardItem> rewards) {
        for (RewardItem reward : rewards) {
            Item item = ItemFactory.getInstance().newItem(reward.itemId);
            if (item == null) {
                throw new IllegalStateException("Item không tồn tại: " + reward.itemId);
            }
            item.isLock = reward.locked;
            item.setQuantity(reward.quantity);
            item.nextsys(reward.sys);
            item.nextupgrade(reward.upgrade);
            item.yen = reward.yen;
            item.expire = reward.expireDays > 0
                    ? System.currentTimeMillis() + (long) reward.expireDays * 86_400_000L : -1;
            for (RewardOption option : reward.options) {
                int value = option.minValue == option.maxValue
                        ? option.minValue : NinjaUtils.nextInt(option.minValue, option.maxValue);
                item.options.add(new ItemOption(option.optionId, value));
            }
            player.addItemToBag(item);
        }
    }

    private static final class Campaign {
        private int id;
        private String key;
        private String title;
        private String npcKey;
        private String type;
        private String requirementKey;
        private long requirementValue;
        private String claimScope;
        private boolean active;
        private long coin;
        private long gold;
        private long xu;
        private long yen;
    }

    private static final class RewardItem {
        private int id;
        private int itemId;
        private int quantity;
        private boolean locked;
        private int sys;
        private int upgrade;
        private int yen;
        private int expireDays;
        private List<RewardOption> options;
    }

    private static final class RewardOption {
        private int optionId;
        private int minValue;
        private int maxValue;
    }
}
