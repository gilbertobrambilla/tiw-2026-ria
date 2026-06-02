package it.polimi.tiw.beans.collaborator;

public class CollaboratorTaskMonthResponse {
    private final int month;
    private final int taskExpectedHours;
    private final int collaboratorWorkedHours;

    public CollaboratorTaskMonthResponse(int month, int taskExpectedHours, int collaboratorWorkedHours) {
        this.month = month;
        this.taskExpectedHours = taskExpectedHours;
        this.collaboratorWorkedHours = collaboratorWorkedHours;
    }

    public int getMonth() {
        return month;
    }

    public int getTaskExpectedHours() {
        return taskExpectedHours;
    }

    public int getCollaboratorWorkedHours() {
        return collaboratorWorkedHours;
    }
}
