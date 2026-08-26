package Exe_Z.server;

import Exe_Z.constants.ItemName;
import Exe_Z.item.Item;
import Exe_Z.item.ItemFactory;
import Exe_Z.model.Char;
import Exe_Z.option.ItemOption;
import Exe_Z.util.NinjaUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Soanlv
 */
public class SendGems extends JFrame {
    JTextField player;
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JLabel jLabel3;
    JTextField quantity;
    JTextField upgrade;
    JButton xacnhan;

    private void initCompnent() {
        jLabel1 = new JLabel();
        jLabel2 = new JLabel();
        jLabel3 = new JLabel();
        quantity = new javax.swing.JTextField();
        player = new javax.swing.JTextField();
        upgrade = new javax.swing.JTextField();
        xacnhan = new javax.swing.JButton();
        quantity.setText("1");
        upgrade.setText("1");
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("");
        jLabel1.setText("Tên");

        jLabel2.setText("Số lượng");

        jLabel3.setText("Nâng cấp");
        xacnhan.setText("Xác nhận");


        xacnhan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                xacnhanActionPerformed(evt);
            }

            private void xacnhanActionPerformed(ActionEvent evt) {
                if (player.getText().equals("") || upgrade.getText().equals("")
                        || quantity.getText().equals("")) {
                    JOptionPane.showMessageDialog(rootPane, "Vui lòng nhập đủ các trường!");
                } else {
                    Char chars = Char.findCharByName(player.getText());
                    if (chars != null) {
                        if (!checkNumber(quantity.getText()) || !checkNumber(upgrade.getText())) {
                            JOptionPane.showMessageDialog(rootPane, "Sai định dạng!");
                            return;
                        }
                        int[] idngoc = {ItemName.LUC_NGOC, ItemName.LAM_TINH_NGOC, ItemName.HUYET_NGOC, ItemName.HUYEN_TINH_NGOC};
                        int itemID = idngoc[NinjaUtils.nextInt(idngoc.length)];
                       addNgoc(itemID,Integer.parseInt(upgrade.getText()),false,false,chars,Integer.parseInt(quantity.getText()));
                    } else {
                        JOptionPane.showMessageDialog(rootPane, "Người này không tồn tại hoặc không online!");
                    }
                }
//                JOptionPane.showMessageDialog(rootPane, "" + player.getText() + " Nhận ngọc thành côgn");
            }
        });

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(xacnhan)
                                .addGap(110, 110, 110))
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(26, 26, 26)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(jLabel1)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 25, Short.MAX_VALUE)
                                                                .addComponent(player, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addComponent(jLabel2)
                                                                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                                .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING)
                                                                        ))
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                                        .addComponent(player, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                                                                        .addComponent(quantity)
                                                                        .addComponent(upgrade)
                                                                ))
                                                ))
                                )
                                .addContainerGap(27, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(player, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel1))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(quantity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel2))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(upgrade, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel3))
                                .addGap(19, 19, 19)
                                .addComponent(xacnhan)
                                .addGap(16, 16, 16))
        );

        pack();
    }

    public static boolean checkNumber(String str) {
        if (str.matches("[0-9]+")) {
            return true;
        } else {
            return false;
        }
    }

    public static void run() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new SendGems().setVisible(true);
            }
        });
    }

    public SendGems() {
        initCompnent();
    }

    public void addNgoc(int itemID, int upgrade, boolean max, boolean lock, Char _char, int quantity) {
        Item itemNgoc = ItemFactory.getInstance().newGem(itemID, max);
        itemNgoc.setLock(lock);
        int n = quantity;
        if (itemNgoc.template.isUpToUp) {
            n = 1;
        }
        for (int j = 0; j < n; j++) {
            Item newItem = ItemFactory.getInstance().newGem(itemNgoc.id, max);
            if (itemNgoc.template.isUpToUp) {
                newItem.setQuantity(n);
            } else {
                newItem.setQuantity(1);
            }
            newItem.isLock = lock;
            for (int i = newItem.upgrade; i < upgrade; i++) {
                newItem.upgrade++;
                for (ItemOption option : newItem.options) {
                    switch (option.optionTemplate.id) {
                        case 73:
                            // tấn công
                            if (option.param > 0) {
                                int[] paramUp = {0, 50, 100, 150, 200, 250, 300, 350, 400, 450};
                                option.param += paramUp[i];
                            } else {
                                option.param -= 100;
                            }
                            break;
                        case 115:
                            // né đòn
                            if (option.param > 0) {
                                int[] paramUp = {0, 10, 20, 30, 40, 50, 60, 70, 80, 90};
                                option.param += paramUp[i];
                            } else {
                                option.param -= 10;
                            }
                            break;
                        case 116:
                            // chính xác
                            if (option.param > 0) {
                                int[] paramUp = {0, 10, 20, 30, 40, 50, 60, 70, 80, 90};
                                option.param += paramUp[i];
                            } else {
                                option.param -= 10;
                            }
                            break;
                        case 124:
                            // giảm trừ sát thương
                            if (option.param > 0) {
                                int[] paramUp = {0, 10, 15, 20, 25, 30, 35, 40, 45, 50};
                                option.param += paramUp[i];
                            } else {
                                option.param -= 5;
                            }
                            break;
                        case 114:
                            // chí mạng
                            if (option.param > 0) {
                                int[] paramUp = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
                                option.param += paramUp[i];
                            } else {
                                option.param -= 5;
                            }
                            break;
                        case 126:
                            // phản đòn
                            if (option.param > 0) {
                                int[] paramUp = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
                                option.param += paramUp[i];
                            } else {
                                option.param -= 5;
                            }
                            break;
                        case 118:
                            // kháng tất cả
                            if (option.param > 0) {
                                int[] paramUp = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
                                option.param += paramUp[i];
                            } else {
                                option.param -= 5;
                            }
                            break;
                        case 102:
                            // sát thương lên quái
                            if (option.param > 0) {
                                int[] paramUp = {0, 100, 200, 400, 600, 800, 1000, 1200, 1400, 1600};
                                option.param += paramUp[i];
                            } else {
                                option.param -= 500;
                            }
                            break;
                        case 105:
                            // sát thương chí mạng
                            // lên 6 2600
                            if (option.param > 0) {
                                int[] paramUp = {0, 100, 200, 300, 400, 500, 600, 700, 900, 1200};
                                option.param += paramUp[i];
                            } else {
                                option.param -= 500;
                            }
                            break;
                        case 103:
                            // sát thương lên người
                            // lên 4 900 , lên 5 1500 , lên 6 2300
                            if (option.param > 0) {
                                int[] paramUp = {0, 100, 200, 300, 400, 500, 600, 700, 900, 1200};
                                option.param += paramUp[i];
                            } else {
                                option.param -= 200;
                            }
                            break;
                        case 121:
                            // kháng sát thương chí mạng
                            if (option.param > 0) {
                                int[] paramUp = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
                                option.param += paramUp[i];
                            } else {
                                option.param -= 5;
                            }
                            break;
                        case 117:
                        case 125:
                            // hp, mp tối đa
                            if (option.param > 0) {
                                int[] paramUp = {0, 100, 200, 300, 400, 500, 600, 700, 800, 900};
                                option.param += paramUp[i];
                            } else {
                                option.param -= 50;
                            }
                            break;
                        case 119:
                        case 229:
                            // hồi phục hp,
                            // mp
                            if (option.param > 0) {
                                int[] paramUp = {0, 2, 4, 6, 8, 10, 12, 14, 16, 18};
                                option.param += paramUp[i];
                            } else {
                                option.param -= 10;
                            }
                            break;
                        case 123:
                            int[] giaKham = {800000, 1600000, 2400000, 3200000, 4800000, 7200000, 10800000, 15600000,
                                    20100000, 28100000};
                            option.param = giaKham[i];
                            break;
                        default:
                            break;
                    }
                }
            }
            _char.addItemToBag(newItem);
        }

    }
}
