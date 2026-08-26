/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Exe_Z.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import Exe_Z.server.Server;
import Exe_Z.clan.Clan;
import Exe_Z.db.jdbc.DbManager;
import Exe_Z.model.Char;
import static Exe_Z.server.Server.loadListVip;
import Exe_Z.util.Log;
import Exe_Z.util.NinjaUtils;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Optional;

/**
 *
 * @author PC
 */
public class Ranked {

    public static final String[] NAME = {"Top Đại gia", "Top Cao Thủ", "Top Gia tộc", "Top Hang động", "Top Tài Phú",  "Top VXMM", "Top Boss","Top VXMM"};
    public static final String[] NAME_CLASS = {"Chưa vào lớp", "Top Kiếm", "Top Tiêu", "Top Kunai" , "Top Cung", "Top Đao", "Top Quạt"};

    public static final String[] RANKED_NAME = {"%d. %s có %s yên", "%d. %s trình độ cấp %d vào ngày %s",
        "%d. Gia tộc %s có trình độ cấp %d do %s làm tộc trưởng, thành viên %d/%d", "%d. %s nhận được %s rương", "%d. %s Đã Ủng Hộ %s Vnđ", "%d. %s có %s điểm", "%d. %s có %s điểm","%d. %s đã win %s lần"};

    public static final Vector[] RANKED = new Vector[8];
    public static final Vector[] RANKED_CLASS = new Vector[7];

    public static void init() {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                refresh();
            }
        };
        long delay = 12 * 60 * 60 * 1000;
        Timer timer = new Timer("Ranked");
        timer.schedule(timerTask, 0, delay);
    }

    public static void refresh() {
        loadListVip();
        initTopDaiGia();
        initTopCaoThu();
        initTopGiaToc();
        initTopHangDong();
        initTopUp();
        initTopVXMM();
        initTopBoss();
        initTopvxmm();
        for (byte i = 1; i < 7; i++) {
            initTopClass(i);
        }
        Log.info("Refresh ranked success.");
    }

    public static void initTopDaiGia() {
        try {
            Vector<String> ranked = new Vector<>();
            Connection conn = DbManager.getInstance().getConnection(DbManager.SERVER);
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT `name`, `yen` FROM `players` WHERE `yen` > 0 AND `server_id` = ? ORDER BY `yen` DESC LIMIT 10;");
            stmt.setInt(1, Config.getInstance().getServerID());
            ResultSet res = stmt.executeQuery();
            int i = 1;
            while (res.next()) {
                String name = res.getString("name");
                ranked.add(String.format(RANKED_NAME[0], i, Char.setNameVip(name),
                        NinjaUtils.getCurrency(res.getInt("yen"))));
                i++;
            }
            res.close();
            stmt.close();
            RANKED[0] = ranked;
        } catch (SQLException ex) {
            Log.error("init top dai gia err", ex);
        }
    }

    public static void initTopCaoThu() {
        try {
            Vector<String> ranked = new Vector<>();
            Connection conn = DbManager.getInstance().getConnection(DbManager.SERVER);
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT `name`, CAST(JSON_EXTRACT(data, \"$.exp\") AS INT) AS `exp`, CAST(JSON_EXTRACT(data, \"$.levelUpTime\") AS INT) AS `levelUpTime` FROM players where `server_id` = ? ORDER BY `exp` DESC, `levelUpTime` ASC LIMIT 10;");
            stmt.setInt(1, Config.getInstance().getServerID());
            ResultSet res = stmt.executeQuery();
            ArrayList<CaoThu> list = new ArrayList<>();
            while (res.next()) {
                CaoThu rank = new CaoThu();
                rank.level = NinjaUtils.getLevel(res.getLong("exp"));
                rank.time = res.getLong("levelUpTime");
                rank.name = res.getString("name");
                list.add(rank);
            }
            order(list);
            int i = 1;
            Calendar cl = Calendar.getInstance();
            for (CaoThu c : list) {
                int level = c.level;
                String time = NinjaUtils.milliSecondsToDateString(c.time, "yyyy/MM/dd HH:mm:ss aa");
                    ranked.add(String.format(RANKED_NAME[1], i, Char.setNameVip(c.name), level, time));          
                i++;
            }

            res.close();
            stmt.close();
            RANKED[1] = ranked;
        } catch (SQLException ex) {
            Log.error("init top cao thu", ex);
        }
    }

    public static void initTopClass(byte clazz) {
        try {
            Vector<String> ranked = new Vector<>();
            Connection conn = DbManager.getInstance().getConnection(DbManager.SERVER);
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT `name`, CAST(JSON_EXTRACT(data, \"$.exp\") AS INT) AS `exp`, CAST(JSON_EXTRACT(data, \"$.levelUpTime\") AS INT) AS `levelUpTime` FROM players where `server_id` = ? and `class` = ? ORDER BY `exp` DESC, `levelUpTime` ASC LIMIT 10;");
            stmt.setInt(1, Config.getInstance().getServerID());
            stmt.setInt(2, clazz);
            ResultSet res = stmt.executeQuery();
            ArrayList<CaoThu> list = new ArrayList<>();
            while (res.next()) {
                CaoThu rank = new CaoThu();
                rank.level = NinjaUtils.getLevel(res.getLong("exp"));
                rank.time = res.getLong("levelUpTime");
                rank.name = res.getString("name");
                list.add(rank);
            }
            order(list);
            int i = 1;
            for (CaoThu c : list) {
                int level = c.level;
                String time = NinjaUtils.milliSecondsToDateString(c.time, "yyyy/MM/dd HH:mm:ss aa");
                ranked.add(String.format(RANKED_NAME[1], i, Char.setNameVip(c.name), level, time));
                i++;
            }

            res.close();
            stmt.close();
            RANKED_CLASS[clazz] = ranked;
        } catch (SQLException ex) {
            Log.error("init top class", ex);
        }
    }

    public static void initTopGiaToc() {
        try {
            Vector<String> ranked = new Vector<>();
            Connection conn = DbManager.getInstance().getConnection(DbManager.SERVER);
            PreparedStatement stmt = conn
                    .prepareStatement("SELECT `id` FROM `clan` WHERE `level` > 1 AND `server_id` = ? ORDER BY `level` DESC LIMIT 10;");
            stmt.setInt(1, Config.getInstance().getServerID());
            ResultSet res = stmt.executeQuery();
            int i = 1;
            while (res.next()) {
                int id = res.getInt("id");
                Optional<Clan> g = Clan.getClanDAO().get(id);
                if (g != null && g.isPresent()) {
                    Clan clan = g.get();
                    ranked.add(String.format(RANKED_NAME[2], i, clan.getName(), clan.getLevel(), Char.setNameVip(clan.getMainName()),
                            clan.getNumberMember(), clan.getMemberMax()));
                    i++;
                }
            }
            res.close();
            stmt.close();
            RANKED[2] = ranked;
        } catch (SQLException ex) {
            Log.error("init top gia toc", ex);
        }
    }

    public static void initTopHangDong() {
        try {
            Vector<String> ranked = new Vector<>();
            Connection conn = DbManager.getInstance().getConnection(DbManager.SERVER);
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT `name`, `rewardPB` FROM `players` WHERE `rewardPB` > 0 AND `server_id` = ? AND CAST(JSON_EXTRACT(data, \"$.level\") AS INT) BETWEEN 60 AND 69 ORDER BY `rewardPB` DESC LIMIT 10;");
            stmt.setInt(1, Config.getInstance().getServerID());
            ResultSet res = stmt.executeQuery();
            int i = 1;
            while (res.next()) {
                String name = res.getString("name");
                ranked.add(String.format(RANKED_NAME[3], i, Char.setNameVip(name),
                        NinjaUtils.getCurrency(res.getInt("rewardPB"))));
                i++;
            }
            res.close();
            stmt.close();
            RANKED[3] = ranked;
        } catch (SQLException ex) {
            Log.error("init top hang dong", ex);
        }
    }

    public static void initTopUp() {
        try {
            Vector<String> ranked = new Vector<>();
            Connection conn = DbManager.getInstance().getConnection(DbManager.SERVER);
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT players.`name`, users.`tongnap`\n" +
                            "FROM players\n" +
                            "JOIN users ON players.user_id = users.id\n" +
                            "ORDER BY users.tongnap DESC\n" +
                            "LIMIT 10;");
//            stmt.setInt(1, Config.getInstance().getServerID());
            ResultSet res = stmt.executeQuery();
            int i = 1;
            while (res.next()) {
                ranked.add(String.format(RANKED_NAME[4], i, res.getString("name"),
                        NinjaUtils.getCurrency(res.getInt("tongnap"))));
                i++;
            }
            res.close();
            stmt.close();
            RANKED[4] = ranked;
        } catch (SQLException ex) {
            Log.error("top nạp", ex);
        }
    }
    
    // bxh vxmm
    private static void initTopVXMM() {
        try {
            Vector<String> ranked = new Vector<>();
            Connection conn = DbManager.getInstance().getConnection(DbManager.SERVER);
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT `name`, `topvxmm` FROM `players` WHERE `topvxmm` > 0 AND `server_id` = ? ORDER BY `topvxmm` DESC LIMIT 10;");
            stmt.setInt(1, Config.getInstance().getServerID());
            ResultSet res = stmt.executeQuery();
            int i = 1;
            while (res.next()) {
                ranked.add(String.format(RANKED_NAME[5], i, res.getString("name"),
                        NinjaUtils.getCurrency(res.getInt("topvxmm"))));
                i++;
            }
            res.close();
            stmt.close();
            RANKED[5] = ranked;
        } catch (SQLException ex) {
            Log.error("init top vxmm err", ex);
        }
    }

    private static void initTopBoss() {
        try {
            Vector<String> ranked = new Vector<>();
            Connection conn = DbManager.getInstance().getConnection(DbManager.SERVER);
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT `name`, `topBoss` FROM `players` WHERE `topBoss` > 0 AND `server_id` = ? ORDER BY `topBoss` DESC LIMIT 100;");
            stmt.setInt(1, Config.getInstance().getServerID());
            ResultSet res = stmt.executeQuery();
            int i = 1;
            while (res.next()) {
                ranked.add(String.format(RANKED_NAME[6], i, res.getString("name"),
                        NinjaUtils.getCurrency(res.getInt("topBoss"))));
                i++;
            }
            res.close();
            stmt.close();
            RANKED[6] = ranked;
        } catch (SQLException ex) {
            Log.error("init top săn boss err", ex);
        }
    }
    
    public static void initTopvxmm(){
    try {
        Calendar now = Calendar.getInstance();
        int dayOfWeek = now.get(Calendar.DAY_OF_WEEK); 
        int hourOfDay = now.get(Calendar.HOUR_OF_DAY); 
        Vector<String> ranked = new Vector<>();
        Connection conn = DbManager.getInstance().getConnection(DbManager.SERVER);
        PreparedStatement stmt = conn.prepareStatement(
           "SELECT `name`, CAST(JSON_EXTRACT(data, \"$.diemvxmm\") AS INT) AS `diemvxmm` " +
           "FROM players WHERE `server_id` = ? AND CAST(JSON_EXTRACT(data, \"$.diemvxmm\") AS INT) > 10 " +
           "ORDER BY `diemvxmm` DESC LIMIT 10;");
        stmt.setInt(1, Config.getInstance().getServerID());
        ResultSet res = stmt.executeQuery();
        int i = 1;
        while (res.next()) {
            String name = res.getString("name");
            ranked.add(String.format(RANKED_NAME[7], i, Char.setNameVip(name),
                NinjaUtils.getCurrency(res.getInt("diemvxmm"))));
            if (dayOfWeek >= Calendar.MONDAY && dayOfWeek <= Calendar.SATURDAY && hourOfDay < 23) {
                stmt = conn.prepareStatement("UPDATE `top_vxmm` SET `name` = ?, `receive` = ? WHERE `id` = ?;");
                stmt.setString(1, name);
                stmt.setInt(2, 1);
                stmt.setInt(3, i);
                stmt.executeUpdate();
            }
            i++;
        }
        res.close();
        stmt.close();
        RANKED[7] = ranked;
    } catch (SQLException ex) {
        Log.error("init top vxmm err", ex);
    }
    }
            
    private static void order(List<CaoThu> ranks) {

        Collections.sort(ranks, new Comparator() {

            public int compare(Object o1, Object o2) {

                Integer level1 = ((CaoThu) o1).level;
                Integer level2 = ((CaoThu) o2).level;
                int sComp = level2.compareTo(level1);
                if (sComp != 0) {
                    return sComp;
                }
                Long x1 = ((CaoThu) o1).time;
                Long x2 = ((CaoThu) o2).time;
                return x1.compareTo(x2);
            }
        });
    }
}

class CaoThu {

    public String name;
    public long time;
    public int level;
}
