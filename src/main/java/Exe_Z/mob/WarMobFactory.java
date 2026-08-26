/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Exe_Z.mob;

import Exe_Z.constants.MobName;
import Exe_Z.map.War;
import Exe_Z.map.zones.Zone;

/**
 *
 * @author kitakeyos - Hoàng Hữu Dũng
 */
public class WarMobFactory extends MobFactory {

    public WarMobFactory(Zone zone) {
        super(zone);
    }

    @Override
    public Mob createMonster(int id, MobPosition mob) {
        MobTemplate template = MobManager.getInstance().find(mob.getId());
        int hp = template.hp;
        short level = template.level;
        switch (zone.map.war.type) {
            case War.TYPE_LEVEL_30_TO_50:
                hp = 100000;
                level = 40;
                if (template.id == MobName.BACH_LONG_TRU || template.id == MobName.HAC_LONG_TRU) {
                    hp = 20000000;
                    level = 99;
                }
                break;

            case War.TYPE_LEVEL_70_TO_90:
                hp = 200000;
                level = 50;
                if (template.id == MobName.BACH_LONG_TRU || template.id == MobName.HAC_LONG_TRU) {
                    hp = 40000000;
                    level = 99;
                }
                break;

            case War.TYPE_ALL_LEVEL:
                hp = 700000;
                level = 70;
                if (template.id == MobName.BACH_LONG_TRU || template.id == MobName.HAC_LONG_TRU) {
                    hp = 20000000;
                    level = 99;
                }
                break;
        }
        Mob monster = new Mob(id, mob.getId(), hp, level, mob.getX(), mob.getY(), mob.isBeast() && zone.id % 5 == 0, template.isBoss(), zone);
        return monster;
    }

}
