package it.polimi.tiw.beans.collaborator;

import java.util.List;

public class CollaboratorWPResponse {
    private final int id;
    private final int sequenceNumber;
    private final String title;
    private final int startMonth;
    private final int endMonth;
    private final List<CollaboratorTaskResponse> tasks;

    public CollaboratorWPResponse(int id, int sequenceNumber, String title, int startMonth, int endMonth, List<CollaboratorTaskResponse> tasks) {
        this.id = id;
        this.sequenceNumber = sequenceNumber;
        this.title = title;
        this.startMonth = startMonth;
        this.endMonth = endMonth;
        this.tasks = tasks;
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

    public int getStartMonth() {
        return startMonth;
    }

    public int getEndMonth() {
        return endMonth;
    }

    public List<CollaboratorTaskResponse> getTasks() {
        return tasks;
    }
}
