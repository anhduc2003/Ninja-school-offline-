package Exe_Z.bot.player;

import Exe_Z.constants.MapName;
import Exe_Z.db.jdbc.DbManager;
import Exe_Z.item.Equip;
import Exe_Z.item.Item;
import Exe_Z.item.Mount;
import Exe_Z.map.MapManager;
import Exe_Z.map.zones.Zone;
import Exe_Z.model.Char;
import Exe_Z.model.User;
import Exe_Z.network.Message;
import Exe_Z.network.NoService;
import Exe_Z.server.ServerManager;
import Exe_Z.util.Log;
import Exe_Z.util.NinjaUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BotPlayer {

    private final int charId;
    private Char botChar;
    private User user;
    private BotPlayerAI ai;
    private boolean active = false;
    private long lastSave = 0;
    private static final long SAVE_INTERVAL = 60000;

    public BotPlayer(int charId) {
        this.charId = charId;
    }

    public boolean start() {
        try {
            botChar = new Char(charId);
            if (!botChar.load()) {
                Log.error("BotPlayer: cannot load char " + charId);
                return false;
            }
            botChar.isBot = true;
            botChar.isHuman = false;
            user = new User(null, "bot_" + charId, "", "");
            user.gold = 999999;
            user.tongnap = 0;
            user.activated = 1;
            botChar.user = user;
            botChar.setService(NoService.getInstance());
            botChar.hp = botChar.maxHP;
            botChar.mp = botChar.maxMP;
            botChar.isDead = false;
            if (botChar.head < 0) {
                botChar.head = (short) (botChar.gender == 0 ? 28 : 26);
            }
            if (botChar.taskMain == null) {
                if (botChar.taskId <= 0) {
                    botChar.taskId = 1;
                }
                try {
                    botChar.takingTask();
                } catch (Exception e) {
                    Log.error("BotPlayer takingTask failed for char " + charId + ": " + e.getMessage());
                }
            }
            int map = botChar.mapId;
            int zoneId = NinjaUtils.randomZoneId(map);
            if (zoneId == -1) {
                map = botChar.saveCoordinate > 0 ? botChar.saveCoordinate : MapName.LANG_TONE;
                zoneId = NinjaUtils.randomZoneId(map);
                if (zoneId == -1) {
                    zoneId = 0;
                }
                short[] xy = NinjaUtils.getXY(map);
                if (xy != null) {
                    botChar.setXY(xy[0], xy[1]);
                }
                botChar.mapId = (short) map;
            }
            ServerManager.addChar(botChar);
            MapManager.getInstance().joinZone(botChar, map, zoneId);
            ai = new BotPlayerAI(this);
            active = true;
            lastSave = System.currentTimeMillis();
            setOnlineInDb(true);
            Log.info("BotPlayer started: char=" + botChar.name + " map=" + map + " zone=" + zoneId + " head=" + botChar.head);
            return true;
        } catch (Exception e) {
            Log.error("BotPlayer start error", e);
            return false;
        }
    }

    public void update(long now) {
        if (!active || botChar == null || botChar.isDead) {
            return;
        }
        if (ai != null) {
            ai.update(now);
        }
        wander(now);
        if (now - lastSave > SAVE_INTERVAL) {
            save();
            lastSave = now;
        }
    }

    private long lastMoveTime = 0;
    private short targetX = -1;
    private short targetY = -1;

    public void wander(long now) {
        if (botChar == null || botChar.zone == null) {
            return;
        }
        if (now - lastMoveTime < 3000) {
            return;
        }
        lastMoveTime = now;
        if (targetX < 0 || Math.abs(botChar.x - targetX) < 20) {
            targetX = (short) (100 + NinjaUtils.nextInt(500));
            targetY = (short) (100 + NinjaUtils.nextInt(300));
        }
        botChar.x = (short) (botChar.x + (targetX > botChar.x ? 5 : -5));
        botChar.y = (short) (botChar.y + (targetY > botChar.y ? 3 : -3));
    }

    public void stop() {
        active = false;
        if (ai != null) {
            ai.setRunning(false);
        }
        setOnlineInDb(false);
        save();
        if (botChar != null) {
            botChar.outZone();
            ServerManager.removeChar(botChar);
        }
        Log.info("BotPlayer stopped: char=" + (botChar != null ? botChar.name : "null"));
    }

    private void setOnlineInDb(boolean online) {
        if (botChar == null) {
            return;
        }
        try {
            java.sql.Connection conn = DbManager.getInstance().getConnection(DbManager.GAME);
            PreparedStatement stmt = conn.prepareStatement(
                "UPDATE `players` SET `online` = ? WHERE `id` = ? LIMIT 1;");
            stmt.setInt(1, online ? 1 : 0);
            stmt.setInt(2, botChar.id);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            Log.error("BotPlayer setOnline error", e);
        }
    }

    private void save() {
        if (botChar == null) {
            return;
        }
        try {
            java.sql.Connection conn = DbManager.getInstance().getConnection(DbManager.GAME);
            PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE `players` SET `hp`=?,`mp`=?,`x`=?,`y`=?,`map_id`=?,`task_id`=? WHERE `id`=?;");
            stmt.setInt(1, botChar.hp);
            stmt.setInt(2, botChar.mp);
            stmt.setShort(3, botChar.x);
            stmt.setShort(4, botChar.y);
            stmt.setShort(5, botChar.mapId);
            stmt.setShort(6, botChar.taskId);
            stmt.setInt(7, botChar.id);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            Log.error("BotPlayer save error", e);
        }
    }

    public Char getChar() {
        return botChar;
    }

    public boolean isActive() {
        return active;
    }

    public int getCharId() {
        return charId;
    }

    public String getCharName() {
        return botChar != null ? botChar.name : "null";
    }
}
