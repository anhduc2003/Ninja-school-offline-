package Exe_Z.map;

import Exe_Z.clan.Clan;
import Exe_Z.constants.ItemName;
import Exe_Z.event.Event;
import Exe_Z.event.KoroKing;
import Exe_Z.item.Item;
import Exe_Z.item.ItemFactory;
import Exe_Z.model.Char;
import Exe_Z.model.WarMember;
import Exe_Z.network.Service;
import Exe_Z.option.ItemOption;
import Exe_Z.server.GlobalService;
import Exe_Z.server.NinjaSchool;
import Exe_Z.server.Server;
import Exe_Z.util.Log;
import Exe_Z.util.NinjaUtils;
import org.jetbrains.annotations.Debug;

import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WarClan {

    public int countDown;
    public int coinTotal;
    public String whiteName;
    public String blackName;
    public ArrayList<Char> allMembers;
    public ArrayList<Char> whiteMembers;
    public ArrayList<Char> blackMembers;
    public int whitePoint;
    public int blackPoint;
    public int whiteTurretKill;
    public int blackTurretKill;
    public int numberJoinedWhite;
    public int numberJoinedBlack;
    public ArrayList<WarMember> members;
    public long time;
    public int status;
    public static Clan clan1;
    public static Clan clan2;
    public static boolean check = true;
    public static boolean IsStart = false;
    public static boolean isDatCuoc = false;

    public ReadWriteLock lock = new ReentrantReadWriteLock();

    public void notify(String text) {
        GlobalService.getInstance().chat("Hệ Thống", text);
    }

    public WarClan() {
        this.blackMembers = new ArrayList<>();
        this.whiteMembers = new ArrayList<>();
        this.members = new ArrayList<>();
        this.numberJoinedWhite = 0;
        this.numberJoinedBlack = 0;
        this.time = System.currentTimeMillis();
        this.whiteTurretKill = 0;
        this.blackTurretKill = 0;
        this.whitePoint = 0;
        this.blackPoint = 0;
        this.whiteName = "";
        this.blackName = "";
        this.countDown = 300;
    }

    public WarClan(Clan clanWhite, Clan clanBlack) {
        this.blackMembers = new ArrayList<>();
        this.whiteMembers = new ArrayList<>();
        this.allMembers = new ArrayList<>();
        this.members = new ArrayList<>();
        this.numberJoinedWhite = 0;
        this.numberJoinedBlack = 0;
        this.time = System.currentTimeMillis();
        this.whiteTurretKill = 0;
        this.blackTurretKill = 0;
        this.whitePoint = 0;
        this.blackPoint = 0;
        this.whiteName = clanWhite.name;
        this.blackName = clanBlack.name;
        this.countDown = 300;
        this.notify("Gia tộc " + whiteName + " và gia tộc " + blackName + " đang bắt đầu cuộc chiến");
    }

    public void initMap() {
        for (Map map : MapManager.getInstance().getMaps()) {
            if (map.id >= 120 && map.id <= 124 || map.id == 98 || map.id == 104) {
                map.setWarClan(this);
                map.initZone(true);
            }
        }
    }

    public void register() {
        this.status = 0;
    }

    public void viewTop(Char _char) {
        String info = "";
        int whitePointAdd = this.whiteTurretKill * 500;
        int blackPointAdd = this.blackTurretKill * 500;
        int whitePoint = this.whitePoint;
        if (whitePoint < 0) {
            whitePoint = 0;
        }
        int blackPoint = this.blackPoint;
        if (blackPoint < 0) {
            blackPoint = 0;
        }
        info += clan1.name + ": tích lũy " + whitePoint + " điểm\n";
        info += clan2.name + ": tích lũy " + blackPoint + " điểm";
        boolean reward = false;
        ArrayList<WarMember> list = new ArrayList<>();
        for (WarMember mem : this.members) {
            WarMember clone = mem.clone();
            if (clone.faction == 0) {
                clone.point += whitePointAdd - blackPointAdd;
            }
            if (clone.faction == 1) {
                clone.point += blackPointAdd - whitePointAdd;
            }
            if (clone.point < 0) {
                clone.point = 0;
            }
            list.add(clone);
        }
        _char.getService().reviewCT(info, reward);
    }

    public void finish(final byte type) {
        if (this.status == 2) {
            this.coinTotal = clan1.MoneyGTC;
            final int coin = this.coinTotal * 2 * 9 / 10;
            allMembers.addAll(blackMembers);
            allMembers.addAll(whiteMembers);
            if (type == -1) {
                clan1.addCoin(coin / 2);
                clan2.addCoin(coin / 2);
                for (final Char player : allMembers) {
                    if (player != null) {
                        player.serverMessage("Hai gia tộc hoà nhau và nhận lại " + coin + " xu gia tộc.");
                    }
                }
            } else if (type == 0) {
                clan1.addCoin(coin);
                for (final Char player : allMembers) {
                    if (player != null) {
                        player.serverMessage("Gia tộc " + clan1.name + " giành chiến thắng và nhận được " + coin + " xu gia tộc.");
                    }
                }
            } else if (type == 1) {
                clan2.addCoin(coin);
                for (final Char player : allMembers) {
                    if (player != null) {
                        player.serverMessage("Gia tộc " + clan2.name + " giành chiến thắng và nhận được " + coin + " xu gia tộc.");
                    }
                }

            }
        }
    }

    public static void setMoney(int Money, Char pl) {
        if (pl.clan.typeGTC == 0) {
            clan1.MoneyGTC = Money;
        } else {
            clan2.MoneyGTC = Money;
        }
        if (clan1 != null && clan2 != null) {
            if (clan1.MoneyGTC == clan2.MoneyGTC) {
                clan1.addCoin(-clan1.MoneyGTC);
                clan2.addCoin(-clan1.MoneyGTC);
                WarClan.initWarClan(clan1, clan2);
                check = false;
                WarClan.isDatCuoc = false;
            }
        }
    }

    public void end() {
        this.status = 2;
        lock.writeLock().lock();
        try {
            finishBasedOnPoints();
            clan1.typeGTC = clan2.typeGTC = -1;
            clan1.MoneyGTC = clan2.MoneyGTC = 0;
            relocateChars(whiteMembers);
            relocateChars(blackMembers);
            check = true;
            IsStart = false;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void finishBasedOnPoints() {
        if (whitePoint == blackPoint) {
            this.finish((byte) (-1));
        } else if (whitePoint > blackPoint) {
            this.finish((byte) (0));
        } else {
            this.finish((byte) (1));
        }
    }

    private void relocateChars(ArrayList<Char> charList) {
        for (Char _char : charList) {
            try {
                _char.warClan = null;
                _char.warClanPoint = 0;
                _char.Clanfaction = -1;
                _char.setTypePk(Char.PK_NORMAL);
                short[] xy = NinjaUtils.getXY(72);
                _char.setXY(xy);
                _char.changeMap(72);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void addMember(Char _char) {
        lock.writeLock().lock();
        try {
            if (_char.Clanfaction == 0) {
                if (!this.whiteMembers.contains(_char)) {
                    this.whiteMembers.add(_char);
                }
            }
            if (_char.Clanfaction == 1) {
                if (!this.blackMembers.contains(_char)) {
                    this.blackMembers.add(_char);
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void addTurretPoint(int faction) {
        if (faction == 0) {
            this.whiteTurretKill += 1;
        }
        if (faction == 1) {
            this.blackTurretKill += 1;
        }
    }

    public void addMember(WarMember mem) {
        lock.writeLock().lock();
        try {
            this.members.add(mem);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void removeMember(Char _char) {
        lock.writeLock().lock();
        try {
            if (_char.Clanfaction == 0) {
                this.whiteMembers.remove(_char);
            }
            if (_char.Clanfaction == 1) {
                this.blackMembers.remove(_char);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void startCounting() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (countDown > 0) {
                    try {
                        Thread.sleep(1000); // delay 1 giây
                        countDown--; // giảm biến đếm
                        if (status == 1) {
                            for (final WarMember member : members) {
                                Char player = Char.findCharByName(member.name);
                                if (player != null) {
                                    player.service.sendTimeInMap(countDown);
                                }
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void start() {
        this.status = 1;
    }

    public static void initWarClan(Clan clanWhite, Clan clanBlack) {
        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    WarClan war = MapManager.getInstance().warClan = new WarClan(clanWhite, clanBlack);
                    Char c1 = Char.findCharByName(clanWhite.main_name);
                    Char c2 = Char.findCharByName(clanBlack.main_name);
                    war.initMap();
                    war.register();
                    war.countDown = 600;
                    war.startCounting();
                    if (c1 != null) {
                        goWarClan(c1, war);
                    }
                    if (c2 != null) {
                        goWarClan(c2, war);
                    }
                    Thread.sleep(war.countDown * 1000L);
                    war.countDown = 6000;
                    war.start();
                    Thread.sleep(war.countDown * 1000L);
                    war.end();
                } catch (InterruptedException ex) {
                    Logger.getLogger(WarClan.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(runnable, 0, 1 * 24 * 60 * 60, TimeUnit.SECONDS);
    }

    public static void goWarClan (Char p, WarClan war) {
        p.warClan = war;
        if (p.Clanfaction == 0) {
            p.setXY((short) 242, (short) 312);
            p.changeMap(98);
        } else if (p.Clanfaction == 1) {
            p.setXY((short) 245, (short) 236);
            p.changeMap(104);
        }
        war.addMember(p);
    }

    public static void cancel(Char _char) {
        Char c1 = Char.findCharByName(WarClan.clan1.main_name);
        Char c2 = Char.findCharByName(WarClan.clan2.main_name);
        if (c1 != null) {
            short[] xy = NinjaUtils.getXY((short) c1.mapBeforeEnterPB);
            c1.setXY(xy);
            c1.setTypePk((byte) -1);
            c1.changeMap(c1.mapBeforeEnterPB);
        }
        if (c2 != null) {
            short[] xy = NinjaUtils.getXY((short) c2.mapBeforeEnterPB);
            c2.setXY(xy);
            c2.setTypePk((byte) -1);
            c2.changeMap(c2.mapBeforeEnterPB);
        }

        WarClan.clan1.typeGTC = WarClan.clan2.typeGTC = -1;
        WarClan.clan1.MoneyGTC = WarClan.clan2.MoneyGTC = 0;

        if (_char != null) {
            WarClan.clan1.getClanService().serverMessage("Gia tộc " + _char.clan.name + " đã hủy gia tộc chiến");
            WarClan.clan2.getClanService().serverMessage("Gia tộc " + _char.clan.name + " đã hủy gia tộc chiến");
        } else {
            WarClan.clan2.getClanService().serverMessage("Trận đấu bị huỷ bỏ");
        }
        WarClan.clan1 = WarClan.clan2 = null;
        WarClan.IsStart = false;
        WarClan.isDatCuoc = false;
    }
}
