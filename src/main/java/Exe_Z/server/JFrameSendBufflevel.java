package Exe_Z.server;


import Exe_Z.constants.ItemName;
import Exe_Z.constants.ItemOptionName;
import Exe_Z.item.Item;
import Exe_Z.item.ItemFactory;
import Exe_Z.model.Char;
import Exe_Z.model.DoBuffBan;
import Exe_Z.option.ItemOption;
import Exe_Z.util.NinjaUtils;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;

public class JFrameSendBufflevel extends JFrame {

    private javax.swing.JTextField level;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JButton xacnhan;
    private javax.swing.JTextField name;
    private JFrame controlPanel;
    DoBuffBan doBuffBan = new DoBuffBan();

    public JFrameSendBufflevel() {
        initComponents();
    }

    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        level = new javax.swing.JTextField();
        name = new javax.swing.JTextField();
        xacnhan = new javax.swing.JButton();

        level.setText("0");
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("");

        jLabel1.setText("Tên Nhân Vật");

        jLabel2.setText("Nhập level");

        xacnhan.setText("Xác nhận");
        xacnhan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                xacnhanActionPerformed(evt);
            }

            private void xacnhanActionPerformed(ActionEvent evt) {
                if (name.getText().equals("") || level.getText().equals("")) {
                    JOptionPane.showMessageDialog(rootPane, "Vui lòng nhập đủ các trường!");
                } else {
                    Char p = Char.findCharByName(name.getText());
                    if (p != null) {
                        if (!checkNumber(level.getText())) {
                            JOptionPane.showMessageDialog(rootPane, "Sai định dạng!");
                            return;
                        }
                        try {
                            int levelValue = Integer.parseInt(level.getText());
                            if (levelValue <= 0 || levelValue > 150) {
                                JOptionPane.showMessageDialog(rootPane, "Dữ liệu không đúng ");
                                return;
                            }
                            long exp = NinjaUtils.getExpFromLevel(levelValue);
                            exp -= p.exp;
                            p.addExp(exp);
                            p.getService().loadAll();
                        } catch (NumberFormatException e) {
                            JOptionPane.showMessageDialog(rootPane, "Sai định dạng số!");
                        }
                    } else {
                        JOptionPane.showMessageDialog(rootPane, "Người này không tồn tại hoặc không online!");
                    }
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
                                        .addComponent(jLabel2))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(level)
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
                                        .addComponent(level, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                new JFrameSendBufflevel().setVisible(true);
            }
        });
    }
}
