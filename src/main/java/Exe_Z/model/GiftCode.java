/*
 * Gift Code runtime lifecycle: redemption eligibility is decided by the game
 * server, not by the web panel or a background scheduler.
 */
package Exe_Z.model;

import Exe_Z.constants.SQLStatement;
import Exe_Z.db.jdbc.DbManager;
import Exe_Z.item.Item;
import Exe_Z.server.Config;
import Exe_Z.util.NinjaUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class GiftCode {

    private static final GiftCode instance = new GiftCode();

    public static GiftCode getInstance() {
        return instance;
    }

    public void use(Char player, String code) {
        List<Item> rewards = new ArrayList<>();
        int gold = 0;
        int yen = 0;
        int coin = 0;
        try (Connection connection = DbManager.getInstance().getConnection()) {
            int length = code.length();
            if (code.equals("") || length < 3 || length > 30) {
                player.getService().serverDialog("Mã quà tặng có chiều dài từ 3 đến 30 ký tự.");
                return;
            }

            connection.setAutoCommit(false);
            try {
                try (PreparedStatement statement = connection.prepareStatement(SQLStatement.GET_GIFT_CODE)) {
                    statement.setString(1, code);
                    statement.setInt(2, Config.getInstance().getServerID());
                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) {
                            player.getService().serverDialog("Mã quà tặng không tồn tại, chưa đến thời gian áp dụng, đã tắt hoặc đã hết hạn.");
                            connection.rollback();
                            return;
                        }

                        int id = result.getInt("id");
                        byte status = result.getByte("status");
                        byte type = result.getByte("type");
                        int maxRedemptions = result.getInt("max_redemptions");
                        boolean hasMaxRedemptions = !result.wasNull();
                        int redemptionCount = result.getInt("redemption_count");
                        if (status == 1) {
                            player.getService().serverDialog("Mã quà tặng đã được sử dụng.");
                            connection.rollback();
                            return;
                        }
                        if (hasMaxRedemptions && redemptionCount >= maxRedemptions) {
                            player.getService().serverDialog("Mã quà tặng đã đạt giới hạn số lượt sử dụng.");
                            connection.rollback();
                            return;
                        }
                        if (type == 1 && isUsedGiftCode(connection, player, code)) {
                            player.getService().serverDialog("Mỗi người chỉ được sử dụng 1 lần.");
                            connection.rollback();
                            return;
                        }
                        if (player.user.session.getCountUseGiftCode() >= 100) {
                            player.getService().serverDialog("Mỗi ngày chỉ có thể nhập tối đa 100 mã quà tặng.");
                            connection.rollback();
                            return;
                        }

                        gold = result.getInt("gold");
                        yen = result.getInt("yen");
                        coin = result.getInt("coin");
                        JSONArray itemRows = (JSONArray) new JSONParser().parse(result.getString("items"));
                        if (itemRows.size() > player.getSlotNull()) {
                            player.getService().serverDialog("Bạn không đủ chỗ trống trong hành trang.");
                            connection.rollback();
                            return;
                        }
                        for (Object rawItem : itemRows) {
                            JSONObject itemRow = (JSONObject) rawItem;
                            Item item = new Item(itemRow);
                            Object expireDaysRaw = itemRow.get("expire_days");
                            if (expireDaysRaw != null) {
                                int expireDays = Integer.parseInt(expireDaysRaw.toString());
                                if (expireDays > 0) {
                                    item.expire = System.currentTimeMillis() + expireDays * 86_400_000L;
                                }
                            }
                            if (item.options.isEmpty()) {
                                item.initOption();
                            }
                            rewards.add(item);
                        }

                        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                        try (PreparedStatement used = connection.prepareStatement(SQLStatement.INSERT_USED_GIFT_CODE);
                             PreparedStatement update = connection.prepareStatement(SQLStatement.UPDATE_GIFT_CODE)) {
                            used.setInt(1, player.id);
                            used.setInt(2, player.user.id);
                            used.setString(3, code);
                            used.setTimestamp(4, timestamp);
                            used.executeUpdate();
                            update.setTimestamp(1, timestamp);
                            update.setInt(2, id);
                            update.executeUpdate();
                        }
                    }
                }
                connection.commit();
            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }

            StringBuilder message = new StringBuilder("Chúc mừng, bạn đã được tặng\n\n");
            if (gold > 0) {
                player.addGold(gold);
                message.append(String.format("- %s lượng", NinjaUtils.getCurrency(gold))).append("\n");
            }
            if (yen > 0) {
                player.addYen(yen);
                message.append(String.format("- %s yên", NinjaUtils.getCurrency(yen))).append("\n");
            }
            if (coin > 0) {
                player.addCoin(coin);
                message.append(String.format("- %s xu", NinjaUtils.getCurrency(coin))).append("\n");
            }
            for (Item item : rewards) {
                player.addItemToBag(item);
                message.append(String.format("- x%s %s", NinjaUtils.getCurrency(item.getQuantity()), item.template.name)).append("\n");
            }
            player.user.session.addUseGiftCode();
            player.getService().showAlert("Mã quà tặng", message.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean isUsedGiftCode(Char player, String giftCode) {
        try (Connection connection = DbManager.getInstance().getConnection()) {
            return isUsedGiftCode(connection, player, giftCode);
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private boolean isUsedGiftCode(Connection connection, Char player, String giftCode) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQLStatement.CHECK_EXIST_USED_GIFT_CODE)) {
            statement.setString(1, giftCode);
            statement.setInt(2, player.id);
            statement.setInt(3, player.user.id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    public void addUsedGiftCode(Char player, String giftCode) {
        try (Connection connection = DbManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SQLStatement.INSERT_USED_GIFT_CODE)) {
            statement.setInt(1, player.id);
            statement.setInt(2, player.user.id);
            statement.setString(3, giftCode);
            statement.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            statement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
