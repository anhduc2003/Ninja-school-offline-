package Exe_Z.model;

import com.mongodb.client.MongoCollection;
import Exe_Z.ability.AbilityCustom;
import Exe_Z.ability.AbilityFromEquip;
import Exe_Z.convert.Converter;
import Exe_Z.db.jdbc.DbManager;
import Exe_Z.effect.Effect;
import Exe_Z.effect.EffectManager;
import Exe_Z.event.eventpoint.EventPoint;
import Exe_Z.fashion.FashionCustom;
import Exe_Z.fashion.FashionFromEquip;
import Exe_Z.item.*;
import Exe_Z.lib.ParseData;
import Exe_Z.map.world.World;
import Exe_Z.map.zones.Zone;
import Exe_Z.mob.Mob;
import Exe_Z.network.Controller;
import Exe_Z.network.NoService;
import Exe_Z.network.Service;
import Exe_Z.server.GameData;
import Exe_Z.skill.Skill;
import Exe_Z.util.Log;
import Exe_Z.util.NinjaUtils;
import org.bson.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Vector;

public class Pet extends Char{
    public Char human;
    public int damePercent;
    public short selectSkillId;
    public short bodyPet;
    public short legPet;
    public Pet(Char _char, int damePercent,String name, short head, short body, short leg) {
        super(-(190000 + _char.id));
        this.isHuman = false;
        this.isPet = true;
        this.human = _char;
        this.damePercent = damePercent;
        this.name = name;
        this.head = head;
        this.bodyPet = body;
        this.legPet = leg;
    }
    public void setDefault() {
        this.bag = new Item[0];
        this.box = new Item[0];
        this.equipment = new Equip[16];
        this.fashion = new Equip[16];
        this.mount = new Mount[5];
        this.bijuu = new Item[5];
        this.level = 1;
        this.typePk = 0;
        this.classId = 0;
        this.vSkill = new Vector<>();
        this.vSupportSkill = new Vector<>();
        this.vSkillFight = new Vector<>();
        Skill skill = GameData.getInstance().getSkill(this.classId, 0, 0);
        if (skill.template.type == Skill.SKILL_AUTO_USE) {
            this.vSupportSkill.add(skill);
        } else if ((skill.template.type == Skill.SKILL_CLICK_USE_ATTACK
                || skill.template.type == Skill.SKILL_CLICK_LIVE
                || skill.template.type == Skill.SKILL_CLICK_USE_BUFF
                || skill.template.type == Skill.SKILL_CLICK_NPC)
                && (skill.template.maxPoint == 0 || (skill.template.maxPoint > 0 && skill.point > 0))) {

            this.vSkillFight.add(skill);
        }
        this.vSkill.add(skill);
        this.isBot = true;
        Item item = ItemFactory.getInstance().newItem(194);
        Equip equip = Converter.getInstance().toEquip(item);
        equip.isLock = true;
        this.equipment[1] = equip;
    }
    @Override
    public EventPoint getEventPoint() {
        return human.getEventPoint();
    }

    public World findWorld(byte type) {
        if (isPet) {
            return human.findWorld(type);
        }
        return super.findWorld(type);

    }

    @Override
    public boolean isMeCanAttackOtherPlayer(Char cAtt) {
        if (isPet) {
            return human.isMeCanAttackOtherPlayer(cAtt);
        }
        return super.isMeCanAttackOtherPlayer(cAtt);
    }

    @Override
    public boolean isMeCanAttackNpc(Mob cAtt) {
        if (isPet) {
            return human.isMeCanAttackNpc(cAtt);
        }
        return super.isMeCanAttackNpc(cAtt);
    }


    @Override
    public void addMp(int add) {
        if (!isPet) {
            this.mp += add;
        }
    }


    @Override
    public void setAbility() {
        super.setAbility();
        if (this.isPet) {
            this.damage = this.damage * damePercent / 100; // human.dame
            this.damage2 = damage;
            this.damage2 -= damage / 10;
        }
    }

    public void move(short x, short y) {
        this.x = x;
        this.y = y;
        zone.getService().playerMove(this);
    }

    @Override
    public void selectSkill(short templateId) {
        try {
            super.selectSkill(templateId);
        } finally {
            if (selectedSkill != null) {
                if (selectedSkill.template.type == Skill.SKILL_CLICK_USE_ATTACK) {
                    selectSkillId = (short) selectedSkill.template.id;
                }
            }
        }
    }

    public void create() {
        this.isDead = false;
        AbilityCustom abilityCustom = AbilityCustom.builder()
                .hp(2000000)
                .build();
        setAbilityStrategy(abilityCustom);
        setAbility();
        FashionCustom fashionCustom = FashionCustom.builder()
                .head(head)
                .body(bodyPet)
                .leg(legPet)
                .weapon((short) 15)
                .build();
        this.setFashionStrategy(fashionCustom);
        setFashion();
        setAbility();
        this.hp = this.maxHP;
        this.mp = this.maxMP;
        selectSkill(this.selectSkillId);
        setXY(human.x, human.y);

    }


    @Override
    public void close() {
        this.isDead = true;
    }

    @Override
    public Service getService() {
            return NoService.getInstance();
    }

    @Override
    public void updateEveryHalfSecond() {
        super.updateEveryHalfSecond();
        if (!isDead) {
            if (isPet && classId == 6) {
                synchronized (vSkillFight) {
                    for (Skill skill : vSkillFight) {
                        if (!skill.isCooldown() && skill.template.type == Skill.SKILL_CLICK_USE_BUFF) {
                            human.useSkillBuff((byte) (human.x > this.x ? 1 : -1), skill);
                        }
                    }
                }
            }
        }
    }

}
