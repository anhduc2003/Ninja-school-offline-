/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Exe_Z.map.zones;

import Exe_Z.ability.AbilityCustom;
import Exe_Z.bot.attack.AttackAround;
import Exe_Z.bot.Bot;
import Exe_Z.constants.SkillName;
import Exe_Z.convert.Converter;
import Exe_Z.fashion.FashionCustom;
import Exe_Z.map.Map;
import Exe_Z.model.Char;
import Exe_Z.model.SelectCardHalloween;
import Exe_Z.server.GameData;
import Exe_Z.util.NinjaUtils;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author kitakeyos - Hoàng Hữu Dũng
 */
public class NymozCave extends AreaWithCountdownTime {

    private final List<Bot> bots = new ArrayList<>();
    private boolean isWin;

    public NymozCave(int id, Map map) {
        super(id, map.tilemap, map);
        this.countDown = 60 * 60;
        bots.add(createBoss(-9999, "Shin Ah", (short) 50, (short) 25, (short) 180, (short) 181, (short) 100, (short) 120));
        bots.add(createBoss(-9998, "Vô Diện", (short) 51, (short) 25, (short) 180, (short) 181, (short) 460, (short) 120));
        bots.add(createBoss(-9997, "Oni", (short) 52, (short) 25, (short) 180, (short) 181, (short) 630, (short) 408));
        bots.add(createBoss(-9996, "Kuma", (short) 53, (short) 25, (short) 180, (short) 181, (short) 275, (short) 336));
        bots.add(createBoss(-9995, "Inu", (short) 54, (short) 25, (short) 180, (short) 181, (short) 160, (short) 576));
    }

    public Bot createBoss(int id, String name, short mask, short head, short body, short leg, short x, short y) {
        Bot bot = Bot.builder()
                .id(id)
                .name(name)
                .level(99)
                .typePk(Char.PK_DOSAT)
                .classId((byte) 0)
                .build();
        bot.setDefault();
        FashionCustom fashionCustom = FashionCustom.builder()
                .head(head)
                .body(body)
                .leg(leg)
                .fMask(mask)
                .build();
        bot.setFashionStrategy(fashionCustom);
        AbilityCustom abilityCustom = AbilityCustom.builder()
                .hp(80000000) //80000000
                .mp(10000000)
                .damage(50000)
                .damage2(50000)
                .exactly(1000)
                .fatal(200)
                .miss(300)
                .build();
        bot.setAbilityStrategy(abilityCustom);
        AttackAround attackAround = new AttackAround();
        attackAround.addSkill(Converter.getInstance().newSkill(GameData.getInstance().getSkill(SkillName.CHIEU_ENKO_BAKUSATSU, 12)));
        attackAround.addSkill(Converter.getInstance().newSkill(GameData.getInstance().getSkill(SkillName.CHIEU_RAIJIN, 12)));
        attackAround.addSkill(Converter.getInstance().newSkill(GameData.getInstance().getSkill(SkillName.CHIEU_SHABONDAMA, 12)));
        attackAround.addSkill(Converter.getInstance().newSkill(GameData.getInstance().getSkill(SkillName.CHIEU_KOGORASERU, 12)));
        attackAround.addSkill(Converter.getInstance().newSkill(GameData.getInstance().getSkill(SkillName.CHIEU_TSUMABENI, 12)));
        attackAround.addSkill(Converter.getInstance().newSkill(GameData.getInstance().getSkill(SkillName.CHIEU_KAMIKAZE, 12)));
        bot.setAttack(attackAround);
        bot.setAbility();
        bot.setFashion();
        bot.recovery();
        bot.setXY(x, y);
        join(bot);
        return bot;
    }

    @Override
    public void returnTownFromDead(@NotNull Char p) {
        p.setXY((short) 360, (short) 672);
        out(p);
        join(p);
    }

    public boolean isAllBotDead() {
        return bots.size() == 5 && bots.stream().allMatch((t) -> t.isDead);
    }

    public void win() {
        setTimeMap(60);
        getChars().stream().filter((t) -> !t.isBot()).forEach((t) -> {
            int spinTime = NinjaUtils.nextInt(2, 5);
            SelectCardHalloween slC = new SelectCardHalloween(spinTime);
            slC.open(t);
            getService().serverMessage(String.format("Bạn nhận được %d lượt quay", spinTime));
        });
    }

    @Override
    public void update() {
        if (!isWin && isAllBotDead()) {
            isWin = true;
            win();
        }
        super.update();
    }

    @Override
    public void close() {
        if (!isClosed()) {
            List<Char> chars = getChars();
            for (Char c : chars) {
                if (c.isBot()) {
                    continue;
                }
                short[] xy = NinjaUtils.getXY(c.mapBeforeEnterPB);
                c.setXY(xy);
                c.changeMap(c.mapBeforeEnterPB);
            }
        }
        super.close();
    }

}
