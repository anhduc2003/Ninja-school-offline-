package Exe_Z.server;

import Exe_Z.db.jdbc.DbManager;
import Exe_Z.mob.Mob;
import Exe_Z.util.Log;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Reads world-boss notification rules from the panel tables and broadcasts them
 * through the same server-wide chat channel used by the game.
 */
public final class WorldBossNotificationService {

    private static final Map<Integer, Long> LAST_SENT_AT = new ConcurrentHashMap<>();

    private WorldBossNotificationService() {
    }

    public static boolean notifySpawn(SpawnBoss spawnBoss, Mob mob, Exe_Z.map.zones.Zone zone) {
        return notifyEvent(spawnBoss, mob, zone, "spawn", "Boss " + mob.template.name + " đã xuất hiện ở " + zone.tilemap.name + " khu " + zone.id, "");
    }

    public static void notifyDefeat(Mob mob, Exe_Z.model.Char killer) {
        SpawnBoss spawnBoss = SpawnBossManager.getInstance().findByMonster(mob);
        notifyDefeat(spawnBoss, mob, killer);
    }

    public static void notifyDefeat(SpawnBoss spawnBoss, Mob mob, Exe_Z.model.Char killer) {
        if (spawnBoss == null || mob == null || spawnBoss.getNotificationGroup() == null) {
            return;
        }
        String killerName = killer == null ? "một dũng sĩ" : killer.name;
        notifyEvent(spawnBoss, mob, mob.zone, "defeat", "Boss " + mob.template.name + " đã bị hạ bởi " + killerName, killerName);
    }

    private static boolean notifyEvent(SpawnBoss spawnBoss, Mob mob, Exe_Z.map.zones.Zone zone, String eventType, String fallback, String killerName) {
        if (spawnBoss == null || mob == null || zone == null || spawnBoss.getNotificationGroup() == null) {
            return false;
        }
        try (Connection connection = DbManager.getInstance().getConnection(DbManager.GAME);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id, sender, message_template, cooldown_seconds FROM panel_world_boss_notifications "
                     + "WHERE boss_group = ? AND event_type = ? AND enabled = 1 AND (server_id = 0 OR server_id = ?) "
                     + "AND (starts_at IS NULL OR starts_at <= NOW()) AND (ends_at IS NULL OR ends_at > NOW()) "
                     + "ORDER BY server_id DESC, id DESC LIMIT 1")) {
            statement.setString(1, spawnBoss.getNotificationGroup());
            statement.setString(2, eventType);
            statement.setInt(3, Config.getInstance().getServerID());
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return false;
                }
                int configId = result.getInt("id");
                int cooldown = Math.max(0, result.getInt("cooldown_seconds"));
                long now = System.currentTimeMillis();
                Long lastSent = LAST_SENT_AT.get(configId);
                if (lastSent != null && cooldown > 0 && now - lastSent < cooldown * 1000L) {
                    return true;
                }
                String sender = result.getString("sender");
                String template = result.getString("message_template");
                String text = render(template, mob, zone, killerName);
                GlobalService.getInstance().chat(sender, text);
                LAST_SENT_AT.put(configId, now);
                writeLog(connection, configId, eventType, mob, zone, text);
                return true;
            }
        } catch (Exception exception) {
            Log.debug("World boss notification unavailable: " + exception.getMessage());
            return false;
        }
    }

    public static int sendTest(int configId) throws Exception {
        try (Connection connection = DbManager.getInstance().getConnection(DbManager.GAME);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT sender, message_template FROM panel_world_boss_notifications WHERE id = ? AND enabled = 1 LIMIT 1")) {
            statement.setInt(1, configId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalArgumentException("Không tìm thấy cấu hình thông báo Boss đang bật.");
                }
                String text = result.getString("message_template");
                text = text.replace("{boss}", "Boss thế giới mẫu")
                        .replace("{map}", "Bản đồ mẫu")
                        .replace("{zone}", "0")
                        .replace("{killer}", "Dũng sĩ mẫu")
                        .replace("{time}", "12:00");
                GlobalService.getInstance().chat(result.getString("sender"), text);
                return ServerManager.getNumberOnline();
            }
        }
    }

    private static String render(String template, Mob mob, Exe_Z.map.zones.Zone zone, String killerName) {
        return template.replace("{boss}", mob.template.name)
                .replace("{map}", zone.tilemap.name)
                .replace("{zone}", String.valueOf(zone.id))
                .replace("{killer}", killerName == null ? "một dũng sĩ" : killerName)
                .replace("{time}", java.time.LocalTime.now().withNano(0).toString());
    }

    private static void writeLog(Connection connection, int configId, String eventType, Mob mob, Exe_Z.map.zones.Zone zone, String text) {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO panel_world_boss_notification_logs (config_id, event_type, boss_name, map_name, zone_id, message, online_players) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            statement.setInt(1, configId);
            statement.setString(2, eventType);
            statement.setString(3, mob.template.name);
            statement.setString(4, zone.tilemap.name);
            statement.setInt(5, zone.id);
            statement.setString(6, text);
            statement.setInt(7, ServerManager.getNumberOnline());
            statement.executeUpdate();
        } catch (Exception exception) {
            Log.debug("World boss notification log unavailable: " + exception.getMessage());
        }
    }
}
