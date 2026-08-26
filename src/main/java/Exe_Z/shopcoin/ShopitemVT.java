package Exe_Z.shopcoin;


import Exe_Z.constants.CMD;
import Exe_Z.item.Item;
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

public class ShopitemVT {

    public static final List<Item> item_shop = new ArrayList<>(Arrays.asList(
            new Item(1030),
            new Item(1031),
            new Item(1032),
            new Item(1033),
            new Item(1034),
            new Item(1035),
            new Item(1036),
            new Item(1037),
            new Item(1038),
            new Item(1039),
            new Item(1040),
            new Item(1041),
            new Item(1042),
            new Item(1043),
            new Item(1044),
            new Item(1045),
            new Item(1046),
            new Item(1047),
            new Item(947),
            new Item(948),
            new Item(949),
            new Item(950),
            new Item(951),
            new Item(952),
            new Item(953),
            new Item(954),
            new Item(955),
            new Item(956),
            new Item(957),
            new Item(958),
            new Item(959),
            new Item(960),
            new Item(961),
            new Item(962),
            new Item(963),
            new Item(964),
            new Item(965),
            new Item(966),
            new Item(967),
            new Item(968),
            new Item(969),
            new Item(970),
            new Item(971),
            new Item(972),
            new Item(973),
            new Item(974),
            new Item(975),
            new Item(976),
            new Item(977),
            new Item(978),
            new Item(979),
            new Item(980),
            new Item(981),
            new Item(982)
    ));
    public static HashMap<Integer, List<ItemOption>> itemOptionsMap = new HashMap<>();
    static {
        itemOptionsMap.put(1030, Arrays.asList(
                new ItemOption(82, 1000),
                new ItemOption(83, 1000),
                new ItemOption(87, 1000)
        ));
        itemOptionsMap.put(1031, Arrays.asList(
                new ItemOption(82, 2000),
                new ItemOption(83, 2000),
                new ItemOption(87, 2000)
        ));
        itemOptionsMap.put(1032, Arrays.asList(
                new ItemOption(82, 3000),
                new ItemOption(83, 3000),
                new ItemOption(87, 3000)
        ));
        itemOptionsMap.put(1033, Arrays.asList(
                new ItemOption(82, 4000),
                new ItemOption(83, 4000),
                new ItemOption(87, 4000)
        ));
        itemOptionsMap.put(1034, Arrays.asList(
                new ItemOption(82, 5000),
                new ItemOption(83, 5000),
                new ItemOption(87, 5000)
        ));
        itemOptionsMap.put(1035, Arrays.asList(
                new ItemOption(82, 6000),
                new ItemOption(83, 6000),
                new ItemOption(87, 6000)
        ));
        itemOptionsMap.put(1036, Arrays.asList(
                new ItemOption(82, 7000),
                new ItemOption(83, 7000),
                new ItemOption(87, 7000)
        ));
        itemOptionsMap.put(1037, Arrays.asList(
                new ItemOption(82, 8000),
                new ItemOption(83, 8000),
                new ItemOption(87, 8000)
        ));
        itemOptionsMap.put(1038, Arrays.asList(
                new ItemOption(82, 9000),
                new ItemOption(83, 9000),
                new ItemOption(87, 9000)
        ));
        itemOptionsMap.put(1039, Arrays.asList(
                new ItemOption(82, 10000),
                new ItemOption(83, 10000),
                new ItemOption(87, 10000)
        ));
        itemOptionsMap.put(1040, Arrays.asList(
                new ItemOption(82, 11000),
                new ItemOption(83, 11000),
                new ItemOption(87, 11000)
        ));
        itemOptionsMap.put(1041, Arrays.asList(
                new ItemOption(82, 12000),
                new ItemOption(83, 12000),
                new ItemOption(87, 12000)
        ));
        itemOptionsMap.put(1042, Arrays.asList(
                new ItemOption(82, 13000),
                new ItemOption(83, 13000),
                new ItemOption(87, 13000)
        ));
        itemOptionsMap.put(1043, Arrays.asList(
                new ItemOption(82, 14000),
                new ItemOption(83, 14000),
                new ItemOption(87, 14000)
        ));
        itemOptionsMap.put(1044, Arrays.asList(
                new ItemOption(82, 15000),
                new ItemOption(83, 15000),
                new ItemOption(87, 15000)
        ));
        itemOptionsMap.put(1045, Arrays.asList(
                new ItemOption(82, 16000),
                new ItemOption(83, 16000),
                new ItemOption(87, 16000)
        ));
        itemOptionsMap.put(1046, Arrays.asList(
                new ItemOption(82, 17000),
                new ItemOption(83, 17000),
                new ItemOption(87, 17000)
        ));
        itemOptionsMap.put(1047, Arrays.asList(
                new ItemOption(82, 18000),
                new ItemOption(83, 18000),
                new ItemOption(87, 18000)
        ));
        itemOptionsMap.put(947, Arrays.asList(
                new ItemOption(82, 3000),
                new ItemOption(83, 3000),
                new ItemOption(87, 3000)
        ));
        itemOptionsMap.put(948, Arrays.asList(
                new ItemOption(82, 3000),
                new ItemOption(83, 3000),
                new ItemOption(87, 3000)
        ));
        itemOptionsMap.put(949, Arrays.asList(
                new ItemOption(82, 3000),
                new ItemOption(83, 3000),
                new ItemOption(87, 3000)
        ));
        itemOptionsMap.put(950, Arrays.asList(
                new ItemOption(82, 3000),
                new ItemOption(83, 3000),
                new ItemOption(87, 3000)
        ));
        itemOptionsMap.put(951, Arrays.asList(
                new ItemOption(82, 4000),
                new ItemOption(83, 4000),
                new ItemOption(87, 4000)
        ));
        itemOptionsMap.put(952, Arrays.asList(
                new ItemOption(82, 4000),
                new ItemOption(83, 4000),
                new ItemOption(87, 4000)
        ));
        itemOptionsMap.put(953, Arrays.asList(
                new ItemOption(82, 4000),
                new ItemOption(83, 4000),
                new ItemOption(87, 4000)
        ));
        itemOptionsMap.put(954, Arrays.asList(
                new ItemOption(82, 4000),
                new ItemOption(83, 4000),
                new ItemOption(87, 4000)
        ));
        itemOptionsMap.put(955, Arrays.asList(
                new ItemOption(82, 5000),
                new ItemOption(83, 5000),
                new ItemOption(87, 5000)
        ));
        itemOptionsMap.put(956, Arrays.asList(
                new ItemOption(82, 5000),
                new ItemOption(83, 5000),
                new ItemOption(87, 5000)
        ));
        itemOptionsMap.put(957, Arrays.asList(
                new ItemOption(82, 5000),
                new ItemOption(83, 5000),
                new ItemOption(87, 5000)
        ));
        itemOptionsMap.put(958, Arrays.asList(
                new ItemOption(82, 5000),
                new ItemOption(83, 5000),
                new ItemOption(87, 5000)
        ));
        itemOptionsMap.put(959, Arrays.asList(
                new ItemOption(82, 6000),
                new ItemOption(83, 6000),
                new ItemOption(87, 6000)
        ));
        itemOptionsMap.put(960, Arrays.asList(
                new ItemOption(82, 6000),
                new ItemOption(83, 6000),
                new ItemOption(87, 6000)
        ));
        itemOptionsMap.put(961, Arrays.asList(
                new ItemOption(82, 6000),
                new ItemOption(83, 6000),
                new ItemOption(87, 6000)
        ));
        itemOptionsMap.put(962, Arrays.asList(
                new ItemOption(82, 6000),
                new ItemOption(83, 6000),
                new ItemOption(87, 6000)
        ));
        itemOptionsMap.put(963, Arrays.asList(
                new ItemOption(82, 7000),
                new ItemOption(83, 7000),
                new ItemOption(87, 7000)
        ));
        itemOptionsMap.put(964, Arrays.asList(
                new ItemOption(82, 7000),
                new ItemOption(83, 7000),
                new ItemOption(87, 7000)
        ));
        itemOptionsMap.put(965, Arrays.asList(
                new ItemOption(82, 7000),
                new ItemOption(83, 7000),
                new ItemOption(87, 7000)
        ));
        itemOptionsMap.put(966, Arrays.asList(
                new ItemOption(82, 7000),
                new ItemOption(83, 7000),
                new ItemOption(87, 7000)
        ));
        itemOptionsMap.put(967, Arrays.asList(
                new ItemOption(82, 8000),
                new ItemOption(83, 8000),
                new ItemOption(87, 8000)
        ));
        itemOptionsMap.put(968, Arrays.asList(
                new ItemOption(82, 8000),
                new ItemOption(83, 8000),
                new ItemOption(87, 8000)
        ));
        itemOptionsMap.put(969, Arrays.asList(
                new ItemOption(82, 8000),
                new ItemOption(83, 8000),
                new ItemOption(87, 8000)
        ));
        itemOptionsMap.put(970, Arrays.asList(
                new ItemOption(82, 8000),
                new ItemOption(83, 8000),
                new ItemOption(87, 8000)
        ));
        itemOptionsMap.put(971, Arrays.asList(
                new ItemOption(82, 9000),
                new ItemOption(83, 9000),
                new ItemOption(87, 9000)
        ));
        itemOptionsMap.put(972, Arrays.asList(
                new ItemOption(82, 9000),
                new ItemOption(83, 9000),
                new ItemOption(87, 9000)
        ));
        itemOptionsMap.put(973, Arrays.asList(
                new ItemOption(82, 9000),
                new ItemOption(83, 9000),
                new ItemOption(87, 9000)
        ));
        itemOptionsMap.put(974, Arrays.asList(
                new ItemOption(82, 9000),
                new ItemOption(83, 9000),
                new ItemOption(87, 9000)
        ));
        itemOptionsMap.put(975, Arrays.asList(
                new ItemOption(82, 10000),
                new ItemOption(83, 10000),
                new ItemOption(87, 10000)
        ));
        itemOptionsMap.put(976, Arrays.asList(
                new ItemOption(82, 10000),
                new ItemOption(83, 10000),
                new ItemOption(87, 10000)
        ));
        itemOptionsMap.put(977, Arrays.asList(
                new ItemOption(82, 10000),
                new ItemOption(83, 10000),
                new ItemOption(87, 10000)
        ));
        itemOptionsMap.put(978, Arrays.asList(
                new ItemOption(82, 10000),
                new ItemOption(83, 10000),
                new ItemOption(87, 10000)
        ));
        itemOptionsMap.put(979, Arrays.asList(
                new ItemOption(82, 11000),
                new ItemOption(83, 11000),
                new ItemOption(87, 11000)
        ));
        itemOptionsMap.put(980, Arrays.asList(
                new ItemOption(82, 11000),
                new ItemOption(83, 11000),
                new ItemOption(87, 11000)
        ));
        itemOptionsMap.put(981, Arrays.asList(
                new ItemOption(82, 11000),
                new ItemOption(83, 11000),
                new ItemOption(87, 11000)
        ));
        itemOptionsMap.put(982, Arrays.asList(
                new ItemOption(82, 11000),
                new ItemOption(83, 11000),
                new ItemOption(87, 11000)
        ));
    }

    public static List<ItemOption> getItemOptions(int itemId) {
        return itemOptionsMap.get(itemId);
    }


        public static HashMap<Integer, Integer> itemPrices = new HashMap<>();
        static {
        itemPrices.put(1030, 10000);
        itemPrices.put(1031, 20000);
        itemPrices.put(1032, 30000);
        itemPrices.put(1033, 40000);
        itemPrices.put(1034, 50000);
        itemPrices.put(1035, 60000);
        itemPrices.put(1036, 70000);
        itemPrices.put(1037, 80000);
        itemPrices.put(1038, 90000);
        itemPrices.put(1039, 100000);
        itemPrices.put(1040, 110000);
        itemPrices.put(1041, 120000);
        itemPrices.put(1042, 130000);
        itemPrices.put(1043, 140000);
        itemPrices.put(1044, 150000);
        itemPrices.put(1045, 160000);
        itemPrices.put(1046, 170000);
        itemPrices.put(1047, 180000);
        itemPrices.put(947, 190000);
        itemPrices.put(948, 200000);
        itemPrices.put(949, 210000);
        itemPrices.put(950, 220000);
        itemPrices.put(951, 230000);
        itemPrices.put(952, 240000);
        itemPrices.put(953, 250000);
        itemPrices.put(954, 260000);
        itemPrices.put(955, 270000);
        itemPrices.put(956, 280000);
        itemPrices.put(957, 290000);
        itemPrices.put(958, 300000);
        itemPrices.put(959, 310000);
        itemPrices.put(960, 320000);
        itemPrices.put(961, 230000);
        itemPrices.put(962, 240000);
        itemPrices.put(963, 250000);
        itemPrices.put(964, 260000);
        itemPrices.put(965, 270000);
        itemPrices.put(966, 280000);
        itemPrices.put(967, 290000);
        itemPrices.put(968, 300000);
        itemPrices.put(969, 310000);
        itemPrices.put(970, 320000);
        itemPrices.put(971, 230000);
        itemPrices.put(972, 240000);
        itemPrices.put(973, 250000);
        itemPrices.put(974, 260000);
        itemPrices.put(975, 270000);
        itemPrices.put(976, 280000);
        itemPrices.put(977, 290000);
        itemPrices.put(978, 300000);
        itemPrices.put(979, 310000);
        itemPrices.put(980, 320000);
        itemPrices.put(981, 320000);
        itemPrices.put(982, 320000);
    }
        public static HashMap<Integer, Integer> itemUpgrades = new HashMap<>();

    static {
        itemUpgrades.put(1030, 16);
        itemUpgrades.put(1031, 16);
        itemUpgrades.put(1032, 16);
        itemUpgrades.put(1033, 16);
        itemUpgrades.put(1034, 16);
        itemUpgrades.put(1035, 16);
        itemUpgrades.put(1036, 16);
        itemUpgrades.put(1037, 16);
        itemUpgrades.put(1038, 16);
        itemUpgrades.put(1039, 16);
        itemUpgrades.put(1040, 16);
        itemUpgrades.put(1041, 16);
        itemUpgrades.put(1042, 16);
        itemUpgrades.put(1043, 16);
        itemUpgrades.put(1044, 16);
        itemUpgrades.put(1045, 16);
        itemUpgrades.put(1046, 16);
        itemUpgrades.put(1047, 16);
        itemUpgrades.put(947, 16);
        itemUpgrades.put(948, 16);
        itemUpgrades.put(949, 16);
        itemUpgrades.put(950, 16);
        itemUpgrades.put(951, 16);
        itemUpgrades.put(952, 16);
        itemUpgrades.put(953, 16);
        itemUpgrades.put(954, 16);
        itemUpgrades.put(955, 16);
        itemUpgrades.put(956, 16);
        itemUpgrades.put(957, 16);
        itemUpgrades.put(958, 16);
        itemUpgrades.put(959, 16);
        itemUpgrades.put(960, 16);
        itemUpgrades.put(961, 16);
        itemUpgrades.put(962, 16);
        itemUpgrades.put(963, 16);
        itemUpgrades.put(964, 16);
        itemUpgrades.put(965, 16);
        itemUpgrades.put(966, 16);
        itemUpgrades.put(967, 16);
        itemUpgrades.put(968, 16);
        itemUpgrades.put(969, 16);
        itemUpgrades.put(970, 16);
        itemUpgrades.put(971, 16);
        itemUpgrades.put(972, 16);
        itemUpgrades.put(973, 16);
        itemUpgrades.put(974, 16);
        itemUpgrades.put(975, 16);
        itemUpgrades.put(976, 16);
        itemUpgrades.put(977, 16);
        itemUpgrades.put(978, 16);
        itemUpgrades.put(979, 16);
        itemUpgrades.put(980, 16);
        itemUpgrades.put(981, 16);
        itemUpgrades.put(982, 16);
    }
    public static HashMap<Integer, Integer> itemSys = new HashMap<>();

    static {
        itemSys.put(1030, 0);
        itemSys.put(1031, 0);
        itemSys.put(1032, 0);
        itemSys.put(1033, 0);
        itemSys.put(1034, 0);
        itemSys.put(1035, 0);
        itemSys.put(1036, 0);
        itemSys.put(1037, 0);
        itemSys.put(1038, 0);
        itemSys.put(1039, 0);
        itemSys.put(1040, 0);
        itemSys.put(1041, 0);
        itemSys.put(1042, 0);
        itemSys.put(1043, 0);
        itemSys.put(1044, 0);
        itemSys.put(1045, 0);
        itemSys.put(1046, 0);
        itemSys.put(1047, 0);
        itemSys.put(947, 0);
        itemSys.put(948, 0);
        itemSys.put(949, 0);
        itemSys.put(950, 0);
        itemSys.put(951, 0);
        itemSys.put(952, 0);
        itemSys.put(953, 0);
        itemSys.put(954, 0);
        itemSys.put(955, 0);
        itemSys.put(956, 0);
        itemSys.put(957, 0);
        itemSys.put(958, 0);
        itemSys.put(959, 0);
        itemSys.put(960, 0);
        itemSys.put(961, 0);
        itemSys.put(962, 0);
        itemSys.put(963, 0);
        itemSys.put(964, 0);
        itemSys.put(965, 0);
        itemSys.put(966, 0);
        itemSys.put(967, 0);
        itemSys.put(968, 0);
        itemSys.put(969, 0);
        itemSys.put(970, 0);
        itemSys.put(971, 0);
        itemSys.put(972, 0);
        itemSys.put(973, 0);
        itemSys.put(974, 0);
        itemSys.put(975, 0);
        itemSys.put(976, 0);
        itemSys.put(977, 0);
        itemSys.put(978, 0);
        itemSys.put(979, 0);
        itemSys.put(980, 0);
        itemSys.put(981, 0);
        itemSys.put(982, 0);
    }

    
    public static void send_info_item_create(Char c, byte typeUI, byte indexUI) {
        try {
            Message mss = new Message(CMD.REQUEST_ITEM_INFO);
            Item item = item_shop.get(indexUI);
            DataOutputStream ds = mss.writer();
            ds.writeByte(typeUI);
            ds.writeByte(indexUI);
            ds.writeLong(item.expire);
            ds.writeInt(item.yen);
            if (item.template.isTypeBody() || item.template.isTypeMount() || item.template.isTypeNgocKham() || item.template.isTypeVT()) {
                ds.writeByte(item.sys);
                ArrayList<ItemOption> options1 = item.getDisplayOptions();
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
