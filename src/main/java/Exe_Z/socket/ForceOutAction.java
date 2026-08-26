/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Exe_Z.socket;

import Exe_Z.model.User;
import Exe_Z.server.ServerManager;
import Exe_Z.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import Exe_Z.network.Service;
import Exe_Z.server.Config;

/**
 *
 * @author PC
 */
public class ForceOutAction implements IAction {

    @Override
    public void call(JSONObject json) {
        try {
            int userId = json.getInt("user_id");
            if (json.has("current_server")) {
                int currentServer = json.getInt("current_server");
                if (currentServer == Config.getInstance().getServerID()) { // ignore current server
                    return;
                }
            }
            User user = ServerManager.findUserByUserID(userId);
            if (user != null && user.sltChar != null) {
                if (!user.isCleaned) {
                    ((Service) user.session.getService()).serverDialog("Có người đăng nhập vào tài khoản của bạn.");
                    user.session.disconnect();
                }
            }
        } catch (JSONException ex) {
            Log.error("Error get socket", ex);
        }
    }

}