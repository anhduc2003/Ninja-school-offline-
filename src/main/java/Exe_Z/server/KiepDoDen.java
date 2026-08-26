package Exe_Z.server;

import Exe_Z.db.jdbc.DbManager;
import Exe_Z.model.Char;
import Exe_Z.model.History;
import Exe_Z.model.SoiCau;
import Exe_Z.util.NinjaUtils;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class KiepDoDen {

    public static Thread thread;
    public static KiepDoDen instance = new KiepDoDen();
    public static long time = 60000L;
    public static int TAI = 1;
    public static int XIU = 2;
    public int totalTai;
    public int totalXiu;
    public HashMap<Integer, Integer> memberTai;
    public HashMap<Integer, Integer> memberXiu;
    public long timeStart;
    public int typeWin;
    public int baseId;
    
    private boolean kqtaiwin = false;
    private boolean kqxiuwin = false;
    
    public static KiepDoDen getInstance() {
        return instance;
    }
    
    public KiepDoDen() {
        this.baseId = 1;
        this.totalTai = 0;
        this.totalXiu = 0;
        this.memberTai = new HashMap<>();
        this.memberXiu = new HashMap<>();
        this.timeStart = System.currentTimeMillis();
        this.typeWin = 0;
        thread = new Thread(this::processGame);
        thread.start();
    }
    
    public void Mesage(Char player) {
        int time = getRemainingTime();
        player.getService().serverDialog(String.format("Thông tin Phiên #%s\n"
                + "Thời Gian : %s giây\n"
                + "Số Người Tham Gia : %s\n"
                + "Tổng Xu Tham Gia Tiêu : %s Xu\n"
                + "Tổng Xu Tham Gia Kiếm : %s Xu\n\n"
                + "Kết Quả Phiên Trước : %s", baseId, time, memberTai.size() + memberXiu.size(), NinjaUtils.getCurrency(totalTai), NinjaUtils.getCurrency(totalXiu), getTypeWin()));
    }


    public static KiepDoDen gI() {
        if (instance == null) {
            instance = new KiepDoDen();
            return instance;
        }
        return instance;
    }

    private void processGame() {
        while (true) {
            if (checkTime()) {
                try {
                    calculateResult();
                    result();
                } catch (SQLException e) {
                    System.err.println("ERROR Kiếm Tiêu");
                }
            }
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void joinGame(Char player, int betType, int betAmount) {
        if (player == null) {
            return;
        }
        if (NinjaSchool.isStop) {
            player.getService().serverDialog("Máy chủ đang tiến hành bảo trì không thể đặt cược");
            return;
        }
        if (betAmount <= 0 || betAmount % 100 != 0) {
            player.getService().serverDialog("Giá trị đặt cược phải là bội số của 100");
            return;
        }
        if (betAmount < 1000000 || betAmount > 100000000) {
            player.getService().serverDialog("Min Đặt 1tr Xu Max 100Tr Xu");
            return;
        }
        if (player.coin > 1000000000) {
            player.getService().serverDialog("Số xu trong hành trang quá 1 tỷ vui lòng cất xu để đặt cược.");
            return;
        }
        if (player.coin < betAmount) {
            player.getService().serverDialog("Không đủ xu để đặt cược.");
            return;
        }
        if (betType != TAI && betType != XIU) {
            return;
        }
        if (betType == XIU && memberTai.containsKey(player.id)) {
            player.getService().serverDialog("Không thể đặt");
            return;
        } else if (betType == TAI && memberXiu.containsKey(player.id)) {
            player.getService().serverDialog("Không thể đặt");
            return;
        }

        int currentBet = 0;
        if (betType == TAI) {
            currentBet = memberTai.getOrDefault(player.id, 0);
        } else if (betType == XIU) {
            currentBet = memberXiu.getOrDefault(player.id, 0);
        }
        if (currentBet + betAmount > 500000000) {
            player.getService().serverDialog("Tổng giá trị đặt thêm không được vượt quá 500tr Xu.");
            return;
        }
        if (memberTai.containsKey(player.id) || memberXiu.containsKey(player.id)) {
            if (betType == TAI) {
                int existingBetAmount = memberTai.get(player.id);
                memberTai.put(player.id, existingBetAmount + betAmount);
                totalTai += betAmount;
                player.getService().serverDialog("Bạn đã đặt thêm " + NinjaUtils.getCurrency(betAmount) + " Xu vào Tiêu");
            } else {
                int existingBetAmount = memberXiu.get(player.id);
                memberXiu.put(player.id, existingBetAmount + betAmount);
                totalXiu += betAmount;
                player.getService().serverDialog("Bạn đã đặt thêm " + NinjaUtils.getCurrency(betAmount) + " Xu vào Kiếm");
            }
            player.addCoin(-betAmount);
        } else {
            if (betType == TAI) {
                memberTai.put(player.id, betAmount);
                totalTai += betAmount;
                player.getService().serverDialog("Bạn đã tham gia " + NinjaUtils.getCurrency(betAmount) + " Xu thành công vào Tiêu");
            } else if (betType == XIU) {
                memberXiu.put(player.id, betAmount);
                totalXiu += betAmount;
                player.getService().serverDialog("Bạn đã tham gia " + NinjaUtils.getCurrency(betAmount) + " Xu thành công vào Kiếm");
            }
            player.addCoin(-betAmount);
        }
    }


    public void taiwin() {
        kqtaiwin = true;
    }

    public void xiuwin() {
        kqxiuwin = true;
    }
    
    public byte intervention;
    
    // Tính kết quả
    private void calculateResult() {
        int a, b, c, result;
        int randomValue = NinjaUtils.nextInt(100);
        if (kqtaiwin) {
            do {
                a = NinjaUtils.nextInt(1, 6);
                b = NinjaUtils.nextInt(1, 6);
                c = NinjaUtils.nextInt(1, 6);
                result = a + b + c;
            } while (result <= 10);
            typeWin = TAI;
            kqtaiwin = false;
        } else if (kqxiuwin) {
            do {
                a = NinjaUtils.nextInt(1, 6);
                b = NinjaUtils.nextInt(1, 6);
                c = NinjaUtils.nextInt(1, 6);
                result = a + b + c;
            } while (result > 10);
            typeWin = XIU;
            kqxiuwin = false;
        } else {
            if (totalTai < totalXiu && randomValue > 30) {
                do {
                    a = NinjaUtils.nextInt(1, 6);
                    b = NinjaUtils.nextInt(1, 6);
                    c = NinjaUtils.nextInt(1, 6);
                    result = a + b + c;
                } while (result <= 10);
                typeWin = TAI;
            } else if (totalXiu > totalTai && randomValue > 30) {
                do {
                    a = NinjaUtils.nextInt(1, 6);
                    b = NinjaUtils.nextInt(1, 6);
                    c = NinjaUtils.nextInt(1, 6);
                    result = a + b + c;
                } while (result > 10);
                typeWin = XIU;
            } else {
                a = NinjaUtils.nextInt(1, 6);
                b = NinjaUtils.nextInt(1, 6);
                c = NinjaUtils.nextInt(1, 6);
                result = a + b + c;
                typeWin = (result > 10) ? TAI : XIU;
            }
        }
        SoiCau.soicau.add(new SoiCau(String.format("Kết quả phiên thứ %s : %s. Tổng %d+%d+%d=%d ", baseId, getTypeWin(), a, b, c, result), "", ""));
    }

    public void result() throws SQLException {
        switch (this.typeWin) {
            case 1:
                reward(memberTai);
                break;
            case 2:
                reward(memberXiu);
                break;
            default:
                break;
        }
        // Clear game data for the next round
        baseId++;
        totalTai = 0;
        totalXiu = 0;
        kqxiuwin = false;
        kqtaiwin = false;
        memberTai.clear();
        memberXiu.clear();
        timeStart = System.currentTimeMillis();
    }

    public void reward(HashMap<Integer, Integer> list_members) throws SQLException {
        for (Map.Entry<Integer, Integer> entry : list_members.entrySet()) {
            int key = entry.getKey();
            long value = entry.getValue();
            value = value * 19 / 10;
            Char pl = ServerManager.findCharById(key);
            if (pl != null) {
                pl.addCoin(value);
//                pl.toptx += value;
                pl.serverDialog("Bạn nhận được " + NinjaUtils.getCurrency(value) + " xu từ trò chơi Kiếm Tiêu");
            } else {
                long coin = 0;
                int gold = 0;
                int yen = 0;
                long coinold = 0;
                try (Connection conn = DbManager.getInstance().getConnection()) {
                    PreparedStatement stmt = conn.prepareStatement(
                            "SELECT `players`.`xu`, `players`.`data`, `players`.`yen`, `users`.`luong` FROM `players` INNER JOIN `users` ON `players`.`user_id` = `users`.`id` WHERE `players`.`id` = ?;",
                            ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
                    try {
                        History history = new History(key, History.TAI_XIU);
                        stmt.setInt(1, key);
                        try (ResultSet res = stmt.executeQuery()) {
                            if (res.first()) {
                                coinold =  res.getLong("xu");
                                coin = res.getLong("xu");
                                yen = res.getInt("yen");
                                gold = res.getInt("luong");
                                history.setBefore(coin, gold, yen);
                                coin += value;
                                if (coin > 1500000000) {
                                    coin = 1500000000;
                                }
                                history.setAfter(coin, gold, yen);
                                History.insert1(history, pl);
                                History.insert2(history, pl);
                            }
                        }
                    } finally {
                        stmt.close();
                    }
                    DbManager.getInstance().updateCoin(key, (int) coin);
                }
            }
        }
    }

    public String getTypeWin() {
        return (typeWin == TAI) ? "Tiêu" : ((typeWin == XIU) ? "Kiếm" : "Chưa có thông tin");
    }

    
    public boolean checkTime() {
        return System.currentTimeMillis() - this.timeStart >= KiepDoDen.time;
    }

    private int getRemainingTime() {
        long currentTime = System.currentTimeMillis();
        long endTime = timeStart + time;
        int remainingSeconds = (int) ((endTime - currentTime) / 1000);
        return Math.max(0, remainingSeconds);
    }
}
