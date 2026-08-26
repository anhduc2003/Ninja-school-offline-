//package Exe_Z.mob;
//
//import Exe_Z.constants.MobName;
//import Exe_Z.map.zones.Zone;
//
///**
// * @author Soanlv
// */
//public class VithuFactory extends MobFactory {
//    public VithuFactory(Zone zone) {
//        super(zone);
//    }
//
//    @Override
//    public Mob createMonster(int id, MobPosition mob) {
//        int hp = 0;
//        short level = 0;
//        if (this.zone.id == 0) {
//            MobTemplate template = MobManager.getInstance().find(MobName.JUUBI_SHINJU);
//            hp = template.hp;
//            level = template.level;
//            Mob monster = new Mob(0, (short) MobName.JUUBI_SHINJU, hp, level, (short) 830, (short) 240, false, template.isBoss(), zone);
//            return monster;
//        }
//        return null;
//    }
//}
