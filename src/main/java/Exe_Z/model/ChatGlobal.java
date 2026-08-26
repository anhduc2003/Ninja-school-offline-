/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Exe_Z.model;

import Exe_Z.constants.ItemName;
import Exe_Z.event.Event;
import Exe_Z.item.Item;
import Exe_Z.lib.ProfanityFilter;
import Exe_Z.lib.RandomCollection;
import Exe_Z.network.Message;
import Exe_Z.server.GlobalService;
import Exe_Z.server.Server;
import Exe_Z.util.NinjaUtils;
import java.io.IOException;

/**
 *
 * @author kitakeyos - Hoàng Hữu Dũng
 */
public class ChatGlobal implements IChat {

    private static ProfanityFilter profanityFilter;

    public static ProfanityFilter getFilter() {
        if (profanityFilter == null) {
            synchronized (ProfanityFilter.class) {
                if (profanityFilter == null) {
                    profanityFilter = new ProfanityFilter();
                    profanityFilter.addBadWord("lồn");
                    profanityFilter.addBadWord("buồi");
                    profanityFilter.addBadWord("địt");
                    profanityFilter.addBadWord("súc vật");
                    profanityFilter.addBadWord("lon");
                    profanityFilter.addBadWord("buoi");
                    profanityFilter.addBadWord("dit");
                    profanityFilter.addBadWord("suc vat");
                    profanityFilter.addBadWord("mẹ mày");
                    profanityFilter.addBadWord("me may");
                    profanityFilter.addBadWord("đm");
                    profanityFilter.addBadWord("dm");
                    profanityFilter.addBadWord(".com");
                    profanityFilter.addBadWord(".tk");
                    profanityFilter.addBadWord(".ga");
                    profanityFilter.addBadWord(".cf");
                    profanityFilter.addBadWord(".net");
                    profanityFilter.addBadWord(".xyz");
                    profanityFilter.addBadWord(".mobi");
                    profanityFilter.addBadWord(".ml");
                    profanityFilter.addBadWord(".onine");
                }
            }
        }
        return profanityFilter;
    }
    
    

    private Char player;
    private long lastTimeChat;
    private long delay;
    private String text;
    private byte type;

    public ChatGlobal(Char player) {
        this.player = player;
        this.delay = 10000;
    }

    public void read(Message ms) {

        try {
            text = ms.reader().readUTF();
            type = -1;
            if (ms.reader().available() > 0) {
                type = ms.reader().readByte();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void wordFilter() {
        text = getFilter().filterBadWords(text);
    }

    public void send() {
        if (type == -1) {
            long now = System.currentTimeMillis();
            if (now - lastTimeChat < delay) {
                long mili = (int) (now - lastTimeChat);
                player.serverMessage(String.format("Chỉ có thể chat sau %s giây.",
                        NinjaUtils.timeAgo((int) ((delay - mili) / 1000))));
                return;
            }
            lastTimeChat = now;
            if (player.user.gold < 5) {
                player.serverDialog("Bạn không đủ lượng!");
                return;
            }
            if (player.level < 70) {
                    player.serverDialog( "Bạn Phải level 70 mới sử dụng đc chức năng này!");
                    return;
                }
            if (player.user.activated == 0) {
                player.serverDialog("Tài khoản chưa được kích hoạt, Kích hoạt free tại NPC ADMIN hoặc Wed .");
                return;
            }
            player.addGold(-5);
            GlobalService.getInstance().chat(Char.setNameVip(player.name), text);
        } else if (type == 0 || type == 1) {
            if (!Event.isLunarNewYear()) {
                player.serverMessage("Chỉ sử dụng được trong sự kiện tết.");
                return;
            }
            Event event = Event.getEvent();
            text = "Chúc " + text;

            int itemId = type == 0 ? ItemName.THIEP_CHUC_TET : ItemName.THIEP_CHUC_TET_DAC_BIET;
            int indexUI = player.getIndexItemByIdInBag(itemId);
            if (indexUI == -1) {
                player.serverDialog("Bạn không có thiệp chúc tết");
                return;
            }

            RandomCollection<Integer> rc = type == 0 ? event.getItemsRecFromCoinItem() : event.getItemsRecFromGoldItem();
            Item item = player.bag[indexUI];
            event.useEventItem(player, item.id, rc);
            player.getService().chatGlobal(Char.setNameVip(player.name), text);
        }
    }
}
