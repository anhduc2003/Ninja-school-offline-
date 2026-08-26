package Exe_Z.server;


import Exe_Z.clan.Clan;
import Exe_Z.model.Char;
import Exe_Z.stall.StallManager;
import Exe_Z.util.Log;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author ADMIN
 */
public class Autoload {

    private static final long INITIAL_DELAY = 0;
    private static final long PERIOD = 1 * 60 * 1000; // 1 minute

    public static void start() {
        Autoload autoload = new Autoload();
        autoload.startAutoload();
    }

    public void startAutoload() {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(this::autoSave, INITIAL_DELAY, PERIOD, TimeUnit.MILLISECONDS);
    }

    private void autoSave() {
        if (Server.start) {
            Log.info("Bắt Đàu Lưu Dữ Liệu");
            saveShinwa();
            saveClan();
            updateRank();
            savePlayer();
            Log.info("Lưu Dữ Liệu xong ");
//        } else {
//            Log.info("Máy chủ chưa bật");
        }
    }

    private void saveShinwa() {
        StallManager.getInstance().save();
    }

    private void saveClan() {
        List<Clan> clans = Clan.getClanDAO().getAll();
        for (Clan clan : clans) {
            Clan.getClanDAO().update(clan);
        }
    }

    private void updateRank() {
        List<Char> chars = ServerManager.getChars();
        for (Char _char : chars) {
            _char.saveData();
        }
        Ranked.refresh();
    }

    private void savePlayer() {
        try {
            List<Char> chars = ServerManager.getChars();
            for (Char _char : chars) {
                try {
                    if (_char != null && !_char.isCleaned) {
                        _char.saveData();
                    }
                    if (_char.user != null && !_char.user.isCleaned) {
                        _char.user.saveData();
                    }
                    if (_char.clone != null && !_char.clone.isCleaned) {
                        _char.clone.saveData();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Ranked.refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
