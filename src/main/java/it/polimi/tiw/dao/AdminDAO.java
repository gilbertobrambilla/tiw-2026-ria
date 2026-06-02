package it.polimi.tiw.dao;

import it.polimi.tiw.beans.Project;
import it.polimi.tiw.beans.Task;
import it.polimi.tiw.beans.TaskWithHours;
import it.polimi.tiw.beans.WP;
import it.polimi.tiw.beans.admin.AdminProjectStructureResponse;
import it.polimi.tiw.beans.admin.AdminWPStructureResponse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AdminDAO {
    private final Connection connection;

    public AdminDAO(Connection connection) {
        this.connection = connection;
    }

    public AdminProjectStructureResponse getProjectStructure(Project project) throws SQLException {
        List<AdminWPStructureResponse> wpList = new ArrayList<>();
        String query = "SELECT * FROM project_wp_tasks_summary_view " +
                "WHERE project_id = ? " +
                "ORDER BY wp_sequence ASC, task_sequence ASC";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, project.getId());

            try (ResultSet result = stmt.executeQuery()) {
                WP currentWP = null;
                List<TaskWithHours> currentTaskList = null;

                while (result.next()) {
                    int wpId = result.getInt("wp_id");

                    if (currentWP == null || currentWP.getId() != wpId) {
                        currentWP = new WP(
                                wpId,
                                project.getId(),
                                result.getInt("wp_sequence"),
                                result.getString("wp_title"),
                                result.getInt("wp_start_month"),
                                result.getInt("wp_end_month")
                        );
                        currentTaskList = new ArrayList<>();
                        wpList.add(new AdminWPStructureResponse(currentWP, currentTaskList));
                    }

                    int taskId = result.getInt("task_id");
                    if (result.wasNull()) {
                        continue;
                    }

                    Task task = new Task(
                            taskId,
                            wpId,
                            result.getInt("task_sequence"),
                            result.getString("task_title"),
                            result.getString("task_description"),
                            result.getInt("start_month"),
                            result.getInt("end_month")
                    );

                    if (currentTaskList != null) {
                        currentTaskList.add(new TaskWithHours(
                                task,
                                result.getInt("total_expected_hours"),
                                result.getInt("total_worked_hours")
                        ));
                    }
                }
            }
        }

        return new AdminProjectStructureResponse(project, wpList);
    }
}
