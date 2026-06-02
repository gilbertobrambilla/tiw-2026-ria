package it.polimi.tiw.beans.admin.request;

import java.util.List;

public class ProjectStructureBlueprint {
    private String title;
    private int durationMonths;
    private int managedBy;
    private List<WPBlueprint> workPackages;

    public ProjectStructureBlueprint() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getDurationMonths() {
        return durationMonths;
    }

    public void setDurationMonths(int durationMonths) {
        this.durationMonths = durationMonths;
    }

    public int getManagedBy() {
        return managedBy;
    }

    public void setManagedBy(int managedBy) {
        this.managedBy = managedBy;
    }

    public List<WPBlueprint> getWorkPackages() {
        return workPackages;
    }

    public void setWorkPackages(List<WPBlueprint> workPackages) {
        this.workPackages = workPackages;
    }
}
