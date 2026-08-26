/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Exe_Z.model;

import Exe_Z.item.Item;
import com.mongodb.client.MongoCollection;
import Exe_Z.server.Config;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.bson.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author PC
 */
public class History {

    public static final byte NAP_LUONG = 0;
    public static final byte GIAO_DICH = 1;
    public static final byte BO_VAT_PHAM = 2;
    public static final byte BAN_VAT_PHAM = 3;
    public static final byte MUA_VAT_PHAM = 4;
    public static final byte GOLD_TO_COIN = 5;
    public static final byte SHINWA_BAN = 6;
    public static final byte SHINWA_BAN_DUOC = 7;
    public static final byte SHINWA_MUA = 8;
    public static final byte ONLINE = 9;
    public static final byte OFFLINE = 10;
    public static final byte VXMM_DAT = 11;
    public static final byte VXMM_THANG = 12;

    public static final byte NHAT_VAT_PHAM = 13;

    public static final byte LUYEN_NGOC = 14;
    public static final byte THAO_NGOC = 15;
    public static final byte GOT_NGOC = 16;
    public static final byte KHAM_NGOC = 17;
    public static final byte TAI_XIU = 18;
    
    public static final byte GIAO_DICH_GUI = 0;
    public static final byte GIAO_DICH_NHAN = 1;

    public static final byte HANH_TRANG = 0;
    public static final byte RUONG_DO = 1;
    public static final byte TRANG_BI = 2;
    public static final byte THU_CUOI = 3;
    
    private static String getTypeName(byte type) {
        switch (type) {
//            case NAP_LUONG:
//                return "NẠP LƯỢNG";
            case GIAO_DICH:
                return "GIAO DỊCH";
            case BO_VAT_PHAM:
                return "BỎ VẬT PHẨM";
            case BAN_VAT_PHAM:
                return "BÁN VẬT PHẨM";
            case MUA_VAT_PHAM:
                return "MUA VẬT PHẨM";
//            case GOLD_TO_COIN:
//                return "ĐỔI LƯỢNG SANG XU";
            case SHINWA_BAN:
                return "BÁN ĐỒ SHIMWA";
            case SHINWA_BAN_DUOC:
                return "ĐỒ ĐÃ BÁN SHIWA";
            case SHINWA_MUA:
                return "MUA ĐỒ SHINWA";
//            case ONLINE:
//                return "ONLINE GAME";
//            case OFFLINE:
//                return "OFFLINE GAME";
            case VXMM_DAT:
                return "ĐẶT VXMM";
            case VXMM_THANG:
                return "THẮNG VXMM";
            case NHAT_VAT_PHAM:
                return "NHẶT ĐỒ";
            case LUYEN_NGOC:
                return "LUYỆN NGỌC";
            case THAO_NGOC:
                return "THÁO NGỌC";
            case GOT_NGOC:
                return "GỌT NGỌC";
            case KHAM_NGOC:
                return "KHẢM NGỌC";
            default:
                return "UNKNOWN";
        }
    }
    
    public static void insert1(History history, Char p) {
        boolean isOpenHistorySQL = p.serverConfig.isOpenhistorySQL();
        if (isOpenHistorySQL) {
            Config serverConfig = Config.getInstance();
            String url = Config.getInstance().getJdbcUrl();
            String user = serverConfig.getDbUser();
            String password = serverConfig.getDbPassword();
            String query = "INSERT INTO history_table (player_id, type, type_name, `truoc`, items, `sau`, bo_sung, time) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection con = DriverManager.getConnection(url, user, password); PreparedStatement pst = con.prepareStatement(query)) {
                pst.setInt(1, history.playerID);
                pst.setByte(2, history.type);
                pst.setString(3, getTypeName(history.type));
                pst.setString(4, history.befores.toJSONString());
                pst.setString(5, history.items.toJSONString());
                pst.setString(6, history.afters.toJSONString());
                pst.setString(7, history.extras);
                pst.setTimestamp(8, new Timestamp(history.time));
                pst.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public static void insert2(History history, Char p) {
        boolean isOpenHistorySQL = p.serverConfig.isOpenhistorySQL();
        if (isOpenHistorySQL) {
            Config serverConfig = Config.getInstance();
            String url = Config.getInstance().getJdbcUrl();
            String user = serverConfig.getDbUser();
            String password = serverConfig.getDbPassword();
            String query = "INSERT INTO history_tx (player_id, type, type_name, luong_truoc, items, luong_sau, luong_ton, time) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection con = DriverManager.getConnection(url, user, password); PreparedStatement pst = con.prepareStatement(query)) {
                pst.setInt(1, history.playerID);
                pst.setByte(2, history.type);
                pst.setString(3, getTypeName(history.type));
                pst.setString(4, history.befores.toJSONString());
                pst.setString(5, history.items.toJSONString());
                pst.setString(6, history.afters.toJSONString());
                pst.setString(7, history.extras);
                pst.setTimestamp(8, new Timestamp(history.time));
                pst.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    
    public int playerID;
    public byte type;
    public JSONObject befores;
    public JSONArray items;
    public JSONObject afters;
    public String extras;
    public long time;

    public History(int id, byte type) {
        this.playerID = id;
        this.type = type;
        this.extras = "";
        this.befores = new JSONObject();
        this.afters = new JSONObject();
        this.items = new JSONArray();
        if (this.type == GIAO_DICH) {
            this.items.add(new JSONArray());
            this.items.add(new JSONArray());
        }
        if (this.type == ONLINE || this.type == OFFLINE) {
            this.items.add(new JSONArray());
            this.items.add(new JSONArray());
            this.items.add(new JSONArray());
            this.items.add(new JSONArray());
        }
    }

    public void setPartnerID(String name) {
        if (this.type == History.GIAO_DICH) {
            JSONObject partner = new JSONObject();
            partner.put("partner_id", name);
            this.extras = partner.toJSONString();
        }
    }

    public void setIPAddress(String ip) {
        JSONObject obj = new JSONObject();
        obj.put("ip_address", ip);
        this.extras = obj.toJSONString();
    }

    public void setCurrentMap(int map_id, int zone_id, int itemId) {
        JSONObject obj = new JSONObject();
        obj.put("map_id", map_id);
        obj.put("zone_id", zone_id);
        obj.put("item_id", itemId);
        this.extras = obj.toJSONString();
    }

    public void setPrice(int coin, int yen, int gold) {
        JSONObject obj = new JSONObject();
        obj.put("coin", coin);
        obj.put("yen", yen);
        obj.put("gold", gold);
        this.extras = obj.toJSONString();
    }

    public void setBefore(long coin, int gold, long yen) {
        this.befores.put("coin", coin);
        this.befores.put("gold", gold);
        this.befores.put("yen", yen);
    }

    public void setAfter(long coin, int gold, long yen) {
        this.afters.put("coin", coin);
        this.afters.put("gold", gold);
        this.afters.put("yen", yen);
    }

    public void setExtras(String extras) {
        if (this.type != History.GIAO_DICH) {
            this.extras = extras;
        }
    }

    public void setLuckyDraw(int type, int id, int coin, String content) {
        if (this.type == VXMM_DAT || this.type == VXMM_THANG) {
            JSONObject vxmm = new JSONObject();
            vxmm.put("type", type);
            vxmm.put("id", id);
            vxmm.put("coin", coin);
            vxmm.put("content", content);
            this.extras = vxmm.toJSONString();
        }
    }

    public void addItem(Item item) {
        if (this.type != History.GIAO_DICH) {
            this.items.add(item.toJSONObject());
        }
    }

    public void addItem(int type, Item item) {
        if (this.type == History.GIAO_DICH || this.type == OFFLINE || this.type == ONLINE) {
            JSONArray arr = (JSONArray) this.items.get(type);
            arr.add(item.toJSONObject());
            this.items.set(type, arr);
        }
    }

    public void setTime(long time) {
        this.time = time;
    }
}
