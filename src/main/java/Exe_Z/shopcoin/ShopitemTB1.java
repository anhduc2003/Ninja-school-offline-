package Exe_Z.shopcoin;

import Exe_Z.constants.CMD;
import Exe_Z.db.jdbc.DbManager;
import Exe_Z.item.Item;
import Exe_Z.item.ItemFactory;
import Exe_Z.model.Char;
import Exe_Z.network.Message;
import Exe_Z.network.Service;
import Exe_Z.option.ItemOption;
import Exe_Z.server.Config;
import Exe_Z.util.NinjaUtils;

import java.io.DataOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class ShopitemTB1 {
    private static final ShopitemTB1 instance = new ShopitemTB1();
    
    public static ShopitemTB1 getInstance() {
        return instance;
    }

    public static final List<Item> item_shop = new ArrayList<>();
    public static HashMap<Integer, List<ItemOption>> itemOptionsMap = new HashMap<>();
    public static HashMap<Integer, Integer> itemPrices = new HashMap<>();
    public static HashMap<Integer, Byte> itemUpgrades = new HashMap<>();
    public static HashMap<Integer, Integer> itemSys = new HashMap<>();

    static {
        loadDataFromDatabase();
    }

    public static void loadDataFromDatabase() {
        try {
            Config serverConfig = Config.getInstance();
            Connection conn = DbManager.getInstance().getConnection(DbManager.SERVER);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM shopcoin_tb1");

            item_shop.clear();
            itemOptionsMap.clear();
            itemPrices.clear();
            itemUpgrades.clear();
            itemSys.clear();

            while (rs.next()) {
                int idItem = rs.getInt("idItem");
                int price = rs.getInt("price");
                byte upgrade = rs.getByte("upgrade");
                int system = rs.getInt("system");

                Item item = new Item(idItem);
                item.setUpgrade(upgrade);
                item_shop.add(item);

                itemPrices.put(idItem, price);
                itemUpgrades.put(idItem, upgrade);
                itemSys.put(idItem, system);
                String optionsJson = rs.getString("options");
                if (optionsJson != null && !optionsJson.isEmpty()) {
                    List<ItemOption> options = parseItemOptions(optionsJson);  
                    itemOptionsMap.put(idItem, options);
                }
            }

            conn.close();
        } catch (SQLException e) {
            Logger.getLogger(ShopitemTB1.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public static List<ItemOption> parseItemOptions(String jsonString) {
        List<ItemOption> options = new ArrayList<>();

        try {
            JSONArray jArr = new JSONArray(jsonString);

            for (int i = 0; i < jArr.length(); i++) {
                JSONObject obj = jArr.getJSONObject(i);

                int param = obj.getInt("param");
                int id = obj.getInt("id");
                options.add(new ItemOption(id, param));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return options;
    }

    public void load() {
        loadDataFromDatabase(); 
    }

    public static List<ItemOption> getItemOptions(int itemId) {
        return itemOptionsMap.get(itemId);
    }

    public static void send_info_item_create(Char c, byte typeUI, byte indexUI) {
        try {
            Message mss = new Message(CMD.REQUEST_ITEM_INFO);
            Item item = item_shop.get(indexUI);
            Item newItem = ItemFactory.getInstance().newItemMax(item.id);
            DataOutputStream ds = mss.writer();

            ds.writeByte(typeUI);
            ds.writeByte(indexUI);
            ds.writeLong(item.expire);
            ds.writeInt(item.yen);

            if (newItem.template.isTypeBody() || newItem.template.isTypeMount() || newItem.template.isTypeNgocKham()) {
                ds.writeByte(itemSys.get(item.id));
                ArrayList<ItemOption> options1 = newItem.getDisplayOptions();
                for (ItemOption ability1 : options1) {
                    ds.writeByte(ability1.optionTemplate.id);
                    ds.writeInt(ability1.param);
                }
                List<ItemOption> options = itemOptionsMap.get(item.id);
                if (options != null) {
                    for (ItemOption ability : options) {
                        ds.writeByte(ability.optionTemplate.id);
                        ds.writeInt(ability.param);
                    }
                }
            }

            ds.flush();
            c.getService().sendMessage(mss);
            mss.cleanup();
        } catch (Exception ex) {
            Logger.getLogger(Service.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}