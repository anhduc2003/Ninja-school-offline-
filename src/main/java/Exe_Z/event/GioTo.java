package Exe_Z.event;

import Exe_Z.RandomItem.Event.RandomItemGioTo;
import Exe_Z.constants.CMDInputDialog;
import Exe_Z.constants.CMDMenu;
import Exe_Z.constants.ConstTime;
import Exe_Z.item.Item;
import Exe_Z.constants.ItemName;
import Exe_Z.constants.ItemOptionName;
import Exe_Z.constants.MapName;
import Exe_Z.model.Char;
import Exe_Z.model.InputDialog;
import Exe_Z.model.Menu;
import Exe_Z.mob.Mob;
import Exe_Z.constants.NpcName;
import Exe_Z.event.eventpoint.EventPoint;
import Exe_Z.item.ItemFactory;
import Exe_Z.model.RandomItem;
import Exe_Z.npc.Npc;
import Exe_Z.server.Config;
import Exe_Z.mob.MobTemplate;
import Exe_Z.util.NinjaUtils;
import Exe_Z.lib.RandomCollection;
import Exe_Z.map.Map;
import Exe_Z.map.zones.Zone;
import Exe_Z.mob.MobManager;
import Exe_Z.npc.NpcFactory;
import Exe_Z.option.ItemOption;
import Exe_Z.store.ItemStore;
import Exe_Z.store.StoreManager;
import java.util.Calendar;
import java.util.List;

public class GioTo extends Event {

    public static final String TOP_TRE = "top_tre";
    public static final String INVITATION_NUMBER = "invitation_number";
    private static final int TRE_XANH = 0;
    private static final int TRE_VANG = 1;
    protected RandomCollection<Integer> itemsThrownFromMonsters2;
    private RandomCollection<Integer> rd = new RandomCollection<>();
    private RandomCollection<Integer> vipItems = new RandomCollection<>();

    public GioTo() {
//        setId(Event.HALLOWEEN);
        //endTime.set(2024, 11, 12, 8, 50, 59);
        endTime.set(Config.getInstance().getEventYear(),
                Config.getInstance().getEventMonth() - 1,
                Config.getInstance().getEventDay(),
                Config.getInstance().getEventHour(),
                Config.getInstance().getEventMinute(),
                Config.getInstance().getEventSecond());
        keyEventPoint.add(EventPoint.DIEM_TIEU_XAI);
        keyEventPoint.add(TOP_TRE);
        keyEventPoint.add(INVITATION_NUMBER);

    }

    @Override
    public void useItem(Char _char, Item item) {
        if (item.id == ItemName.TRE_XANH_TRAM_DOT) {
            if (_char.getSlotNull() == 0) {
                _char.warningBagFull();
                return;
            }
            _char.addExp(3000000);
            int indexUI = _char.getIndexItemByIdInBag(ItemName.TRE_XANH_TRAM_DOT);
            if (indexUI == -1) {
                return;
            }
            RandomCollection<Integer> rand = RandomItemGioTo.TRE_XANH_TRAM_DOT;
            int id = rand.next();
            Item itm = ItemFactory.getInstance().newItem(id);
            itm.initExpire();
            _char.addItemToBag(itm);
            _char.removeItem(indexUI, 1, true);
            return;
        }
        if (item.id == ItemName.TRE_VANG_TRAM_DOT) {
            if (_char.getSlotNull() == 0) {
                _char.warningBagFull();
                return;
            }
            _char.addExp(5000000);
            int indexUI = _char.getIndexItemByIdInBag(ItemName.TRE_VANG_TRAM_DOT);
            if (indexUI == -1) {
                _char.serverMessage("Không có item.");
                return;
            }
            RandomCollection<Integer> rand = RandomItemGioTo.TRE_VANG_TRAM_DOT;
            int id = rand.next();
            Item itm = ItemFactory.getInstance().newItem(id);
            itm.initExpire();
            _char.addItemToBag(itm);
            _char.removeItem(indexUI, 1, true);
            return;
        }
        if (item.id == ItemName.TIN_VAT) {
            if (_char.getSlotNull() == 0) {
                _char.warningBagFull();
                return;
            }
            Npc npc = _char.zone.getNpc(NpcName.THANH_GIONG);
            if (npc == null) {
                _char.serverMessage("Hãy lại gần Thánh Gióng để sử dụng.");
                return;
            }
            int distance = NinjaUtils.getDistance(npc.cx, npc.cy, _char.x, _char.y);
            if (distance > 100) {
                _char.serverMessage("Hãy lại gần Thánh Gióng để sử dụng.");
                return;
            }
            _char.addExp(5000000);
            int indexUI = _char.getIndexItemByIdInBag(ItemName.TIN_VAT);
            if (indexUI == -1) {
                _char.serverMessage("Không có item.");
                return;
            }
            RandomCollection<Integer> rand = RandomItemGioTo.TIN_VAT;
            int id = rand.next();
            Item itm = ItemFactory.getInstance().newItem(id);
            itm.initExpire();
            _char.addItemToBag(itm);
            _char.removeItem(indexUI, 1, true);
            return;
        }

    }

    @Override
    public void action(Char p, int type, int amount) {
        if (isEnded()) {
            p.serverMessage("Sự kiện đã kết thúc");
            return;
        }
        switch (type) {
            case TRE_XANH:
                makeTreXanh(p, amount);
                break;

            case TRE_VANG:
                makeTreVang(p, amount);
                break;
        }
    }

    public void makeTreXanh(Char p, int amount) {
        int[][] itemRequires = new int[][]{{ItemName.DOT_TRE_XANH, 100}};
        int itemIdReceive = ItemName.TRE_XANH_TRAM_DOT;
        boolean isDone = makeEventItem(p, amount, itemRequires, 0, 50000, 10000, itemIdReceive);
        if (isDone) {
            p.getEventPoint().addPoint(GioTo.TOP_TRE, amount);
        }
    }

    public void makeTreVang(Char p, int amount) {
        int[][] itemRequires = new int[][]{{ItemName.DOT_TRE_VANG, 100}};
        int itemIdReceive = ItemName.TRE_VANG_TRAM_DOT;
        boolean isDone = makeEventItem(p, amount, itemRequires, 50, 0, 10000, itemIdReceive);
        if (isDone) {
            p.getEventPoint().addPoint(GioTo.TOP_TRE, amount);
        }
    }

    @Override
    public void menu(Char p) {

        if (!isEnded()) {
            p.menus.add(new Menu(CMDMenu.EXECUTE, "Làm Tre Xanh", () -> {
                p.setInput(new InputDialog(CMDInputDialog.EXECUTE, "Tre Xanh", () -> {
                    InputDialog input = p.getInput();
                    try {
                        int number = input.intValue();
                        action(p, TRE_XANH, number);
                    } catch (Exception e) {
                        if (!input.isEmpty()) {
                            p.inputInvalid();
                        }
                    }
                }));
                p.getService().showInputDialog();
            }));
            p.menus.add(new Menu(CMDMenu.EXECUTE, "Làm Tre Vàng", () -> {
                p.setInput(new InputDialog(CMDInputDialog.EXECUTE, "Tre Vàng", () -> {
                    InputDialog input = p.getInput();
                    try {
                        int number = input.intValue();
                        action(p, TRE_VANG, number);
                    } catch (Exception e) {
                        if (!input.isEmpty()) {
                            p.inputInvalid();
                        }
                    }
                }));
                p.getService().showInputDialog();
            }));
        }
        p.menus.add(new Menu(CMDMenu.EXECUTE, "Đổi lồng đèn", () -> {
            p.menus.clear();
            p.menus.add(new Menu(CMDMenu.EXECUTE, "2 triệu xu", () -> {
                p.setCommandBox(Char.DOI_LONG_DEN_XU);
                List<Item> list = p.getListItemByID(ItemName.LONG_DEN_TRON, ItemName.LONG_DEN_CA_CHEP,
                        ItemName.LONG_DEN_MAT_TRANG, ItemName.LONG_DEN_NGOI_SAO);
                p.getService().openUIShopTrungThu(list, "Đổi lồng đèn 2 triệu xu", "Đổi (2 triệu xu)");
            }));
            p.menus.add(new Menu(CMDMenu.EXECUTE, "1000 lượng", () -> {
                p.setCommandBox(Char.DOI_LONG_DEN_LUONG);
                List<Item> list = p.getListItemByID(ItemName.LONG_DEN_TRON, ItemName.LONG_DEN_CA_CHEP,
                        ItemName.LONG_DEN_MAT_TRANG, ItemName.LONG_DEN_NGOI_SAO);
                p.getService().openUIShopTrungThu(list, "Đổi lồng đèn 1000 lượng", "Đổi (1000L)");
            }));
            p.getService().openUIMenu();
        }));
        p.menus.add(new Menu(CMDMenu.EXECUTE, "Top sự kiện", () -> {
            p.menus.clear();
            p.menus.add(new Menu(CMDMenu.EXECUTE, "Bxh làm tre", () -> {
                viewTop(p, TOP_TRE, "Top tre vàng", "%d. %s đã làm %s tre vàng trăm đốt");
            }));
//            p.menus.add(new Menu(CMDMenu.EXECUTE, "Quà đua top", () -> {
//                StringBuilder sb = new StringBuilder();
//                sb.append("Top 1 :\n"
//                        + "\n"
//                        + "1 Thú cưỡi Hỏa kì lân vĩnh viễn\n"
//                        + "2 Rương Huyền bí\n"
//                        + "1 Pet bí rễ hành có chống pk 30 ngày\n"
//                        + "\n"
//                        + "Top 2-10\n"
//                        + "\n"
//                        + "1 Thú cưỡi Hỏa kì lân max 5* 30 ngày\n"
//                        + "2 Rương Bạch Ngân\n"
//                        + "1 pet bí rễ hành có chống pk 15 ngày\n"
//                        + "\n"
//                        + "Lưu ý :\n"
////                        + "- Tối thiểu sử dụng 50,000 kẹo táo được xếp top . Ai xếp trước tính người đó\n"
//                        + "\n"
//                        + "- Có 3 bộ option được chọn khi nhận thưởng hỏa kì lân\n"
//                        + "* Công :\n"
//                        + "Tăng tấn công %\n"
//                        + "Chí mạng\n"
//                        + "Chính xác\n"
//                        + "Tấn công khi đánh chí mạng\n"
//                        + "\n"
//                        + "* Thủ :\n"
//                        + "Chịu sát thương cho chủ\n"
//                        + "Giảm sát thương\n"
//                        + "Hp tối đa\n"
//                        + "Kháng tất cả\n"
//                        + "\n"
//                        + "* Hồi phục :\n"
//                        + "Mỗi 5s hồi phục mp\n"
//                        + "Mỗi 5s hồi phục hp\n"
//                        + "Né đòn\n"
//                        + "Cộng tiềm năng");
//                p.getService().showAlert("Phần thưởng", sb.toString());
//            }));
//            if (isEnded()) {
//                int ranking = getRanking(p, TOP_TRE);
//                if (ranking <= 10 && p.getEventPoint().getRewarded(TOP_TRE) == 0) {
//                    p.menus.add(new Menu(CMDMenu.EXECUTE, String.format("Nhận Thưởng TOP %d", ranking), () -> {
//                        receiveReward(p, TOP_TRE);
//                    }));
//                }
//            }
            p.getService().openUIMenu();
        }));

        p.menus.add(new Menu(CMDMenu.EXECUTE, "Hướng dẫn", () -> {
            StringBuilder sb = new StringBuilder();
            sb.append("- 100 đốt tre xanh + 50k xu + 10k yên = Tre xanh trăm đốt \n"
                    + "- 100 đốt tre vàng + 50 lượng + 10k yên = Tre vàng trăm đốt \n");
            p.getService().showAlert("Hướng Dẫn", sb.toString());
        }));
    }

    // TODO: cửa hàng goosho
    @Override
    public void initStore() {
//        StoreManager.getInstance().addItem((byte) StoreManager.TYPE_MISCELLANEOUS, ItemStore.builder()
//                .id(999)
//                .itemID(ItemName.TIN_VAT)
//                .gold(100)
//                .expire(ConstTime.FOREVER)
//                .build());

    }

    //TODO: quà top sự kiện
    public void receiveReward(Char p, String key) {
        int ranking = getRanking(p, key);
        if (ranking > 10) {
            p.getService().serverDialog("Bạn không đủ điều kiện nhận phần thưởng");
            return;
        }
        if (p.getEventPoint().getRewarded(key) == 1) {
            p.getService().serverDialog("Bạn đã nhận phần thưởng rồi");
            return;
        }
        if (p.getSlotNull() < 6) {
            p.getService().serverDialog("Bạn cần để hành trang trống tối thiểu 6 ô");
            return;
        }

        Item mount = ItemFactory.getInstance().newItem(ItemName.HOA_KY_LAN);
        Item choiBay = ItemFactory.getInstance().newItem(ItemName.CHOI_BAY);
        int maskId = p.gender == 1 ? ItemName.SHIRAIJI : ItemName.HAJIRO;
        Item mask = ItemFactory.getInstance().newItem(maskId);

        if (ranking == 1) {
            mount.options.add(new ItemOption(ItemOptionName.NE_DON_ADD_POINT_TYPE_1, 200));
            mount.options.add(new ItemOption(ItemOptionName.CHINH_XAC_ADD_POINT_TYPE_1, 100));
            mount.options.add(new ItemOption(ItemOptionName.TAN_CONG_KHI_DANH_CHI_MANG_POINT_PERCENT_TYPE_1, 100));
            mount.options.add(new ItemOption(ItemOptionName.CHI_MANG_ADD_POINT_TYPE_1, 100));
            mount.options.add(new ItemOption(ItemOptionName.CONG_THEM_TIEM_NANG_ADD_POINT_PERCENT_TYPE_0, 20));
            mount.options.add(new ItemOption(ItemOptionName.TAN_CONG_ADD_POINT_PERCENT_TYPE_8, 20));

            mask.options.add(new ItemOption(125, 3000));
            mask.options.add(new ItemOption(117, 3000));
            mask.options.add(new ItemOption(94, 10));
            mask.options.add(new ItemOption(136, 20));
            mask.options.add(new ItemOption(127, 10));
            mask.options.add(new ItemOption(130, 10));
            mask.options.add(new ItemOption(131, 10));

            choiBay.setQuantity(10);
            p.addItemToBag(choiBay);
            for (int i = 0; i < 3; i++) {
                Item mysteryChest = ItemFactory.getInstance().newItem(ItemName.RUONG_HUYEN_BI);
                p.addItemToBag(mysteryChest);
            }
        } else if (ranking == 2) {
            choiBay.setQuantity(5);
            p.addItemToBag(choiBay);
            Item mysteryChest = ItemFactory.getInstance().newItem(ItemName.RUONG_HUYEN_BI);
            p.addItemToBag(mysteryChest);
        } else if (ranking >= 3 && ranking <= 5) {
            mount.expire = System.currentTimeMillis() + 86400000 * 90;
            choiBay.setQuantity(3);
            p.addItemToBag(choiBay);
            for (int i = 0; i < 2; i++) {
                Item blueChest = ItemFactory.getInstance().newItem(ItemName.RUONG_HUYEN_BI);
                p.addItemToBag(blueChest);
            }
        } else {
            mount.expire = System.currentTimeMillis() + 86400000 * 30;
            Item blueChest = ItemFactory.getInstance().newItem(ItemName.RUONG_HUYEN_BI);
            p.addItemToBag(blueChest);
        }

        p.addItemToBag(mount);
        p.addItemToBag(mask);

        p.getEventPoint().setRewarded(key, 1);
    }

    // TODO: cây effect
    @Override
    public void initMap(Zone zone) {
        Map map = zone.map;
        int mapID = map.id;
        switch (mapID) {
            case MapName.KHU_LUYEN_TAP:
                break;
            case MapName.TRUONG_OOKAZA:
                zone.addNpc(NpcFactory.getInstance().newNpc(99, NpcName.THANH_GIONG, 1426, 552, 0));
                break;
            case MapName.TRUONG_HARUNA:
                zone.addNpc(NpcFactory.getInstance().newNpc(99, NpcName.THANH_GIONG, 2048, 360, 0));
                break;
            case MapName.TRUONG_HIROSAKI:
                zone.addNpc(NpcFactory.getInstance().newNpc(99, NpcName.THANH_GIONG, 1207, 168, 0));
                break;

        }
    }

}
