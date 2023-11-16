package util.cli;

import model.CliSummary;
import model.TaskParameter;

import java.util.ArrayList;
import java.util.List;

public class CliTaskManager {
    List<CliTask> tasks;
    CliTask currentTask;

    public CliTaskManager(List<CliTask> tasks) {
        this.tasks = tasks;
    }


    public List<String> getAllTaskNames() {
        List<String> names = new ArrayList<>();
        for (CliTask task: tasks) {
            names.add(task.getName());
        }
        return names;
    }

    public boolean selectTask(int index) {
        if (index < 0 || index >= tasks.size()) {
            return false;
        }
        currentTask = tasks.get(index);
        return true;
    }

    public CliTask getCurrentTask() {
        return this.currentTask;
    }



    public List<TaskParameter> getParameters() {
        return this.currentTask.getParameters();
    }


    public CliSummary validateParameters() {
        return this.currentTask.validateParameters();
    }

    public CliSummary runCurrentTask() {
        return this.currentTask.run();
    }

    enum State {
        TASK_SELECTION,
        PARAMETER_PROCESSING,
        TASK_EXECUTION
    }

}
