package it.polimi.tiw.beans.collaborator;

import java.util.List;

public class CollaboratorTaskResponse {
    private final int id;
    private final int sequenceNumber;
    private final String title;
    private final String description;
    private final int startMonth;
    private final int endMonth;
    private final int taskTotalExpectedHours;
    private final int collaboratorTotalWorkedHours;
    private final List<CollaboratorTaskMonthResponse> months;

    public CollaboratorTaskResponse(int id, int sequenceNumber, String title, String description, int startMonth,
                                    int endMonth, int taskTotalExpectedHours, int collaboratorTotalWorkedHours,
                                    List<CollaboratorTaskMonthResponse> months) {
        this.id = id;
        this.sequenceNumber = sequenceNumber;
        this.title = title;
        this.description = description;
        this.startMonth = startMonth;
        this.endMonth = endMonth;
        this.taskTotalExpectedHours = taskTotalExpectedHours;
        this.collaboratorTotalWorkedHours = collaboratorTotalWorkedHours;
        this.months = months;
    }

    public int getId() {
        return id;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getStartMonth() {
        return startMonth;
    }

    public int getEndMonth() {
        return endMonth;
    }

    public int getTaskTotalExpectedHours() {
        return taskTotalExpectedHours;
    }

    public int getCollaboratorTotalWorkedHours() {
        return collaboratorTotalWorkedHours;
    }

    public List<CollaboratorTaskMonthResponse> getMonths() {
        return months;
    }
}
