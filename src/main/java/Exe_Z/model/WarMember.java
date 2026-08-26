/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Exe_Z.model;

import Exe_Z.db.jdbc.DbManager;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import Exe_Z.server.Config;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Calendar;
import org.bson.Document;
import org.bson.conversions.Bson;

/**
 *
 * @author Admin
 */
public class WarMember {

    public int id;
    public String name;
    public int point;
    public byte faction;
    public byte type;

    public WarMember clone() {
        WarMember mem = new WarMember();
        mem.id = this.id;
        mem.name = this.name;
        mem.point = this.point;
        mem.faction = this.faction;
        mem.type = this.type;
        return mem;
    }

    public String getRank() {
        String result;
        if (this.point >= 4000) {
            result = "Nhẫn Giả";
        } else if (this.point >= 1500) {
            result = "Thượng Nhẫn";
        } else if (this.point >= 600) {
            result = "Trung Nhẫn";
        } else if (this.point >= 200) {
            result = "Hạ Nhẫn";
        } else {
            result = "Học Giả";
        }
        return result;
    }

     public void save() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);

        String query = """
        INSERT INTO top_war (player_id, name, point, server_id, week, month, year, type)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        ON DUPLICATE KEY UPDATE
        name = VALUES(name),
        point = VALUES(point);
    """;
        try (Connection connection = DbManager.getInstance().getConnection(DbManager.LOADTOP); PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, id);
            statement.setString(2, name);
            statement.setInt(3, point);
            statement.setInt(4, Config.getInstance().getServerID());
            statement.setInt(5, weekOfYear);
            statement.setInt(6, month);
            statement.setInt(7, year);
            statement.setInt(8, type);

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
