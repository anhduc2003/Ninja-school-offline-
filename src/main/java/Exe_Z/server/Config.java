package Exe_Z.server;

import Exe_Z.util.Log;
import Exe_Z.util.StringUtils;
import Exe_Z.constants.SQLStatement;

import java.io.File;
import java.io.FileInputStream;
import Exe_Z.db.jdbc.DbManager;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import lombok.Getter;

/**
 *
 * @author Administrator
 */
@Getter
public class Config {

    private static final Config instance = new Config();

    public static Config getInstance() {
        return instance;
    }

    // Version
    private int dataVersion = 26;
    private int itemVersion = 26;
    private int mapVersion = 26;
    private int skillVersion = 26;

    // Server
    private int serverID;
    private int port;

    // MySql
    private String dbHost;
    private int dbPort;
    private String dbUser;
    private String dbPassword;
    private String dbName;
    private String dbDriver;
    private int dbMinConnections;
    private int dbMaxConnections;
    private int ratenjtl;

    // MongoDB
    private String mongodbHost;
    private int mongodbPort;
    private String mongodbName;
    private String mongodbUser;
    private String mongodbPassword;

    // Socket.IO
    private String websocketHost;
    private int websocketPort;

    // Game
    private double maxPercentAdd;
    private int sale;
    private boolean showLog;
    private boolean shinwa;
    private int shinwaFee;
    private int auctionMax;
    private int shinwaMax;
    private boolean arena;
    private int ipAddressLimit;
    private int maxQuantity;
    private double expconf;
    private String serverDir;
    private String notification;
    private int messageSizeMax;
    private String event;
    private int EXP;
    private int choimoi;
    private int damemob;
    private int maxLv;
    private boolean historySQL;
    
    private int eventYear;
    private int eventMonth;
    private int eventDay;
    private int eventHour;
    private int eventMinute;
    private int eventSecond;

    
    public boolean load() {
        try {
            FileInputStream input = new FileInputStream(new File("config.properties"));
            Properties props = new Properties();
            props.load(new InputStreamReader(input, StandardCharsets.UTF_8));
            props.forEach((t, u) -> {
                Log.info(String.format("Config - %s: %s", t, u));
            });
            showLog = Boolean.parseBoolean(props.getProperty("server.log.display"));
            serverID = Integer.parseInt(props.getProperty("server.id"));
            port = Integer.parseInt(props.getProperty("server.port"));
            dbDriver = props.getProperty("db.driver");
            dbHost = props.getProperty("db.host");
            dbPort = Integer.parseInt(props.getProperty("db.port"));
            dbUser = props.getProperty("db.user");
            dbPassword = props.getProperty("db.password");
            dbName = props.getProperty("db.dbname");
            dbMaxConnections = Integer.parseInt(props.getProperty("db.maxconnections"));
            dbMinConnections = Integer.parseInt(props.getProperty("db.minconnections"));
            mongodbHost = props.getProperty("mongodb.host");
            mongodbPort = Integer.parseInt(props.getProperty("mongodb.port"));
            mongodbUser = props.getProperty("mongodb.user");
            mongodbPassword = props.getProperty("mongodb.password");
            mongodbName = props.getProperty("mongodb.dbname");
            websocketHost = props.getProperty("websocket.host");
            websocketPort = Integer.parseInt(props.getProperty("websocket.port"));
            maxPercentAdd = Integer.parseInt(props.getProperty("game.upgrade.percent.add"));
            sale = Integer.parseInt(props.getProperty("game.store.discount"));
            shinwa = Boolean.parseBoolean(props.getProperty("game.shinwa.active"));
            shinwaFee = Integer.parseInt(props.getProperty("game.shinwa.fee"));
            auctionMax = Integer.parseInt(props.getProperty("game.shinwa.max"));
            shinwaMax = Integer.parseInt(props.getProperty("game.shinwa.player.max"));
            arena = Boolean.parseBoolean(props.getProperty("game.arena.active"));
            ipAddressLimit = Integer.parseInt(props.getProperty("game.login.limit"));
            maxQuantity = Integer.parseInt(props.getProperty("game.quantity.display.max"));
            damemob = Integer.parseInt(props.getProperty("percent.damemob"));
            maxLv= Integer.parseInt(props.getProperty("game.maxLv"));
            if (props.containsKey("server.resources.dir")) {
                serverDir = props.getProperty("server.resources.dir");
            } else {
                serverDir = System.getProperty("user.dir");
            }
            if (props.containsKey("server.notification")) {
                notification = props.getProperty("server.notification");
            }
            if (props.containsKey("game.data.version")) {
                dataVersion = Integer.parseInt(props.getProperty("game.data.version"));
            }
            if (props.containsKey("game.item.version")) {
                itemVersion = Integer.parseInt(props.getProperty("game.item.version"));
            }
            if (props.containsKey("game.map.version")) {
                mapVersion = Integer.parseInt(props.getProperty("game.map.version"));
            }
            if (props.containsKey("game.skill.version")) {
                skillVersion = Integer.parseInt(props.getProperty("game.skill.version"));
            }
            if (props.containsKey("client.data.size.max")) {
                messageSizeMax = Integer.parseInt(props.getProperty("client.data.size.max"));
            } else {
                messageSizeMax = 1024;
            }
            if (props.containsKey("game.event")) {
                event = props.getProperty("game.event");
            }
            if (props.containsKey("game.EXP")) {
                EXP = Integer.parseInt(props.getProperty("game.EXP"));
            } else {
                EXP = 1;
            }
            if (props.containsKey("open.historySQL")) {
                historySQL = Boolean.parseBoolean(props.getProperty("open.historySQL"));
            }else{
                historySQL = false;
            }
            if (props.containsKey("event.year")) {
                eventYear = Integer.parseInt(props.getProperty("event.year"));
            }
            if (props.containsKey("event.month")) {
                eventMonth = Integer.parseInt(props.getProperty("event.month"));
            }
            if (props.containsKey("event.day")) {
                eventDay = Integer.parseInt(props.getProperty("event.day"));
            }
            if (props.containsKey("event.hour")) {
                eventHour = Integer.parseInt(props.getProperty("event.hour"));
            }
            if (props.containsKey("event.minute")) {
                eventMinute = Integer.parseInt(props.getProperty("event.minute"));
            }
            if (props.containsKey("event.second")) {
                eventSecond = Integer.parseInt(props.getProperty("event.second"));
            }
        } catch (IOException | NumberFormatException ex) {
            Log.error("load config err: " + ex.getMessage(), ex);
            return false;
        }
        return true;
    }
    public int getStatusChoiMoi() {
        return choimoi;
    }
        public int getPercentDameMob() {
        return damemob;
    }
    public void reload() {
        try {

            Connection conn = DbManager.getInstance().getConnection(DbManager.GAME);
            PreparedStatement stmt = conn.prepareStatement(SQLStatement.LOAD_EXP_SERVER,
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            ResultSet resultSet = stmt.executeQuery();
            resultSet.last();
            resultSet.beforeFirst();
            if (resultSet.next()) {
                this.expconf = resultSet.getDouble("value");
            }
            resultSet.close();
            stmt.close();  
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }  
    
    public void reloadnjtl() {
        try {

            Connection conn = DbManager.getInstance().getConnection(DbManager.GAME);
            PreparedStatement stmt = conn.prepareStatement(SQLStatement.LOAD_LVNJTL_SERVER,
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            ResultSet resultSet = stmt.executeQuery();
            resultSet.last();
            resultSet.beforeFirst();
            if (resultSet.next()) {
                this.ratenjtl = resultSet.getInt("value");
            }
            resultSet.close();
            stmt.close();  
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }
    
    public boolean isOpenhistorySQL() {
        return this.historySQL;
    }

    public String getJdbcUrl() {
        return "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName;
    }
    public double getexpconf() {
        return expconf;
    }
     public int getEventYear() {
        return eventYear;
    }

    public int getEventMonth() {
        return eventMonth;
    }

    public int getEventDay() {
        return eventDay;
    }

    public int getEventHour() {
        return eventHour;
    }

    public int getEventMinute() {
        return eventMinute;
    }

    public int getEventSecond() {
        return eventSecond;
    }

//    public String getMongodbUrl() {
//        if (!StringUtils.isNullOrEmpty(mongodbUser) && !StringUtils.isNullOrEmpty(mongodbPassword)) {
//            return String.format("mongodb://%s:%s@%s:%d/%s", mongodbUser, mongodbPassword, mongodbHost, mongodbPort, mongodbName);
//        }
//        return String.format("mongodb://%s:%d", mongodbHost, mongodbPort);
//    }
}
