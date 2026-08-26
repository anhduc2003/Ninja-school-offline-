package Exe_Z.server;

import Exe_Z.constants.SQLStatement;
import Exe_Z.db.jdbc.DbManager;
import Exe_Z.util.Log;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLOutput;

/**
 *
 * @author ToanT
 */

public class JFrameUserProcess extends JFrame {
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JTextField username;
    private JButton processButton;

    public JFrameUserProcess() {
        initComponents();
    }

    public void initComponents() {
        jLabel1 = new JLabel();
        jLabel2 = new JLabel();

        username = new JTextField();
        processButton = new JButton();
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        setTitle("Xu ly user");
        jLabel1.setText("Tên");
        jLabel2.setText("");
        processButton.setText("Xử lý");

        processButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                processActionPerformed(evt);
            }
            private void processActionPerformed(ActionEvent evt) {
                if(username.getText().equals("")) {
                    JOptionPane.showMessageDialog(rootPane, "Vui lòng nhập đủ các trường");
                    return;
                }
                System.out.println("User name:" + username.getText());

                processUser(username.getText());
            }
        });

        username.setPreferredSize(new java.awt.Dimension(200, 30)); // Adjust the dimensions as needed

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(26, 26, 26)
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(username, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(27, Short.MAX_VALUE))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(processButton)
                                .addGap(110, 110, 110))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel1)
                                        .addComponent(username, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(processButton)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pack();
    }

    public static void run() {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                new JFrameUserProcess().setVisible(true);
            }
        });
    }

    private void processUser(String username) {
        int id;
        int userId;
        int xu;
        int playerOnline;
        PreparedStatement stmt = null;
        try {
            try {
                Connection connection = DbManager.getInstance().getConnection(DbManager.SERVER);
                stmt = connection.prepareStatement(SQLStatement.SELECT_PLAYER_BY_NAME, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    userId = rs.getInt("user_id");
                    id = userId;
                    xu = rs.getInt("xu");
                    playerOnline = rs.getInt("online");
                    System.out.println("user_id/id: " + userId);
                    System.out.println("Player online: " + playerOnline);
                    System.out.println("Xu:" + xu);


                    //update online in players table
                    PreparedStatement playerStmt = null;
                    try {
                        playerStmt = DbManager.getInstance().getConnection(DbManager.UPDATE)
                                .prepareStatement(SQLStatement.UPDATE_PLAYER_ONLINE_STATUS);
                        playerStmt.setInt(1, 0);
                        playerStmt.setInt(2, userId);
                        playerStmt.executeUpdate();
                    } catch (SQLException e) {
                        Log.error("[Xy ly user] ex " + userId, e);
                    } finally {
                        if (playerStmt != null) {
                            playerStmt.close();
                        }
                    }

                    //update online in users table
                    PreparedStatement userStmt = null;
                    try {
                        userStmt = DbManager.getInstance().getConnection(DbManager.UPDATE)
                                .prepareStatement(SQLStatement.UPDATE_USERS_ONLINE_STATUS);
                        userStmt.setInt(1, 0);
                        userStmt.setInt(2, id);
                        userStmt.executeUpdate();
                    } catch (SQLException e) {
                        Log.error("[Xy ly user] ex " + userId, e);
                    } finally {
                        if (userStmt != null) {
                            userStmt.close();
                        }
                    }

                    //update xu in players table
                    PreparedStatement playerXuStmt = null;
                    if(xu < 0) {
                        System.out.println("Xu ly xu < 0");
                        try {
                            playerXuStmt = DbManager.getInstance().getConnection(DbManager.UPDATE)
                                    .prepareStatement(SQLStatement.UPDATE_PLAYER_XU_STATUS);
                            playerXuStmt.setInt(1, 0);
                            playerXuStmt.setInt(2, userId);
                            playerXuStmt.executeUpdate();
                        } catch (SQLException e) {
                            Log.error("[Xy ly user] ex " + userId, e);
                        } finally {
                            if (playerXuStmt != null) {
                                playerXuStmt.close();
                            }
                        }
                    }
                    JOptionPane.showMessageDialog(rootPane, "Xử lý thành công");
                } else {
                    JOptionPane.showMessageDialog(rootPane, "Không tìm thấy thông tin user");
                }
            } catch (HeadlessException | SQLException e) {
                Log.error("[Xy ly user] ex", e);
                JOptionPane.showMessageDialog(rootPane, "Có lỗi trong quá trình xử lý");
            } finally {
                assert stmt != null;
                stmt.close();
            }
        } catch (HeadlessException | SQLException e) {
            Log.error("[Xy ly user] ex", e);
            JOptionPane.showMessageDialog(rootPane, "Có lỗi trong quá trình xử lý");
        }
    }
}
