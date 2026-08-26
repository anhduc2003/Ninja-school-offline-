/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Exe_Z.map.zones;

import Exe_Z.map.Map;
import Exe_Z.constants.MapName;
import Exe_Z.constants.MobName;
import Exe_Z.map.TileMap;
import Exe_Z.map.Waypoint;
import Exe_Z.map.world.Territory;
import Exe_Z.map.world.World;
import Exe_Z.mob.Mob;
import Exe_Z.mob.TerritoryMobFactory;
import Exe_Z.model.Char;
import Exe_Z.util.NinjaUtils;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author Admin
 */
public class ClanTerritory extends ZWorld {

    public ClanTerritory(int id, TileMap tilemap, Map map) {
        super(id, tilemap, map);
    }

    @Override
    public void requestChangeMap(@NotNull Char p) {
        Waypoint wp = tilemap.findWaypoint(p.x, p.y);
        if (wp == null) {
            return;
        }
        int nextID = wp.next;
        Zone z = world.find(nextID);
        if (z != null) {
            if (z.isOpened) {
                p.setXY(wp.x, wp.y);
                p.outZone();
                z.join(p);
                return;
            }
        }
        p.returnToPreviousPostion(() -> {
            p.serverDialog("Cửa này vẫn chưa được mở.");
        });
    }

    @Override
    public void returnTownFromDead(@NotNull Char p) {
        p.outZone();
        if (map.id == MapName.NGHIA_DIA_QUAN) {
            int posX = NinjaUtils.nextInt(230, 425);
            p.setXY((short) posX, (short) 120);
            Zone z = world.find(MapName.NGHIA_DIA_QUAN);
            z.join(p);
        } else {
            p.setXY((short) 191, (short) 144);
            Zone z = world.find(MapName.CUA_CHO);
            z.join(p);
        }
    }

    @Override
    public void mobDead(Mob mob, Char killer) {
        if (killer != null) {
            Territory territory = (Territory) world;
            TerritoryMobFactory mobFactory = (TerritoryMobFactory) getMobFactory();
            if (mob.template.id == MobName.BAO_QUAN) {
                territory.waitForNextTurn();
            } else if (mob.levelBoss == 0 && tilemap.id != MapName.NGHIA_DIA_QUAN) {
                mobFactory.bornLastBoss();
            } else if (mob.levelBoss == 1 && isLastBossWasBorn) {
                mobFactory.createMonsterLamThao();
            }
        }
    }

}
