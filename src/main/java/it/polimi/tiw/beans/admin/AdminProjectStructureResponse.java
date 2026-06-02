package it.polimi.tiw.beans.admin;

import it.polimi.tiw.beans.Project;

import java.util.List;

public class AdminProjectStructureResponse {
    private final Project project;
    private final List<AdminWPStructureResponse> wpList;

    public AdminProjectStructureResponse(Project project, List<AdminWPStructureResponse> wpList) {
        this.project = project;
        this.wpList = wpList;
    }

    public Project getProject() {
        return project;
    }

    public List<AdminWPStructureResponse> getWpList() {
        return wpList;
    }
}
