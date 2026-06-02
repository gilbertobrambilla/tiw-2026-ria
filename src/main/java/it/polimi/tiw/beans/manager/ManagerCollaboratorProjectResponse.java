package it.polimi.tiw.beans.manager;

import it.polimi.tiw.beans.Project;

import java.util.List;

public class ManagerCollaboratorProjectResponse {
    private final Project project;
    private final List<ManagerCollaboratorWPResponse> wpList;

    public ManagerCollaboratorProjectResponse(Project project, List<ManagerCollaboratorWPResponse> wpList) {
        this.project = project;
        this.wpList = wpList;
    }

    public Project getProject() {
        return project;
    }

    public List<ManagerCollaboratorWPResponse> getWpList() {
        return wpList;
    }
}
