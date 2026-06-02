package it.polimi.tiw.beans.admin;

import it.polimi.tiw.beans.TaskWithHours;
import it.polimi.tiw.beans.WP;

import java.util.List;

public class AdminWPStructureResponse {
    private final WP wp;
    private final List<TaskWithHours> tasks;

    public AdminWPStructureResponse(WP wp, List<TaskWithHours> tasks) {
        this.wp = wp;
        this.tasks = tasks;
    }

    public WP getWp() {
        return wp;
    }

    public List<TaskWithHours> getTasks() {
        return tasks;
    }
}
