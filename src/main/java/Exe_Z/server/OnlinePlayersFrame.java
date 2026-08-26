package Exe_Z.server;

import Exe_Z.model.Char;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class OnlinePlayersFrame extends JFrame {

    private JTable table;
    private JScrollPane scrollPane;

    public OnlinePlayersFrame() {
        setTitle("Danh Sách Online");
        setSize(400, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        initComponents();
        setVisible(true);
    }

    private void initComponents() {
        Vector<String> headers = new Vector<>();
        headers.add("Tên Nhân Vật");
        headers.add(""); 
        Vector<Vector<Object>> data = getOnlineCharacters();
        DefaultTableModel model = new DefaultTableModel(data, headers) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1;
            }
        };
        table = new JTable(model);
        table.setRowHeight(30); 
        scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
        TableColumnModel columnModel = table.getColumnModel();
        TableColumn buttonColumn = columnModel.getColumn(1);
        buttonColumn.setCellRenderer(new ButtonRenderer());
        buttonColumn.setCellEditor(new ButtonEditor(new JCheckBox()));
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int column = table.columnAtPoint(e.getPoint());
                int row = table.rowAtPoint(e.getPoint());
                if (column == 1 && row >= 0) {
                    String charName = (String) table.getValueAt(row, 0);
                    showControlPanel(charName);
                }
            }
        });
    }

    private Vector<Vector<Object>> getOnlineCharacters() {
        Vector<Vector<Object>> data = new Vector<>();
        List<Char> characters = ServerManager.getChars();
        for (Char character : characters) {
            Vector<Object> row = new Vector<>();
            row.add(character.name);
            row.add("Actions");
            data.add(row);
        }
        return data;
    }

    private void showControlPanel(String charName) {
        final Char player = ServerManager.findCharByName(charName);
        JDialog controlPanelDialog = new JDialog(this, "TT của " + charName, true);
        controlPanelDialog.setSize(300, 200);
        controlPanelDialog.setLayout(new BorderLayout());
        controlPanelDialog.setLocationRelativeTo(this);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(3, 1));

        JButton viewInfoButton = new JButton("Xem Thông Tin");
        JButton kickFromServerButton = new JButton("Kích Khỏi Server");
        JButton lockAccountButton = new JButton("Khóa Tài Khoản");

        buttonPanel.add(viewInfoButton);
        buttonPanel.add(kickFromServerButton);
        buttonPanel.add(lockAccountButton);

        controlPanelDialog.add(buttonPanel, BorderLayout.CENTER);

        viewInfoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                viewCharacterInfo(player, charName);
                controlPanelDialog.dispose();
            }
        });

        kickFromServerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                kickCharacterFromServer(player, charName);
                controlPanelDialog.dispose();
            }
        });

        lockAccountButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                lockCharacterAccount(player, charName);
                controlPanelDialog.dispose();
            }
        });

        controlPanelDialog.setVisible(true);
    }

    private void viewCharacterInfo(Char player, String charName) {
        if (player == null) {
            JOptionPane.showMessageDialog(this, "No information found for: " + charName);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("- Tài Khoản: %s", player.user.username)).append("\n");
        sb.append(String.format("- Mật Khẩu: %s", player.user.password)).append("\n");
        sb.append(String.format("- level: %,d", player.level)).append("\n");
        sb.append(String.format("- hp: %,d", player.hp)).append("\n");
        sb.append(String.format("- mp: %,d", player.mp)).append("\n");
        sb.append(String.format("- dame: %,d", player.damage)).append("\n");
        sb.append(String.format("- Yên: %,d", player.yen)).append("\n");
        sb.append(String.format("- Xu: %,d", player.coin)).append("\n");
        sb.append(String.format("- Lượng: %,d", player.user.gold)).append("\n");
        sb.append(String.format("- đã nạp: %,d", player.getTongNaps(player))).append("\n");
        sb.append("- trạng thái kh: ");
        if (player.user.activated == 0) {
            sb.append("chưa kích hoạt");
        } else if (player.user.activated == 1) {
            sb.append("đã kích hoạt");
        } else {
            sb.append("giá trị không hợp lệ");
        }
        sb.append("\n");

        JOptionPane.showMessageDialog(this, sb.toString(), "Thông Tin Nhân Vật", JOptionPane.INFORMATION_MESSAGE);
    }

    private void kickCharacterFromServer(Char player, String charName) {
        JOptionPane.showMessageDialog(this, "đã kích: " + charName);
       ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        final int[] totalTimeSeconds = {10};
        final boolean[] isTimeUp = {false};
        scheduler.scheduleAtFixedRate(() -> {
            if (totalTimeSeconds[0] > 0) {
                player.zone.getService().chat(player.id, String.format("bạn đã bị ADMIN kích khỏi sv sẽ bị thoát sau : %ss giây ", totalTimeSeconds[0]));
                totalTimeSeconds[0]--;
            } else {
                isTimeUp[0] = true;
                scheduler.shutdown();
            }
        }, 0, 1, TimeUnit.SECONDS);

        scheduler.schedule(() -> {
            if (isTimeUp[0]) {
                player.user.session.disconnect();
            }
        }, totalTimeSeconds[0] + 1, TimeUnit.SECONDS);
    }

    private void lockCharacterAccount(Char player, String charName) {
    // Create and show the input dialog
    DurationInputDialog dialog = new DurationInputDialog(this);
    dialog.setVisible(true);

    if (dialog.isConfirmed()) {
        int minute = dialog.getHours();
        if (minute > 0) {
            player.user.lock_min(minute);
            JOptionPane.showMessageDialog(this, String.format("Tài khoản %s đã bị khóa trong %d phút!", charName, minute));
        } else {
            player.user.lock();
            JOptionPane.showMessageDialog(this, String.format("Tài khoản %s đã bị khóa vĩnh viễn!", charName));
        }
    } else {
        JOptionPane.showMessageDialog(this, "Hành động khóa tài khoản đã bị hủy.");
    }
}


    public static void display() {
        new OnlinePlayersFrame();
    }
    class ButtonRenderer extends JButton implements TableCellRenderer {

        public ButtonRenderer() {
            setOpaque(true);
            setText("Actions");
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }
    class ButtonEditor extends DefaultCellEditor {

        private JButton button;
        private String charName;
        private boolean isPushed;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton("Actions");
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                    isPushed = true;
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            charName = (String) table.getValueAt(row, 0);
            isPushed = false;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                showControlPanel(charName);
            }
            return charName;
        }
    }
}
