	/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Exe_Z.map.zones;

import Exe_Z.map.Map;
import Exe_Z.map.TileMap;
import Exe_Z.map.world.World;
import Exe_Z.model.Char;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Admin
 */
public class ZWorld extends Zone {

    @Getter
    @Setter
    protected World world;

    public ZWorld(int id, TileMap tilemap, Map map) {
        super(id, tilemap, map);
    }

    @Override
    public void join(Char p) {
        Zone preZone = p.zone;
        if (preZone != null) {
            if (!preZone.tilemap.isWorld()) {
                p.addMemberForWorld(preZone, this);
            }
        }
        super.join(p);
        p.getService().sendTimeInMap(world.getCountDown());
    }

}
