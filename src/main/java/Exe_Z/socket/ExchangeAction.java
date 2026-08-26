package Exe_Z.socket;

import Exe_Z.db.jdbc.DbManager;
import Exe_Z.model.User;
import Exe_Z.server.ServerManager;
import Exe_Z.util.Log;
import Exe_Z.util.NinjaUtils;

import org.json.JSONObject;

public class ExchangeAction implements IAction {

    @Override
    public void call(JSONObject json) {
        try {
            int coin = json.getInt("coin");
            int gold = json.getInt("gold");
            int yen = json.getInt("yen");
            int userId = json.getInt("user_id");
            User user = ServerManager.findUserByUserID(userId);
            if (user != null) {
                StringBuilder sb = new StringBuilder("Bạn nhận được ");
                if (gold > 0) {
                    DbManager.getInstance().updateAmountUnpaid(user.id, 0);
                    user.addGold(gold);
                    sb.append(String.format("%s lượng, ", NinjaUtils.getCurrency(gold)));
                }
                if (coin > 0) {
                    user.sltChar.addCoin(coin);
                    sb.append(String.format("%s xu, ", NinjaUtils.getCurrency(coin)));
                }
                if (yen > 0) {
                    user.sltChar.addYen(yen);
                    sb.append(String.format("%s yên, ", NinjaUtils.getCurrency(yen)));
                }

                sb.replace(sb.length() - 2, sb.length(), "");
                user.sltChar.getService().serverMessage(sb.toString());
            }
        } catch (Exception ex) {
            Log.error("Error get socket", ex);
        }
    }

}
