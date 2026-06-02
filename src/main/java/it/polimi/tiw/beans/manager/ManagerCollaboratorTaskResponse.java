package it.polimi.tiw.beans.manager;

import it.polimi.tiw.beans.Task;
import it.polimi.tiw.beans.manager.monthly.TaskWorkedHoursMonth;

import java.util.List;

public class ManagerCollaboratorTaskResponse {
    private final Task task;
    private final List<TaskWorkedHoursMonth> months;

    public ManagerCollaboratorTaskResponse(Task task, List<TaskWorkedHoursMonth> months) {
        this.task = task;
        this.months = months;
    }

    public Task getTask() {
        return task;
    }

    public List<TaskWorkedHoursMonth> getMonths() {
        return months;
    }
}
