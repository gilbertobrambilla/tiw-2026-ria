package it.polimi.tiw.beans.admin.request;

import java.util.List;

public class WPBlueprint {
    private String title;
    private int startMonth;
    private int endMonth;
    private List<TaskBlueprint> tasks;

    public WPBlueprint() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public List<TaskBlueprint> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskBlueprint> tasks) {
        this.tasks = tasks;
    }
}
