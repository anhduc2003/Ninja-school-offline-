package Exe_Z.server;
import Exe_Z.constants.SQLStatement;
import Exe_Z.db.jdbc.DbManager;
import Exe_Z.util.Log;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class JFrameSearchItem extends JFrame {

    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JTextField name;
    private javax.swing.JButton xacnhan;


    public JFrameSearchItem() {
        initComponents();
    }

    private void initComponents() {
        jLabel1 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();

        name = new javax.swing.JTextField();
        xacnhan = new javax.swing.JButton();
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        setTitle("Tìm kiếm Item");
        jLabel1.setText("Tên item");
        jLabel7.setText("Them item vao cua hang");
        jLabel7.setToolTipText("");
        xacnhan.setText("Xác nhận");

        xacnhan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                xacnhanActionPerformed(evt);
            }

            private void xacnhanActionPerformed(ActionEvent evt) {
                if (name.getText().equals("")) {
                    JOptionPane.showMessageDialog(rootPane, "Vui lòng nhập đủ các trường!");
                    return;
                }
                System.out.println("" + name.getText());

                searchItem(name.getText());
            }
        });

        name.setPreferredSize(new java.awt.Dimension(200, 30)); // Adjust the dimensions as needed

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(28, 28, 28)
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(name, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(29, Short.MAX_VALUE))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(xacnhan)
                                .addGap(112, 112, 112))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel1)
                                        .addComponent(name, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(xacnhan)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pack();
    }

    public static void run() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new JFrameSearchItem().setVisible(true);
            }
        });
    }

    private void searchItem(String itemName) {
        PreparedStatement stmt = null;
        try {
            try {
                Connection conn = DbManager.getInstance().getConnection(DbManager.SERVER);
                stmt = conn.prepareStatement(
                        SQLStatement.LOAD_ITEM_BY_NAME, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

                stmt.setString(1, "%" + itemName + "%");
                ResultSet rs = stmt.executeQuery();
                new JTableItem(rs);

            } catch (Exception ex) {
                Log.error("[addItem] ex100 ", ex);
                JOptionPane.showMessageDialog(rootPane, "Không tìm thấy thông tin item");
            } finally {
                assert stmt != null;
                stmt.close();
            }
        } catch (Exception e) {
            Log.error("[addItem] ex200 ", e);
            JOptionPane.showMessageDialog(rootPane, "Có lỗi trong quá trình xử lý");
        }
    }
}