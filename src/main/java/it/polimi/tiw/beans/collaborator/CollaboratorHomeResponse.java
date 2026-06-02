package it.polimi.tiw.beans.collaborator;

import java.util.List;

public class CollaboratorHomeResponse {
    private final List<CollaboratorProjectResponse> projects;

    public CollaboratorHomeResponse(List<CollaboratorProjectResponse> projects) {
        this.projects = projects;
    }

    public List<CollaboratorProjectResponse> getProjects() {
        return projects;
    }
}
