/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Exe_Z.map.zones;

import Exe_Z.map.Map;
import Exe_Z.map.TileMap;
import Exe_Z.mob.Mob;
import Exe_Z.constants.MobName;

/**
 *
 * @author Admin
 */
public class WaitingArea extends Z7Beasts {

    private Mob woodenDummy;

    public WaitingArea(int id, TileMap tilemap, Map map) {
        super(id, tilemap, map);
    }

    public void initMob() {
        woodenDummy = new Mob(0, (short) MobName.MOC_NHAN, 15000000, (short) 62, (short) 221, (short) 312, false, false, this);
        addMob(woodenDummy);
    }

    @Override
    public void refresh() {
        if (this.level < 5) {
            this.level++;
            if (woodenDummy == null) {
                initMob();
            } else {
                woodenDummy.recovery();
                getService().recoveryMonster(woodenDummy);
            }
        }
    }
}
