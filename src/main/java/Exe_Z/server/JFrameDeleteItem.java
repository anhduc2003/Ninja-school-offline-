package Exe_Z.server;
import Exe_Z.constants.SQLStatement;
import Exe_Z.db.jdbc.DbManager;
import Exe_Z.util.Log;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;

public class JFrameDeleteItem extends JFrame {
    private JFrame jFrame1;
    private JTable jTable1;
    private JTextField id;
    private JButton xacnhan;

    public JFrameDeleteItem(ResultSet rs) {
        initComponents(rs);
    }

    private void initComponents(ResultSet result) {
        jFrame1 = new JFrame();
        Container container = jFrame1.getContentPane();
        container.setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel();

        id = new JTextField();
        id.setPreferredSize(new Dimension(100, 20));

        xacnhan = new JButton();

        jFrame1.setTitle("List item");
        xacnhan.setText("Xác nhận");
        JLabel label = new JLabel("Nhập ID:");
        label.setBounds(250, 0, 100, 20);

        buttonPanel.add(label);
        buttonPanel.add(id);
        buttonPanel.add(xacnhan);

        container.add(buttonPanel, BorderLayout.NORTH);

        xacnhan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                xacnhanActionPerformed(evt);
            }

            private void xacnhanActionPerformed(ActionEvent evt) {
                if (id.getText().equals("")) {
                    JOptionPane.showMessageDialog(rootPane, "Vui lòng nhập đủ các trường!");
                    return;
                }
                System.out.println("" + id.getText());
                deleteItem(id.getText());
            }
        });

        try {
            DefaultTableModel tableModel = new DefaultTableModel();
            String[] colsName = {"Mã item", "Tên item"};
            tableModel.setColumnIdentifiers(colsName);

            while (result.next()) {

                String rows[] = new String[2];
                rows[0] = result.getString(1);
                rows[1] = result.getString(2);
                tableModel.addRow(rows);
            }

            jTable1 = new JTable(tableModel);
            jTable1.setModel(tableModel);
            jTable1.setBounds(30, 40, 200, 100);

            JScrollPane sp = new JScrollPane(jTable1);
            container.add(sp);
            jFrame1.setSize(500, 300);
            jFrame1.setVisible(true);

        } catch (Exception ex) {
            Log.error("[deleteItem] ex0 ", ex);
        }
    }

    private void deleteItem(String id) {
        PreparedStatement stmt = null;

        try {
            Connection connection = DbManager.getInstance().getConnection(DbManager.SERVER);
            stmt = connection.prepareStatement(SQLStatement.FIND_ITEM_BY_ID, ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            stmt.setInt(1, Integer.parseInt(id));
            ResultSet resultType = stmt.executeQuery();

            if (resultType.next()) {
                try {
                    stmt = connection.prepareStatement(
                            SQLStatement.DELETE_ITEM_BY_ID, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

                    stmt.setInt(1, resultType.getInt("id"));
                    stmt.executeUpdate();
                    JOptionPane.showMessageDialog(rootPane,"Đã xóa.");
                } catch (Exception ex) {
                    Log.error("[deleteItem] ex1112 ", ex);
                } finally {
                    assert stmt != null;
                    stmt.close();
                }
            }
        } catch (Exception e) {
            Log.error("[deleteItem] ", e);
            JOptionPane.showMessageDialog(rootPane, "Có lỗi trong quá trình xử lý");
        }
    }

    public static void run() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new JFrameSearchItemForDelete().setVisible(true);
            }
        });
    }
}
