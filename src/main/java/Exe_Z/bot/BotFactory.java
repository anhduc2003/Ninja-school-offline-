/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Exe_Z.bot;

import Exe_Z.ability.AbilityCustom;
import Exe_Z.constants.CMDInputDialog;
import Exe_Z.constants.CMDMenu;
import Exe_Z.constants.ItemName;
import Exe_Z.convert.Converter;
import Exe_Z.fashion.FashionCustom;
import Exe_Z.item.Item;
import Exe_Z.item.ItemFactory;
import Exe_Z.map.MapManager;
import Exe_Z.model.Char;
import Exe_Z.model.InputDialog;
import Exe_Z.model.Menu;
import Exe_Z.option.ItemOption;
import Exe_Z.server.ServerManager;
import Exe_Z.store.ItemStore;
import Exe_Z.store.StoreManager;
import Exe_Z.util.NinjaUtils;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Properties;
import lombok.Builder;
import lombok.Getter;

/**
 *
 * @author Admin
 */
public class BotFactory {

    @Getter
    private static final BotFactory instance = new BotFactory();
    
    public static void setLeven(Char p, String[] args) {
        try {
            if (p != null) {
                int level = Integer.parseInt(args[1]);
                long exp = NinjaUtils.getExpFromLevel(level);
                exp -= p.exp;
                p.addExp(exp);
                p.getService().loadAll();
            } else {
            }
        } catch (NumberFormatException ex) {
        } catch (Exception ex) {
        }
    }
    
//    public boolean process(Char p, String text) {
//        String notEnoughA = p.language.getString("NOT_ENOUGH_A");
//        if (text.equals(notEnoughA)) {
//            p.openUIA(p);
//            return true;
//        }
//        if (text.equals(p.language.getString("NOT_ENOUGH_ERRO"))) {
//            p.openUI(p);
//            p.openUII(p);
//            return true;
//        }
//        if (text.equals(p.language.getString("NOT_ENOUGH_ERROR"))) {
//            p.open1(p);
//            return true;
//        }
//        String[] args = text.split(" ");
//        if (args[0].equals(p.language.getString("NOT_ENOUGH_LEVEN"))) {
//                setLeven(p, args);
//                return true;
//            }
//        return false;
//    }
    
    @Builder
    public Bot newBot(int id, String name, int level, byte typePK, byte clazz, short head, short body, short leg, short wp, int hp, int mp, int damage, int miss, int exactly, int fatal) {
        Bot bot = Bot.builder().id(id)
                .name(name)
                .level(level)
                .typePk(typePK)
                .classId(clazz)
                .build();
        bot.setDefault();
        FashionCustom fashionCustom = FashionCustom.builder()
                .head(head)
                .body(body)
                .leg(leg)
                .weapon(wp)
                .build();
        bot.setFashionStrategy(fashionCustom);
        AbilityCustom abilityCustom = AbilityCustom.builder()
                .hp(hp)
                .mp(mp)
                .damage(damage)
                .damage2(damage - (damage / 10))
                .miss(miss)
                .exactly(exactly)
                .fatal(fatal)
                .build();
        bot.setAbilityStrategy(abilityCustom);
        return bot;
    }
}
