package it.polimi.tiw.beans.manager;

import it.polimi.tiw.beans.User;

import java.util.List;

public class ManagerCollaboratorReportResponse {
    private final User collaborator;
    private final List<ManagerCollaboratorProjectResponse> projects;

    public ManagerCollaboratorReportResponse(User collaborator, List<ManagerCollaboratorProjectResponse> projects) {
        this.collaborator = collaborator;
        this.projects = projects;
    }

    public User getCollaborator() {
        return collaborator;
    }

    public List<ManagerCollaboratorProjectResponse> getProjects() {
        return projects;
    }
}
