package Exe_Z.bot.player;

import Exe_Z.bot.Bot;
import Exe_Z.map.MapManager;
import Exe_Z.model.Char;
import Exe_Z.util.Log;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BotPlayerAI {

    private final BotPlayer botPlayer;
    private final List<BotTask> tasks = new ArrayList<>();
    private BotTask currentTask = null;
    private long lastUpdate = 0;
    private static final long UPDATE_INTERVAL = 500;
    private long nextActionDelay = 0;
    private boolean running = true;

    public BotPlayerAI(BotPlayer botPlayer) {
        this.botPlayer = botPlayer;
        tasks.add(new BotRestTask());
        tasks.add(new BotBossHuntTask());
        tasks.add(new BotQuestTask());
        tasks.add(new BotLootTask());
    }

    public void update(long now) {
        if (!running || botPlayer.getChar() == null || botPlayer.getChar().isDead) {
            return;
        }
        if (now < lastUpdate + UPDATE_INTERVAL) {
            return;
        }
        lastUpdate = now;

        if (now < nextActionDelay) {
            return;
        }

        Char botChar = botPlayer.getChar();
        if (botChar == null || botChar.isDead) {
            return;
        }

        tasks.sort(Comparator.comparingInt(BotTask::getPriority).reversed());
        for (BotTask task : tasks) {
            if (!task.isActive()) {
                continue;
            }
            if (!task.canExecute(botChar)) {
                continue;
            }
            if (currentTask != null && !currentTask.isComplete(botChar)) {
                if (task.getPriority() > currentTask.getPriority()) {
                    currentTask = task;
                }
            } else {
                currentTask = task;
            }
            if (currentTask != null && currentTask.execute(botChar, now)) {
                nextActionDelay = now + 200 + (long) (Math.random() * 800);
                return;
            }
        }
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean isRunning() {
        return running;
    }

    public String getStatus() {
        if (currentTask == null) {
            return "Idle";
        }
        return currentTask.getName();
    }

    public List<BotTask> getTasks() {
        return tasks;
    }
}
