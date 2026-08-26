package Exe_Z.server;

import Exe_Z.constants.SQLStatement;
import Exe_Z.db.jdbc.DbManager;
import Exe_Z.server.Config;
import Exe_Z.server.GlobalService;
import Exe_Z.util.Log;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExpChangePanel extends JFrame {
    private JTextField expField;
    private JLabel currentExpLabel;

    public ExpChangePanel() {
        setTitle("Cpanel EXP");
        setSize(300, 200);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        double currentExp = getCurrentExp();
        currentExpLabel = new JLabel("EXP Hiện Tại: " + currentExp);
        expField = new JTextField();
        JButton saveButton = new JButton("Save");

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    double newExp = Double.parseDouble(expField.getText());
                    setNewExp(newExp);
                    GlobalService.getInstance().chat("Hệ Thống", "EXP của toàn server đã được đổi. EXP hiện tại là X" + newExp);
                     Config.getInstance().reload();
                     Log.info("đã load và mở x" + newExp + " EXP");
                    dispose();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(ExpChangePanel.this, "Vui lòng nhập số hợp lệ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 1));
        panel.add(currentExpLabel);
        panel.add(expField);
        panel.add(saveButton);

        add(panel, BorderLayout.CENTER);
    }

    private double getCurrentExp() {
        double exp = 0;
        try {
            Connection conn = DbManager.getInstance().getConnection(DbManager.GAME);
            PreparedStatement stmt = conn.prepareStatement(SQLStatement.LOAD_EXP_SERVER,
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                exp = resultSet.getDouble("value");
            }
            resultSet.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return exp;
    }

    private void setNewExp(double newExp) {
        try {
            Connection conn = DbManager.getInstance().getConnection(DbManager.GAME);
            PreparedStatement stmt = conn.prepareStatement("UPDATE `options` SET `value` = ? WHERE `key` = 'expserver'");
            stmt.setDouble(1, newExp);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
