package Exe_Z.shopcoin;

import Exe_Z.constants.CMD;
import Exe_Z.constants.ItemName;
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

public class ShopitemThu {

    public static final List<Item> item_shop = new ArrayList<>(Arrays.asList(
            new Item(ItemName.PHUONG_HOANG_BANG),
            new Item(ItemName.XICH_NHAN_NGAN_LANG),
            new Item(ItemName.THU_TRANG),
            new Item(ItemName.GIAP_THU),
            new Item(ItemName.YEN2),
            new Item(ItemName.DAY_CUONG),
            new Item(ItemName.HARLEY_DAVIDSON),
            new Item(ItemName.XE_MAY),
            new Item(ItemName.BO_DIEU_KHIEN),
            new Item(ItemName.DONG_CO_V_POWER),
            new Item(ItemName.DINH_VI),
            new Item(ItemName.BINH_NITRO),
            new Item(ItemName.BACH_HO),
            new Item(ItemName.BACH_HO),
            new Item(ItemName.HOA_KY_LAN),
            new Item(ItemName.GAY_MAT_TRANG),
            new Item(ItemName.GAY_TRAI_TIM),
            new Item(ItemName.MAT_NA_VO_DIEN),
            new Item(ItemName.MAT_NA_ONI),
            new Item(ItemName.TON_HANH_GIA),
            new Item(ItemName.HOA_SU_VUONG),
            new Item(ItemName.TAMADAI)
    ));
    public static HashMap<Integer, List<ItemOption>> itemOptionsMap = new HashMap<>();

    static {
        itemOptionsMap.put(ItemName.PHUONG_HOANG_BANG, Arrays.asList(
                new ItemOption(10, 190),
                new ItemOption(67, 95),
                new ItemOption(69, 190),
                new ItemOption(73, 1900),
                new ItemOption(58, 20),
                new ItemOption(94, 12),
                new ItemOption(134, 10),
                new ItemOption(176, 10)
        ));
        itemOptionsMap.put(ItemName.XICH_NHAN_NGAN_LANG, Arrays.asList(
                new ItemOption(10, 190),
                new ItemOption(67, 95),
                new ItemOption(69, 190),
                new ItemOption(73, 1900),
                new ItemOption(94, 12)
        ));
        itemOptionsMap.put(ItemName.THU_TRANG, Arrays.asList(
                new ItemOption(82, 500),
                new ItemOption(83, 500),
                new ItemOption(87, 500)
        ));
        itemOptionsMap.put(ItemName.GIAP_THU, Arrays.asList(
                new ItemOption(82, 500),
                new ItemOption(83, 500),
                new ItemOption(87, 500)
        ));
        itemOptionsMap.put(ItemName.YEN2, Arrays.asList(
                new ItemOption(82, 500),
                new ItemOption(83, 500),
                new ItemOption(87, 500)
        ));
        itemOptionsMap.put(ItemName.DAY_CUONG, Arrays.asList(
                new ItemOption(82, 500),
                new ItemOption(83, 500),
                new ItemOption(87, 500)
        ));
        itemOptionsMap.put(ItemName.HARLEY_DAVIDSON, Arrays.asList(
                new ItemOption(10, 190),
                new ItemOption(67, 95),
                new ItemOption(69, 190),
                new ItemOption(73, 1900),
                new ItemOption(94, 20)
        ));
        itemOptionsMap.put(ItemName.XE_MAY, Arrays.asList(
                new ItemOption(10, 190),
                new ItemOption(67, 95),
                new ItemOption(69, 190),
                new ItemOption(73, 1900),
                new ItemOption(58, 20)
        ));
        itemOptionsMap.put(ItemName.BO_DIEU_KHIEN, Arrays.asList(
                new ItemOption(82, 500),
                new ItemOption(83, 500),
                new ItemOption(87, 500)
        ));
        itemOptionsMap.put(ItemName.DONG_CO_V_POWER, Arrays.asList(
                new ItemOption(82, 500),
                new ItemOption(83, 500),
                new ItemOption(87, 500)));
        itemOptionsMap.put(ItemName.DINH_VI, Arrays.asList(
                new ItemOption(82, 500),
                new ItemOption(83, 500),
                new ItemOption(87, 500)
        ));
        itemOptionsMap.put(ItemName.BINH_NITRO, Arrays.asList(
                new ItemOption(82, 500),
                new ItemOption(83, 500),
                new ItemOption(87, 500)
        ));
        itemOptionsMap.put(ItemName.BACH_HO, Arrays.asList(
                new ItemOption(10, 190),
                new ItemOption(67, 95),
                new ItemOption(69, 190),
                new ItemOption(73, 1900),
                new ItemOption(58, 20),
                new ItemOption(94, 20),
                new ItemOption(174, 10)
        ));
        itemOptionsMap.put(ItemName.BACH_HO, Arrays.asList(
                new ItemOption(10, 190),
                new ItemOption(67, 95),
                new ItemOption(69, 190),
                new ItemOption(73, 1900),
                new ItemOption(58, 20),
                new ItemOption(94, 20),
                new ItemOption(175, 10)
        ));
        itemOptionsMap.put(ItemName.HOA_KY_LAN, Arrays.asList(
                new ItemOption(10, 190),
                new ItemOption(67, 95),
                new ItemOption(69, 190),
                new ItemOption(73, 1900),
                new ItemOption(58, 20),
                new ItemOption(94, 20),
                new ItemOption(173, 10)
        ));
        itemOptionsMap.put(ItemName.GAY_MAT_TRANG, Arrays.asList(
                new ItemOption(82, 3000),
                new ItemOption(83, 3000),
                new ItemOption(87, 3000)
        ));
        itemOptionsMap.put(ItemName.GAY_TRAI_TIM, Arrays.asList(
                new ItemOption(82, 3000),
                new ItemOption(83, 3000),
                new ItemOption(87, 3000)
        ));
        itemOptionsMap.put(ItemName.MAT_NA_VO_DIEN, Arrays.asList(
                new ItemOption(0, NinjaUtils.nextInt(500, 500)), // tấn công ngoai

                new ItemOption(5, NinjaUtils.nextInt(100, 100)), // né đòn
                new ItemOption(6, NinjaUtils.nextInt(2000, 2000)), // hp tối đa

                new ItemOption(8, NinjaUtils.nextInt(200, 200)), // vật công ngoại

                new ItemOption(57, NinjaUtils.nextInt(120, 120)), // cộng tiềm năng cho tất cả
                new ItemOption(58, NinjaUtils.nextInt(30, 30)), // cộng % tiềm năng
                new ItemOption(87, NinjaUtils.nextInt(5000, 5000)) // tấn công
        ));
        itemOptionsMap.put(ItemName.MAT_NA_ONI, Arrays.asList(
                new ItemOption(1, NinjaUtils.nextInt(500, 500)), // tấn công nội

                new ItemOption(5, NinjaUtils.nextInt(100, 100)), // né đòn
                new ItemOption(6, NinjaUtils.nextInt(2000, 2000)), // hp tối đa

                new ItemOption(9, NinjaUtils.nextInt(200, 200)), // vật công nội

                new ItemOption(57, NinjaUtils.nextInt(120, 120)), // cộng tiềm năng cho tất cả
                new ItemOption(58, NinjaUtils.nextInt(30, 30)), // cộng % tiềm năng
                new ItemOption(87, NinjaUtils.nextInt(5000, 5000)) // tấn công
        ));
        itemOptionsMap.put(ItemName.TON_HANH_GIA, Arrays.asList(
                new ItemOption(58, NinjaUtils.nextInt(25, 25))
        ));
        itemOptionsMap.put(ItemName.HOA_SU_VUONG, Arrays.asList(
                new ItemOption(10, 190),
                new ItemOption(67, 95),
                new ItemOption(69, 190),
                new ItemOption(73, 1900),
                new ItemOption(58, 30),
                new ItemOption(94, 20),
                new ItemOption(173, 10),
                new ItemOption(174, 10),
                new ItemOption(175, 10)
        ));
        itemOptionsMap.put(ItemName.TAMADAI, Arrays.asList(
        ));

    }

    public static List<ItemOption> getItemOptions(int itemId) {
        return itemOptionsMap.get(itemId);
    }

    public static HashMap<Integer, Integer> itemPrices = new HashMap<>();

    static {
        itemPrices.put(ItemName.PHUONG_HOANG_BANG, 100000);
        itemPrices.put(ItemName.XICH_NHAN_NGAN_LANG, 50000);
        itemPrices.put(ItemName.THU_TRANG, 5000);
        itemPrices.put(ItemName.GIAP_THU, 5000);
        itemPrices.put(ItemName.YEN2, 5000);
        itemPrices.put(ItemName.DAY_CUONG, 5000);
        itemPrices.put(ItemName.HARLEY_DAVIDSON, 70000);
        itemPrices.put(ItemName.XE_MAY, 80000);
        itemPrices.put(ItemName.BO_DIEU_KHIEN, 5000);
        itemPrices.put(ItemName.DONG_CO_V_POWER, 5000);
        itemPrices.put(ItemName.DINH_VI, 5000);
        itemPrices.put(ItemName.BINH_NITRO, 5000);
        itemPrices.put(ItemName.BACH_HO, 130000);
        itemPrices.put(ItemName.BACH_HO, 130000);
        itemPrices.put(ItemName.HOA_KY_LAN, 150000);
        itemPrices.put(ItemName.GAY_MAT_TRANG, 100000);
        itemPrices.put(ItemName.GAY_TRAI_TIM, 100000);
        itemPrices.put(ItemName.MAT_NA_VO_DIEN, 150000);
        itemPrices.put(ItemName.MAT_NA_ONI, 150000);
        itemPrices.put(ItemName.TON_HANH_GIA, 350000);
        itemPrices.put(ItemName.HOA_SU_VUONG, 200000);
        itemPrices.put(ItemName.TAMADAI, 50000);
    }
    public static HashMap<Integer, Integer> itemUpgrades = new HashMap<>();

    static {
        itemUpgrades.put(ItemName.PHUONG_HOANG_BANG, 0);
        itemUpgrades.put(ItemName.XICH_NHAN_NGAN_LANG, 0);
        itemUpgrades.put(ItemName.THU_TRANG, 0);
        itemUpgrades.put(ItemName.GIAP_THU, 0);
        itemUpgrades.put(ItemName.YEN2, 0);
        itemUpgrades.put(ItemName.DAY_CUONG, 0);
        itemUpgrades.put(ItemName.HARLEY_DAVIDSON, 0);
        itemUpgrades.put(ItemName.XE_MAY, 0);
        itemUpgrades.put(ItemName.BO_DIEU_KHIEN, 0);
        itemUpgrades.put(ItemName.DONG_CO_V_POWER, 0);
        itemUpgrades.put(ItemName.DINH_VI, 0);
        itemUpgrades.put(ItemName.BINH_NITRO, 0);
        itemUpgrades.put(ItemName.BACH_HO, 0);
        itemUpgrades.put(ItemName.BACH_HO, 0);
        itemUpgrades.put(ItemName.HOA_KY_LAN, 0);
        itemUpgrades.put(ItemName.GAY_MAT_TRANG, 0);
        itemUpgrades.put(ItemName.GAY_TRAI_TIM, 0);
        itemUpgrades.put(ItemName.MAT_NA_VO_DIEN, 0);
        itemUpgrades.put(ItemName.MAT_NA_ONI, 0);
        itemUpgrades.put(ItemName.TON_HANH_GIA, 0);
        itemUpgrades.put(ItemName.HOA_SU_VUONG, 0);
        itemUpgrades.put(ItemName.TAMADAI, 0);
    }
    public static HashMap<Integer, Integer> itemSys = new HashMap<>();

    static {
        itemSys.put(ItemName.PHUONG_HOANG_BANG, 4);
        itemSys.put(ItemName.XICH_NHAN_NGAN_LANG, 4);
        itemSys.put(ItemName.THU_TRANG, 0);
        itemSys.put(ItemName.GIAP_THU, 0);
        itemSys.put(ItemName.YEN2, 0);
        itemSys.put(ItemName.DAY_CUONG, 0);
        itemSys.put(ItemName.HARLEY_DAVIDSON, 4);
        itemSys.put(ItemName.XE_MAY, 4);
        itemSys.put(ItemName.BO_DIEU_KHIEN, 0);
        itemSys.put(ItemName.DONG_CO_V_POWER, 0);
        itemSys.put(ItemName.DINH_VI, 0);
        itemSys.put(ItemName.BINH_NITRO, 0);
        itemSys.put(ItemName.BACH_HO, 4);
        itemSys.put(ItemName.BACH_HO, 4);
        itemSys.put(ItemName.HOA_KY_LAN, 4);
        itemSys.put(ItemName.GAY_MAT_TRANG, 0);
        itemSys.put(ItemName.GAY_TRAI_TIM, 0);
        itemSys.put(ItemName.MAT_NA_VO_DIEN, 0);
        itemSys.put(ItemName.MAT_NA_ONI, 0);
        itemSys.put(ItemName.TON_HANH_GIA, 0);
        itemSys.put(ItemName.HOA_SU_VUONG, 4);
        itemSys.put(ItemName.TAMADAI, 0);
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
            if (item.template.isTypeBody() || item.template.isTypeMount() || item.template.isTypeNgocKham()) {
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
