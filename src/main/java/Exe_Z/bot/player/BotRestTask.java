package Exe_Z.bot.player;

import Exe_Z.model.Char;
import Exe_Z.util.NinjaUtils;

public class BotRestTask extends BotTask {

    private long restUntil = 0;
    private static final long REST_DURATION = 3000;

    public BotRestTask() {
        super("Rest", 15);
    }

    @Override
    public boolean canExecute(Char botChar) {
        return botChar != null
                && (botChar.hp < botChar.maxHP * 0.3 || botChar.mp < botChar.maxMP * 0.2);
    }

    @Override
    public boolean execute(Char botChar, long now) {
        if (restUntil == 0) {
            restUntil = now + REST_DURATION + NinjaUtils.nextInt(2000);
        }
        if (now < restUntil) {
            return true;
        }
        restUntil = 0;
        botChar.hp = botChar.maxHP;
        botChar.mp = botChar.maxMP;
        return false;
    }

    @Override
    public boolean isComplete(Char botChar) {
        return botChar.hp > botChar.maxHP * 0.8 && botChar.mp > botChar.maxMP * 0.6;
    }
}
