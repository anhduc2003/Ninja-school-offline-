/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Exe_Z.map.world;

import Exe_Z.constants.CMD;
import Exe_Z.model.Char;
import Exe_Z.network.AbsService;
import Exe_Z.network.Message;
import Exe_Z.util.Log;
import java.io.DataOutputStream;
import java.util.List;

/**
 *
 * @author Admin
 */
public class WorldService extends AbsService {

    private World world;

    public WorldService(World world) {
        this.world = world;
    }

    public void sendTimeInMap(int timeCountDown) {
        try {
            Message ms = messageSubCommand(CMD.MAP_TIME);
            DataOutputStream ds = ms.writer();
            ds.writeInt(timeCountDown);
            ds.flush();
            sendMessage(ms);
            ms.cleanup();
        } catch (Exception e) {

        }
    }

    @Override
    public void chat(String name, String text) {

    }

    @Override
    public void sendMessage(Message ms) {
        List<Char> members = world.getMembers();
        for (Char _char : members) {
            try {
                _char.getService().sendMessage(ms);
            } catch (Exception e) {
                Log.error("worldService sendMessage ex: " + e.getMessage(), e);
            }
        }
    }

}
