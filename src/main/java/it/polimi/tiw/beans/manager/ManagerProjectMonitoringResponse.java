package it.polimi.tiw.beans.manager;

import it.polimi.tiw.beans.Project;

import java.util.List;

public class ManagerProjectMonitoringResponse {
    private final Project project;
    private final List<ManagerWPMonitoringResponse> wpList;
    private final boolean closable;

    public ManagerProjectMonitoringResponse(Project project, List<ManagerWPMonitoringResponse> wpList, boolean closable) {
        this.project = project;
        this.wpList = wpList;
        this.closable = closable;
    }

    public Project getProject() {
        return project;
    }

    public List<ManagerWPMonitoringResponse> getWpList() {
        return wpList;
    }

    public boolean isClosable() {
        return closable;
    }
}
