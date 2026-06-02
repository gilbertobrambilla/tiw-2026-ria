package it.polimi.tiw.beans.manager;

import it.polimi.tiw.beans.ManagedTask;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.beans.manager.monthly.ExpectedHoursMonth;

import java.util.List;

public class ManagerTaskAssignmentResponse {
    private final ManagedTask managedTask;
    private final List<User> availableCollaborators;
    private final List<ExpectedHoursMonth> expectedHours;
    private final List<Integer> collaboratorIds;

    public ManagerTaskAssignmentResponse(ManagedTask managedTask, List<User> availableCollaborators,
                                         List<ExpectedHoursMonth> expectedHours, List<Integer> collaboratorIds) {
        this.managedTask = managedTask;
        this.availableCollaborators = availableCollaborators;
        this.expectedHours = expectedHours;
        this.collaboratorIds = collaboratorIds;
    }

    public ManagedTask getManagedTask() {
        return managedTask;
    }

    public List<User> getAvailableCollaborators() {
        return availableCollaborators;
    }

    public List<ExpectedHoursMonth> getExpectedHours() {
        return expectedHours;
    }

    public List<Integer> getCollaboratorIds() {
        return collaboratorIds;
    }
}
