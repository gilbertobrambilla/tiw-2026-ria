package it.polimi.tiw.beans.manager;

import it.polimi.tiw.beans.Task;

import java.util.Map;

public class ManagerTaskMonitoringResponse {
    private final Task task;
    private final Map<Integer, Integer> expectedHoursByMonth;
    private final Map<Integer, Integer> workedHoursByMonth;

    public ManagerTaskMonitoringResponse(Task task, Map<Integer, Integer> expectedHoursByMonth,
                                         Map<Integer, Integer> workedHoursByMonth) {
        this.task = task;
        this.expectedHoursByMonth = expectedHoursByMonth;
        this.workedHoursByMonth = workedHoursByMonth;
    }

    public Task getTask() {
        return task;
    }

    public Map<Integer, Integer> getExpectedHoursByMonth() {
        return expectedHoursByMonth;
    }

    public Map<Integer, Integer> getWorkedHoursByMonth() {
        return workedHoursByMonth;
    }
}
