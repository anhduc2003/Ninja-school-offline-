package Exe_Z.server;

import Exe_Z.constants.ItemName;
import Exe_Z.constants.ItemOptionName;
import Exe_Z.db.jdbc.DbManager;
import Exe_Z.item.Item;
import Exe_Z.item.ItemFactory;
import Exe_Z.model.Char;
import Exe_Z.model.DoBuffBan;
import Exe_Z.option.ItemOption;
import Exe_Z.util.NinjaUtils;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class JFrameSendBuffcoin extends JFrame {

    private javax.swing.JTextField balance;
    private javax.swing.JTextField coin;
    private javax.swing.JTextField tongnap;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JButton xacnhan;
    private javax.swing.JTextField name;
    private JFrame controlPanel;
    DoBuffBan doBuffBan = new DoBuffBan();

    public JFrameSendBuffcoin() {
        initComponents();
    }

    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        coin = new javax.swing.JTextField();
        tongnap = new javax.swing.JTextField();
        name = new javax.swing.JTextField();
        xacnhan = new javax.swing.JButton();

        coin.setText("0");
        tongnap.setText("0");
        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("");

        jLabel1.setText("Tên Nhân Vật");

        jLabel2.setText("Nhập số coin");

        jLabel3.setText("Nhập tổng nạp");

        xacnhan.setText("Xác nhận");
        xacnhan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                xacnhanActionPerformed(evt);
            }

            private void xacnhanActionPerformed(ActionEvent evt) {
                if (name.getText().equals("") || coin.getText().equals("") || tongnap.getText().equals("")) {
                    JOptionPane.showMessageDialog(rootPane, "Vui lòng nhập đủ các trường!");
                } else {
                    Char p = Char.findCharByName(name.getText());
                    if (p != null) {
                        if (!checkNumber(coin.getText()) || !checkNumber(tongnap.getText())) {
                            JOptionPane.showMessageDialog(rootPane, "Sai định dạng!");
                            return;
                        }
                        try {
                            int balance1 = Integer.parseInt(coin.getText());
                            int tongnap1 = Integer.parseInt(tongnap.getText());
                            if (balance1 < 0 || balance1 > 10000000 || tongnap1 < 0 || tongnap1 > 10000000) {
                                JOptionPane.showMessageDialog(rootPane, "chỉ có thể nhập 0-10tr");
                                return;
                            }
                            try (Connection conn = DbManager.getInstance().getConnection(DbManager.UPDATACOIN)) {
                                String sql = "UPDATE `users` SET `coin` = `coin` + ?, `tongnap` = `tongnap` + ? WHERE `id` = ? LIMIT 1;";
                                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                                    conn.setAutoCommit(false);
                                    stmt.setInt(1, balance1);
                                    stmt.setInt(2, tongnap1);
                                    stmt.setInt(3, p.user.id);
                                    stmt.executeUpdate();
                                    conn.commit();
                                    JOptionPane.showMessageDialog(rootPane, "Cập nhật thành công!");
                                } catch (SQLException ex) {
                                    conn.rollback();
                                    JOptionPane.showMessageDialog(rootPane, "Lỗi trong khi cập nhật dữ liệu!");
                                    ex.printStackTrace();
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        } catch (NumberFormatException e) {
                            JOptionPane.showMessageDialog(rootPane, "Sai định dạng số!");
                        }
                    } else {
                        JOptionPane.showMessageDialog(rootPane, "Người này không tồn tại hoặc không online!");
                    }
                }
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int confirmed = JOptionPane.showConfirmDialog(rootPane, "Bạn có chắc chắn muốn đóng?", "Xác nhận", JOptionPane.YES_NO_OPTION);
                if (confirmed == JOptionPane.YES_OPTION) {
                    dispose();
                }
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(30, 30, 30)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel1)
                                        .addComponent(jLabel2)
                                        .addComponent(jLabel3))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(coin)
                                        .addComponent(tongnap)
                                        .addComponent(name, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE))
                                .addContainerGap(30, Short.MAX_VALUE))
                        .addGroup(layout.createSequentialGroup()
                                .addGap(90, 90, 90)
                                .addComponent(xacnhan)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(30, 30, 30)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel1)
                                        .addComponent(name, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel2)
                                        .addComponent(coin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(30, 30, 30)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel3)
                                        .addComponent(tongnap, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(30, 30, 30)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(xacnhan))
                                .addContainerGap(30, Short.MAX_VALUE))
        );

        pack();
    }

    public static boolean checkNumber(String str) {
        return str.matches("[0-9]+");
    }

    public static void run() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new JFrameSendBuffcoin().setVisible(true);
            }
        });
    }
}
