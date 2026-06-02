package it.polimi.tiw.beans.manager;

import it.polimi.tiw.beans.ManagedTask;
import it.polimi.tiw.beans.User;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ManagerTaskAssignmentResponse {
    private final ManagedTask managedTask;
    private final List<User> availableCollaborators;
    private final Map<Integer, Integer> expectedHoursByMonth;
    private final List<Integer> collaboratorIds;

    public ManagerTaskAssignmentResponse(ManagedTask managedTask, List<User> availableCollaborators,
                                         Map<Integer, Integer> expectedHoursByMonth, List<Integer> collaboratorIds) {
        this.managedTask = managedTask;
        this.availableCollaborators = availableCollaborators;
        this.expectedHoursByMonth = expectedHoursByMonth;
        this.collaboratorIds = collaboratorIds;
    }

    public ManagedTask getManagedTask() {
        return managedTask;
    }

    public List<User> getAvailableCollaborators() {
        return availableCollaborators;
    }

    public Map<Integer, Integer> getExpectedHoursByMonth() {
        return expectedHoursByMonth;
    }

    public List<Integer> getCollaboratorIds() {
        return collaboratorIds;
    }
}
