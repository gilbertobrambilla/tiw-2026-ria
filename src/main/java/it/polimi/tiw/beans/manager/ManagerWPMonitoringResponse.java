package it.polimi.tiw.beans.manager;

import it.polimi.tiw.beans.WP;

import java.util.List;
import java.util.Map;

public class ManagerWPMonitoringResponse {
    private final WP wp;
    private final Map<Integer, Integer> totalExpectedHoursByMonth;
    private final Map<Integer, Integer> totalWorkedHoursByMonth;
    private final List<ManagerTaskMonitoringResponse> tasks;

    public ManagerWPMonitoringResponse(WP wp, Map<Integer, Integer> totalExpectedHoursByMonth,
                                       Map<Integer, Integer> totalWorkedHoursByMonth,
                                       List<ManagerTaskMonitoringResponse> tasks) {
        this.wp = wp;
        this.totalExpectedHoursByMonth = totalExpectedHoursByMonth;
        this.totalWorkedHoursByMonth = totalWorkedHoursByMonth;
        this.tasks = tasks;
    }

    public WP getWp() {
        return wp;
    }

    public Map<Integer, Integer> getTotalExpectedHoursByMonth() {
        return totalExpectedHoursByMonth;
    }

    public Map<Integer, Integer> getTotalWorkedHoursByMonth() {
        return totalWorkedHoursByMonth;
    }

    public List<ManagerTaskMonitoringResponse> getTasks() {
        return tasks;
    }
}
