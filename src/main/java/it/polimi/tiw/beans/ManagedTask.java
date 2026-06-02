package it.polimi.tiw.beans;

public class ManagedTask {
    private final Project project;
    private final WP wp;
    private final Task task;

    public ManagedTask(Project project, WP wp, Task task) {
        this.project = project;
        this.wp = wp;
        this.task = task;
    }

    public Project getProject() {
        return project;
    }

    public WP getWp() {
        return wp;
    }

    public Task getTask() {
        return task;
    }
}
