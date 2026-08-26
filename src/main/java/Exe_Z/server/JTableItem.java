/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Exe_Z.server;
import Exe_Z.constants.SQLStatement;
import Exe_Z.db.jdbc.DbManager;
import Exe_Z.util.Log;
import Exe_Z.util.StringUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
/**
 *
 * @author Administrator
 */
public class JTableItem extends JFrame {

    private JFrame jFrame1;
    private JTable jTable1;
    private JTextField id;
    private JTextField coin;
    private JTextField gold;
    private JTextField yen;
    private JButton xacnhan;

    // Constructor
    public JTableItem(ResultSet rs) {
        initComponents(rs);
    }

    private void initComponents(ResultSet result) {
        // Frame initialization
        jFrame1 = new JFrame();
        Container container = jFrame1.getContentPane();
        container.setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setPreferredSize(new Dimension(500,70));

        id = new JTextField();
        id.setPreferredSize(new Dimension(60, 20));

        coin = new JTextField();
        coin.setPreferredSize(new Dimension(60,20));

        gold = new JTextField();
        gold.setPreferredSize(new Dimension(60,20));

        yen = new JTextField();
        yen.setPreferredSize(new Dimension(60,20));

        xacnhan = new JButton();


        // Frame Title
        jFrame1.setTitle("Danh sách item");
        xacnhan.setText("Xác nhận");

        JLabel labelID = new JLabel("Nhập ID:");
        labelID.setBounds(250, 0, 100, 20);
        JLabel labelCoin = new JLabel("Coin:");
        labelCoin.setBounds(250, 0, 60, 20);
        JLabel labelGold = new JLabel("Gold:");
        labelGold.setBounds(250, 0, 60, 20);
        JLabel labelYen = new JLabel("Yen:");
        labelYen.setBounds(250, 0, 60, 20);

        buttonPanel.add(labelID);
        buttonPanel.add(id);
        buttonPanel.add(labelCoin);
        buttonPanel.add(coin);
        buttonPanel.add(labelGold);
        buttonPanel.add(gold);
        buttonPanel.add(labelYen);
        buttonPanel.add(yen);
        buttonPanel.add(xacnhan);

        // Add components to the container
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
                addItem(id.getText(), coin.getText(),gold.getText(),yen.getText());
            }
        });

        try {
            DefaultTableModel tableModel = new DefaultTableModel();
            String[] colsName = {"Mã item", "Tên item"};
            tableModel.setColumnIdentifiers(colsName);

            while (result.next()) {

                String rows[] = new String[2];
                rows[0] = result.getString(1); // lấy dữ liệu tại cột số 1 (ứng với mã hàng)
                rows[1] = result.getString(2); // lấy dữ liệu tai cột số 2 ứng với tên hàng
                tableModel.addRow(rows);
            }

            jTable1 = new JTable(tableModel);
            jTable1.setModel(tableModel);
            jTable1.setBounds(30, 40, 200, 100);

            JScrollPane sp = new JScrollPane(jTable1);
            container.add(sp);
            // Frame Size
            jFrame1.setSize(500, 300);
            // Frame Visible = true
            jFrame1.setVisible(true);

        } catch (Exception ex) {
            Log.error("[addItem] ex0 ", ex);
        }
    }

    private void addItem(String id, String coin, String gold, String yen) {
        PreparedStatement stmt = null;

        try {
            // tìm item theo id
            Connection connection = DbManager.getInstance().getConnection(DbManager.SERVER);
            stmt = connection.prepareStatement(SQLStatement.FIND_ITEM_BY_ID, ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            stmt.setInt(1, Integer.parseInt(id));
            ResultSet resultType = stmt.executeQuery();

            if (resultType.next()) {
                try {
                    // lấy id và type gán item vào store_data
                    stmt = connection.prepareStatement(
                            SQLStatement.ADD_ITEM_INTO_SHOPDATA, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

                    stmt.setInt(1, resultType.getInt("id"));
                    stmt.setInt(2, resultType.getInt("type"));
                    stmt.setInt(3, StringUtils.isNullOrEmpty(coin) ? 0 : Integer.parseInt(coin));
                    stmt.setInt(4, StringUtils.isNullOrEmpty(gold) ? 0 : Integer.parseInt(gold));
                    stmt.setInt(5, StringUtils.isNullOrEmpty(yen) ? 0 : Integer.parseInt(yen));
                    stmt.executeUpdate();

                    JOptionPane.showMessageDialog(rootPane,"Thêm mới thành công.");

                } catch (Exception ex) {
                    Log.error("[addItem] ex111 ", ex);
                } finally {
                    assert stmt != null;
                    stmt.close();
                }
            }
        } catch (Exception e) {
            Log.error("[addItem] ex222 ", e);
        }
    }


    public static void run() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new JFrameSearchItem().setVisible(true);
            }
        });
    }
}