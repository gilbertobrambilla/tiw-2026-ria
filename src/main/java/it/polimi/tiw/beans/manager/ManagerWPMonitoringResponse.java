package it.polimi.tiw.beans.manager;

import it.polimi.tiw.beans.WP;
import it.polimi.tiw.beans.manager.monthly.WPHoursMonth;

import java.util.List;

public class ManagerWPMonitoringResponse {
    private final WP wp;
    private final List<WPHoursMonth> months;
    private final List<ManagerTaskMonitoringResponse> tasks;

    public ManagerWPMonitoringResponse(WP wp, List<WPHoursMonth> months, List<ManagerTaskMonitoringResponse> tasks) {
        this.wp = wp;
        this.months = months;
        this.tasks = tasks;
    }

    public WP getWp() {
        return wp;
    }

    public List<WPHoursMonth> getMonths() {
        return months;
    }

    public List<ManagerTaskMonitoringResponse> getTasks() {
        return tasks;
    }
}
