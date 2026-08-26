package Exe_Z.server;

import Exe_Z.db.jdbc.DbManager;
import Exe_Z.item.Equip;
import Exe_Z.model.Char;
import Exe_Z.model.User;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class JFrameItemDele extends JFrame {

    private JTextField idField;
    private JTextField usernameField;
    private JButton deleteButton;
    private JTextArea outputArea;
    private JRadioButton deleteUsersButton;
    private JRadioButton deleteCloneCharButton;
    private JRadioButton deleteMultiplePlayersButton;
    private ButtonGroup buttonGroup;

    public JFrameItemDele() {
        setTitle("Thu Hồi Item");
        setSize(400, 400);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        
        setLayout(null);

        JLabel idLabel = new JLabel("Nhập ID Item Cần Thu Hồi:");
        idLabel.setBounds(20, 20, 100, 25);
        add(idLabel);

        idField = new JTextField();
        idField.setBounds(100, 20, 150, 25);
        add(idField);

        JLabel usernameLabel = new JLabel("Nhập Username:");
        usernameLabel.setBounds(20, 60, 120, 25);
        add(usernameLabel);

        usernameField = new JTextField();
        usernameField.setBounds(140, 60, 150, 25);
        add(usernameField);

        deleteUsersButton = new JRadioButton("Thu Hồi Item All User");
        deleteUsersButton.setBounds(20, 100, 150, 25);
        add(deleteUsersButton);

        deleteCloneCharButton = new JRadioButton("Thu Hồi Item Phân Thân");
        deleteCloneCharButton.setBounds(180, 100, 200, 25);
        add(deleteCloneCharButton);

        deleteMultiplePlayersButton = new JRadioButton("Thu Hồi 1 Người");
        deleteMultiplePlayersButton.setBounds(20, 140, 250, 25);
        add(deleteMultiplePlayersButton);

        buttonGroup = new ButtonGroup();
        buttonGroup.add(deleteUsersButton);
        buttonGroup.add(deleteCloneCharButton);
        buttonGroup.add(deleteMultiplePlayersButton);

        deleteButton = new JButton("Thu Hồi");
        deleteButton.setBounds(260, 180, 100, 25);
        add(deleteButton);

        outputArea = new JTextArea();
        outputArea.setBounds(20, 220, 340, 130);
        outputArea.setEditable(false);
        add(outputArea);

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteButton.setEnabled(false);
                String username = usernameField.getText().trim();
                int id;
                try {
                    id = Integer.parseInt(idField.getText().trim());
                } catch (NumberFormatException ex) {
                    outputArea.setText("Invalid ID format.");
                    deleteButton.setEnabled(true);
                    return;
                }
                try {
                    if (deleteUsersButton.isSelected()) {
                        boolean result = deleteItemById(id);
                        if (result) {
                            outputArea.setText("Item có ID " + id + " đã được xóa khỏi Người dùng thành công.");
                        } else {
                            outputArea.setText("Không tìm thấy mục có ID " + id + " trong Người dùng.");
                        }
                    } else if (deleteCloneCharButton.isSelected()) {
                        deleteItemByIdPT(id);
                        outputArea.setText("Item có ID " + id + " đã bị xóa khỏi phân thân.");
                    } else if (deleteMultiplePlayersButton.isSelected()) {
                        if (username.isEmpty()) {
                            outputArea.setText("Vui lòng nhập tên người chơi.");
                            deleteButton.setEnabled(true);
                            return;
                        }
                        boolean result = deleteItemByUsername(username, id);
                        if (result) {
                            outputArea.setText("Item có ID " + id + " đã được xóa khỏi các người chơi với username " + username + " thành công.");
                        } else {
                            outputArea.setText("Không tìm thấy người chơi với username " + username + ".");
                        }
                    } else {
                        outputArea.setText("Vui lòng chọn tùy chọn xóa.");
                    }
                } catch (Exception ex) {
                    outputArea.setText("Đã xảy ra lỗi: " + ex.getMessage());
                } finally {
                    deleteButton.setEnabled(true);
                }
            }
        });
    }

    private boolean deleteItemById(int id) throws SQLException {
        boolean itemFound = false;
        Connection conn = DbManager.getInstance().getConnection(DbManager.LOAD_CHAR);
        List<UpdateData1> updates = new ArrayList<>();
        String query = "SELECT `id`, `user_id`, `bag`, `box`, `equiped`, `fashion` FROM `players`";
        PreparedStatement stmt = conn.prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            boolean modified = false;
            JSONArray bagArray = (JSONArray) JSONValue.parse(rs.getString("bag"));
            JSONArray boxArray = (JSONArray) JSONValue.parse(rs.getString("box"));
            JSONArray equipedArray = (JSONArray) JSONValue.parse(rs.getString("equiped"));
            JSONArray fashionArray = (JSONArray) JSONValue.parse(rs.getString("fashion"));

            JSONArray originalBagArray = (JSONArray) JSONValue.parse(rs.getString("bag"));
            JSONArray originalBoxArray = (JSONArray) JSONValue.parse(rs.getString("box"));
            JSONArray originalEquipArray = (JSONArray) JSONValue.parse(rs.getString("equiped"));
            JSONArray originalFashionArray = (JSONArray) JSONValue.parse(rs.getString("fashion"));

            bagArray = removeItemFromJsonArray(bagArray, id);
            boxArray = removeItemFromJsonArray(boxArray, id);
            equipedArray = removeItemFromJsonArray(equipedArray, id);
            fashionArray = removeItemFromJsonArray(fashionArray, id);

            if (bagArray.size() != originalBagArray.size()
                    || boxArray.size() != originalBoxArray.size()
                    || equipedArray.size() != originalEquipArray.size()
                    || fashionArray.size() != originalFashionArray.size()) {
                modified = true;
                itemFound = true;
                updates.add(new UpdateData1(rs.getInt("id"), bagArray, boxArray, equipedArray, fashionArray));
            }
        }
        closemmen();
        try {
            Connection connForUpdate = DbManager.getInstance().getConnection(DbManager.LOAD_CHAR);
            for (UpdateData1 updateData1 : updates) {
                String updateQuery = "UPDATE `players` SET `bag` = ?, `box` = ?, `equiped` = ?, `fashion` = ? WHERE `id` = ?";
                PreparedStatement updateStmt = connForUpdate.prepareStatement(updateQuery);
                updateStmt.setString(1, updateData1.getBagArray1().toJSONString());
                updateStmt.setString(2, updateData1.getBoxArray1().toJSONString());
                updateStmt.setString(3, updateData1.getEquipedArray1().toJSONString());
                updateStmt.setString(4, updateData1.getFashionArray1().toJSONString());
                updateStmt.setInt(5, updateData1.getId1());
                updateStmt.executeUpdate();
                updateStmt.close();
            }
            connForUpdate.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        stmt.close();
        conn.close();
        return itemFound;
    }

    private class UpdateData1 {
        private final int id;
        private final JSONArray bagArray;
        private final JSONArray boxArray;
        private final JSONArray equipedArray;
        private final JSONArray fashionArray;

        public UpdateData1(int id, JSONArray bagArray, JSONArray boxArray, JSONArray equipedArray, JSONArray fashionArray) {
            this.id = id;
            this.bagArray = bagArray;
            this.boxArray = boxArray;
            this.equipedArray = equipedArray;
            this.fashionArray = fashionArray;
        }

        public int getId1() {
            return id;
        }

        public JSONArray getBagArray1() {
            return bagArray;
        }

        public JSONArray getBoxArray1() {
            return boxArray;
        }

        public JSONArray getEquipedArray1() {
            return equipedArray;
        }

        public JSONArray getFashionArray1() {
            return fashionArray;
        }
    }

    private boolean deleteItemByUsername(String username, int id) throws SQLException {
        boolean itemFound = false;
        int userId = -1;
        Connection conn = DbManager.getInstance().getConnection(DbManager.LOAD_CHAR);
        List<UpdateData> updatess = new ArrayList<>();
        String query = "SELECT `id`, `user_id`, `bag`, `box`, `equiped`, `fashion` FROM `players` WHERE `name` = ?";
        PreparedStatement stmt = conn.prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
        stmt.setString(1, username);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            userId = rs.getInt("user_id");
            boolean modified = false;
            JSONArray bagArray = (JSONArray) JSONValue.parse(rs.getString("bag"));
            JSONArray boxArray = (JSONArray) JSONValue.parse(rs.getString("box"));
            JSONArray equipedArray = (JSONArray) JSONValue.parse(rs.getString("equiped"));
            JSONArray fashionArray = (JSONArray) JSONValue.parse(rs.getString("fashion"));
            JSONArray originalBagArray = (JSONArray) JSONValue.parse(rs.getString("bag"));
            JSONArray originalBoxArray = (JSONArray) JSONValue.parse(rs.getString("box"));
            JSONArray originalEquipArray = (JSONArray) JSONValue.parse(rs.getString("equiped"));
            JSONArray originalFashionArray = (JSONArray) JSONValue.parse(rs.getString("fashion"));
            bagArray = removeItemFromJsonArray(bagArray, id);
            boxArray = removeItemFromJsonArray(boxArray, id);
            equipedArray = removeItemFromJsonArray(equipedArray, id);
            fashionArray = removeItemFromJsonArray(fashionArray, id);
            if (bagArray.size() != originalBagArray.size()
                    || boxArray.size() != originalBoxArray.size()
                    || equipedArray.size() != originalEquipArray.size()
                    || fashionArray.size() != originalFashionArray.size()) {
                modified = true;
                itemFound = true;
                updatess.add(new UpdateData(rs.getInt("id"), bagArray, boxArray, equipedArray, fashionArray));
            }
        }
        if (userId != -1) {
            closemmen1(userId);
        }
        try {
            Connection connForUpdate = DbManager.getInstance().getConnection(DbManager.LOAD_CHAR);
            for (UpdateData updateData : updatess) {
                String updateQuery = "UPDATE `players` SET `bag` = ?, `box` = ?, `equiped` = ?, `fashion` = ? WHERE `id` = ?";
                PreparedStatement updateStmt = connForUpdate.prepareStatement(updateQuery);
                updateStmt.setString(1, updateData.getBagArray().toJSONString());
                updateStmt.setString(2, updateData.getBoxArray().toJSONString());
                updateStmt.setString(3, updateData.getEquipedArray().toJSONString());
                updateStmt.setString(4, updateData.getFashionArray().toJSONString());
                updateStmt.setInt(5, updateData.getId());
                updateStmt.executeUpdate();
                updateStmt.close();
            }
            connForUpdate.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        stmt.close();
        conn.close();
        return itemFound;
    }

    public static void closemmen1(int id) {
        try {
            User users = ServerManager.findUserByUserID(id);
            if (!users.isCleaned) {
                users.session.closeMessage();
            }
        } catch (Exception e) {
        }
    }
    
    private class UpdateData {

        private final int id;
        private final JSONArray bagArray;
        private final JSONArray boxArray;
        private final JSONArray equipedArray;
        private final JSONArray fashionArray;

        public UpdateData(int id, JSONArray bagArray, JSONArray boxArray, JSONArray equipedArray, JSONArray fashionArray) {
            this.id = id;
            this.bagArray = bagArray;
            this.boxArray = boxArray;
            this.equipedArray = equipedArray;
            this.fashionArray = fashionArray;
        }

        public int getId() {
            return id;
        }

        public JSONArray getBagArray() {
            return bagArray;
        }

        public JSONArray getBoxArray() {
            return boxArray;
        }

        public JSONArray getEquipedArray() {
            return equipedArray;
        }

        public JSONArray getFashionArray() {
            return fashionArray;
        }
    }

    private JSONArray removeItemFromJsonArray(JSONArray array, int id) {
        JSONArray newArray = new JSONArray();
        for (Object obj : array) {
            JSONObject item = (JSONObject) obj;
            if (item.get("id") != null && Integer.parseInt(item.get("id").toString()) != id) {
                newArray.add(item);
            }
        }
        return newArray;
    }

    public static void run() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new JFrameItemDele().setVisible(true);
            }
        });
    }

    private boolean deleteItemByIdPT(int id) throws SQLException {
        boolean itemFound = false;
        Connection conn = DbManager.getInstance().getConnection(DbManager.LOAD_CHAR);
        List<UpdateData2> updates = new ArrayList<>();
        String query = "SELECT `id`, `equiped`, `fashion` FROM `clone_char`;";
        PreparedStatement stmt = conn.prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            boolean modified = false;
            int recordId = rs.getInt("id");
            JSONArray equipedArray = (JSONArray) JSONValue.parse(rs.getString("equiped"));
            JSONArray fashionArray = (JSONArray) JSONValue.parse(rs.getString("fashion"));

            JSONArray originalEquipArray = (JSONArray) JSONValue.parse(rs.getString("equiped"));
            JSONArray originalFashionArray = (JSONArray) JSONValue.parse(rs.getString("fashion"));

            equipedArray = removeItemFromJsonArray(equipedArray, id);
            fashionArray = removeItemFromJsonArray(fashionArray, id);

            if (equipedArray.size() != originalEquipArray.size() 
                    || fashionArray.size() != originalFashionArray.size()) {
                modified = true;
                itemFound = true;
                updates.add(new UpdateData2(recordId, equipedArray, fashionArray));
            }
        }
        closemmen();
        stmt.close();
        conn.close();
        try {
            Connection connForUpdate = DbManager.getInstance().getConnection(DbManager.LOAD_CHAR);
            for (UpdateData2 updateData : updates) {
                String updateQuery = "UPDATE `clone_char` SET `equiped` = ?, `fashion` = ? WHERE `id` = ?";
                PreparedStatement updateStmt = connForUpdate.prepareStatement(updateQuery);
                updateStmt.setString(1, updateData.getEquipedArray2().toJSONString());
                updateStmt.setString(2, updateData.getFashionArray2().toJSONString());
                updateStmt.setInt(3, updateData.getId2());
                updateStmt.executeUpdate();
                updateStmt.close();
            }
            connForUpdate.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return itemFound;
    }

    private class UpdateData2 {
        private final int id;
        private final JSONArray equipedArray;
        private final JSONArray fashionArray;

        public UpdateData2(int id, JSONArray equipedArray, JSONArray fashionArray) {
            this.id = id;
            this.equipedArray = equipedArray;
            this.fashionArray = fashionArray;
        }

        public int getId2() {
            return id;
        }

        public JSONArray getEquipedArray2() {
            return equipedArray;
        }

        public JSONArray getFashionArray2() {
            return fashionArray;
        }
    }

    
    public static void closemmen() {
        try {
            List<User> users = ServerManager.getUsers();
            for (User user : users) {
                if (!user.isCleaned) {
                    user.session.closeMessage();
                }
            }
        } catch (Exception e) {
            System.err.println("Error in closing users: " + e.getMessage());
        }
    }
}
