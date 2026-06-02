package it.polimi.tiw.beans.manager;

import it.polimi.tiw.beans.WP;
import it.polimi.tiw.beans.manager.monthly.WPWorkedHoursMonth;

import java.util.List;

public class ManagerCollaboratorWPResponse {
    private final WP wp;
    private final List<WPWorkedHoursMonth> months;
    private final List<ManagerCollaboratorTaskResponse> tasks;

    public ManagerCollaboratorWPResponse(WP wp, List<WPWorkedHoursMonth> months,
                                         List<ManagerCollaboratorTaskResponse> tasks) {
        this.wp = wp;
        this.months = months;
        this.tasks = tasks;
    }

    public WP getWp() {
        return wp;
    }

    public List<WPWorkedHoursMonth> getMonths() {
        return months;
    }

    public List<ManagerCollaboratorTaskResponse> getTasks() {
        return tasks;
    }
}
