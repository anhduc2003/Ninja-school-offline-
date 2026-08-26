package Exe_Z.network;

import Exe_Z.util.NinjaUtils;
import lombok.Getter;

public class Spammer {
    @Getter
    private Object id;

    private long lockedUntilAt;

    private int count;

    public void setLock() {
        count++;
        lockedUntilAt = System.currentTimeMillis();
        lockedUntilAt += NinjaUtils.getMilisByMinute(getMinutesBan());
    }

    public int getMinutesBan() {
        return 1;
    }

    public boolean isLocked() {
        return Math.max((lockedUntilAt - System.currentTimeMillis()) / 1000L, 0L) > 0;
    }

    public int getTime() {
        return (int) Math.max((lockedUntilAt - System.currentTimeMillis()) / 1000L, 0L);
    }
}
