package it.polimi.tiw.beans.collaborator;

import java.util.List;

public class CollaboratorProjectResponse {
    private final int id;
    private final String title;
    private final int durationMonths;
    private final List<CollaboratorWPResponse> workPackages;

    public CollaboratorProjectResponse(int id, String title, int durationMonths, List<CollaboratorWPResponse> workPackages) {
        this.id = id;
        this.title = title;
        this.durationMonths = durationMonths;
        this.workPackages = workPackages;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getDurationMonths() {
        return durationMonths;
    }

    public List<CollaboratorWPResponse> getWorkPackages() {
        return workPackages;
    }
}
