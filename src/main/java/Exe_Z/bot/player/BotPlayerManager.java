package Exe_Z.bot.player;

import Exe_Z.server.Config;
import Exe_Z.util.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
                if (idx < bots.size()) {
                    BotPlayer bot = bots.get(idx);
                    if (bot != null && bot.isActive()) {
                        bot.update(System.currentTimeMillis());
                    }
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

    public BotPlayer getBotByCharId(int charId) {
        for (BotPlayer bot : bots) {
            if (bot.getCharId() == charId) {
                return bot;
            }
        }
        return null;
    }

    public List<BotStatus> getBotStatuses() {
        List<BotStatus> statuses = new ArrayList<>();
        for (BotPlayer bot : bots) {
            statuses.add(new BotStatus(
                bot.getCharId(),
                bot.getCharName(),
                bot.isActive(),
                bot.getChar() != null ? bot.getChar().mapId : -1,
                bot.getChar() != null ? bot.getChar().hp : 0,
                bot.getChar() != null ? bot.getChar().maxHP : 0,
                bot.getChar() != null ? bot.getChar().mp : 0,
                bot.getChar() != null ? bot.getChar().maxMP : 0
            ));
        }
        return statuses;
    }

    public boolean startBot(int charId) {
        BotPlayer existing = getBotByCharId(charId);
        if (existing != null) {
            if (!existing.isActive()) {
                existing.start();
                return true;
            }
            return false;
        }
        BotPlayer bot = new BotPlayer(charId);
        boolean started = bot.start();
        if (started) {
            bots.add(bot);
        }
        return started;
    }

    public void stopBot(int charId) {
        BotPlayer bot = getBotByCharId(charId);
        if (bot != null) {
            bot.stop();
        }
    }

    public boolean addAndStartBot(int charId) {
        BotPlayer existing = getBotByCharId(charId);
        if (existing != null) {
            if (!existing.isActive()) {
                existing.start();
                return true;
            }
            return false;
        }
        BotPlayer bot = new BotPlayer(charId);
        boolean started = bot.start();
        if (started) {
            bots.add(bot);
        }
        return started;
    }

    public void removeBotByCharId(int charId) {
        BotPlayer bot = getBotByCharId(charId);
        if (bot != null) {
            bot.stop();
            bots.remove(bot);
        }
    }

    public void loadFromDb() {
        try (Connection conn = Exe_Z.db.jdbc.DbManager.getInstance().getConnection(Exe_Z.db.jdbc.DbManager.GAME);
             PreparedStatement stmt = conn.prepareStatement("SELECT char_id FROM panel_bots WHERE enabled = 1");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int charId = rs.getInt("char_id");
                if (getBotByCharId(charId) == null) {
                    BotPlayer bot = new BotPlayer(charId);
                    boolean started = bot.start();
                    if (started) {
                        bots.add(bot);
                        Log.info("Loaded bot from DB: charId=" + charId);
                    }
                }
            }
        } catch (SQLException e) {
            Log.error("BotPlayerManager loadFromDb error", e);
        }
    }

    public static class BotStatus {
        public final int charId;
        public final String name;
        public final boolean active;
        public final int mapId;
        public final int hp;
        public final int maxHp;
        public final int mp;
        public final int maxMp;

        public BotStatus(int charId, String name, boolean active, int mapId, int hp, int maxHp, int mp, int maxMp) {
            this.charId = charId;
            this.name = name;
            this.active = active;
            this.mapId = mapId;
            this.hp = hp;
            this.maxHp = maxHp;
            this.mp = mp;
            this.maxMp = maxMp;
        }
    }
}
