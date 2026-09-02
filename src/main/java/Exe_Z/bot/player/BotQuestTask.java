package Exe_Z.bot.player;

import Exe_Z.model.Char;
import Exe_Z.task.Task;
import Exe_Z.task.TaskOrder;

import java.util.ArrayList;
import java.util.List;

public class BotQuestTask extends BotTask {

    private Task mainTask;
    private TaskOrder sideTask;
    private List<Integer> completedQuests = new ArrayList<>();

    public BotQuestTask() {
        super("Questing", 10);
    }

    @Override
    public boolean canExecute(Char botChar) {
        return botChar != null && !botChar.isDead;
    }

    @Override
    public boolean execute(Char botChar, long now) {
        if (mainTask == null) {
            mainTask = botChar.taskMain;
        }
        if (sideTask == null && botChar.taskOrders != null && !botChar.taskOrders.isEmpty()) {
            sideTask = botChar.taskOrders.get(0);
        }

        if (mainTask != null && mainTask.isComplete()) {
            botChar.updateTask();
            mainTask = botChar.taskMain;
            return true;
        }

        if (sideTask != null && sideTask.isComplete()) {
            sideTask.updateTask(999);
            sideTask = botChar.taskOrders != null && !botChar.taskOrders.isEmpty()
                    ? botChar.taskOrders.get(0) : null;
            return true;
        }

        return false;
    }

    @Override
    public boolean isComplete(Char botChar) {
        return mainTask == null && sideTask == null;
    }

    public boolean hasActiveQuest() {
        return mainTask != null || sideTask != null;
    }

    public String getQuestStatus(Char botChar) {
        if (mainTask != null) {
            return "Main quest step: " + mainTask.index;
        }
        if (sideTask != null) {
            return "Side quest: " + sideTask.count + "/" + sideTask.maxCount;
        }
        return "No active quest";
    }
}
