package Exe_Z.model;

import Exe_Z.constants.CMD;
import Exe_Z.item.Item;
import Exe_Z.network.Message;
import Exe_Z.network.Service;
import Exe_Z.option.ItemOption;
import Exe_Z.util.NinjaUtils;

import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LinhAn {
    public static final int[] id_ngoc_tho = new int[]{1176, 1180, 1184, 1188, 1192, 1196, 1200, 1204, 1208, 1212};
    public static final int[] id_tinh_che = new int[]{1177, 1181, 1185, 1189, 1193, 1197, 1201, 1205, 1209, 1213};
    public static final int[] id_thien_thach = new int[]{1178, 1182, 1186, 1190, 1194, 1198, 1202, 1206, 1210};
    public static final int[] id_da_huyen_bi = new int[]{1179, 1183, 1187, 1191, 1195, 1199, 1203, 1207, 1211, 1215, 1214};
    public static final int[] id_lua = new int[]{1216, 1217, 1218, 1219, 1220, 1221, 1222};
    public static final List<Item> item_create = new ArrayList<>(Arrays.asList(
            new Item(1223),
            new Item(1227),
            new Item(1231)
    ));
    public static final int[] quantity_create = new int[]{30, 20, 15, 10, 3};
    public static final int[] quantity_upgrade = new int[]{20, 20, 15, 15, 10, 5, 2};
    public static final int[] xu_upgrade = new int[]{50000, 100000, 150000, 200000, 250000, 300000, 350000, 400000, 450000, 500000, 550000, 600000, 650000, 700000, 750000, 800000};
    public static final int[] percent_upgrade = new int[]{8000, 7000, 6500, 5000, 5500, 5000, 4500, 4000, 3500, 3000, 2500, 2200, 1700, 1200, 900, 500};
    public static final int[] options = new int[]{87, 88, 89, 90, 6, 7, 99, 10, 2, 3, 4, 69, 128};
    public static final int[] options_special = new int[]{9, 8, 67, 88, 89, 90, 21, 22, 23, 24, 25, 26, 57, 100, 101, 127, 130, 131, 121, 113, 98};

    public static boolean isLinhAn(int id) {
        return 1223 <= id && id <= 1234;
    }

    public static boolean isKrypton(int id) {
        return 1235 <= id && id <= 1237;
    }

    public static boolean replace(Item existingItem, Item newItem) {
        return LinhAn.isLinhAn(existingItem.id) && LinhAn.isLinhAn(newItem.id) ||
                LinhAn.isKrypton(existingItem.id) && LinhAn.isKrypton(newItem.id) ||
                existingItem.id == 1238 && newItem.id == 1238;
    }

    public static int[] setMaterialCreate() {
        int[] arr = new int[5];
        arr[0] = NinjaUtils.nextInt(id_ngoc_tho);
        arr[1] = NinjaUtils.nextInt(id_tinh_che);
        arr[2] = NinjaUtils.nextInt(id_thien_thach);
        arr[3] = NinjaUtils.nextInt(id_da_huyen_bi);
        arr[4] = NinjaUtils.nextInt(id_lua);
        return arr;
    }

    public static int[] setMaterialUpgrade() {
        int[] arr = new int[7];
        arr[0] = NinjaUtils.nextInt(id_ngoc_tho);
        do {
            arr[1] = NinjaUtils.nextInt(id_ngoc_tho);
        } while (arr[0] == arr[1]);
        arr[2] = NinjaUtils.nextInt(id_tinh_che);
        do {
            arr[3] = NinjaUtils.nextInt(id_tinh_che);
        } while (arr[2] == arr[3]);
        arr[4] = NinjaUtils.nextInt(id_thien_thach);
        arr[5] = NinjaUtils.nextInt(id_da_huyen_bi);
        arr[6] = NinjaUtils.nextInt(id_lua);
        return arr;
    }

    public static void send_info_item_create(Char c, byte typeUI, byte indexUI) {
        try {
            Message mss = new Message(CMD.REQUEST_ITEM_INFO);
            Item item = item_create.get(indexUI);
            DataOutputStream ds = mss.writer();
            ds.writeByte(typeUI);
            ds.writeByte(indexUI);
            ds.writeLong(item.expire);
            ds.writeInt(item.yen);
            if (item.template.isTypeBody() || item.template.isTypeMount() || item.template.isTypeNgocKham()) {
                ds.writeByte(item.sys);
                ArrayList<ItemOption> options = item.getDisplayOptions();
                for (int i = 0; i < 5; i++) {
                    options.add(new ItemOption(c.create_linhan[i] - 969, quantity_create[i]));
                }
                for (ItemOption ability : options) {
                    ds.writeByte(ability.optionTemplate.id);
                    ds.writeInt(ability.param);
                }
            }
            ds.flush();
            c.getService().sendMessage(mss);
            mss.cleanup();
        } catch (Exception ex) {
            Logger.getLogger(Service.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void send_info_item_upgrade(Char c, byte typeUI, byte indexUI) {
        try {
            Message mss = new Message(CMD.REQUEST_ITEM_INFO);
            Item item = c.getItemNangLinhAn().get(indexUI);
            DataOutputStream ds = mss.writer();
            ds.writeByte(typeUI);
            ds.writeByte(indexUI);
            ds.writeLong(item.expire);
            ds.writeInt(item.yen);
            if (item.template.isTypeBody() || item.template.isTypeMount() || item.template.isTypeNgocKham()) {
                ds.writeByte(item.sys);
                ArrayList<ItemOption> options = item.getDisplayOptions();
                if (LinhAn.isLinhAn(item.id)) {
                    for (int i = 0; i < 7; i++) {
                        options.add(new ItemOption(item.material[i] - 969, quantity_upgrade[i] * (item.upgrade + 1)));
                    }
                }
                for (ItemOption ability : options) {
                    ds.writeByte(ability.optionTemplate.id);
                    ds.writeInt(ability.param);
                }
            }
            ds.flush();
            c.getService().sendMessage(mss);
            mss.cleanup();
        } catch (Exception ex) {
            Logger.getLogger(Service.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static int getParamBase(int id_option, int heso) {
        heso = ((heso / 10) - 1) / 2;
        switch (id_option) {
            case 2, 3, 4:
                return NinjaUtils.nextInt(10, 20);
            case 6, 7:
                return NinjaUtils.nextInt(100, 200);
            case 8, 9:
                return NinjaUtils.nextInt(150, 200);
            case 88, 89, 90:
                return NinjaUtils.nextInt(150, 200);
            case 94, 160:
                return NinjaUtils.nextInt(10, 20);
            case 99:
                return NinjaUtils.nextInt(200, 400);
            case 10, 69, 67:
                return NinjaUtils.nextInt(5, 10);
            case 57, 100, 101, 127, 130, 131, 98:
                return NinjaUtils.nextInt(3, 5) * heso;
            case 113:
                return NinjaUtils.nextInt(100, 500) * heso;
            case 21, 22, 23, 24, 25, 26:
                return NinjaUtils.nextInt(50, 100);
            case 121:
                return NinjaUtils.nextInt(5, 15);
            case 128:
                return NinjaUtils.nextInt(5, 15);
            default:
                return 1;
        }
    }
}