package it.polimi.tiw.beans.admin.request;

public class TaskBlueprint {
    private String title;
    private String description;
    private int startMonth;
    private int endMonth;

    public TaskBlueprint() {
    }

    public TaskBlueprint(String title, String description, int startMonth, int endMonth) {
        this.title = title;
        this.description = description;
        this.startMonth = startMonth;
        this.endMonth = endMonth;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getStartMonth() {
        return startMonth;
    }

    public void setStartMonth(int startMonth) {
        this.startMonth = startMonth;
    }

    public int getEndMonth() {
        return endMonth;
    }

    public void setEndMonth(int endMonth) {
        this.endMonth = endMonth;
    }
}
