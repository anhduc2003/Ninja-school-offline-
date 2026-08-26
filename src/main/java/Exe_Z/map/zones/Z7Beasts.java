/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Exe_Z.map.zones;

import Exe_Z.constants.ItemName;
import Exe_Z.map.Map;
import Exe_Z.constants.MapName;
import Exe_Z.constants.MobName;
import Exe_Z.item.Item;
import Exe_Z.item.ItemFactory;
import Exe_Z.map.TileMap;
import Exe_Z.map.Waypoint;
import Exe_Z.map.world.SevenBeasts;
import Exe_Z.map.world.World;
import Exe_Z.mob.Mob;
import Exe_Z.model.Char;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author Admin
 */
public abstract class Z7Beasts extends ZWorld {

    @Getter
    protected int level;

    public Z7Beasts(int id, TileMap tilemap, Map map) {
        super(id, tilemap, map);
    }

    @Override
    public void returnTownFromDead(@NotNull Char p) {
        p.outZone();
        Zone z = world.find(MapName.KHU_VUC_CHO);
        p.setXY((short) 35, (short) 360);
        z.join(p);
        if (map.id == MapName.THAT_THU_AI) {
            world.getService().serverMessage(String.format("%s đã rời ải, xin mời thành viên tiếp theo", Char.setNameVip(p.name)));
        }
    }

    @Override
    public void requestChangeMap(@NotNull Char p) {
        if (world.getCountDown() > 3600) {
            p.returnToPreviousPostion(() -> {
                p.serverDialog("Cửa ải chưa được mở.");
            });
            return;
        }
        Waypoint wp = tilemap.findWaypoint(p.x, p.y);
        if (wp == null) {
            return;
        }
        int nextID = wp.next;
        Z7Beasts z = (Z7Beasts) world.find(nextID);
        if (nextID == MapName.KHU_VUC_CHO) {
            p.returnToPreviousPostion(() -> {
                p.serverDialog("Lối ra đã bị chặn. Bạn chỉ còn cách tiêu diệt hết số quái trong ải");
            });
            return;
        } else if (nextID == MapName.THAT_THU_AI) {
            BeastArea area = (BeastArea) z;
            if ((area.getNumberChar() > 0 || area.getPreviousPlayerDied() == p.id) && area.getLevel() < 6) {
                p.returnToPreviousPostion(() -> {
                    p.serverDialog("Đã có người vào ải, hoặc chưa tới lượt đánh của bạn. Vui lòng chờ ở bên ngoài.");
                });
                return;
            }
            refresh();
        }
        p.outZone();
        p.setXY(wp.x, wp.y);
        z.join(p);
    }

    @Override
    public void mobDead(Mob mob, Char killer) {
        if (killer != null) {
            if ((mob.template.id == MobName.MOC_NHAN || mob.template.id == MobName.MUC_ONG_DO)) {
                Item item = ItemFactory.getInstance().newItem(ItemName.THAT_THU_THU_BAO);
                item.setQuantity(1);
                item.isLock = true;
                if (killer.getSlotNull() > 0) {
                    killer.addItemToBag(item);
                } else {
                    killer.warningBagFull();
                }
                SevenBeasts sevenBeasts = (SevenBeasts) killer.findWorld(World.SEVEN_BEASTS);
                sevenBeasts.getService().serverMessage(String.format("%s nhận được %s rơi ra từ %s", Char.setNameVip(killer.name), item.template.name, mob.template.name));
                if (mob.template.id == MobName.MUC_ONG_DO) {
                    sevenBeasts.setCountdown(15);
                    sevenBeasts.getService().serverMessage("Xin chúc mừng nhóm của bạn đã vượt qua được thất thú ải.");
                }
            }
        }
    }

    public abstract void refresh();
}
