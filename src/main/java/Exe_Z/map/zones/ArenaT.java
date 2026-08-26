/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Exe_Z.map.zones;

import Exe_Z.bot.Bot;
import Exe_Z.map.Map;
import Exe_Z.map.TileMap;
import Exe_Z.model.Char;
import Exe_Z.thiendia.Ranking;
import Exe_Z.thiendia.ThienDiaData;
import Exe_Z.util.NinjaUtils;
import lombok.Setter;

/**
 *
 * @author Admin
 */
public class ArenaT extends Zone {

    @Setter
    private Bot bot;
    @Setter
    private Char player;
    @Setter
    private Ranking ranking1, ranking2;
    @Setter
    private ThienDiaData thienDiaData;

    public ArenaT(int id, TileMap tilemap, Map map) {
        super(id, tilemap, map);
    }

    public void setWin(boolean win) {
        if (ranking1 != null && ranking2 != null && thienDiaData != null) {
            ranking1.setFighting(false);
            ranking2.setFighting(false);
            if (win) {
                if (ranking1.getRanked() < ranking2.getRanked()) {
                    int temp = ranking1.getRanked();
                    ranking1.setRanked(ranking2.getRanked());
                    ranking2.setRanked(temp);
                }
                thienDiaData.sort();
            }
        }
        if (bot != null) {
            bot.setArenaT(null);
            bot.outZone();
            bot = null;
        }
        if (player != null) {
            player.setArenaT(null);
            short[] xy = NinjaUtils.getXY(player.mapBeforeEnterPB);
            player.setXY(xy[0], xy[1]);
            player.changeMap(player.mapBeforeEnterPB);
            if (win) {
                player.getService().serverMessage(String.format("Bạn đã thắng và được thăng lên hạng %d", ranking2.getRanked()));
            } else {
                player.countArenaT = 0;
                player.getService().serverMessage("Bạn đã thua");
            }
            player = null;
        }

        close();
    }

}
