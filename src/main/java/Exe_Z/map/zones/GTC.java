package Exe_Z.map.zones;

import Exe_Z.constants.MapName;
import Exe_Z.constants.MobName;
import Exe_Z.map.Map;
import Exe_Z.map.TileMap;
import Exe_Z.map.WarClan;
import Exe_Z.map.Waypoint;
import Exe_Z.map.world.World;
import Exe_Z.mob.Mob;
import Exe_Z.model.Char;
import org.jetbrains.annotations.NotNull;

public class GTC extends Zone {
    public GTC(int id, TileMap tilemap, Map map) {
        super(id, tilemap, map);
    }

    @Override
    public void requestChangeMap(@NotNull Char p) {
        Waypoint wp = tilemap.findWaypoint(p.x, p.y);
        if (wp == null) {
            return;
        }
        int nextID = wp.next;
        if (map.warClan != null && map.warClan.status == 0 && (p.mapId == MapName.CAN_CU_DIA || p.mapId == MapName.CAN_CU_DIA_2)) {
            p.returnToPreviousPostion(() -> {
                p.serverDialog("Gia tộc chiến chưa bắt đầu.");
            });
            return;
        }
        if ((p.Clanfaction == 0 && nextID == MapName.CAN_CU_DIA_2) || (p.Clanfaction == 1 && nextID == MapName.CAN_CU_DIA)) {
            p.returnToPreviousPostion(() -> {
                p.serverDialog("Không thể vào khu vực này.");
            });
            return;
        }
        p.setXY(wp.x, wp.y);
        if (p.mapId == 98) {
            p.setXY((short) 40, (short) 288);
            nextID = 120;
        } else if (p.mapId == 104) {
            p.setXY((short) 700, (short) 264);
            nextID = 124;
        }
        p.changeMap(nextID);
    }

    @Override
    public void returnTownFromDead(@NotNull Char p) {
        int mapID = -1;
        short x = -1;
        short y = -1;
        if (p.Clanfaction == 0) {
            mapID = 98;
            x = 242;
            y = 312;
        }
        if (p.Clanfaction == 1) {
            mapID = 104;
            x = 245;
            y = 236;
        }
        p.setXY(x, y);
        p.changeMap(mapID);
    }

    public void join(Char p) {
        super.join(p);
        if (tilemap.isGTC() && map.warClan != null) {
            if (p.Clanfaction == 0 && p.mapId != 117) {
                p.setTypePk(Char.PK_PHE1);
            }
            if (p.Clanfaction == 1 && p.mapId != 117) {
                p.setTypePk(Char.PK_PHE2);
            }
            p.getService().warClanInfo();
            getService().changePk(p);
            p.getService().sendTimeInMap(map.warClan.countDown);
        }
    }
}
