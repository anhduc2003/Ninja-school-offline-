package Exe_Z.server;

import Exe_Z.constants.SQLStatement;
import Exe_Z.db.jdbc.DbManager;
import Exe_Z.model.User;
import Exe_Z.util.Log;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import Exe_Z.model.InputDialog;
import Exe_Z.model.Char;

/**
 *
 * @author mrQuy
 */
public class JFrameBan extends JFrame{
    
  private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JTextField name;
    private javax.swing.JButton xacnhan;

    public JFrameBan() {
        initComponents();
    }

    private void initComponents() {
        jLabel1 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();

        name = new javax.swing.JTextField();
        xacnhan = new javax.swing.JButton();
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        setTitle("Ban ACC");
        jLabel1.setText("Tên");
        jLabel7.setText("Ban Acc");
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

                //[QUY]-ban acc
                banAcc(name.getText());
            }
        });


        // Set the preferred size for the JTextField
        name.setPreferredSize(new java.awt.Dimension(200, 30)); // Adjust the dimensions as needed

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(26, 26, 26)
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(name, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(27, Short.MAX_VALUE))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(xacnhan)
                                .addGap(110, 110, 110))
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
                new JFrameBan().setVisible(true);
            }
        });
    }

    private void banAcc(String accName) {
        int id;
        PreparedStatement stmt = null;
        try {
            try {
                Connection conn = DbManager.getInstance().getConnection(DbManager.SERVER);
                stmt = conn.prepareStatement(
                        SQLStatement.SELECT_PLAYER_BY_NAME, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

                stmt.setString(1, accName);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    // get user_id from player by accName
                    id = rs.getInt("user_id");

                    // update activated = 0 and status = 0 from user_id
                    try (PreparedStatement stmt2 = DbManager.getInstance().getConnection(DbManager.UPDATE)
                            .prepareStatement(SQLStatement.UPDATE_USERS_STATUS)) {
                        stmt2.setInt(1, 1);//sửa thành x1
                        stmt2.setInt(2, 1);//sửa thành x1
                        stmt2.setInt(3, id);
                        stmt2.executeUpdate();
                    } catch (Exception ex) {
                        Log.error("[banAcc] ex " + id, ex);
                    }
                    // logout user
                    logoutSession(id);
                } else {
                    JOptionPane.showMessageDialog(rootPane, "Không tìm thấy thông tin user");
                }
            } catch (Exception ex) {
                Log.error("[banAcc] ex ", ex);
                JOptionPane.showMessageDialog(rootPane, "Có lỗi trong quá trình xử lý");
            } finally {
                assert stmt != null;
                stmt.close();
            }
        } catch (Exception e) {
            Log.error("[banAcc] ex ", e);
            JOptionPane.showMessageDialog(rootPane, "Có lỗi trong quá trình xử lý");
        }
    }

    private void logoutSession(int id) {
        User user = ServerManager.findUserByUserID(id);
        if (user != null && user.sltChar != null) {
//            if (!user.isCleaned) {
                user.activated = 0;
                user.session.disconnect();
                JOptionPane.showMessageDialog(rootPane, "Ban Acc thành công");
            }
        }
    }
//}
