package it.polimi.tiw.beans.manager;

import it.polimi.tiw.beans.Task;
import it.polimi.tiw.beans.manager.monthly.TaskHoursMonth;

import java.util.List;

public class ManagerTaskMonitoringResponse {
    private final Task task;
    private final List<TaskHoursMonth> months;

    public ManagerTaskMonitoringResponse(Task task, List<TaskHoursMonth> months) {
        this.task = task;
        this.months = months;
    }

    public Task getTask() {
        return task;
    }

    public List<TaskHoursMonth> getMonths() {
        return months;
    }
}
