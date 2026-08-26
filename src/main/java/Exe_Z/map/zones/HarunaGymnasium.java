/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Exe_Z.map.zones;

import Exe_Z.ability.AbilityCustom;
import Exe_Z.bot.attack.AttackAround;
import Exe_Z.bot.move.MoveWithinCustom;
import Exe_Z.bot.Bot;
import Exe_Z.fashion.FashionCustom;
import Exe_Z.map.Map;
import Exe_Z.map.TileMap;
import Exe_Z.model.Char;
import Exe_Z.skill.SkillFactory;
import Exe_Z.constants.SkillName;

/**
 *
 * @author Admin
 */
public class HarunaGymnasium extends Gymnasium {

    public HarunaGymnasium(int id, TileMap tilemap, Map map) {
        super(id, tilemap, map);
    }

    @Override
    public void initBot() {
        Bot bot = Bot.builder().id(-11111).name("Thầy Kazeto")
                .level(50)
                .typePk(Char.PK_DOSAT)
                .build();
        bot.setDefault();
        FashionCustom fashionCustom = FashionCustom.builder()
                .head((short) 65)
                .body((short) 66)
                .leg((short) 67)
                .weapon((short) -1)
                .build();
        bot.setFashionStrategy(fashionCustom);
        AbilityCustom abilityCustom = AbilityCustom.builder()
                .hp(1000)
                .mp(1000)
                .damage(1000)
                .damage2(900)
                .miss(10)
                .exactly(100)
                .fatal(100)
                .build();
        bot.setAbilityStrategy(abilityCustom);
        MoveWithinCustom move = MoveWithinCustom.builder()
                .minX(792)
                .maxX(1176)
                .minY(100)
                .maxY(240)
                .build();
        bot.setMove(move);
        AttackAround attackAround = new AttackAround();
        attackAround.addSkill(SkillFactory.getInstance().newSkill(SkillName.CHIEU_RAIKOUTO, 1));
        attackAround.addSkill(SkillFactory.getInstance().newSkill(SkillName.CHIEU_TATSUMAKI, 1));
        bot.setAttack(attackAround);
        bot.setAbility();
        bot.setFashion();
        bot.recovery();
        bot.setXY((short) 1000, (short) 240);
        setBot(bot);
        join(bot);
    }

}
