package Exe_Z.event;

import Exe_Z.constants.CMDInputDialog;
import Exe_Z.constants.CMDMenu;
import Exe_Z.constants.ItemName;
import Exe_Z.event.eventpoint.EventPoint;
import Exe_Z.map.zones.Zone;
import Exe_Z.model.Char;
import Exe_Z.model.InputDialog;
import Exe_Z.model.Menu;
import Exe_Z.server.Config;
import Exe_Z.util.NinjaUtils;
import java.util.Calendar;

public class StarFestival extends Event {

    public static final String TOP_STAR_LANTERN = "star_lantern";
    private static final int EXCHANGE_STAR_LANTERN = 0;

    public StarFestival() {
        setId(Event.STAR_FESTIVAL);
        endTime = Calendar.getInstance();
        endTime.set(Config.getInstance().getEventYear(), Config.getInstance().getEventMonth() - 1,
                Config.getInstance().getEventDay(), Config.getInstance().getEventHour(),
                Config.getInstance().getEventMinute(), Config.getInstance().getEventSecond());
        loadMonsterDropTable("star_festival", "item_roi/event_StarFestival/STAR_FESTIVAL.json");
        keyEventPoint.add(EventPoint.DIEM_TIEU_XAI);
        keyEventPoint.add(TOP_STAR_LANTERN);
    }

    @Override
    public void action(Char p, int type, int amount) {
        if (type != EXCHANGE_STAR_LANTERN) {
            return;
        }
        if (makeEventItem(p, amount, new int[][]{{ItemName.LONG_DEN_NGOI_SAO, 10}}, 0, 0, 0, ItemName.HUYEN_TINH_NGOC)) {
            p.getEventPoint().addPoint(TOP_STAR_LANTERN, amount * 10);
            p.getEventPoint().addPoint(EventPoint.DIEM_TIEU_XAI, amount);
        }
    }

    @Override
    public void menu(Char p) {
        if (isEnded()) {
            p.getService().showAlert("Lễ hội Sao Đêm", "Sự kiện đã kết thúc.");
            return;
        }
        p.menus.clear();
        p.menus.add(new Menu(CMDMenu.EXECUTE, "Đổi 10 lồng đèn sao", () -> {
            p.setInput(new InputDialog(CMDInputDialog.EXECUTE, "Số lần đổi", () -> {
                InputDialog input = p.getInput();
                try {
                    action(p, EXCHANGE_STAR_LANTERN, input.intValue());
                } catch (Exception ex) {
                    if (!input.isEmpty()) {
                        p.inputInvalid();
                    }
                }
            }));
            p.getService().showInputDialog();
        }));
        p.menus.add(new Menu(CMDMenu.EXECUTE, "Đua top Sao Đêm", () -> viewTop(p, TOP_STAR_LANTERN, "Top Sao Đêm", "%d. %s đã thả %s lồng đèn")));
        p.menus.add(new Menu(CMDMenu.EXECUTE, "Hướng dẫn", () -> p.getService().showAlert("Lễ hội Sao Đêm", "Đánh quái để nhận lồng đèn sao. Gom 10 lồng đèn sao để đổi 1 Huyền tinh ngọc.\nĐiểm đổi quà được tính vào top Sao Đêm.")));
    }

    @Override
    public void initStore() {
    }

    @Override
    public void initMap(Zone zone) {
    }
}
