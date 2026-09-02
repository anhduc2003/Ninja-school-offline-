package Exe_Z.bot.player;

import Exe_Z.map.MapManager;
import Exe_Z.map.TileMap;
import Exe_Z.map.item.ItemMap;
import Exe_Z.map.zones.Zone;
import Exe_Z.model.Char;
import Exe_Z.util.NinjaUtils;

import java.util.List;

public class BotLootTask extends BotTask {

    private ItemMap currentTarget = null;
    private long nextSearchTime = 0;
    private static final long SEARCH_INTERVAL = 5000;

    public BotLootTask() {
        super("Loot", 8);
    }

    @Override
    public boolean canExecute(Char botChar) {
        return botChar != null && !botChar.isDead;
    }

    @Override
    public boolean execute(Char botChar, long now) {
        if (now < nextSearchTime) {
            return false;
        }
        nextSearchTime = now + SEARCH_INTERVAL;

        if (currentTarget == null) {
            currentTarget = findNearestItem(botChar);
            return false;
        }

        Zone zone = botChar.zone;
        if (zone != null && zone.getItemMaps() != null) {
            for (ItemMap im : zone.getItemMaps()) {
                if (im != null && im == currentTarget && botChar.getService() != null) {
                    botChar.getService().pickItem(im);
                    currentTarget = null;
                    return true;
                }
            }
        }
        currentTarget = null;
        return false;
    }

    @Override
    public boolean isComplete(Char botChar) {
        return currentTarget == null;
    }

    private ItemMap findNearestItem(Char botChar) {
        Zone zone = botChar.zone;
        if (zone == null || zone.getItemMaps() == null) {
            return null;
        }
        ItemMap nearest = null;
        double minDist = Double.MAX_VALUE;
        for (ItemMap im : zone.getItemMaps()) {
            if (im == null) {
                continue;
            }
            double dist = Math.hypot(im.getX() - botChar.x, im.getY() - botChar.y);
            if (dist < minDist && dist < 100) {
                minDist = dist;
                nearest = im;
            }
        }
        return nearest;
    }
}
