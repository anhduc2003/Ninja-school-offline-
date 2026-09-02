package Exe_Z.bot.player;

import Exe_Z.map.Map;
import Exe_Z.map.MapManager;
import Exe_Z.map.zones.Zone;
import Exe_Z.mob.Mob;
import Exe_Z.model.Char;
import Exe_Z.network.Message;
import Exe_Z.util.Log;
import Exe_Z.util.NinjaUtils;

import java.util.List;

public class BotBossHuntTask extends BotTask {

    private int currentBossTemplateId = -1;
    private int currentBossMapId = -1;
    private int currentZoneId = -1;
    private long nextSearchTime = 0;
    private static final long SEARCH_INTERVAL = 30000;

    public BotBossHuntTask() {
        super("BossHunt", 5);
    }

    @Override
    public boolean canExecute(Char botChar) {
        return botChar != null && !botChar.isDead && botChar.level >= 20;
    }

    @Override
    public boolean execute(Char botChar, long now) {
        if (now < nextSearchTime) {
            return false;
        }
        nextSearchTime = now + SEARCH_INTERVAL + NinjaUtils.nextInt(10000);

        if (currentBossTemplateId == -1) {
            findNextBoss(botChar, now);
            return false;
        }

        if (isBossAlive(currentBossTemplateId)) {
            if (botChar.mapId != currentBossMapId) {
                botChar.changeMap(currentBossMapId);
                return true;
            }
            Map map = MapManager.getInstance().find(currentBossMapId);
            if (map == null || map.getZones() == null) {
                currentBossTemplateId = -1;
                return false;
            }
            List<Zone> zones = map.getZones();
            int zoneIdx = currentZoneId % zones.size();
            Zone zone = zones.get(zoneIdx);
            Mob targetMob = findBossInZone(zone, currentBossTemplateId);
            if (targetMob != null) {
                try {
                    Message ms = new Message((byte) 0);
                    ms.writer().writeByte(targetMob.id);
                    botChar.attackMonster(ms);
                    ms.cleanup();
                } catch (Exception e) {
                    Log.error("BotBossHunt attack error", e);
                }
                return true;
            }
        } else {
            currentBossTemplateId = -1;
            currentBossMapId = -1;
            currentZoneId = -1;
        }

        return false;
    }

    @Override
    public boolean isComplete(Char botChar) {
        return currentBossTemplateId == -1;
    }

    private void findNextBoss(Char botChar, long now) {
        for (Map map : MapManager.getInstance().getMaps()) {
            if (map == null || map.getZones() == null) {
                continue;
            }
            for (Zone zone : map.getZones()) {
                if (zone == null || zone.monsters == null) {
                    continue;
                }
                for (Mob mob : zone.monsters) {
                    if (mob != null && mob.isBoss && !mob.isDead) {
                        currentBossTemplateId = mob.template.id;
                        currentBossMapId = map.id;
                        currentZoneId = zone.id;
                        return;
                    }
                }
            }
        }
    }

    private boolean isBossAlive(int templateId) {
        for (Map map : MapManager.getInstance().getMaps()) {
            if (map == null || map.getZones() == null) {
                continue;
            }
            for (Zone zone : map.getZones()) {
                if (zone == null || zone.monsters == null) {
                    continue;
                }
                for (Mob mob : zone.monsters) {
                    if (mob != null && mob.template != null && mob.template.id == templateId && !mob.isDead) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private Mob findBossInZone(Zone zone, int templateId) {
        if (zone == null || zone.monsters == null) {
            return null;
        }
        for (Mob mob : zone.monsters) {
            if (mob != null && mob.template != null && mob.template.id == templateId && !mob.isDead) {
                return mob;
            }
        }
        return null;
    }

    public int getCurrentBossTemplateId() {
        return currentBossTemplateId;
    }

    public int getCurrentBossMapId() {
        return currentBossMapId;
    }
}
