package Exe_Z.network;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SpammerManager {
    public Map<Object, Spammer> spammers = new ConcurrentHashMap<>();

    public void lock(Object id) {
        Spammer spammer = spammers.get(id);
        if (spammer == null) {
            spammer = new Spammer();
            spammers.put(id, spammer);
        }
        spammer.setLock();
    }

    public Spammer get(Object id) {
        return spammers.get(id);
    }

    public int getTime(Object id) {
        Spammer spammer = spammers.get(id);
        if (spammer == null) {
            return 0;
        }
        return spammer.getTime();
    }

    public void remove(Object id) {
        spammers.remove(id);
    }

    public static SpammerManager getInstance() {
        return SINGLETON.INSTANCE;
    }

    public boolean isLocked(Object id) {
        return spammers.containsKey(id);
    }

    public static final class SINGLETON {
        private static final SpammerManager INSTANCE = new SpammerManager();
    }
}
