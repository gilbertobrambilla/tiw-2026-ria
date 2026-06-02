package it.polimi.tiw.beans.manager;

import it.polimi.tiw.beans.Task;

import java.util.Map;

public class ManagerCollaboratorTaskResponse {
    private final Task task;
    private final Map<Integer, Integer> workedHoursByMonth;

    public ManagerCollaboratorTaskResponse(Task task, Map<Integer, Integer> workedHoursByMonth) {
        this.task = task;
        this.workedHoursByMonth = workedHoursByMonth;
    }

    public Task getTask() {
        return task;
    }

    public Map<Integer, Integer> getWorkedHoursByMonth() {
        return workedHoursByMonth;
    }
}
