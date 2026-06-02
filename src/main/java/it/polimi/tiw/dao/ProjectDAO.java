package it.polimi.tiw.dao;

import it.polimi.tiw.beans.Project;
import it.polimi.tiw.beans.ProjectStatus;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProjectDAO {
    private final Connection connection;

    public ProjectDAO(Connection connection) {
        this.connection = connection;
    }

    public int createProject(String title, int durationMonths, int createdBy, int managedBy) throws SQLException {
        String query = "INSERT INTO projects (title, duration_months, created_by, managed_by) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, title);
            stmt.setInt(2, durationMonths);
            stmt.setInt(3, createdBy);
            stmt.setInt(4, managedBy);
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }
        throw new SQLException("Project creation failed, no generated id returned");
    }

    public List<Project> getProjectsByCreator(int creatorId) throws SQLException {
        List<Project> projects = new ArrayList<>();
        String query = "SELECT * FROM projects WHERE created_by = ? ORDER BY id DESC";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, creatorId);

            try (ResultSet result = stmt.executeQuery()) {
                while (result.next()) {
                    Project project = new Project(
                            result.getInt("id"),
                            result.getString("title"),
                            result.getInt("duration_months"),
                            ProjectStatus.valueOf(result.getString("status")),
                            result.getInt("created_by"),
                            result.getInt("managed_by")
                    );
                    projects.add(project);
                }
            }
        }
        return projects;
    }

    public Project getProjectByIdAndCreator(int projectId, int creatorId) throws SQLException {
        String query = "SELECT * FROM projects WHERE id = ? AND created_by = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, projectId);
            stmt.setInt(2, creatorId);

            try (ResultSet result = stmt.executeQuery()) {
                if (result.next()) {
                    return new Project(
                            result.getInt("id"),
                            result.getString("title"),
                            result.getInt("duration_months"),
                            ProjectStatus.valueOf(result.getString("status")),
                            result.getInt("created_by"),
                            result.getInt("managed_by")
                    );
                }
            }
        }
        return null;
    }

    public boolean isProjectClosable(int projectId) throws SQLException {
        String query = "SELECT COUNT(*) AS uncompleted_tasks_count FROM project_wp_tasks_summary_view WHERE project_id = ? AND total_worked_hours < total_expected_hours";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, projectId);

            try (ResultSet result = stmt.executeQuery()) {
                if (result.next()) {
                    // true is returned if no uncompleted tasks are found
                    return result.getInt("uncompleted_tasks_count") == 0;
                }
            }
        }
        return false;
    }

    public void updateProjectStatus(int projectId, ProjectStatus status) throws SQLException {
        String query = "UPDATE projects SET status = ? WHERE id = ?";

        // project status is updated in the database
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, status.name().toLowerCase());
            stmt.setInt(2, projectId);
            stmt.executeUpdate();
        }
    }

    public boolean isProjectAssignable(int projectId) throws SQLException {
        String query = "SELECT COUNT(*) AS blocking_issues_count " +
                "FROM (" +
                "SELECT t.id " +
                "FROM tasks t " +
                "JOIN wp w ON t.wp_id = w.id " +
                "LEFT JOIN task_workers tw ON t.id = tw.task_id " +
                "LEFT JOIN task_expected_hours teh ON t.id = teh.task_id " +
                "AND teh.month_index BETWEEN t.start_month AND t.end_month " +
                "WHERE w.project_id = ? " +
                "GROUP BY t.id, t.start_month, t.end_month " +
                "HAVING COUNT(DISTINCT tw.user_id) = 0 " +
                "OR COUNT(DISTINCT teh.month_index) < (t.end_month - t.start_month + 1)" +
                ") blocking_tasks";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, projectId);

            try (ResultSet result = stmt.executeQuery()) {
                if (result.next()) {
                    return result.getInt("blocking_issues_count") == 0;
                }
            }
        }

        return false;
    }

    public List<Project> getAssignedAndCompletedProjectsByManager(int managerId) throws SQLException {
        List<Project> projects = new ArrayList<>();

        String query = "SELECT * FROM projects " +
                "WHERE managed_by = ? AND status IN (?, ?) " +
                "ORDER BY CASE status WHEN 'assigned' THEN 0 WHEN 'completed' THEN 1 ELSE 2 END, id DESC";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, managerId);
            stmt.setString(2, ProjectStatus.assigned.name());
            stmt.setString(3, ProjectStatus.completed.name());

            try (ResultSet result = stmt.executeQuery()) {
                while (result.next()) {
                    Project project = new Project(
                            result.getInt("id"),
                            result.getString("title"),
                            result.getInt("duration_months"),
                            ProjectStatus.valueOf(result.getString("status")),
                            result.getInt("created_by"),
                            result.getInt("managed_by")
                    );
                    projects.add(project);
                }
            }
        }

        return projects;
    }

    public Project getProjectByIdAndManager(int projectId, int managerId) throws SQLException {
        String query = "SELECT * FROM projects WHERE id = ? AND managed_by = ?";

        // project ownership is verified directly in the query
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, projectId);
            stmt.setInt(2, managerId);

            try (ResultSet result = stmt.executeQuery()) {
                if (result.next()) {
                    return new Project(
                            result.getInt("id"),
                            result.getString("title"),
                            result.getInt("duration_months"),
                            ProjectStatus.valueOf(result.getString("status")),
                            result.getInt("created_by"),
                            result.getInt("managed_by")
                    );
                }
            }
        }
        return null;
    }
}
