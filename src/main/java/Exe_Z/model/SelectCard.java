
package Exe_Z.model;

import Exe_Z.constants.ItemName;
import Exe_Z.constants.TaskName;
import Exe_Z.convert.Converter;
import Exe_Z.item.Item;
import Exe_Z.item.ItemFactory;
import Exe_Z.store.ItemStore;
import Exe_Z.store.StoreManager;
import Exe_Z.util.NinjaUtils;
import Exe_Z.server.GlobalService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class SelectCard extends AbsSelectCard {

    private static final SelectCard instance = new SelectCard();

    public static SelectCard getInstance() {
        return instance;
    }

    public static final long EXPIRE_3_DAY = 3 * 24 * 60 * 60 * 1000;
    public static final long EXPIRE_7_DAY = 7 * 24 * 60 * 60 * 1000;
    public static final long EXPIRE_20_DAY = 20 * 24 * 60 * 60 * 1000;
    private int level;

   @Override
    public void init() {
        StringBuilder objStr = new StringBuilder();
        try {
            String content = Files.readString(Paths.get("item_roi/LatHinh/Lat_Hinh_Thuong.json"));
            objStr.append(content);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            JSONArray js = (JSONArray) JSONValue.parse(objStr.toString());
            for (int i = 0; i < js.size(); i++) {
                JSONObject job1 = (JSONObject) JSONValue.parse(js.get(i).toString());
                int id = Integer.parseInt(job1.get("id").toString());
                double rate = Double.parseDouble(job1.get("rate").toString());
                int expire = Integer.parseInt(job1.get("expire").toString());
                boolean isLock = Boolean.parseBoolean(job1.get("isLock").toString());
                long expireMillis;
                if (expire == -1) {
                    expireMillis = -1;
                } else {
                    expireMillis = convertDaysToMillis(expire);
                }
                int quantity = Integer.parseInt(job1.get("quantity").toString());
                add(Card.builder().id(id).rate(rate).expire(expireMillis).quantity(quantity).isLocked(isLock).build());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    private static long convertDaysToMillis(long days) {
        return days * 24 * 60 * 60 * 1000L;
    }

    @Override
    protected Card reward(@NotNull Char p, Card card) {
        int itemID = 0;

        // nếu rơi vào đồ này chưa kích hoạt sẽ ra yên
        itemID = p.user.activated == 0 && checkItem(card.getId()) ? ItemName.YEN : card.getId();

        int quantity = card.getQuantity();
        if (itemID == ItemName.YEN) {
            p.addYen(quantity);
            p.serverMessage("Bạn nhận được " + NinjaUtils.getCurrency(quantity) + " Yên");
        } else {
            Item item = ItemFactory.getInstance().newItem(itemID);
            long expire = card.getExpire();

            // nếu rơi vào đồ vĩnh viễn thì chưa kích hoạt sẽ ra hạn
            if (expire == -1 ) {
                item.expire = (p.user.activated == 1) || item.template.isUpToUp ? -1 : System.currentTimeMillis() + (p.user.activated == 0 ? 86400000 * 3 : 0);
                if (p.user.activated == 1 && itemID == ItemName.HUYET_SAC_HUNG_LANG || itemID == ItemName.CHIM_TINH_ANH ) {
                    GlobalService.getInstance().chat("server", "Chúc mừng " + p.name + " lật hình nhận được " + item.template.name + " vĩnh viễn");
                }
            } else {
                item.expire = System.currentTimeMillis() + expire;
            } 
            if (NinjaUtils.nextInt(2000) <= 100) {
                int itemLevel = p.level / 10 * 10;
                if (itemLevel < 10) {
                    itemLevel = 40;
                }
                if (itemLevel >= 60) {
                    itemLevel = 60;
                }
                List<ItemStore> list = StoreManager.getInstance().getListEquipmentWithLevelRange(itemLevel, itemLevel + 9);
                if (!list.isEmpty()) {
                    int rd = NinjaUtils.nextInt(list.size());
                    ItemStore itemStore = list.get(rd);
                    if (itemStore != null) {
                        itemID = itemStore.getItemID();
                        item = Converter.getInstance().toItem(itemStore, Converter.MAX_OPTION);
                        card = Card.builder().id(itemID).build();
                    }
                }
            }
//            if (p.user.activated == 1 && itemID == ItemName.HUYET_SAC_HUNG_LANG || itemID == ItemName.CHIM_TINH_ANH
//                        || itemID == ItemName.MAT_NA_SUPER_BROLY || itemID == ItemName.MAT_NA_ONNA_BUGEISHA) {
//                    GlobalService.getInstance().chat("server", "Chúc mừng " + p.name + " lật hình nhận được " + item.template.name);
//                }
            p.addItemToBag(item);
        }
        return card;
    }

    @Override
    protected boolean isCanSelect(Char p) {
        int index = p.getIndexItemByIdInBag(ItemName.PHIEU_MAY_MAN);
        if (index == -1 || p.bag[index] == null || !p.bag[index].has()) {
            p.serverDialog("Bạn không có phiếu may mắn!");
            return false;
        }
        if (p.getSlotNull() == 0) {
            p.serverDialog("Không đủ chỗ trống.");
            return false;
        }
        return true;
    }

    @Override
    protected void selecctCardSuccessful(@NotNull Char p) {
        int index = p.getIndexItemByIdInBag(ItemName.PHIEU_MAY_MAN);
        p.removeItem(index, 1, true);
        if (p.taskId == TaskName.NV_THU_TAI_MAY_MAN) {
            if (p.taskMain != null && p.taskMain.index == 3) {
                p.updateTaskCount(1);
            }
        }
    }
    
    private boolean checkItem(int itemId){
        return itemId == ItemName.THE_BAI_KINH_NGHIEM_GIA_TOC_SO || itemId == ItemName.THE_BAI_KINH_NGHIEM_GIA_TOC_CAO || itemId == ItemName.THE_BAI_KINH_NGHIEM_GIA_TOC_TRUNG;
    }

}
