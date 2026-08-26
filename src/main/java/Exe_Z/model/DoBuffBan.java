package Exe_Z.model;

import Exe_Z.item.Item;
import Exe_Z.item.ItemFactory;

public class DoBuffBan {

    public static void dovip(Char p) {
        int baseItemId = p.gender == 0 ? 715 : 712;
        int[] itemOffsets = p.gender == 0 ? new int[]{0, 1, 38, 39, 40, 41, 42, 43, 44} : new int[]{0, 1, 34, 35, 36, 37, 38, 39, 40};
        for (int offset : itemOffsets) {
            int itemId = baseItemId + offset;
            DoBuffBan.addItemToBagdovip(itemId, p);
        }
    }

    private static void addItemToBagdovip(int itemId, Char p) {
        if (p.classId == 1 || p.classId == 2) {
            Item item = ItemFactory.getInstance().newItem9X(itemId);
            item.nextupgrade(16);
            item.sys = 1;
            item.nextTLMax(9);
            p.addItemToBag(item);
        }
        if (p.classId == 3 || p.classId == 4) {
            Item item = ItemFactory.getInstance().newItem9X(itemId);
            item.nextupgrade(16);
            item.sys = 2;
            item.nextTLMax(9);
            p.addItemToBag(item);
        }
        if (p.classId == 5 || p.classId == 6) {
            Item item = ItemFactory.getInstance().newItem9X(itemId);
            item.nextupgrade(16);
            item.sys = 3;
            item.nextTLMax(9);
            p.addItemToBag(item);
        }
    }

    public static void do10x(Char p) {
        Item itm1 = ItemFactory.getInstance().newItem9X(1101 - p.gender);
        itm1.nextupgrade(16);
        itm1.nextTLMax(9);
        p.addItemToBag(itm1);
        Item itm2 = ItemFactory.getInstance().newItem9X(1103 - p.gender);
        itm2.nextupgrade(16);
        itm2.nextTLMax(9);
        p.addItemToBag(itm2);
        Item itm3 = ItemFactory.getInstance().newItem9X(1105 - p.gender);
        itm3.nextupgrade(16);
        itm3.nextTLMax(9);
        p.addItemToBag(itm3);
        Item itm4 = ItemFactory.getInstance().newItem9X(1107 - p.gender);
        itm4.nextupgrade(16);
        itm4.nextTLMax(9);
        p.addItemToBag(itm4);
        Item itm5 = ItemFactory.getInstance().newItem9X(1109 - p.gender);
        itm5.nextupgrade(16);
        itm5.nextTLMax(9);
        p.addItemToBag(itm5);
        Item itm6 = ItemFactory.getInstance().newItem9X(1110);
        itm6.nextupgrade(16);
        itm6.nextTLMax(9);
        p.addItemToBag(itm6);
        Item itm7 = ItemFactory.getInstance().newItem9X(1111);
        itm7.nextupgrade(16);
        itm7.nextTLMax(9);
        p.addItemToBag(itm7);
        Item itm8 = ItemFactory.getInstance().newItem9X(1112);
        itm8.nextupgrade(16);
        itm8.nextTLMax(9);
        p.addItemToBag(itm8);
        Item itm9 = ItemFactory.getInstance().newItem9X(1113);
        itm9.nextupgrade(16);
        itm9.nextTLMax(9);
        p.addItemToBag(itm9);
    }

    public static void do11x(Char p) {
        for (int i = 1154; i <= 1162; i++) {
            addItemToBagdo11x(i, p);
        }
    }

    private static void addItemToBagdo11x(int itemId, Char p) {
        Item item = ItemFactory.getInstance().newItem9X(itemId);
        item.nextupgrade(16);
        if (p.classId == 1 || p.classId == 2) {
            item.sys = 1;
        } else if (p.classId == 3 || p.classId == 4) {
            item.sys = 2;
        } else if (p.classId == 5 || p.classId == 6) {
            item.sys = 3;
        } else {
            return;
        }
        item.nextTLMax(9);
        p.addItemToBag(item);
    }

    public static void vk11x(Char p) {
        Item itm1 = ItemFactory.getInstance().newItem9X(1148);
        itm1.nextupgrade(16);
        itm1.nextTLMax(9);
        p.addItemToBag(itm1);
        Item itm2 = ItemFactory.getInstance().newItem9X(1149);
        itm2.nextupgrade(16);
        itm2.nextTLMax(9);
        p.addItemToBag(itm2);
        Item itm3 = ItemFactory.getInstance().newItem9X(1150);
        itm3.nextupgrade(16);
        itm3.nextTLMax(9);
        p.addItemToBag(itm3);
        Item itm4 = ItemFactory.getInstance().newItem9X(1151);
        itm4.nextupgrade(16);
        itm4.nextTLMax(9);
        p.addItemToBag(itm4);
        Item itm5 = ItemFactory.getInstance().newItem9X(1152);
        itm5.nextupgrade(16);
        itm5.nextTLMax(9);
        p.addItemToBag(itm5);
        Item itm6 = ItemFactory.getInstance().newItem9X(1153);
        itm6.nextupgrade(16);
        itm6.nextTLMax(9);
        p.addItemToBag(itm6);
    }
    public static void vk12x(Char p) {
        Item itm1 = ItemFactory.getInstance().newItem9X(1254);
        itm1.nextupgrade(16);
        itm1.nextTLMax(9);
        p.addItemToBag(itm1);
        Item itm2 = ItemFactory.getInstance().newItem9X(1255);
        itm2.nextupgrade(16);
        itm2.nextTLMax(9);
        p.addItemToBag(itm2);
        Item itm3 = ItemFactory.getInstance().newItem9X(1256);
        itm3.nextupgrade(16);
        itm3.nextTLMax(9);
        p.addItemToBag(itm3);
        Item itm4 = ItemFactory.getInstance().newItem9X(1257);
        itm4.nextupgrade(16);
        itm4.nextTLMax(9);
        p.addItemToBag(itm4);
        Item itm5 = ItemFactory.getInstance().newItem9X(1258);
        itm5.nextupgrade(16);
        itm5.nextTLMax(9);
        p.addItemToBag(itm5);
        Item itm6 = ItemFactory.getInstance().newItem9X(1259);
        itm6.nextupgrade(16);
        itm6.nextTLMax(9);
        p.addItemToBag(itm6);
    }

    public static void vk9x(Char p) {
        Item itm1 = ItemFactory.getInstance().newItem9X(632);
        itm1.nextupgrade(16);
        itm1.nextTLMax(9);
        p.addItemToBag(itm1);
        Item itm2 = ItemFactory.getInstance().newItem9X(633);
        itm2.nextupgrade(16);
        itm2.nextTLMax(9);
        p.addItemToBag(itm2);
        Item itm3 = ItemFactory.getInstance().newItem9X(634);
        itm3.nextupgrade(16);
        itm3.nextTLMax(9);
        p.addItemToBag(itm3);
        Item itm4 = ItemFactory.getInstance().newItem9X(635);
        itm4.nextupgrade(16);
        itm4.nextTLMax(9);
        p.addItemToBag(itm4);
        Item itm5 = ItemFactory.getInstance().newItem9X(636);
        itm5.nextupgrade(16);
        itm5.nextTLMax(9);
        p.addItemToBag(itm5);
        Item itm6 = ItemFactory.getInstance().newItem9X(637);
        itm6.nextupgrade(16);
        itm6.nextTLMax(9);
        p.addItemToBag(itm6);
    }

    public static void dohacam(Char p) {
        for (int i = 1227; i <= 1235; i++) {
            addItemToBagHacAm(i, p);
        }
    }

    private static void addItemToBagHacAm(int itemId, Char p) {
        Item item = ItemFactory.getInstance().newItem9X(itemId);
        item.nextupgrade(16);
        if (p.classId == 1 || p.classId == 2) {
            item.sys = 1;
        } else if (p.classId == 3 || p.classId == 4) {
            item.sys = 2;
        } else if (p.classId == 5 || p.classId == 6) {
            item.sys = 3;
        } else {
            return;
        }
        item.nextTLMax(9);
        p.addItemToBag(item);
    }

    public static void doht(Char p) {
        for (int i = 1236; i <= 1244; i++) {
            addItemToBagHT(i, p);
        }
    }

    private static void addItemToBagHT(int itemId, Char p) {
        Item item = ItemFactory.getInstance().newItem9X(itemId);
        item.nextupgrade(16);
        if (p.classId == 1 || p.classId == 2) {
            item.sys = 1;
        } else if (p.classId == 3 || p.classId == 4) {
            item.sys = 2;
        } else if (p.classId == 5 || p.classId == 6) {
            item.sys = 3;
        } else {
            return;
        }
        item.nextTLMax(9);
        p.addItemToBag(item);
    }

    public static void doLB(Char p) {
        for (int i = 1245; i <= 1253; i++) {
            addItemToBagLB(i, p);
        }
    }

    public static void addItemToBagLB(int itemId, Char p) {
        Item item = ItemFactory.getInstance().newItem9X(itemId);
        item.nextupgrade(16);
        if (p.classId == 1 || p.classId == 2) {
            item.sys = 1;
        } else if (p.classId == 3 || p.classId == 4) {
            item.sys = 2;
        } else if (p.classId == 5 || p.classId == 6) {
            item.sys = 3;
        } else {
            return;
        }
        item.nextTLMax(9);
        p.addItemToBag(item);
    }

    public static void dovip8(Char p) {
        int baseItemId = p.gender == 0 ? 715 : 712;
        int[] itemOffsets = p.gender == 0 ? new int[]{0, 1, 38, 39, 40, 41, 42, 43, 44} : new int[]{0, 1, 34, 35, 36, 37, 38, 39, 40};
        for (int offset : itemOffsets) {
            int itemId = baseItemId + offset;
            addItemToBag8(itemId, p); // Gọi addItemToBag chỉ với itemId
        }
    }

    public static void addItemToBag8(int itemId, Char p) {
        if (p.classId == 1 || p.classId == 2) {
            Item item = ItemFactory.getInstance().newItem9X(itemId);
            item.nextupgrade(8);
            item.sys = 1;
            p.addItemToBag(item);
        }
        if (p.classId == 3 || p.classId == 4) {
            Item item = ItemFactory.getInstance().newItem9X(itemId);
            item.nextupgrade(8);
            item.sys = 2;
            p.addItemToBag(item);
        }
        if (p.classId == 5 || p.classId == 6) {
            Item item = ItemFactory.getInstance().newItem9X(itemId);
            item.nextupgrade(8);
            item.sys = 3;
            p.addItemToBag(item);
        }
    }
}
