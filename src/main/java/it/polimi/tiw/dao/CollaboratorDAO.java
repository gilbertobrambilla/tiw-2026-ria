package it.polimi.tiw.dao;

import it.polimi.tiw.beans.collaborator.CollaboratorHomeResponse;
import it.polimi.tiw.beans.collaborator.CollaboratorProjectResponse;
import it.polimi.tiw.beans.collaborator.CollaboratorTaskMonthResponse;
import it.polimi.tiw.beans.collaborator.CollaboratorTaskResponse;
import it.polimi.tiw.beans.collaborator.CollaboratorWPResponse;
import it.polimi.tiw.beans.Task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollaboratorDAO {
    private final Connection connection;

    public CollaboratorDAO(Connection connection) {
        this.connection = connection;
    }

    public CollaboratorHomeResponse getCollaboratorHomeData(int collaboratorId) throws SQLException {
        List<CollaboratorProjectResponse> projects = new ArrayList<>();
        String query = "SELECT p.id AS p_id, p.title AS p_title, p.duration_months, " +
                "w.id AS w_id, w.sequence_number AS w_sequence, w.title AS w_title, w.start_month AS w_start, w.end_month AS w_end, " +
                "t.id AS t_id, t.sequence_number AS t_sequence, t.title AS t_title, t.description, t.start_month AS t_start, t.end_month AS t_end, " +
                "COALESCE(teh.task_total_expected_hours, 0) AS task_total_expected_hours, " +
                "COALESCE(twh.collaborator_total_worked_hours, 0) AS collaborator_total_worked_hours " +
                "FROM projects p " +
                "JOIN wp w ON p.id = w.project_id " +
                "JOIN tasks t ON w.id = t.wp_id " +
                "JOIN task_workers tw ON t.id = tw.task_id " +
                "LEFT JOIN (SELECT task_id, SUM(hours) AS task_total_expected_hours FROM task_expected_hours GROUP BY task_id) teh ON t.id = teh.task_id " +
                "LEFT JOIN (SELECT task_id, user_id, SUM(hours) AS collaborator_total_worked_hours FROM task_worked_hours WHERE user_id = ? GROUP BY task_id, user_id) twh " +
                "ON t.id = twh.task_id AND tw.user_id = twh.user_id " +
                "WHERE tw.user_id = ? AND p.status = 'assigned' " +
                "ORDER BY p.id DESC, w.sequence_number ASC, t.sequence_number ASC";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, collaboratorId);
            stmt.setInt(2, collaboratorId);

            try (ResultSet result = stmt.executeQuery()) {
                int currentProjectId = -1;
                int currentWpId = -1;
                List<CollaboratorWPResponse> currentWpList = null;
                List<CollaboratorTaskResponse> currentTaskList = null;

                while (result.next()) {
                    int projectId = result.getInt("p_id");
                    int wpId = result.getInt("w_id");
                    int taskId = result.getInt("t_id");

                    if (projectId != currentProjectId) {
                        currentProjectId = projectId;
                        currentWpId = -1;
                        currentWpList = new ArrayList<>();
                        projects.add(new CollaboratorProjectResponse(
                                projectId,
                                result.getString("p_title"),
                                result.getInt("duration_months"),
                                currentWpList
                        ));
                    }

                    if (wpId != currentWpId) {
                        currentWpId = wpId;
                        currentTaskList = new ArrayList<>();
                        if (currentWpList != null) {
                            currentWpList.add(new CollaboratorWPResponse(
                                    wpId,
                                    result.getInt("w_sequence"),
                                    result.getString("w_title"),
                                    result.getInt("w_start"),
                                    result.getInt("w_end"),
                                    currentTaskList
                            ));
                        }
                    }

                    if (currentTaskList != null) {
                        int startMonth = result.getInt("t_start");
                        int endMonth = result.getInt("t_end");
                        currentTaskList.add(new CollaboratorTaskResponse(
                                taskId,
                                result.getInt("t_sequence"),
                                result.getString("t_title"),
                                result.getString("description"),
                                startMonth,
                                endMonth,
                                result.getInt("task_total_expected_hours"),
                                result.getInt("collaborator_total_worked_hours"),
                                getCollaboratorTaskMonths(taskId, collaboratorId, startMonth, endMonth)
                        ));
                    }
                }
            }
        }

        return new CollaboratorHomeResponse(projects);
    }

    public void saveCollaboratorWorkedHours(int taskId, int collaboratorId, int month, int hours) throws SQLException {
        String selectQuery = "SELECT task_id FROM task_worked_hours WHERE task_id = ? AND user_id = ? AND month_index = ?";
        boolean existingWorkedHours;

        try (PreparedStatement stmt = connection.prepareStatement(selectQuery)) {
            stmt.setInt(1, taskId);
            stmt.setInt(2, collaboratorId);
            stmt.setInt(3, month);

            try (ResultSet result = stmt.executeQuery()) {
                existingWorkedHours = result.next();
            }
        }

        if (existingWorkedHours) {
            String updateQuery = "UPDATE task_worked_hours SET hours = ? WHERE task_id = ? AND user_id = ? AND month_index = ?";

            try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
                stmt.setInt(1, hours);
                stmt.setInt(2, taskId);
                stmt.setInt(3, collaboratorId);
                stmt.setInt(4, month);
                stmt.executeUpdate();
            }
        } else {
            String insertQuery = "INSERT INTO task_worked_hours (task_id, user_id, month_index, hours) VALUES (?, ?, ?, ?)";

            try (PreparedStatement stmt = connection.prepareStatement(insertQuery)) {
                stmt.setInt(1, taskId);
                stmt.setInt(2, collaboratorId);
                stmt.setInt(3, month);
                stmt.setInt(4, hours);
                stmt.executeUpdate();
            }
        }
    }

    private List<CollaboratorTaskMonthResponse> getCollaboratorTaskMonths(int taskId, int collaboratorId, int startMonth, int endMonth) throws SQLException {
        Map<Integer, Integer> taskExpectedHours = getExpectedHoursByTask(taskId);
        Map<Integer, Integer> collaboratorWorkedHours = getWorkedHoursByTaskAndCollaborator(taskId, collaboratorId);
        List<CollaboratorTaskMonthResponse> months = new ArrayList<>();

        for (int month = startMonth; month <= endMonth; month++) {
            months.add(new CollaboratorTaskMonthResponse(
                    month,
                    taskExpectedHours.getOrDefault(month, 0),
                    collaboratorWorkedHours.getOrDefault(month, 0)
            ));
        }

        return months;
    }

    private Map<Integer, Integer> getExpectedHoursByTask(int taskId) throws SQLException {
        Map<Integer, Integer> taskExpectedHours = new HashMap<>();
        String query = "SELECT month_index, hours FROM task_expected_hours WHERE task_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, taskId);

            try (ResultSet result = stmt.executeQuery()) {
                while (result.next()) {
                    taskExpectedHours.put(result.getInt("month_index"), result.getInt("hours"));
                }
            }
        }

        return taskExpectedHours;
    }

    private Map<Integer, Integer> getWorkedHoursByTaskAndCollaborator(int taskId, int collaboratorId) throws SQLException {
        Map<Integer, Integer> collaboratorWorkedHours = new HashMap<>();
        String query = "SELECT month_index, hours FROM task_worked_hours WHERE task_id = ? AND user_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, taskId);
            stmt.setInt(2, collaboratorId);

            try (ResultSet result = stmt.executeQuery()) {
                while (result.next()) {
                    collaboratorWorkedHours.put(result.getInt("month_index"), result.getInt("hours"));
                }
            }
        }

        return collaboratorWorkedHours;
    }

    public Task getAssignedTaskForWorkedHoursUpdate(int taskId, int collaboratorId) throws SQLException {
        String query = "SELECT t.* " +
                "FROM tasks t " +
                "JOIN wp w ON t.wp_id = w.id " +
                "JOIN projects p ON w.project_id = p.id " +
                "JOIN task_workers tw ON t.id = tw.task_id " +
                "WHERE t.id = ? AND tw.user_id = ? AND p.status = 'assigned'";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, taskId);
            stmt.setInt(2, collaboratorId);

            try (ResultSet result = stmt.executeQuery()) {
                if (result.next()) {
                    return new Task(
                            result.getInt("id"),
                            result.getInt("wp_id"),
                            result.getInt("sequence_number"),
                            result.getString("title"),
                            result.getString("description"),
                            result.getInt("start_month"),
                            result.getInt("end_month")
                    );
                }
            }
        }

        return null;
    }
}
