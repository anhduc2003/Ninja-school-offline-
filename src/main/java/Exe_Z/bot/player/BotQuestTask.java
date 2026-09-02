package Exe_Z.bot.player;

import Exe_Z.constants.MapName;
import Exe_Z.constants.TaskName;
import Exe_Z.map.Map;
import Exe_Z.map.MapManager;
import Exe_Z.map.zones.Zone;
import Exe_Z.mob.Mob;
import Exe_Z.model.Char;
import Exe_Z.task.Task;
import Exe_Z.task.TaskOrder;
import Exe_Z.util.NinjaUtils;

import java.util.ArrayList;
import java.util.List;

public class BotQuestTask extends BotTask {

    private Task mainTask;
    private TaskOrder sideTask;
    private List<Integer> completedQuests = new ArrayList<>();
    private long nextMoveTime = 0;
    private long nextAttackTime = 0;
    private static final long MOVE_DELAY = 2000;
    private static final long ATTACK_DELAY = 1500;

    public BotQuestTask() {
        super("Questing", 10);
    }

    @Override
    public boolean canExecute(Char botChar) {
        return botChar != null && !botChar.isDead && botChar.hp > 0;
    }

    @Override
    public boolean execute(Char botChar, long now) {
        if (botChar == null) {
            return false;
        }
        if (botChar.taskMain != null && mainTask == null) {
            mainTask = botChar.taskMain;
        }
        if (sideTask == null && botChar.taskOrders != null && !botChar.taskOrders.isEmpty()) {
            sideTask = botChar.taskOrders.get(0);
        }

        if (mainTask != null && mainTask.isComplete()) {
            botChar.updateTask();
            mainTask = botChar.taskMain;
            return true;
        }
        if (sideTask != null && sideTask.isComplete()) {
            sideTask.updateTask(999);
            sideTask = botChar.taskOrders != null && !botChar.taskOrders.isEmpty()
                    ? botChar.taskOrders.get(0) : null;
            return true;
        }

        int questMapId = getQuestMapId(botChar);
        if (questMapId > 0 && questMapId != botChar.mapId) {
            if (now >= nextMoveTime) {
                nextMoveTime = now + MOVE_DELAY;
                botChar.changeMap(questMapId);
                return true;
            }
        } else if (questMapId == botChar.mapId && now >= nextAttackTime) {
            nextAttackTime = now + ATTACK_DELAY;
            if (attackNearestMob(botChar)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isComplete(Char botChar) {
        return mainTask == null && sideTask == null;
    }

    public boolean hasActiveQuest() {
        return mainTask != null || sideTask != null;
    }

    public String getQuestStatus(Char botChar) {
        if (mainTask != null) {
            return "Main quest step: " + mainTask.index + " (map: " + getQuestMapId(botChar) + ")";
        }
        if (sideTask != null) {
            return "Side quest: " + sideTask.count + "/" + sideTask.maxCount;
        }
        return "No active quest";
    }

    private int getQuestMapId(Char botChar) {
        if (botChar.taskId <= 0) {
            return -1;
        }
        if (sideTask != null && sideTask.mapId > 0) {
            return sideTask.mapId;
        }
        if (mainTask == null) {
            return -1;
        }
        int mapId = QuestMapRegistry.getMapId(botChar.taskId, mainTask.index);
        if (mapId > 0) {
            return mapId;
        }
        return QuestMapRegistry.getMapId(botChar.taskId, -1);
    }

    private boolean attackNearestMob(Char botChar) {
        if (botChar.zone == null) {
            return false;
        }
        Mob target = null;
        double minDist = Double.MAX_VALUE;
        List<Mob> mobs = botChar.zone.getLivingMonsters();
        if (mobs == null || mobs.isEmpty()) {
            return false;
        }
        short[] questMobs = null;
        if (mainTask != null && mainTask.template != null) {
            short[][] allMobs = mainTask.template.getMobs();
            if (allMobs != null && mainTask.index < allMobs.length) {
                questMobs = allMobs[mainTask.index];
            }
        }
        for (Mob mob : mobs) {
            if (mob == null || mob.isDead || mob.hp <= 0) {
                continue;
            }
            if (questMobs != null && questMobs.length > 0 && questMobs[0] > 0) {
                if (mob.template == null || mob.template.id != questMobs[0]) {
                    continue;
                }
            }
            double dist = Math.hypot(mob.x - botChar.x, mob.y - botChar.y);
            if (dist < minDist && dist < 500) {
                minDist = dist;
                target = mob;
            }
        }
        if (target != null) {
            try {
                Exe_Z.network.Message ms = new Exe_Z.network.Message((byte) 0);
                ms.writer().writeByte(target.id);
                botChar.attackMonster(ms);
                ms.cleanup();
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }
}
