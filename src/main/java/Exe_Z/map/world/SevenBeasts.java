/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Exe_Z.map.world;

import Exe_Z.map.Map;
import Exe_Z.constants.MapName;
import Exe_Z.map.zones.BeastArea;
import Exe_Z.map.zones.WaitingArea;
import Exe_Z.map.zones.Z7Beasts;
import Exe_Z.model.Char;
import Exe_Z.map.MapManager;
import Exe_Z.map.zones.Zone;
import Exe_Z.util.Log;
import Exe_Z.util.NinjaUtils;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Admin
 */
public class SevenBeasts extends World {

    private ArrayList<Integer> listCharId;

    @Getter
    @Setter
    private byte mType;

    public SevenBeasts(int countDown) {
        setType(World.SEVEN_BEASTS);
        this.name = "SevenBeasts";
        generateId();
        this.listCharId = new ArrayList<>();
        this.countDown = countDown;
        initZone();
        initFinished = true;
    }

    public void addCharId(int id) {
        synchronized (listCharId) {
            if (listCharId.indexOf(id) == -1) {
                listCharId.add(id);
            }
        }
    }

    public boolean isInSevenBeasts(int id) {
        synchronized (listCharId) {
            return listCharId.indexOf(id) != -1;
        }
    }

    public void initZone() {
        Map map = MapManager.getInstance().find(MapName.KHU_VUC_CHO);
        Z7Beasts waitingArea = new WaitingArea(0, map.tilemap, map);
        waitingArea.setWorld(this);
        addZone(waitingArea);
        map = MapManager.getInstance().find(MapName.THAT_THU_AI);
        Z7Beasts beastArea = new BeastArea(0, map.tilemap, map);
        beastArea.setWorld(this);
        addZone(beastArea);
    }

    public void join(Char p) {
        zones.get(0).join(p);
    }

    public void close() {
        if (this.isClosed) {
            return;
        }
        List<Char> members = getMembers();
        for (Char _char : members) {
            try {
                short[] xy = NinjaUtils.getXY(_char.mapBeforeEnterPB);
                _char.setXY(xy[0], xy[1]);
                _char.changeMap(_char.mapBeforeEnterPB);
                _char.serverMessage("Thất thú ải đã khép lại.");
                _char.removeWorld(World.SEVEN_BEASTS);
            } catch (Exception e) {
                Log.error("player leave map err", e);
            }
        }
        MapManager.getInstance().removeSevenBeasts(this);
        super.close();
    }

    @Override
    public boolean enterWorld(Zone pre, Zone next) {
        return !pre.tilemap.isThatThuAi() && next.tilemap.isThatThuAi();
    }

    @Override
    public boolean leaveWorld(Zone pre, Zone next) {
        return pre.tilemap.isThatThuAi() && !next.tilemap.isThatThuAi();
    }
}
