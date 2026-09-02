package Exe_Z.bot.player;

import Exe_Z.model.Char;

public abstract class BotTask {
    protected BotPlayerAI ai;
    protected int priority = 1;
    protected boolean active = true;
    protected String name;

    public BotTask(String name, int priority) {
        this.name = name;
        this.priority = priority;
    }

    public abstract boolean execute(Char botChar, long now);

    public abstract boolean isComplete(Char botChar);

    public abstract boolean canExecute(Char botChar);

    public int getPriority() {
        return priority;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getName() {
        return name;
    }
}
