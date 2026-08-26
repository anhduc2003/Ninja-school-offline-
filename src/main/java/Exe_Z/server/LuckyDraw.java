package Exe_Z.server;

import Exe_Z.constants.CMD;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import Exe_Z.util.Log;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import Exe_Z.db.jdbc.DbManager;
import Exe_Z.model.Char;
import Exe_Z.model.History;
import Exe_Z.util.NinjaUtils;
import Exe_Z.lib.ParseData;
import Exe_Z.lib.RandomCollection;
import Exe_Z.network.Message;

import java.io.DataOutputStream;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

@Getter
public class LuckyDraw {

    public class Player {

        int id;
        public int xu;
        String name;
    }

    private int id;
    private String name;
    private int totalMoney;
    private long xuTop;
    private int xuWin;
    private int timeCount;
    private String nameWin = "";
    private int typeColor;
    private int xuThamGia;
    private byte type;
    private List<Player> members = new ArrayList<>();
    private int xuMin, xuMax;
    private boolean stop;

    public LuckyDraw(String name, byte type) {
        this.name = name;
        this.type = type;
        this.id = 0;
        if (type == LuckyDrawManager.NORMAL) {
            xuMin = 10000;
            xuMax = 1000000;
        } else if (type == LuckyDrawManager.VIP) {
            xuMin = 1000000;
            xuMax = 100000000;
        }
        this.timeCount = LuckyDrawManager.TIME_COUNT_DOWN;
    }

    public int getNumberOfMemeber() {
        return this.members.size();
    }

    public synchronized void join(Char pl, int numb) {
        if (pl.trade != null) {
            pl.warningTrade();
            return;
        }
        if (!pl.isHuman) {
            pl.warningClone();
            return;
        }

        if (LuckyDrawManager.getInstance().isWaitStop()) {
            pl.serverMessage("Vòng xoay đang chờ dừng hoạt động, vui lòng thử lại sau!");
            return;
        }
        if (timeCount < 11) {
            pl.serverMessage("Đã hết thời gian tham gia vui lòng quay lại vào vòng sau");
            return;
        }
        if (this.members.size() >= 10) {
            pl.serverMessage("Số người tham gia tối đa là 10");
            return;
        }
        if (pl.coin < 11000000) {
            pl.serverMessage("Bạn phải có 10.000.000  Xu  hành trang mới có thể tham gia");
            return;
        }
        if (pl.coin < numb) {
            pl.serverMessage("Bạn không đủ xu để tham gia");
            return;
        }
        for (Player m : members) {
            if (m.id == pl.id) {
                if (m.xu + numb > xuMax) {
                    if (xuMax - (m.xu + numb) < xuMin) {
                        pl.serverMessage("Bạn không thể đặt thêm xu");
                    } else {
                        pl.serverMessage("Bạn chỉ có thể đặt thêm tối đa " + NinjaUtils.getCurrency(xuMax - m.xu) + " xu");
                    }
                    return;
                }
                if (numb < xuMin) {
                    pl.serverMessage("Bạn chỉ có thể đặt từ " + NinjaUtils.getCurrency(xuMin) + " đến " + NinjaUtils.getCurrency(xuMax) + " xu!");
                    return;
                }
                totalMoney += numb;
                m.xu += numb;
                History history = new History(pl.id, History.VXMM_DAT);
                history.setBefore(pl.coin, pl.user.gold, pl.yen);
                pl.addCoin(-numb);
                history.setAfter(pl.coin, pl.user.gold, pl.yen);
                history.setTime(System.currentTimeMillis());
                history.setLuckyDraw(this.type, this.id, numb, "Đặt thêm");
                History.insert1(history, pl);
                pl.serverMessage("Bạn đã đặt thêm " + NinjaUtils.getCurrency(numb) + " xu thành công!");
                return;
            }
        }
        if (numb < xuMin || numb > xuMax) {
            pl.serverMessage("Bạn chỉ có thể đặt từ " + NinjaUtils.getCurrency(xuMin) + " đến " + NinjaUtils.getCurrency(xuMax) + " xu!");
            return;
        }
        Player m = new Player();
        m.id = pl.id;
        m.xu = numb;
        totalMoney += numb;
        m.name = pl.name;
        members.add(m);
        History history = new History(pl.id, History.VXMM_DAT);
        history.setBefore(pl.coin, pl.user.gold, pl.yen);
        pl.addCoin(-numb);
        history.setAfter(pl.coin, pl.user.gold, pl.yen);
        history.setTime(System.currentTimeMillis());
        history.setLuckyDraw(this.type, this.id, numb, "Đặt mới");
        History.insert1(history, pl);
        pl.serverMessage("Bạn đã tham gia " + NinjaUtils.getCurrency(numb) + " xu thành công");
    }

    public void update() {
        if (!stop) {
            boolean isWaitStop = LuckyDrawManager.getInstance().isWaitStop();
            int numberOfMember = getNumberOfMemeber();
            if (numberOfMember >= 2) {
                timeCount--;
                if (timeCount <= 0) {
                    try {
                        randomCharWin();
                        chat();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        if (isWaitStop) {
                            stop();
                        } else {
                            refresh();
                        }
                    }
                }
            } else {
                if (isWaitStop) {
                    stop();
                }
            }
        }
    }

    public void stop() {
        this.stop = true;
    }

    public void randomCharWin() {
        try {
            Player m = null;
            for (Player player : members) {
                if (player.name.equals("admin")) { // muốn ai win thì sửa ở đây kkk
                    m = player;
                    break;
                }
            }

            if (m == null) {
                RandomCollection<Player> rd = new RandomCollection<>();
                for (Player player : members) {
                    try {
                        rd.add(player.xu, player);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                m = rd.next();
            }
            int receive = totalMoney;
            // phí 10%
            if (members.size() > 10) {
                receive -= receive / 10;
            } else {
                receive -= receive * (members.size() - 1) / 100;
            }
            History history = new History(m.id, History.VXMM_THANG);
            Char pl = ServerManager.findCharById(m.id);
            // tính điểm vxmm
            if (pl != null) {
                pl.updateTopVxmm(xuTop);
                history.setBefore(pl.coin, pl.user.gold, pl.yen);
                pl.addCoin(receive);
//                pl.countvxmm += diemthang + receive / 1000000;
                history.setAfter(pl.coin, pl.user.gold, pl.yen);
            } else {
                long coin = 0;
                int gold = 0;
                int yen = 0;
                int topvxmm = 0;
                int point = (int) m.xu / 1000000;
                Connection conn = DbManager.getInstance().getConnection(DbManager.SERVER);
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT `players`.`xu`, `players`.`data`, `players`.`yen`, `users`.`luong` , `players`.`topvxmm` FROM `players` INNER JOIN `users` ON `players`.`user_id` = `users`.`id` WHERE `players`.`id` = ?;",
                        ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

                try {
                    stmt.setInt(1, m.id);
                    ResultSet res = stmt.executeQuery();
                    if (res.first()) {
                        JSONObject json = (JSONObject) JSONValue.parse(res.getString("data"));
                        ParseData parse = new ParseData(json);
                        long coinMax = parse.getLong("coinMax");

                        coin = res.getLong("xu");
                        yen = res.getInt("yen");
                        gold = res.getInt("luong");
                        topvxmm = res.getInt("topvxmm");
                        history.setBefore(coin, gold, yen);
                        coin += receive;
                        topvxmm += point;
                        if (coin > coinMax) {
                            coin = coinMax;
                        }
                        history.setAfter(coin, gold, yen);
                    }
                    res.close();
                } finally {
                    stmt.close();
                }
                DbManager.getInstance().updateCoin(m.id, (int) coin);
                DbManager.getInstance().updateTopvxmm(m.id, topvxmm);
            }
            history.setLuckyDraw(this.type, this.id, receive, "Thắng");
            history.setTime(System.currentTimeMillis());
            History.insert1(history, pl);
            nameWin = m.name;
            xuWin = receive;
            xuThamGia = m.xu;
        } catch (Exception ex) {
            Logger.getLogger(LuckyDraw.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int getNumberMoney() {
        return totalMoney;
    }

    public Player find(int id) {
        synchronized (members) {
            for (Player pl : members) {
                if (pl.id == id) {
                    return pl;
                }
            }
        }
        return null;
    }

    public void refresh() {
        this.id++;
        timeCount = LuckyDrawManager.TIME_COUNT_DOWN;
        totalMoney = 0;
        members.clear();
        typeColor = NinjaUtils.nextInt(10);
    }

    public void show(Char p) {
        try {
            Player pl = find(p.id);
            int xu = 0;
            if (pl != null) {
                xu = pl.xu;
            }
            int total = totalMoney;
            if (total == 0) {
                total = 1;
            }
            float percent = (float) xu * 100f / (float) total;
            String[] splits = String.format("%.2f", percent).replaceAll(",", ".").split("\\.");
            int p1 = Integer.parseInt(splits[0]);
            int p2 = Integer.parseInt(splits[1]);
            Message ms = new Message(CMD.ALERT_MESSAGE);
            DataOutputStream ds = ms.writer();
            ds.writeUTF("typemoi");
            ds.writeUTF(this.name);
            ds.writeShort(this.timeCount);
            if (type == LuckyDrawManager.NORMAL) {
                ds.writeUTF("Trên 10K dưới " + shortString(this.totalMoney));
            } else {
                ds.writeUTF("Trên 1Triệu dưới " + shortString(this.totalMoney));
            }
            ds.writeShort(p1);
            if (p2 > 0 && p2 < 10) {
                ds.writeUTF(splits[1]);
            } else {
                ds.writeUTF(String.valueOf(p2));
            }
            ds.writeShort(getNumberOfMemeber());
            if (!"".equals(nameWin)) {
                ds.writeUTF("Người vừa chiến thắng:" + NinjaUtils.getColor(typeColor) + Char.setNameVip(nameWin) + "\nSố xu thắng: " + getXuWinStr(p, xuWin) + "Xu \nSố xu tham gia: " + getXuWinStr(p, xuThamGia) + "Xu");
            } else {
                ds.writeUTF("Chưa có thông tin!");
            }
            ds.writeByte(type);
            ds.writeUTF(String.format("%s", NinjaUtils.getCurrency(xu)));
            ds.flush();
            p.getService().sendMessage(ms);
            ms.cleanup();
        } catch (Exception ex) {
            Logger.getLogger(LuckyDraw.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getXuWinStr(Char p, int xu) {
        if (!p.name.equals(nameWin)) {
            if (type == LuckyDrawManager.NORMAL) {
                return "Trên 10k dưới " + shortString(xu);
            } else {
                return "Trên 1tr dưới " + shortString(xu);
            }
        } else {
            return NinjaUtils.getCurrency(xu);
        }
    }

    public static String shortString(long xu) {
        if (xu < 100000) {
            return "100k";
        } else if (xu < 900000) {
            long rounded = xu / 1000;
            rounded = (rounded + 100 - rounded % 100);
            return rounded + "k ";
        } else if (xu < 900000000) {
            long rounded = xu / 1000000;
            if (rounded < 10) {
                rounded += 1;
            } else if (rounded < 100) {
                rounded += 10 - rounded % 10;
            } else {
                rounded += 100 - rounded % 100;
            }
            return rounded + "tr ";
        } else {
            long rounded = xu / 1000000000;
            if (rounded < 10) {
                rounded += 1;
            } else if (rounded < 100) {
                rounded += 10 - rounded % 10;
            } else {
                rounded += 100 - rounded % 100;
            }
            return rounded + "tỷ ";
        }
    }

    public void chat() {
        try {
            List<Char> chars = ServerManager.getChars();
            for (Char _char : chars) {
                Message ms = new Message(CMD.CHAT_SERVER);
                DataOutputStream ds = ms.writer();
                String text = "Chúc mừng " + Char.setNameVip(nameWin).toUpperCase() + " đã chiến thắng " + getXuWinStr(_char, xuWin) + " xu trong trò chơi Vòng xoay may mắn với " + getXuWinStr(_char, xuThamGia) + " xu";
                ds.writeUTF("Admin");
                ds.writeUTF(text);
                ds.flush();
                _char.getService().sendMessage(ms);
                ms.cleanup();
            }
        } catch (IOException ex) {
            Log.error("chat global err: " + ex.getMessage(), ex);
        }
    }
}
