//
//package Exe_Z.model;
//
//import Exe_Z.constants.ItemName;
//import Exe_Z.constants.TaskName;
//import Exe_Z.convert.Converter;
//import Exe_Z.item.Item;
//import Exe_Z.item.ItemFactory;
//import Exe_Z.store.ItemStore;
//import Exe_Z.store.StoreManager;
//import Exe_Z.util.NinjaUtils;
//import java.util.List;
//import org.jetbrains.annotations.NotNull;
//
//
//public class SelectCardVip extends AbsSelectCard {
//
//    private static final SelectCardVip instance = new SelectCardVip();
//
//    public static SelectCardVip getInstance() {
//        return instance;
//    }
//
//    public static final long EXPIRE_1_DAY = 1 * 24 * 60 * 60 * 1000;
//    public static final long EXPIRE_3_DAY = 3 * 24 * 60 * 60 * 1000;
//    public static final long EXPIRE_7_DAY = 7 * 24 * 60 * 60 * 1000;
//    public static final long EXPIRE_15_DAY = 15 * 24 * 60 * 60 * 1000;
//    public static final long EXPIRE_30_DAY = 30 * 24 * 60 * 60 * 1000;
//    public static final long EXPIRE_60_DAY = 60 * 24 * 60 * 60 * 1000;
//    public static final long EXPIRE_90_DAY = 90 * 24 * 60 * 60 * 1000;
//    private int level;
//
//    @Override
//    protected void init() {
//        add(Card.builder().id(ItemName.MAT_NA_SUPER_BROLY).rate(2).expire(EXPIRE_7_DAY).build());
//        add(Card.builder().id(ItemName.MAT_NA_ONNA_BUGEISHA).rate(2).expire(EXPIRE_7_DAY).build());
//        add(Card.builder().id(ItemName.HAKAIRO_YOROI).rate(5).expire(EXPIRE_7_DAY).build());
//        add(Card.builder().id(ItemName.MAT_NA_VO_DIEN).rate(3).expire(EXPIRE_7_DAY).build());
//        add(Card.builder().id(ItemName.MAT_NA_ONI).rate(3).expire(EXPIRE_3_DAY).build());
//        add(Card.builder().id(ItemName.MAT_NA_KUMA).rate(3).expire(EXPIRE_3_DAY).build());
//        add(Card.builder().id(ItemName.MAT_NA_INU).rate(3).expire(EXPIRE_3_DAY).build());
//        add(Card.builder().id(ItemName.GAY_MAT_TRANG).rate(3).expire(EXPIRE_3_DAY).build());
//        add(Card.builder().id(ItemName.GAY_TRAI_TIM).rate(3).expire(EXPIRE_3_DAY).build());
//        add(Card.builder().id(ItemName.HAKAIRO_YOROI).rate(5).expire(EXPIRE_3_DAY).build());
//        add(Card.builder().id(ItemName.MAT_NA_VO_DIEN).rate(30).expire(EXPIRE_3_DAY).build());
//        add(Card.builder().id(ItemName.MAT_NA_ONI).rate(30).expire(EXPIRE_3_DAY).build());
//        add(Card.builder().id(ItemName.MAT_NA_KUMA).rate(30).expire(EXPIRE_3_DAY).build());
//        add(Card.builder().id(ItemName.MAT_NA_INU).rate(30).expire(EXPIRE_3_DAY).build());
//        add(Card.builder().id(ItemName.GAY_MAT_TRANG).rate(30).expire(EXPIRE_3_DAY).build());
//        add(Card.builder().id(ItemName.GAY_TRAI_TIM).rate(30).expire(EXPIRE_3_DAY).build());
//        add(Card.builder().id(ItemName.MANH_SACH_CO).rate(10).build());
//        add(Card.builder().id(ItemName.BAT_BAO).rate(0.1).build());
//        add(Card.builder().id(ItemName.LINH_CHI_NGAN_NAM).rate(5).build());
//        add(Card.builder().id(ItemName.LINH_CHI_VAN_NAM).rate(5).build());
//        add(Card.builder().id(ItemName.YEN).rate(40).quantity(5000).build());
//        add(Card.builder().id(ItemName.YEN).rate(40).quantity(1000).build());
//        add(Card.builder().id(ItemName.YEN).rate(40).quantity(2000).build());
//        add(Card.builder().id(ItemName.XE_MAY).rate(0.5).build());
//        add(Card.builder().id(ItemName.HARLEY_DAVIDSON).rate(0.01).build());
//        add(Card.builder().id(ItemName.TRUNG_VI_THU).rate(0.01).build());
//        add(Card.builder().id(ItemName.SACH_VO_CONG_IKKAKUJUU).rate(0.1).build());
//        add(Card.builder().id(ItemName.SACH_VO_CONG_HIBASHIRI).rate(0.1).build());
//        add(Card.builder().id(ItemName.SACH_VO_CONG_SAIHYOKEN).rate(0.1).build());
//        add(Card.builder().id(ItemName.SACH_VO_CONG_AISU_MEIKU).rate(0.1).build());
//        add(Card.builder().id(ItemName.SACH_VO_CONG_KAMINARI).rate(0.1).build());
//        add(Card.builder().id(ItemName.SACH_VO_CONG_KOKAZE).rate(0.1).build());
//        add(Card.builder().id(ItemName.SACH_VO_CONG_KAGE_BUNSHIN).rate(1).build());
//        add(Card.builder().id(ItemName.BAO_HIEM_NHU_Y).rate(45).build());
//        add(Card.builder().id(ItemName.SASHIMI_CAO_CAP).rate(45).build());
//        add(Card.builder().id(ItemName.HOAN_COT_CHI_CHU_TRUNG_CAP).rate(3).build());
//        add(Card.builder().id(ItemName.HOA_TUYET).rate(30).build());
//        add(Card.builder().id(ItemName.NHAM_THACH_).rate(30).build());
//        add(Card.builder().id(ItemName.PHA_LE).rate(30).build());
//        add(Card.builder().id(ItemName.LONG_KHI).rate(10).build());
//    }
//
//    @Override
//    protected Card reward(@NotNull Char p, Card card) {
//        int itemID = card.getId();
//        int quantity = card.getQuantity();
//        if (itemID == ItemName.YEN) {
//            p.addCoin(quantity);
//            p.serverMessage("Bạn nhận được " + NinjaUtils.getCurrency(quantity) + " Xu");
//        } else {
//            Item item = ItemFactory.getInstance().newItem(itemID);
//            long expire = card.getExpire();
//            if (expire == -1) {
//                item.expire = -1;
//            } else {
//                item.expire = System.currentTimeMillis() + expire;
//            }
//            if (NinjaUtils.nextInt(2000) == 1 || p.user.isAdmin()) {
//                int itemLevel = p.level / 10 * 10;
//                if (itemLevel < 80) {
//                    itemLevel = 80;
//                }
//                if (itemLevel > 100) {
//                    itemLevel = 100;
//                }
//                List<ItemStore> list = StoreManager.getInstance().getListEquipmentWithLevelRange(itemLevel, itemLevel + 9);
//                if (!list.isEmpty()) {
//                    int rd = NinjaUtils.nextInt(list.size());
//                    ItemStore itemStore = list.get(rd);
//                    if (itemStore != null) {
//                        itemID = itemStore.getItemID();
//                        item = Converter.getInstance().toItem(itemStore, Converter.MAX_OPTION);
//                        card = Card.builder().id(itemID).build();
//                    }
//                }
//            }
//            p.addItemToBag(item);
//        }
//        return card;
//    }
//
//    @Override
//    protected boolean isCanSelect(Char p) {
//        p.addGold(-1000);
//        if (p.getSlotNull() == 0) {
//            p.serverDialog("Hành Trang Con Không đủ chỗ trống.");
//            return false;
//        }
//         if (p.user.gold < 1000) {
//            p.serverDialog("Đủ 1k Lượng Đi Rồi Tao Cho Lật .");
//            return false;
//        }
//          if (p.user.activated == 0) {
//            p.serverDialog("Kích Hoạt Đi Rồi Tao Cho Lật .");
//            return false;
//        }
//        return true;
//    }
//
//    @Override
//    protected void selecctCardSuccessful(@NotNull Char p) {
////        int index = p.getIndexItemByIdInBag(ItemName.VE_VIP);
////        p.removeItem(index, 1, true);
//        if (p.taskId == TaskName.NV_THU_TAI_MAY_MAN) {
//            if (p.taskMain != null && p.taskMain.index == 3) {
//                p.updateTaskCount(1);
//            }
//       }
//    }
//}
