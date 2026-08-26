package Exe_Z.event;

import Exe_Z.constants.CMDInputDialog;
import Exe_Z.constants.CMDMenu;
import Exe_Z.constants.ConstTime;
import Exe_Z.constants.ItemName;
import Exe_Z.constants.ItemOptionName;
import Exe_Z.constants.MapName;
import Exe_Z.constants.NpcName;
import Exe_Z.effect.Effect;
import Exe_Z.effect.EffectAutoDataManager;
import static Exe_Z.event.Event.useVipEventItem;
import Exe_Z.event.eventpoint.EventPoint;
import Exe_Z.item.Item;
import Exe_Z.item.ItemFactory;
import Exe_Z.lib.RandomCollection;
import Exe_Z.map.Map;
import Exe_Z.map.Tree;
import Exe_Z.map.zones.Zone;
import Exe_Z.model.Char;
import Exe_Z.model.InputDialog;
import Exe_Z.model.Menu;
import Exe_Z.server.Config;
import Exe_Z.network.Service;
import Exe_Z.option.ItemOption;
import Exe_Z.store.ItemStore;
import Exe_Z.store.StoreManager;
import Exe_Z.util.NinjaUtils;
import java.util.ArrayList;
import java.util.List;

public class OFF extends Event {
    public OFF() {

    }

    @Override
    public void initMap(Zone zone) {
        Map map = zone.map;
        int mapID = map.id;
        switch (mapID) {
            case MapName.TRUONG_OOKAZA:
                zone.addTree(Tree.builder().id(EffectAutoDataManager.CAY_HALLOWEEN).x((short) 1426).y((short) 552).build());
                zone.addTree(Tree.builder().id(EffectAutoDataManager.CAY_HALLOWEEN).x((short) 784).y((short) 648).build());
                break;

        }
    }


@Override
public void initStore() {
    System.out.println("Initializing store for OFF event.");
}


@Override
public void action(Char p, int type, int amount) {
        System.out.println("Initializing store for OFF event.");
}

    @Override
    public void menu(Char p) {
      //  p.menus.clear();
        p.menus.add(new Menu(CMDMenu.EXECUTE, "Nói chuyện", () -> {
            StringBuilder sb = new StringBuilder();
            //sb.append("THÔNG BÁO").append("\n");
            sb.append("Hiện tại chưa có sự kiện diễn ra, hãy chờ đón các sự kiện trong thời gian sắp tới nhé!").append("\n");
            p.getService().showAlert("THÔNG BÁO", sb.toString());
        }));

    }
}
