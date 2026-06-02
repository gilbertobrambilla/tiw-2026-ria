package it.polimi.tiw.beans.manager;

import it.polimi.tiw.beans.WP;

import java.util.List;
import java.util.Map;

public class ManagerCollaboratorWPResponse {
    private final WP wp;
    private final Map<Integer, Integer> totalWorkedHoursByMonth;
    private final List<ManagerCollaboratorTaskResponse> tasks;

    public ManagerCollaboratorWPResponse(WP wp, Map<Integer, Integer> totalWorkedHoursByMonth,
                                         List<ManagerCollaboratorTaskResponse> tasks) {
        this.wp = wp;
        this.totalWorkedHoursByMonth = totalWorkedHoursByMonth;
        this.tasks = tasks;
    }

    public WP getWp() {
        return wp;
    }

    public Map<Integer, Integer> getTotalWorkedHoursByMonth() {
        return totalWorkedHoursByMonth;
    }

    public List<ManagerCollaboratorTaskResponse> getTasks() {
        return tasks;
    }
}
