/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Exe_Z.clan;

import Exe_Z.constants.CMD;
import Exe_Z.item.Item;
import Exe_Z.model.Char;
import Exe_Z.model.ThanThu;
import Exe_Z.network.AbsService;
import Exe_Z.network.Message;
import Exe_Z.network.Service;
import Exe_Z.option.ItemOption;
import Exe_Z.util.Log;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Admin
 */
public class ClanService extends AbsService {

    private Clan clan;

    public ClanService(Clan clan) {
        this.clan = clan;
    }

    public void chat(String name, String text) {
        try {
            Message mss = new Message(CMD.CHAT_CLAN);
            DataOutputStream ds = mss.writer();
            ds.writeUTF(Char.setNameVip(name));
            ds.writeUTF(text);
            ds.flush();
            sendMessage(mss);
            mss.cleanup();
        } catch (Exception ex) {
            Log.error("chat err: " + ex.getMessage(), ex);
        }
    }

    public void requestClanInfo() {
        try {
            Message ms = messageNotMap(CMD.REQUEST_CLAN_INFO);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(Char.setNameVip(clan.getName()));
            ds.writeUTF(Char.setNameVip(clan.getMainName()));
            ds.writeUTF(clan.getAssistName());
            ds.writeShort(clan.getNumberMember());
            ds.writeByte(clan.getOpenDun());
            ds.writeByte(clan.getLevel());
            ds.writeInt(clan.getExp());
            ds.writeInt(clan.getExpNext());
            ds.writeInt(clan.getCoin());
            ds.writeInt(clan.getFreeCoin());
            ds.writeInt(clan.getCoinUp());
            ds.writeUTF(clan.getRegDate().toString());
            ds.writeUTF(clan.getAlert());
            ds.writeInt(clan.getUseCard());
            ds.writeByte(clan.getItemLevel());
            ds.flush();
            sendMessage(ms);
            ms.cleanup();
        } catch (Exception ex) {
            Logger.getLogger(Service.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void requestClanMember() {
        try {
            List<Member> members = clan.memberDAO.getAll();
            Message mss = messageNotMap(CMD.REQUEST_CLAN_MEMBER);
            DataOutputStream ds = mss.writer();
            synchronized (members) {
                ds.writeShort(members.size());
                for (Member mem : members) {
                    ds.writeByte(mem.getClassId());
                    ds.writeByte(mem.getLevel());
                    ds.writeByte(mem.getType());
                    ds.writeUTF(Char.setNameVip(mem.getName()));
                    ds.writeInt(mem.getPointClan());
                    ds.writeBoolean(mem.isOnline());
                }
                for (Member mem : members) {
                    ds.writeInt(mem.getPointClanWeek());
                }
            }
            ds.flush();
            sendMessage(mss);
            mss.cleanup();
        } catch (Exception ex) {
            Logger.getLogger(Service.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void requestClanItem() {
        try {
            Item[] items = clan.getItems();
            Message mss = messageNotMap(CMD.REQUEST_CLAN_ITEM);
            DataOutputStream ds = mss.writer();
            ds.writeByte(items.length);
            for (Item item : items) {
                ds.writeShort(item.getQuantityDisplay());
                ds.writeShort(item.id);

            }
            ds.writeByte(clan.thanThus.size()); // thần thú
            for (ThanThu thanThu : clan.thanThus) {
                if (thanThu.getEggHatchingTime() == -1) {
                    ds.writeUTF(String.format("%s cấp %d", thanThu.getName(), thanThu.getLevel()));
                } else {
                    ds.writeUTF(thanThu.getName());
                }
                ds.writeShort(thanThu.getIcon());
                ds.writeShort(thanThu.getId());
                ds.writeInt(thanThu.getEggHatchingTime());
                ArrayList<ItemOption> options = thanThu.getOptions();
                ds.writeByte(options.size());
                if (thanThu.getEggHatchingTime() >= 0) {
                    ds.writeUTF("Thời gian nở: ");
                } else {
                    for (ItemOption option : options) {
                        if (option.optionTemplate.id == ThanThu.ST_QUAI_ID) {
                            ds.writeUTF("Sát thương quái: " + option.param);
                        } else if (option.optionTemplate.id == ThanThu.ST_NGUOI_ID) {
                            ds.writeUTF("Sát thương người: " + option.param);
                        } else {
                            ds.writeUTF(option.getOptionString());
                        }
                    }
                    ds.writeInt(thanThu.getCurrentExp());
                    ds.writeInt(thanThu.getMaxExp());
                }
                ds.writeByte(thanThu.getStars());
            }
            ds.flush();
            sendMessage(mss);
            mss.cleanup();
        } catch (Exception ex) {
            Logger.getLogger(Service.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void writeLog() {
        try {
            Message mss = messageNotMap(CMD.REQUEST_CLAN_LOG);
            DataOutputStream ds = mss.writer();
            ds.writeUTF(clan.getLog());
            ds.flush();
            sendMessage(mss);
            mss.cleanup();
        } catch (Exception ex) {
            Logger.getLogger(Service.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void sendMessage(Message ms) {
        List<Member> members = clan.memberDAO.getAll();
        synchronized (members) {
            for (Member mem : members) {
                if (mem != null) {
                    Char _char = mem.getChar();
                    if (_char != null) {
                        _char.getService().sendMessage(ms);
                    }
                }
            }
        }
    }

}
