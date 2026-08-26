//package Exe_Z.api;
//
//import Exe_Z.constants.ItemName;
//import Exe_Z.item.Item;
//import Exe_Z.item.ItemFactory;
//import Exe_Z.model.Char;
//import Exe_Z.model.User;
//import Exe_Z.server.ServerManager;
//import Exe_Z.util.NinjaUtils;
//import org.json.JSONArray;
//import org.json.JSONObject;
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.net.ServerSocket;
//import java.net.Socket;
//import org.json.JSONException;
//
//public class Ex implements Runnable {
//    private static final int PORT = 5500;
//    private static final String VALID_KEY = "klklllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllll";
//    
//    @Override
//    public void run() {
//        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
//            while (true) {
//                try (Socket socket = serverSocket.accept()) {
//                    InputStream input = socket.getInputStream();
//                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
//                    String message;
//                    while ((message = reader.readLine()) != null) {
//                        processMessage(message);
//                    }
//                } catch (IOException ex) {
//                    ex.printStackTrace();
//                }
//            }
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//    }
//
//    private void processMessage(String message) {
//        try {
//            JSONObject jsonObject = new JSONObject(message);
//            String position = jsonObject.optString("position", "");
//            int userId = jsonObject.optInt("userId", -1);
//            int amount = jsonObject.optInt("amount", 0);
//            int xu = jsonObject.optInt("xu", 0);
//            int yen = jsonObject.optInt("yen", 0);
//            String name = jsonObject.optString("name", "");
//            String key = jsonObject.optString("key", "");
//            JSONArray idItemsArray = jsonObject.optJSONArray("idItem");
//            if (!VALID_KEY.equals(key)) {
//                System.out.println("Invalid key");
//                return;
//            }
//            int[] idItems = null;
//            if (idItemsArray != null) {
//                idItems = new int[idItemsArray.length()];
//                for (int i = 0; i < idItemsArray.length(); i++) {
//                    idItems[i] = idItemsArray.optInt(i);
//                }
//            }
//            switch (position) {
//                case "active" -> System.out.println("Đã kích hoạt thành công người dùng có ID: " + userId); 
//                case "exchange" -> {
//                    addGold(amount, userId);
//                    System.out.println("Người chơi có ID: " + userId + " nhận được " + amount + " points.");
//                }
//                case "rollcall" -> {
//                    addItem(idItems, name, xu, yen, amount);
//                    System.out.println("Người chơi có ID: " + userId + " nhận được các vật phẩm.");
//                }
//                default -> System.out.println("Unknown position: " + position);
//            }
//        } catch (JSONException e) {
//            System.out.println("Error processing message: " + e.getMessage());
//        }
//    }
//
//    private void addGold(long number, int userId) {
//        User user = ServerManager.findUserByUserID(userId);
//        if (user != null) {
//            user.addGold((int) number);
//            String formattedNumber = String.format("%,d", number);
//            user.service.serverDialog("Đổi thành công " + formattedNumber + " lượng.");
//        } else {
//            System.out.println("User not found");
//        }
//    }
//
//    private void addItem(int[] idItems, String name, int xu, int yen, int luong) {
//        final Char player = ServerManager.findCharByName(name);
//        if (player != null) {
//            int gold = luong;
//            int coin = xu;
//            int emptySlots = player.getSlotNull();
//            if (emptySlots <= 0) {
//                player.getService().serverDialog("Bạn không đủ chỗ trống trong hành trang.");
//                return;
//            }
//            StringBuilder sb = new StringBuilder();
//            sb.append("Chúc mừng, Bạn Nhận Được").append("\n\n");
//            if (gold > 0) {
//                player.addGold(gold);
//                sb.append(String.format("- %s lượng", NinjaUtils.getCurrency(gold))).append("\n");
//            }
//            if (yen > 0) {
//                player.addYen(yen);
//                sb.append(String.format("- %s yên", NinjaUtils.getCurrency(yen))).append("\n");
//            }
//            if (coin > 0) {
//                player.addCoin(coin);
//                sb.append(String.format("- %s xu", NinjaUtils.getCurrency(coin))).append("\n");
//            }
//            if (idItems != null) {
//                for (int idItem : idItems) {
//                    Item nameItem = ItemFactory.getInstance().newItem(idItem);
//                    if (nameItem != null) {
//                        nameItem.isLock = false;
//                        nameItem.setQuantity(1);
//                        player.addItemToBag(nameItem);
//                        sb.append(String.format("- x%s %s", NinjaUtils.getCurrency(nameItem.getQuantity()), nameItem.template.name)).append("\n");
//                    }
//                }
//            }
//            player.getService().showAlert("Nhận", sb.toString());
//        } else {
//            System.out.println("Player not found");
//        }
//    }
//}
