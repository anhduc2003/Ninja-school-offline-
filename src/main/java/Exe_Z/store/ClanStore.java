/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Exe_Z.store;

import Exe_Z.clan.Clan;
import Exe_Z.constants.CMDMenu;
import Exe_Z.constants.ItemName;
import Exe_Z.convert.Converter;
import Exe_Z.item.Item;
import Exe_Z.item.ItemTemplate;
import Exe_Z.model.Char;
import Exe_Z.model.Menu;
import Exe_Z.model.ThanThu;
import Exe_Z.option.ItemOption;
import Exe_Z.util.NinjaUtils;

import java.util.ArrayList;

/**
 * @author kitakeyos - Hoàng Hữu Dũng
 */
public class ClanStore extends Store {

    public ClanStore(int type, String name) {
        super(type, name);
    }

    @Override
    public void buy(Char p, int indexUI, int quantity) {
        ItemStore item = get(indexUI);
        if (item == null) {
            return;
        }

        ItemTemplate template = item.getTemplate();

        Clan clan = p.clan;
        if (clan == null) {
            p.serverDialog("Bạn không trong gia tộc.");
            return;
        }

        int typeClan = clan.getMemberByName(p.name).getType();
        if (typeClan != Clan.TYPE_TOCTRUONG && typeClan != Clan.TYPE_TOCPHO) {
            p.serverDialog("Bạn không phải tộc trưởng.");
            return;
        }
        if (template.id == ItemName.TRUNG_DI_LONG || template.id == ItemName.TRUNG_HAI_MA || template.id == ItemName.TRUNG_HOA_LONG) {
            if (clan.level < 25 ) {
                p.serverDialog("Gia tộc phải trên level 25 mới có thể mua");
                return;
            }
        }

        if (template.type == 13) {
            int t = template.id - 422;
            if (clan.getItemLevel() < t) {
                p.serverDialog("Vật phẩm này gia tộc bạn chưa mở khoá.");
                return;
            }
        }
        if (template.id == ItemName.TRUNG_HOA_LONG) {
            if (clan.thanThus.size() < 2) {
                p.serverDialog("Gia tộc phải có đủ Hải mã và Dị long 3 sao  mới có thể mua");
                return;
            }
            for (ThanThu thanThu : clan.thanThus) {
                if (thanThu.getEggHatchingTime() > -1 && thanThu.getLevel() < ThanThu.MAX_LEVEL && thanThu.getStars() < ThanThu.MAX_STAR) {
                    p.serverDialog("Thần thú phải đạt 3 sao cấp độ 100 thì mới có thể mua");
                    return;
                }
            }
        }
        if (template.id == ItemName.TRUNG_DI_LONG || template.id == ItemName.TRUNG_HAI_MA || template.id == ItemName.TRUNG_HOA_LONG) {
            quantity = 1;
        }
        long giaXu = ((long) item.getCoin()) * ((long) quantity);
        if (giaXu < 0) {
            return;
        }
        if (giaXu > clan.getCoin()) {
            p.serverDialog("Không đủ xu gia tộc.");
            return;
        }

        if (template.id == ItemName.TRUNG_DI_LONG || template.id == ItemName.TRUNG_HAI_MA || template.id == ItemName.TRUNG_HOA_LONG) {

            byte type = template.id == ItemName.TRUNG_DI_LONG ? ThanThu.DI_LONG : template.id == ItemName.TRUNG_HOA_LONG ? ThanThu.HOA_LONG : ThanThu.HAI_MA;
            ThanThu thanThu = clan.getThanThu(type);
            if (thanThu != null) {
                p.serverDialog("Gia tộc bạn đã sở hữu thần thú này rồi.");
                return;
            }
            thanThu = new ThanThu();
            thanThu.setType(type);
            thanThu.setCurrentExp(0);
//            thanThu.setEggHatchingTime(7 * 24 * 60 * 60 * 1000);
            // time nở thần thú
            thanThu.setEggHatchingTime(60 * 1000);
            thanThu.setLevel(1);
            thanThu.setStars((byte) 1);
            ArrayList<ItemOption> options = new ArrayList<>();
            thanThu.setOptions(options);
            clan.thanThus.add(thanThu);
        } else {
            Item newItem = Converter.getInstance().toItem(item, Converter.MAX_OPTION);
            newItem.expire = item.getExpire();
            newItem.setQuantity(quantity);
            clan.addItem(newItem);
        }
        clan.addCoin(-((int) giaXu));
        clan.getClanService().requestClanItem();
        p.serverDialog("Vật phẩm đã được thêm vào kho.");
    }

}
