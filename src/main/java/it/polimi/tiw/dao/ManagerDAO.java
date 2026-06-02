package it.polimi.tiw.dao;

import it.polimi.tiw.beans.Project;
import it.polimi.tiw.beans.ProjectStatus;
import it.polimi.tiw.beans.Task;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.beans.WP;
import it.polimi.tiw.beans.manager.ManagerCollaboratorProjectResponse;
import it.polimi.tiw.beans.manager.ManagerCollaboratorReportResponse;
import it.polimi.tiw.beans.manager.ManagerCollaboratorTaskResponse;
import it.polimi.tiw.beans.manager.ManagerCollaboratorWPResponse;
import it.polimi.tiw.beans.manager.ManagerProjectMonitoringResponse;
import it.polimi.tiw.beans.manager.ManagerTaskMonitoringResponse;
import it.polimi.tiw.beans.manager.ManagerWPMonitoringResponse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ManagerDAO {
    private final Connection connection;

    public ManagerDAO(Connection connection) {
        this.connection = connection;
    }

    public ManagerProjectMonitoringResponse getProjectMonitoring(Project project, boolean closable) throws SQLException {
        Map<Integer, WPMonitoringData> wpDataById = new LinkedHashMap<>();
        String query = "SELECT * FROM project_monthly_report_view " +
                "WHERE project_id = ? " +
                "ORDER BY wp_sequence ASC, task_sequence ASC, month_index ASC";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, project.getId());

            try (ResultSet result = stmt.executeQuery()) {
                while (result.next()) {
                    int wpId = result.getInt("wp_id");
                    WPMonitoringData wpData = wpDataById.get(wpId);
                    if (wpData == null) {
                        WP wp = new WP(
                                wpId,
                                project.getId(),
                                result.getInt("wp_sequence"),
                                result.getString("wp_title"),
                                result.getInt("wp_start_month"),
                                result.getInt("wp_end_month")
                        );
                        wpData = new WPMonitoringData(wp);
                        wpDataById.put(wpId, wpData);
                    }

                    int taskId = result.getInt("task_id");
                    TaskMonitoringData taskData = wpData.taskDataById.get(taskId);
                    if (taskData == null) {
                        Task task = new Task(
                                taskId,
                                wpId,
                                result.getInt("task_sequence"),
                                result.getString("task_title"),
                                result.getString("task_description"),
                                result.getInt("task_start_month"),
                                result.getInt("task_end_month")
                        );
                        taskData = new TaskMonitoringData(task);
                        wpData.taskDataById.put(taskId, taskData);
                    }

                    int monthIndex = result.getInt("month_index");
                    if (!result.wasNull()) {
                        int expected = result.getInt("expected_hours");
                        int worked = result.getInt("worked_hours");

                        taskData.expectedHoursByMonth.put(monthIndex, expected);
                        taskData.workedHoursByMonth.put(monthIndex, worked);
                        wpData.totalExpectedHoursByMonth.merge(monthIndex, expected, Integer::sum);
                        wpData.totalWorkedHoursByMonth.merge(monthIndex, worked, Integer::sum);
                    }
                }
            }
        }

        List<ManagerWPMonitoringResponse> wpList = new ArrayList<>();
        for (WPMonitoringData wpData : wpDataById.values()) {
            List<ManagerTaskMonitoringResponse> tasks = new ArrayList<>();
            for (TaskMonitoringData taskData : wpData.taskDataById.values()) {
                tasks.add(new ManagerTaskMonitoringResponse(
                        taskData.task,
                        taskData.expectedHoursByMonth,
                        taskData.workedHoursByMonth
                ));
            }

            wpList.add(new ManagerWPMonitoringResponse(
                    wpData.wp,
                    wpData.totalExpectedHoursByMonth,
                    wpData.totalWorkedHoursByMonth,
                    tasks
            ));
        }

        return new ManagerProjectMonitoringResponse(project, wpList, closable);
    }

    public ManagerCollaboratorReportResponse getCollaboratorReport(User collaborator, int managerId) throws SQLException {
        Map<Integer, CollaboratorProjectData> projectDataById = new LinkedHashMap<>();
        String query = "SELECT p.id AS project_id, p.title AS project_title, p.duration_months AS project_duration, " +
                "p.status AS project_status, p.created_by AS project_created_by, p.managed_by AS manager_id, " +
                "w.id AS wp_id, w.sequence_number AS wp_sequence, w.title AS wp_title, " +
                "w.start_month AS wp_start_month, w.end_month AS wp_end_month, " +
                "t.id AS task_id, t.sequence_number AS task_sequence, t.title AS task_title, " +
                "t.description AS task_description, t.start_month AS task_start_month, t.end_month AS task_end_month, " +
                "twh.month_index, COALESCE(twh.hours, 0) AS worked_hours " +
                "FROM projects p " +
                "JOIN wp w ON p.id = w.project_id " +
                "JOIN tasks t ON w.id = t.wp_id " +
                "JOIN task_workers tw ON t.id = tw.task_id " +
                "LEFT JOIN task_worked_hours twh ON t.id = twh.task_id AND tw.user_id = twh.user_id " +
                "WHERE p.managed_by = ? AND tw.user_id = ? " +
                "ORDER BY p.id DESC, w.sequence_number ASC, t.sequence_number ASC, twh.month_index ASC";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, managerId);
            stmt.setInt(2, collaborator.getId());

            try (ResultSet result = stmt.executeQuery()) {
                while (result.next()) {
                    int projectId = result.getInt("project_id");
                    CollaboratorProjectData projectData = projectDataById.get(projectId);
                    if (projectData == null) {
                        Project project = new Project(
                                projectId,
                                result.getString("project_title"),
                                result.getInt("project_duration"),
                                ProjectStatus.valueOf(result.getString("project_status")),
                                result.getInt("project_created_by"),
                                result.getInt("manager_id")
                        );
                        projectData = new CollaboratorProjectData(project);
                        projectDataById.put(projectId, projectData);
                    }

                    int wpId = result.getInt("wp_id");
                    CollaboratorWPData wpData = projectData.wpDataById.get(wpId);
                    if (wpData == null) {
                        WP wp = new WP(
                                wpId,
                                projectId,
                                result.getInt("wp_sequence"),
                                result.getString("wp_title"),
                                result.getInt("wp_start_month"),
                                result.getInt("wp_end_month")
                        );
                        wpData = new CollaboratorWPData(wp);
                        projectData.wpDataById.put(wpId, wpData);
                    }

                    int taskId = result.getInt("task_id");
                    CollaboratorTaskData taskData = wpData.taskDataById.get(taskId);
                    if (taskData == null) {
                        Task task = new Task(
                                taskId,
                                wpId,
                                result.getInt("task_sequence"),
                                result.getString("task_title"),
                                result.getString("task_description"),
                                result.getInt("task_start_month"),
                                result.getInt("task_end_month")
                        );
                        taskData = new CollaboratorTaskData(task);
                        wpData.taskDataById.put(taskId, taskData);
                    }

                    int monthIndex = result.getInt("month_index");
                    if (!result.wasNull()) {
                        int worked = result.getInt("worked_hours");
                        taskData.workedHoursByMonth.put(monthIndex, worked);
                        wpData.totalWorkedHoursByMonth.merge(monthIndex, worked, Integer::sum);
                    }
                }
            }
        }

        List<ManagerCollaboratorProjectResponse> projects = new ArrayList<>();
        for (CollaboratorProjectData projectData : projectDataById.values()) {
            List<ManagerCollaboratorWPResponse> wpList = new ArrayList<>();
            for (CollaboratorWPData wpData : projectData.wpDataById.values()) {
                List<ManagerCollaboratorTaskResponse> tasks = new ArrayList<>();
                for (CollaboratorTaskData taskData : wpData.taskDataById.values()) {
                    tasks.add(new ManagerCollaboratorTaskResponse(
                            taskData.task,
                            taskData.workedHoursByMonth
                    ));
                }

                wpList.add(new ManagerCollaboratorWPResponse(
                        wpData.wp,
                        wpData.totalWorkedHoursByMonth,
                        tasks
                ));
            }

            projects.add(new ManagerCollaboratorProjectResponse(projectData.project, wpList));
        }

        return new ManagerCollaboratorReportResponse(collaborator, projects);
    }

    private static class WPMonitoringData {
        private final WP wp;
        private final Map<Integer, Integer> totalExpectedHoursByMonth = new HashMap<>();
        private final Map<Integer, Integer> totalWorkedHoursByMonth = new HashMap<>();
        private final Map<Integer, TaskMonitoringData> taskDataById = new LinkedHashMap<>();

        private WPMonitoringData(WP wp) {
            this.wp = wp;
        }
    }

    private static class TaskMonitoringData {
        private final Task task;
        private final Map<Integer, Integer> expectedHoursByMonth = new HashMap<>();
        private final Map<Integer, Integer> workedHoursByMonth = new HashMap<>();

        private TaskMonitoringData(Task task) {
            this.task = task;
        }
    }

    private static class CollaboratorProjectData {
        private final Project project;
        private final Map<Integer, CollaboratorWPData> wpDataById = new LinkedHashMap<>();

        private CollaboratorProjectData(Project project) {
            this.project = project;
        }
    }

    private static class CollaboratorWPData {
        private final WP wp;
        private final Map<Integer, Integer> totalWorkedHoursByMonth = new HashMap<>();
        private final Map<Integer, CollaboratorTaskData> taskDataById = new LinkedHashMap<>();

        private CollaboratorWPData(WP wp) {
            this.wp = wp;
        }
    }

    private static class CollaboratorTaskData {
        private final Task task;
        private final Map<Integer, Integer> workedHoursByMonth = new HashMap<>();

        private CollaboratorTaskData(Task task) {
            this.task = task;
        }
    }
}
