package it.polimi.tiw.dao;

import it.polimi.tiw.beans.ManagedTask;
import it.polimi.tiw.beans.Project;
import it.polimi.tiw.beans.ProjectStatus;
import it.polimi.tiw.beans.Task;
import it.polimi.tiw.beans.WP;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {
    private final Connection connection;

    public TaskDAO(Connection connection) {
        this.connection = connection;
    }

    public int createTask(int wpId, String title, String description, int startMonth, int endMonth) throws SQLException {
        String seqQuery = "SELECT COALESCE(MAX(sequence_number), 0) + 1 AS next_seq FROM tasks WHERE wp_id = ?";
        String insertQuery = "INSERT INTO tasks (wp_id, sequence_number, title, description, start_month, end_month) VALUES (?, ?, ?, ?, ?, ?)";

        int nextSeq = 1;

        // sequence number assigned dynamically based on the parent wp
        try (PreparedStatement seqStmt = connection.prepareStatement(seqQuery)) {
            seqStmt.setInt(1, wpId);
            try (ResultSet rs = seqStmt.executeQuery()) {
                if (rs.next()) {
                    nextSeq = rs.getInt("next_seq");
                }
            }
        }

        try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            insertStmt.setInt(1, wpId);
            insertStmt.setInt(2, nextSeq);
            insertStmt.setString(3, title);
            insertStmt.setString(4, description);
            insertStmt.setInt(5, startMonth);
            insertStmt.setInt(6, endMonth);
            insertStmt.executeUpdate();

            try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }
        throw new SQLException("Task creation failed, no generated id returned");
    }

    public void deleteWorkersByTask(int taskId) throws SQLException {
        String query = "DELETE FROM task_workers WHERE task_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, taskId);
            stmt.executeUpdate();
        }
    }

    public void addWorkerToTask(int taskId, int userId) throws SQLException {
        String query = "INSERT INTO task_workers (task_id, user_id) VALUES (?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, taskId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }

    public Task getTaskByIdAndManager(int taskId, int managerId) throws SQLException {
        // double join prevents unauthorized access to tasks belonging to other managers
        String query = "SELECT t.* FROM tasks t " +
                "JOIN wp w ON t.wp_id = w.id " +
                "JOIN projects p ON w.project_id = p.id " +
                "WHERE t.id = ? AND p.managed_by = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, taskId);
            stmt.setInt(2, managerId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Task(
                            rs.getInt("id"),
                            rs.getInt("wp_id"),
                            rs.getInt("sequence_number"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getInt("start_month"),
                            rs.getInt("end_month")
                    );
                }
            }
        }
        return null;
    }

    public Task getTaskByIdAndManagerAndProjectStatus(int taskId, int managerId, ProjectStatus projectStatus) throws SQLException {
        String query = "SELECT t.* FROM tasks t " +
                "JOIN wp w ON t.wp_id = w.id " +
                "JOIN projects p ON w.project_id = p.id " +
                "WHERE t.id = ? AND p.managed_by = ? AND p.status = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, taskId);
            stmt.setInt(2, managerId);
            stmt.setString(3, projectStatus.name());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Task(
                            rs.getInt("id"),
                            rs.getInt("wp_id"),
                            rs.getInt("sequence_number"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getInt("start_month"),
                            rs.getInt("end_month")
                    );
                }
            }
        }
        return null;
    }

    public int getProjectIdByTask(int taskId) throws SQLException {
        // project id is extracted traversing upward the wp relationship
        String query = "SELECT w.project_id FROM wp w JOIN tasks t ON w.id = t.wp_id WHERE t.id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, taskId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("project_id");
                }
            }
        }
        return -1;
    }

    public List<ManagedTask> getManagedTasksByManagerAndProjectStatus(int managerId, ProjectStatus status) throws SQLException {
        List<ManagedTask> managedTasks = new ArrayList<>();
        String query = "SELECT p.id AS p_id, p.title AS p_title, p.duration_months, p.status, p.created_by, p.managed_by, " +
                "w.id AS w_id, w.sequence_number AS w_seq, w.title AS w_title, w.start_month AS w_start, w.end_month AS w_end, " +
                "t.id AS t_id, t.sequence_number AS t_seq, t.title AS t_title, t.description, t.start_month AS t_start, t.end_month AS t_end " +
                "FROM projects p " +
                "JOIN wp w ON p.id = w.project_id " +
                "JOIN tasks t ON w.id = t.wp_id " +
                "WHERE p.managed_by = ? AND p.status = ? " +
                "ORDER BY p.id DESC, w.sequence_number ASC, t.sequence_number ASC";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, managerId);
            stmt.setString(2, status.name());

            try (ResultSet result = stmt.executeQuery()) {
                while (result.next()) {
                    Project project = new Project(
                            result.getInt("p_id"),
                            result.getString("p_title"),
                            result.getInt("duration_months"),
                            ProjectStatus.valueOf(result.getString("status")),
                            result.getInt("created_by"),
                            result.getInt("managed_by")
                    );

                    WP wp = new WP(
                            result.getInt("w_id"),
                            project.getId(),
                            result.getInt("w_seq"),
                            result.getString("w_title"),
                            result.getInt("w_start"),
                            result.getInt("w_end")
                    );

                    Task task = new Task(
                            result.getInt("t_id"),
                            wp.getId(),
                            result.getInt("t_seq"),
                            result.getString("t_title"),
                            result.getString("description"),
                            result.getInt("t_start"),
                            result.getInt("t_end")
                    );

                    managedTasks.add(new ManagedTask(project, wp, task));
                }
            }
        }
        return managedTasks;
    }

    public ManagedTask getManagedTaskByTaskAndManagerAndProjectStatus(int taskId, int managerId, ProjectStatus status) throws SQLException {
        String query = "SELECT p.id AS p_id, p.title AS p_title, p.duration_months, p.status, p.created_by, p.managed_by, " +
                "w.id AS w_id, w.sequence_number AS w_seq, w.title AS w_title, w.start_month AS w_start, w.end_month AS w_end, " +
                "t.id AS t_id, t.sequence_number AS t_seq, t.title AS t_title, t.description, t.start_month AS t_start, t.end_month AS t_end " +
                "FROM projects p " +
                "JOIN wp w ON p.id = w.project_id " +
                "JOIN tasks t ON w.id = t.wp_id " +
                "WHERE t.id = ? AND p.managed_by = ? AND p.status = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, taskId);
            stmt.setInt(2, managerId);
            stmt.setString(3, status.name());

            try (ResultSet result = stmt.executeQuery()) {
                if (result.next()) {
                    Project project = new Project(
                            result.getInt("p_id"),
                            result.getString("p_title"),
                            result.getInt("duration_months"),
                            ProjectStatus.valueOf(result.getString("status")),
                            result.getInt("created_by"),
                            result.getInt("managed_by")
                    );

                    WP wp = new WP(
                            result.getInt("w_id"),
                            project.getId(),
                            result.getInt("w_seq"),
                            result.getString("w_title"),
                            result.getInt("w_start"),
                            result.getInt("w_end")
                    );

                    Task task = new Task(
                            result.getInt("t_id"),
                            wp.getId(),
                            result.getInt("t_seq"),
                            result.getString("t_title"),
                            result.getString("description"),
                            result.getInt("t_start"),
                            result.getInt("t_end")
                    );

                    return new ManagedTask(project, wp, task);
                }
            }
        }
        return null;
    }
}
