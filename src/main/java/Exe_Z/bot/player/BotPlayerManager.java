package Exe_Z.bot.player;

import Exe_Z.db.jdbc.DbManager;
import Exe_Z.server.Config;
import Exe_Z.util.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BotPlayerManager {

    private static final BotPlayerManager instance = new BotPlayerManager();
    private final List<BotPlayer> bots = new CopyOnWriteArrayList<>();
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
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            for (BotPlayer bot : bots) {
                if (bot != null && bot.isActive()) {
                    try {
                        bot.update(now);
                    } catch (Exception e) {
                        Log.error("BotPlayerManager update error: charId=" + bot.getCharId(), e);
                    }
                }
            }
        }, 1, 1, TimeUnit.SECONDS);
        if (botCount > 0) {
            loadAndAutoCreateBots(botCount);
        }
        running = true;
        Log.info("BotPlayerManager started with " + botCount + " bots (active=" + getActiveBotCount() + ")");
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
        if (bot != null && getBotByCharId(bot.getCharId()) == null) {
            bots.add(bot);
        }
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
            if (!bot.isActive()) {
                bot.start();
            }
        }
    }

    public void startAllBots() {
        int started = 0;
        int failed = 0;
        try (Connection conn = DbManager.getInstance().getConnection(DbManager.GAME);
             PreparedStatement stmt = conn.prepareStatement("SELECT char_id FROM panel_bots WHERE enabled = 1")) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int charId = rs.getInt("char_id");
                    if (getBotByCharId(charId) == null) {
                        BotPlayer bot = new BotPlayer(charId);
                        boolean ok = bot.start();
                        if (ok) {
                            bots.add(bot);
                            started++;
                        } else {
                            failed++;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            Log.error("BotPlayerManager startAllBots error", e);
        }
        Log.info("BotPlayerManager startAllBots: started=" + started + ", failed=" + failed);
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

    private void loadAndAutoCreateBots(int botCount) {
        Set<Integer> usedCharIds = new HashSet<>();
        List<BotPlayer> loadedBots = new ArrayList<>();
        
        // Load existing bots from panel_bots
        try (Connection conn = DbManager.getInstance().getConnection(DbManager.GAME)) {
            // Load all bot configs to know which chars are already used
            try (PreparedStatement stmt = conn.prepareStatement("SELECT char_id, enabled FROM panel_bots");
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    usedCharIds.add(rs.getInt("char_id"));
                }
            }
            
            // Start enabled bots
            try (PreparedStatement stmt = conn.prepareStatement("SELECT char_id FROM panel_bots WHERE enabled = 1");
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int charId = rs.getInt("char_id");
                    if (getBotByCharId(charId) == null) {
                        BotPlayer bot = new BotPlayer(charId);
                        boolean started = bot.start();
                        if (started) {
                            bots.add(bot);
                            loadedBots.add(bot);
                            Log.info("Loaded bot from DB: charId=" + charId);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            Log.error("BotPlayerManager loadFromDb error", e);
        }
        
        // Auto-create missing bots if panel_bots has fewer than botCount
        if (bots.size() < botCount) {
            autoCreateMissingBots(botCount, usedCharIds);
        }
    }

    private void autoCreateMissingBots(int targetCount, Set<Integer> usedCharIds) {
        int needed = targetCount - bots.size();
        if (needed <= 0) return;
        
        List<Integer> availableCharIds = new ArrayList<>();
        try (Connection conn = DbManager.getInstance().getConnection(DbManager.GAME)) {
            // Find characters not already used as bots
            String sql = "SELECT id FROM players WHERE id NOT IN (SELECT char_id FROM panel_bots) ORDER BY id ASC LIMIT ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, needed);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        availableCharIds.add(rs.getInt("id"));
                    }
                }
            }
        } catch (SQLException e) {
            Log.error("BotPlayerManager autoCreateMissingBots query error", e);
            return;
        }
        
        if (availableCharIds.isEmpty()) {
            Log.warn("BotPlayerManager: no available characters for auto-creation");
            return;
        }
        
        int created = 0;
        for (Integer charId : availableCharIds) {
            if (created >= needed) break;
            
            // Create bot entry in panel_bots
            try (Connection conn = DbManager.getInstance().getConnection(DbManager.GAME);
                 PreparedStatement stmt = conn.prepareStatement("INSERT INTO panel_bots (char_id, name, enabled) VALUES (?, ?, 1)")) {
                stmt.setInt(1, charId);
                stmt.setString(2, "bot_" + charId);
                stmt.executeUpdate();
            } catch (SQLException e) {
                Log.error("BotPlayerManager auto-create insert error: charId=" + charId, e);
                continue;
            }
            
            // Start the bot
            BotPlayer bot = new BotPlayer(charId);
            boolean started = bot.start();
            if (started) {
                bots.add(bot);
                created++;
                Log.info("Auto-created bot: charId=" + charId);
            }
        }
        
        if (created > 0) {
            Log.info("Auto-created " + created + " bots (target=" + targetCount + ")");
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
