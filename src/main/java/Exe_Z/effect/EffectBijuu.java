package Exe_Z.effect;

import Exe_Z.model.Char;

public class EffectBijuu {
    public static final int HO_THE = 1;
    public static final int PHA_TRIEN = 2;
    public static final int THUY_HON = 3;
    public static final int DOAT_MENH = 4;
    public static final int CAN_KHON = 5;
    public static final int DOC_KICH = 6;
    public static final int KIM_CANG = 7;
    public static final int PHI_HUYET = 8;
    public static final int TIEN_KHI = 9;

    public Char atk;
    public Char target;
    public int id;
    public long time;

    public EffectBijuu(long time, int id, Char target, Char atk) {
        this.time = time;
        this.id = id;
        this.target = target;
        this.atk = atk;
    }
}
