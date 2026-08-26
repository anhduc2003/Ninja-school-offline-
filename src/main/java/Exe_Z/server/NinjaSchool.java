package Exe_Z.server;

import java.awt.Button;
import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.ImageIcon;

import Exe_Z.model.Char;
import Exe_Z.stall.StallManager;
import Exe_Z.clan.Clan;
import Exe_Z.db.jdbc.DbManager;
import Exe_Z.store.Store;
import Exe_Z.event.Event;
import Exe_Z.model.RandomItem;
import Exe_Z.shopcoin.ShopitemTB1;
import Exe_Z.store.StoreManager;
import Exe_Z.util.Log;
import Exe_Z.util.NinjaUtils;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Panel;
import java.awt.ScrollPane;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class NinjaSchool extends WindowAdapter implements ActionListener {

    private Frame frame;
    private JFrame buffFrame = null;

    public static boolean isStop = false;
    private JTextPane infoTextArea;
    private JLabel actionLabel;

    public NinjaSchool() {
        try {
            frame = new Frame("");
            InputStream is = getClass().getClassLoader().getResourceAsStream("icon.png");
            byte[] data = new byte[is.available()];
            is.read(data);
            ImageIcon img = new ImageIcon(data);
            frame.setIconImage(img.getImage());
            frame.setSize(600, 500);
            frame.setBackground(Color.BLACK);
            frame.setResizable(false);
            frame.addWindowListener(this);

            Panel buttonPanel = new Panel();
            buttonPanel.setLayout(new GridLayout(12, 1, 5, 5));
            buttonPanel.setBounds(10, 50, 210, 450);

            String[] buttonLabels = {
                "Bảo trì", "Lưu Data Player", "Gửi Đồ", "Load Gosho",
                "User", "Ban", "Gửi ngọc", "Thu Hồi",
                "Load Exp", "Load Item", "Buff Đồ", "Cộng Level", "Nạp Tiền",
                "Lưu Data Shinwa", "Lưu Data Gia Tộc", "Load ShopCoin",
                "Load Ninja TLS", "Tối Ưu Ram", "Tối Ưu CPU"
            };
            String[] buttonCommands = {
                "stop", "player", "sendItem", "goosho",
                "xem", "ban", "addGems", "deleteItem",
                "expserver", "update", "buffban", "bufflevel", "buffcoin",
                "shinwaexe", "clanexe", "shopcoin",
                "njtl", "toiuuram", "toiuucpu"
            };

            for (int i = 0; i < buttonLabels.length; i++) {
                Button button = new Button(buttonLabels[i]);
                button.setActionCommand(buttonCommands[i]);
                button.addActionListener(this);
                buttonPanel.add(button);
            }

            infoTextArea = new JTextPane();
            infoTextArea.setEditable(false);
            infoTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            infoTextArea.setForeground(Color.WHITE);
            infoTextArea.setBackground(Color.DARK_GRAY);
            JScrollPane scrollPane = new JScrollPane(infoTextArea);
            scrollPane.setBounds(240, 50, 350, 400);

            actionLabel = new JLabel("...");
            actionLabel.setBounds(240, 460, 350, 30);
            actionLabel.setOpaque(true);
            actionLabel.setBackground(Color.LIGHT_GRAY);
            actionLabel.setForeground(Color.BLACK);
            actionLabel.setHorizontalAlignment(SwingConstants.CENTER);
            actionLabel.setFont(new Font("Arial", Font.BOLD, 14));
            actionLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    clearActionMessage();
                }
            });

            Timer timer = new Timer(1000, e -> updateInfo());
            timer.start();

            frame.setLayout(null);
            frame.add(buttonPanel);
            frame.add(scrollPane);
            frame.add(actionLabel);

            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        } catch (IOException ex) {
            Logger.getLogger(NinjaSchool.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private void showActionMessage(String message) {
        actionLabel.setText(message);
        actionLabel.setBackground(Color.YELLOW);
    }

    private void clearActionMessage() {
        actionLabel.setText("");
        actionLabel.setBackground(Color.LIGHT_GRAY);
    }
    
    private void updateInfo() {
        double mb = 1024 * 1024;
        Runtime runtime = Runtime.getRuntime();
        long total = runtime.totalMemory();
        long free = runtime.freeMemory();
        long used = total - free;
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class
        );
        double processCpuLoad = osBean.getProcessCpuLoad() * 100;

        try {
            StyledDocument doc = infoTextArea.getStyledDocument();
            doc.remove(0, doc.getLength());
            appendText(doc, "Người chơi online: " + ServerManager.getNumberOnline() + "\n", Color.orange);
//            appendText(doc, "Số luồng: " + Thread.activeCount() + "\n", getThreadColor(Thread.activeCount()));
            appendText(doc, String.format("Memory SRC dùng: %.2f / %.2f MB (%d%%)\n",
                    used / mb, total / mb, (used * 100 / total)),
                    getColor((used * 100) / total));
            appendText(doc, String.format("CPU SRC dùng: %.2f%%\n", processCpuLoad), getColor((long) processCpuLoad));
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    private void appendText(StyledDocument doc, String text, Color color) throws BadLocationException {
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setForeground(attrs, color);
        doc.insertString(doc.getLength(), text, attrs);
    }

    private Color getThreadColor(int threadCount) {
        if (threadCount > 1000) {
            return Color.RED;
        } else if (threadCount > 500) {
            return Color.ORANGE;
        } else {
            return Color.GREEN;
        }
    }

    private Color getColor(long Percentage) {
        if (Percentage > 80) {
            return Color.RED;
        } else if (Percentage > 50) {
            return Color.ORANGE;
        } else {
            return Color.GREEN;
        }
    }

    private Color randomColor() {
        Random rand = new Random();
        int red = rand.nextInt(256);
        int green = rand.nextInt(256);
        int blue = rand.nextInt(256);
        return new Color(red, green, blue);
    }

    public static void main(String args[]) {
        if (Config.getInstance().load()) {
            if (!DbManager.getInstance().start()) {
                return;
            }
            if (NinjaUtils.availablePort(Config.getInstance().getPort())) {
                boolean headless = GraphicsEnvironment.isHeadless()
                        || Boolean.parseBoolean(System.getProperty("ninja.headless", "false"));
                if (headless) {
                    Log.info("Chạy ở chế độ headless; giao diện quản trị Swing đã được tắt.");
                } else {
                    new NinjaSchool();
                }
                if (!Server.init()) {
                    Log.error("Khoi tao that bai!");
                    return;
                }
                Server.start();
            } else {
                Log.error("Port " + Config.getInstance().getPort() + " da duoc su dung!");
            }
        } else {
            Log.error("Vui long kiem tra lai cau hinh!");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("shinwaexe")) {
            if (Server.start) {
                Log.info("Lưu Shinwa");
                StallManager.getInstance().save();
                Log.info("Lưu xong");
            } else {
                Log.info("Mãy chủ chưa bật");
            }
        }
        if (e.getActionCommand().equals("stop")) {
            if (Server.start) {
                if (!isStop) {
                    (new Thread(new Runnable() {
                        public void run() {
                            try {
                                Server.maintance();
                                System.exit(0);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    })).start();
                }

            } else {
                Log.info("Máy chủ chưa bật.");
            }
        }
        if (e.getActionCommand().equals("clanexe")) {
            Log.info("Lưu dữ liệu gia tộc.");
            List<Clan> clans = Clan.getClanDAO().getAll();
            synchronized (clans) {
                for (Clan clan : clans) {
                    Clan.getClanDAO().update(clan);
                }
            }
            Log.info("Lưu xong");
        }
        if (e.getActionCommand().equals("player")) {
            List<Char> chars = ServerManager.getChars();
            for (Char _char : chars) {
                _char.saveData();
            }
            Log.info("Làm mới bảng xếp hạng");
            Ranked.refresh();
        }
        if (e.getActionCommand().equals("player")) {
            Log.info("Lưu dữ liệu người chơi");
            List<Char> chars = ServerManager.getChars();
            for (Char _char : chars) {
                try {
                    if (_char != null && !_char.isCleaned) {
                        _char.saveData();
                        if (_char.clone != null && !_char.clone.isCleaned) {
                            _char.clone.saveData();
                        }
                        if (_char.user != null && !_char.user.isCleaned) {
                            if (_char.user != null) {
                                _char.user.saveData();
                            }

                        }

                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            Log.info("Lưu xong");
        }
//        if (e.getActionCommand().equals("restartDB")) {
//            Log.info("Bắt đầu khởi động lại!");
//            DbManager.getInstance().shutdown();
//            DbManager.getInstance().start();
//            Log.info("Khởi động xong!");
//        }
        if (e.getActionCommand().equals("sendItem")) {
            JFrameSendItem.run();
        }
        if (e.getActionCommand().equals("goosho")) {
            StoreManager.getInstance().Reload();
            Store store = StoreManager.getInstance().find((byte) 14);
            if (store != null) {
                store.getItems().clear();
                store.load();
//                    if (Config.getInstance().isTestVersion()) {
//                        Server.storeTestInit();
//                        Server.storeTestInitngoc();
//                    }
                Event event = Event.getEvent();
                if (event != null) {
                    event.loadEventPoint();
                    event.initStore();
                }
                GlobalService.getInstance().chat("Hệ Thống", "cửa hàng Goosho đã được làm mới xin hãy đổi lại tab Goosho để cập nhập");
                Log.info("đã load Goosho");
            }
        }
        if (e.getActionCommand().equals("xem")) {
            OnlinePlayersFrame.display();
        }

        if (e.getActionCommand().equals("ban")) {
            JFrameBan.run();
        }

        if (e.getActionCommand().equals("processUser")) {
            JFrameUserProcess.run();
        }

        if (e.getActionCommand().equals("addItem")) {
            JFrameSearchItem.run();
        }
        if (e.getActionCommand().equals("deleteItem")) {
            JFrameItemDele.run();
        }
//        if (e.getActionCommand().equals("buffbot")) {
//            JFrameSendGoiBot.run();
//        }
        if (e.getActionCommand().equals("infoserver")) {
            showServerInfo();
        }
        if (e.getActionCommand().equals("update")) {
            RandomItem.abc("item_roi/event_Halloween");
            RandomItem.abc("item_roi/event_LunarNewYear");
            RandomItem.abc("item_roi/event_Noel");
            RandomItem.abc("item_roi/event_SumMer");
            RandomItem.abc("item_roi/event_NhaGiaoVN");
            RandomItem.abc("item_roi/event_TrungThu");
            RandomItem.abc("item_roi/loai_khac");
            RandomItem.abc("item_roi/map_LDGT");
            RandomItem.abc("item_roi/map_VDMQ");
            RandomItem.abc("item_roi/map_langco");
            RandomItem.abc("item_roi/map_langtruyenthuyet");
            RandomItem.abc("item_roi/map_thuong");
            RandomItem.abc("item_roi/LatHinh");
        }
        if (e.getActionCommand().equals("expserver")) {
            Server.TitleServer();
            Log.info("Load Exp");
        }
        if (e.getActionCommand().equals("confexp")) {
            new ExpChangePanel().setVisible(true);
        }
        if (e.getActionCommand().equals("bufflevel")) {
            JFrameSendBufflevel.run();
        }
        if (e.getActionCommand().equals("buffcoin")) {
            JFrameSendBuffcoin.run();
        }
        if (e.getActionCommand().equals("buffban")) {
            JFrameSendBuff.run();
        }
//        if (e.getActionCommand().equals("buffPlayer")) {
//            openBuffPlayerFrame();
//        }
        if (e.getActionCommand().equals("addGems")) {
            SendGems.run();
        }
        if (e.getActionCommand().equals("shopcoin")) {
            ShopitemTB1 shopitemTB1 = ShopitemTB1.getInstance();
            if (shopitemTB1 != null) {
                shopitemTB1.item_shop.clear();
                shopitemTB1.loadDataFromDatabase();
                GlobalService.getInstance().chat("Hệ Thống", "Cửa hàng Coin đã được làm mới, xin vui lòng đổi lại tab Cửa hàng Coin để cập nhật");
                Log.info("Load Cửa hàng Coin");
            }
        }
        if (e.getActionCommand().equals("njtl")) {
            Config.getInstance().reloadnjtl();
        }
        if (e.getActionCommand().equals("toiuuram")) {
            optimizeMemory();
        }
        if (e.getActionCommand().equals("toiuucpu")) {
            optimizeCPU();
        }
    }
    
    private void optimizeMemory() {
        long beforeUsedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        System.gc();
        long afterUsedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        Log.info(String.format("Memory Clear: %.2f MB", beforeUsedMemory / (1024.0 * 1024)));
        Log.info(String.format("Memory New: %.2f MB", afterUsedMemory / (1024.0 * 1024)));
    }
    
    private final ConcurrentHashMap<Thread, AtomicBoolean> threadFlags = new ConcurrentHashMap<>();
    
    public void optimizeCPU() {
        int pausedTasks = 0;

        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            if (thread.getName().startsWith("HighCPU") || thread.getName().contains("Worker")) {
                AtomicBoolean flag = new AtomicBoolean(false);
                threadFlags.put(thread, flag);

                pausedTasks++;
                Log.info("Đã yêu cầu tạm dừng luồng: " + thread.getName());
            }
        }
        Timer timer = new Timer(5000, e -> resumeThreads());
        timer.setRepeats(false);
        timer.start();

        Log.info("Toi Uu CPU Thanh Cong: " + pausedTasks);
    }
    private void resumeThreads() {
        for (Thread thread : threadFlags.keySet()) {
            AtomicBoolean flag = threadFlags.get(thread);
            if (flag != null) {
                flag.set(true);
                Log.info("Đã tiếp tục luồng: " + thread.getName());
            }
        }
        threadFlags.clear();
        Log.info("Clear CPU xong.");
    }

    public void showServerInfo() {
        long total, free, used;
        double mb = 1024 * 1024;
        Runtime runtime = Runtime.getRuntime();
        total = runtime.totalMemory();
        free = runtime.freeMemory();
        used = total - free;
        StringBuilder sb = new StringBuilder();
        sb.append("- Số luồng: ").append(Thread.activeCount()).append("\n");
        sb.append("- Số người đang online: ").append(ServerManager.getNumberOnline()).append("\n");
        sb.append("- Memory usage (JVM): ")
                .append(String.format("%.1f/%.1f MB (%d%%)", used / mb, total / mb, (used * 100 / total))).append("\n");
        JOptionPane.showMessageDialog(null, sb.toString(), "Thông Tin", JOptionPane.INFORMATION_MESSAGE);
        return;
    }

    public void windowClosing(WindowEvent e) {
        frame.dispose();
        if (Server.start) {
            Log.info("Đóng máy chủ.");
            Server.stop();
            System.exit(0);
        }
    }
}
