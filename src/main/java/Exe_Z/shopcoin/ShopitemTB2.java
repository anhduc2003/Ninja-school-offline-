package Exe_Z.shopcoin;

import Exe_Z.constants.CMD;
import Exe_Z.item.Item;
import Exe_Z.item.ItemFactory;
import Exe_Z.model.Char;
import Exe_Z.network.Message;
import Exe_Z.network.Service;
import Exe_Z.option.ItemOption;
import Exe_Z.util.NinjaUtils;

import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ShopitemTB2 {

    public static final List<Item> item_shop = new ArrayList<>(Arrays.asList(
            new Item(477),
            new Item(28),
            new Item(778),
            new Item(695),
            new Item(696),
            new Item(697),
            new Item(698),
            new Item(699),
            new Item(700),
            new Item(701),
            new Item(702),
            new Item(703),
            new Item(834),
            new Item(835),
            new Item(836),
            new Item(436),
            new Item(437),
            new Item(438)
    ));
    public static HashMap<Integer, List<ItemOption>> itemOptionsMap = new HashMap<>();

    static {
        itemOptionsMap.put(477, Arrays.asList(
        ));
        itemOptionsMap.put(28, Arrays.asList(
//                new ItemOption(85, 9),
//                new ItemOption(82, 8000),
//                new ItemOption(83, 8000),
//                new ItemOption(87, 8000)
        ));
        itemOptionsMap.put(778, Arrays.asList(
        ));
        itemOptionsMap.put(695, Arrays.asList(
        ));
        itemOptionsMap.put(696, Arrays.asList(
        ));
        itemOptionsMap.put(697, Arrays.asList(
        ));
        itemOptionsMap.put(698, Arrays.asList(
        ));
        itemOptionsMap.put(699, Arrays.asList(
        ));
        itemOptionsMap.put(700, Arrays.asList(
        ));
        itemOptionsMap.put(701, Arrays.asList(
        ));
        itemOptionsMap.put(702, Arrays.asList(
        ));
        itemOptionsMap.put(703, Arrays.asList(
        ));
        itemOptionsMap.put(834, Arrays.asList(
        ));
        itemOptionsMap.put(835, Arrays.asList(
        ));
        itemOptionsMap.put(836, Arrays.asList(
        ));
        itemOptionsMap.put(436, Arrays.asList(
        ));
        itemOptionsMap.put(437, Arrays.asList(
        ));
        itemOptionsMap.put(438, Arrays.asList(
        ));
        

    }

    public static List<ItemOption> getItemOptions(int itemId) {
        return itemOptionsMap.get(itemId);
    }

    public static HashMap<Integer, Integer> itemPrices = new HashMap<>();

    static {

        itemPrices.put(477, 50000);
        itemPrices.put(28, 100000);
        itemPrices.put(778, 10000);
        itemPrices.put(695, 5000);
        itemPrices.put(696, 10000);
        itemPrices.put(697, 15000);
        itemPrices.put(698, 20000);
        itemPrices.put(699, 30000);
        itemPrices.put(700, 50000);
        itemPrices.put(701, 50000);
        itemPrices.put(702, 50000);
        itemPrices.put(703, 50000);
        itemPrices.put(834, 10000);
        itemPrices.put(835, 10000);
        itemPrices.put(836, 10000);
        itemPrices.put(436, 10000);
        itemPrices.put(437, 20000);
        itemPrices.put(438, 50000);
    }
    public static HashMap<Integer, Integer> itemUpgrades = new HashMap<>();

    static {
        itemUpgrades.put(477, 0);
        itemUpgrades.put(28, 0);
        itemUpgrades.put(778, 0);
        itemUpgrades.put(695, 0);
        itemUpgrades.put(696, 0);
        itemUpgrades.put(697, 0);
        itemUpgrades.put(698, 0);
        itemUpgrades.put(699, 0);
        itemUpgrades.put(700, 0);
        itemUpgrades.put(701, 0);
        itemUpgrades.put(702, 0);
        itemUpgrades.put(703, 0);
        itemUpgrades.put(834, 0);
        itemUpgrades.put(835, 0);
        itemUpgrades.put(836, 0);
        itemUpgrades.put(436, 0);
        itemUpgrades.put(437, 0);
        itemUpgrades.put(438, 0);
    }
    public static HashMap<Integer, Integer> itemSys = new HashMap<>();

    static {
        itemSys.put(477, 0);
        itemSys.put(28, 0);
        itemSys.put(778, 0);
        itemSys.put(695, 0);
        itemSys.put(696, 0);
        itemSys.put(697, 0);
        itemSys.put(698, 0);
        itemSys.put(699, 0);
        itemSys.put(700, 0);
        itemSys.put(701, 0);
        itemSys.put(702, 0);
        itemSys.put(703, 0);
        itemSys.put(834, 0);
        itemSys.put(835, 0);
        itemSys.put(836, 0);
        itemSys.put(436, 0);
        itemSys.put(437, 0);
        itemSys.put(438, 0);
    }

    public static void send_info_item_create(Char c, byte typeUI, byte indexUI) {
        try {
            Message mss = new Message(CMD.REQUEST_ITEM_INFO);
            Item item = item_shop.get(indexUI);
            Item newItem = ItemFactory.getInstance().newItem(item.id);
            DataOutputStream ds = mss.writer();
            ds.writeByte(typeUI);
            ds.writeByte(indexUI);
            ds.writeLong(item.expire);
            ds.writeInt(item.yen);
            if (item.template.isTypeBody() || item.template.isTypeMount() || item.template.isTypeNgocKham()) {
                ds.writeByte(item.sys);
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
