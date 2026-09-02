package Exe_Z.bot.player;

import Exe_Z.server.Config;
import Exe_Z.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BotPlayerManager {

    private static final BotPlayerManager instance = new BotPlayerManager();
    private final List<BotPlayer> bots = new ArrayList<>();
    private ScheduledExecutorService scheduler;
    private boolean running = false;

    public static BotPlayerManager getInstance() {
        return instance;
    }

    public void start() {
        if (running) {
            return;
        }
        int botCount = Config.getInstance().getBotPlayerCount();
        if (botCount <= 0) {
            Log.info("BotPlayerManager: no bots configured");
            return;
        }
        scheduler = Executors.newScheduledThreadPool(Math.min(botCount, 10));
        for (int i = 0; i < botCount; i++) {
            final int idx = i;
            scheduler.scheduleAtFixedRate(() -> {
                BotPlayer bot = bots.get(idx % bots.size());
                if (bot != null && bot.isActive()) {
                    bot.update(System.currentTimeMillis());
                }
            }, 1 + i, 1, TimeUnit.SECONDS);
        }
        running = true;
        Log.info("BotPlayerManager started with " + botCount + " bots");
    }

    public void stop() {
        running = false;
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
        for (BotPlayer bot : bots) {
            bot.stop();
        }
        bots.clear();
        Log.info("BotPlayerManager stopped");
    }

    public void addBot(BotPlayer bot) {
        bots.add(bot);
    }

    public void removeBot(BotPlayer bot) {
        bots.remove(bot);
    }

    public List<BotPlayer> getBots() {
        return bots;
    }

    public int getActiveBotCount() {
        int count = 0;
        for (BotPlayer bot : bots) {
            if (bot.isActive()) {
                count++;
            }
        }
        return count;
    }

    public void startAll() {
        for (BotPlayer bot : bots) {
            bot.start();
        }
    }
}
