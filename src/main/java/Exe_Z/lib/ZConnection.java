/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Exe_Z.lib;

import Exe_Z.db.jdbc.DbManager;
import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * @author kitakeyos - Hoàng Hữu Dũng
 */
public class ZConnection {

    private Connection connection;
    private int timeOut;

    public ZConnection(int timeOut) {
        this.timeOut = timeOut;
    }

    public Connection getConnection() {
        try {
            if (connection != null) {
                if (!connection.isValid(timeOut)) {
                    connection.close();
                }
            }
            if (connection == null || connection.isClosed()) {
                connection = DbManager.getInstance().getConnection();
                return getConnection();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return connection;
    }
}
