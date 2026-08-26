package Exe_Z.server;

import Exe_Z.constants.ItemName;
import Exe_Z.constants.ItemOptionName;
import Exe_Z.item.Item;
import Exe_Z.item.ItemFactory;
import Exe_Z.model.Char;
import Exe_Z.model.DoBuffBan;
import Exe_Z.option.ItemOption;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;

public class JFrameSendBuff extends JFrame {

    private javax.swing.JTextField idItem;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JButton xacnhan;
    private javax.swing.JButton huongDan;
    private javax.swing.JTextField name;
    private JFrame controlPanel;
    DoBuffBan doBuffBan = new DoBuffBan();
    public JFrameSendBuff() {
        initComponents();
    }

    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        idItem = new javax.swing.JTextField();
        name = new javax.swing.JTextField();
        xacnhan = new javax.swing.JButton();
        huongDan = new javax.swing.JButton();

        idItem.setText("0");
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("");

        jLabel1.setText("Tên Nhân Vật");

        jLabel2.setText("Nhập ID(xem hướng dẫn)");

        xacnhan.setText("Xác nhận");
        xacnhan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                xacnhanActionPerformed(evt);
            }

            private void xacnhanActionPerformed(ActionEvent evt) {
                if (name.getText().equals("") || idItem.getText().equals("")) {
                    JOptionPane.showMessageDialog(rootPane, "Vui lòng nhập đủ các trường!");
                } else {
                    Char p = Char.findCharByName(name.getText());
                    if (p != null) {
                        if (!checkNumber(idItem.getText())) {
                            JOptionPane.showMessageDialog(rootPane, "Sai định dạng!");
                            return;
                        }
                        if (Integer.parseInt(idItem.getText()) == 0) {
                            JOptionPane.showMessageDialog(rootPane, "Không nhập gì lấy cặc mà buff");
                        }
                        if (Integer.parseInt(idItem.getText()) == 1) {
                            Item mount1 = ItemFactory.getInstance().newItem(ItemName.HOA_KY_LAN);
                            mount1.options.add(new ItemOption(ItemOptionName.NE_DON_ADD_POINT_TYPE_1, 190));
                            mount1.options.add(new ItemOption(ItemOptionName.CHINH_XAC_ADD_POINT_TYPE_1, 190));
                            mount1.options.add(new ItemOption(ItemOptionName.TAN_CONG_KHI_DANH_CHI_MANG_POINT_PERCENT_TYPE_1, 95));
                            mount1.options.add(new ItemOption(ItemOptionName.CHI_MANG_ADD_POINT_TYPE_1, 190));
                            mount1.options.add(new ItemOption(ItemOptionName.TAN_CONG_POINT_TYPE_1, 1900));
                            mount1.options.add(new ItemOption(ItemOptionName.CONG_THEM_TIEM_NANG_ADD_POINT_PERCENT_TYPE_0, 20));
                            mount1.options.add(new ItemOption(ItemOptionName.TAN_CONG_ADD_POINT_PERCENT_TYPE_8, 20));
                            mount1.options.add(new ItemOption(173, 100));
                            mount1.sys = 4;
                            mount1.upgrade=99;
                            mount1.isLock = false;
                            p.addItemToBag(mount1);
                        }
                        if (Integer.parseInt(idItem.getText()) == 2) {
                            Item mount2 = ItemFactory.getInstance().newItem(ItemName.HOA_KY_LAN);
                            mount2.options.add(new ItemOption(ItemOptionName.NE_DON_ADD_POINT_TYPE_1, 190));
                            mount2.options.add(new ItemOption(ItemOptionName.CHINH_XAC_ADD_POINT_TYPE_1, 190));
                            mount2.options.add(new ItemOption(ItemOptionName.TAN_CONG_KHI_DANH_CHI_MANG_POINT_PERCENT_TYPE_1, 95));
                            mount2.options.add(new ItemOption(ItemOptionName.CHI_MANG_ADD_POINT_TYPE_1, 190));
                            mount2.options.add(new ItemOption(ItemOptionName.TAN_CONG_POINT_TYPE_1, 1900));
                            mount2.options.add(new ItemOption(ItemOptionName.CONG_THEM_TIEM_NANG_ADD_POINT_PERCENT_TYPE_0, 20));
                            mount2.options.add(new ItemOption(ItemOptionName.TAN_CONG_ADD_POINT_PERCENT_TYPE_8, 20));
                            mount2.options.add(new ItemOption(174, 100));
                            mount2.sys = 4;
                            mount2.upgrade=99;
                            mount2.isLock = false;
                            p.addItemToBag(mount2);
                        }
                        if (Integer.parseInt(idItem.getText()) == 3) {
                            Item mount3 = ItemFactory.getInstance().newItem(ItemName.HOA_KY_LAN);
                            mount3.options.add(new ItemOption(ItemOptionName.NE_DON_ADD_POINT_TYPE_1, 190));
                            mount3.options.add(new ItemOption(ItemOptionName.CHINH_XAC_ADD_POINT_TYPE_1, 190));
                            mount3.options.add(new ItemOption(ItemOptionName.TAN_CONG_KHI_DANH_CHI_MANG_POINT_PERCENT_TYPE_1, 95));
                            mount3.options.add(new ItemOption(ItemOptionName.CHI_MANG_ADD_POINT_TYPE_1, 190));
                            mount3.options.add(new ItemOption(ItemOptionName.TAN_CONG_POINT_TYPE_1, 1900));
                            mount3.options.add(new ItemOption(ItemOptionName.CONG_THEM_TIEM_NANG_ADD_POINT_PERCENT_TYPE_0, 20));
                            mount3.options.add(new ItemOption(ItemOptionName.TAN_CONG_ADD_POINT_PERCENT_TYPE_8, 20));
                            mount3.options.add(new ItemOption(175, 100));
                            mount3.sys = 4;
                            mount3.upgrade=99;
                            mount3.isLock = false;
                            p.addItemToBag(mount3);
                        }
                        if (Integer.parseInt(idItem.getText()) == 4) {
                            Item mount = ItemFactory.getInstance().newItem(ItemName.PHUONG_HOANG_BANG);
                            mount.options.add(new ItemOption(ItemOptionName.NE_DON_ADD_POINT_TYPE_1, 190));// né 
                            mount.options.add(new ItemOption(ItemOptionName.CHINH_XAC_ADD_POINT_TYPE_1, 190)); // chính xác
                            mount.options.add(new ItemOption(ItemOptionName.TAN_CONG_KHI_DANH_CHI_MANG_POINT_PERCENT_TYPE_1, 95));//tccm
                            mount.options.add(new ItemOption(ItemOptionName.CHI_MANG_ADD_POINT_TYPE_1, 190));//chí mạng
                            mount.options.add(new ItemOption(ItemOptionName.TAN_CONG_POINT_TYPE_1, 1900));// dame
                            mount.options.add(new ItemOption(ItemOptionName.CONG_THEM_TIEM_NANG_ADD_POINT_PERCENT_TYPE_0, 20));// tiềm năng cộng thêm
                            mount.options.add(new ItemOption(ItemOptionName.TAN_CONG_ADD_POINT_PERCENT_TYPE_8, 20));// % tấn công
                            mount.options.add(new ItemOption(ItemOptionName.KY_NANG_MUA_BANG_SUB_GAY_SAT_THUONG_30PERCENT_HP_CUA_MUC_TIEUSUB_PHAM_VI_SAT_THUONG_2MSUB_TY_LE_XH_POINT_PERCENT_TYPE_0, 100));
                            mount.options.add(new ItemOption(ItemOptionName.KY_NANG_VU_NO_BANG_GIA_SUB_PHAN_SAT_THUONG_20PERCENT_HP_MUC_TIEU_DANH_VAOSUB_TY_LE_XH_POINT_PERCENT_TYPE_0, 100));
                            mount.sys = 4;
                            mount.upgrade=99;
                            mount.isLock = false;
                            p.addItemToBag(mount);
                        }
//                        if (Integer.parseInt(idItem.getText()) == 5) {
//                            DoBuffBan.dovip(p);
//                        }
//                        if (Integer.parseInt(idItem.getText()) == 6) {
//                            DoBuffBan.dovip8(p);
//                        }
//                        if (Integer.parseInt(idItem.getText()) == 7) {
//                            DoBuffBan.do11x(p);
//                        }
//                        if (Integer.parseInt(idItem.getText()) == 8) {
//                            DoBuffBan.vk11x(p);
//                        }
//                        if (Integer.parseInt(idItem.getText()) == 9) {
//                            DoBuffBan.dohacam(p);
//                        }
//                        if (Integer.parseInt(idItem.getText()) == 10) {
//                            DoBuffBan.doht(p);
//                        }
//                        if (Integer.parseInt(idItem.getText()) == 11) {
//                            DoBuffBan.doLB(p);
//                        }
//                        if (Integer.parseInt(idItem.getText()) == 12) {
//                            DoBuffBan.vk12x(p);
//                        }
                    } else {
                        JOptionPane.showMessageDialog(rootPane, "Người này không tồn tại hoặc không online!");
                    }
                }
            }
        });

        huongDan.setText("Xem hướng dẫn");
        huongDan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                huongDanActionPerformed(evt);
            }

            private void huongDanActionPerformed(ActionEvent evt) {
                openControlPanel();
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
                                        .addComponent(idItem)
                                        .addComponent(name, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE))
                                .addContainerGap(30, Short.MAX_VALUE))
                        .addGroup(layout.createSequentialGroup()
                                .addGap(90, 90, 90)
                                .addComponent(xacnhan)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(huongDan)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                                        .addComponent(idItem, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(30, 30, 30)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(xacnhan)
                                        .addComponent(huongDan))
                                .addContainerGap(30, Short.MAX_VALUE))
        );

        pack();
    }

    private void openControlPanel() {
        if (controlPanel == null) {
            controlPanel = new JFrame("Hướng Dẫn");
            javax.swing.JLabel ghiChuLabel = new javax.swing.JLabel(""
                    + "<html>Nhập 1 nhận HKL có skill Hỏa Kích maxoption <br>"
                    + "<br>Nhập 2 nhận HKL có skill Cường Thân maxoption <br>"
                    + "<br>Nhập 3 nhận HKL có skill Hộ Thể maxoption <br>"
                    + "<br>Nhập 4 là nhận PHB có skill Mưa Băng maxoption<br>"
//                    + "<br>Nhập 5 là nhận set đồ jrai hoặc yumito +16 tl9 maxoption <br>"
//                    + "<br>Nhập 6 là nhận set đồ 10x +16 tl9 maxoption <br>"
//                    + "<br>Nhập 7 là nhận set đồ 11x +16 tl9 maxoption <br>"
//                    + "<br>Nhập 8 là nhận set 6 cái vũ khí 11x +16 tl9 maxoption <br>"
//                    + "<br>Nhập 9 là nhận set đồ Hắc Ám +16 tl9 maxoption <br>"
//                    + "<br>Nhập 10 là nhận set đồ Huyết Thù +16 tl9 maxoption <br>"
//                    + "<br>Nhập 11 là nhận set đồ Lục Bảo +16 tl9 maxoption <br>"
//                    + "<br>Nhập 12 là nhận set Vũ khí Lục Bảo 12x +16 tl9 maxoption <br>"
                    + "</html>");

            controlPanel.add(ghiChuLabel);
            controlPanel.setSize(300, 550);
            controlPanel.setLocationRelativeTo(null);
        }
        controlPanel.setVisible(true);
    }

    public static boolean checkNumber(String str) {
        return str.matches("[0-9]+");
    }

    public static void run() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new JFrameSendBuff().setVisible(true);
            }
        });
    }
}
