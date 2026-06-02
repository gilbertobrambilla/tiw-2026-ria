package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class TaskHoursDAO {
    private final Connection connection;

    public TaskHoursDAO(Connection connection) {
        this.connection = connection;
    }

    public void saveExpectedHours(int taskId, int monthIndex, int hours) throws SQLException {
        String selectQuery = "SELECT * FROM task_expected_hours WHERE task_id = ? AND month_index = ?";
        boolean existingExpectedHours;

        // existing expected-hours row is checked
        try (PreparedStatement stmt = connection.prepareStatement(selectQuery)) {
            stmt.setInt(1, taskId);
            stmt.setInt(2, monthIndex);

            try (ResultSet result = stmt.executeQuery()) {
                existingExpectedHours = result.next();
            }
        }

        if (existingExpectedHours) {
            String updateQuery = "UPDATE task_expected_hours SET hours = ? WHERE task_id = ? AND month_index = ?";

            try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
                stmt.setInt(1, hours);
                stmt.setInt(2, taskId);
                stmt.setInt(3, monthIndex);
                stmt.executeUpdate();
            }
        } else {
            String insertQuery = "INSERT INTO task_expected_hours (task_id, month_index, hours) VALUES (?, ?, ?)";

            try (PreparedStatement stmt = connection.prepareStatement(insertQuery)) {
                stmt.setInt(1, taskId);
                stmt.setInt(2, monthIndex);
                stmt.setInt(3, hours);
                stmt.executeUpdate();
            }
        }
    }

    public Map<Integer, Integer> getExpectedHoursByTask(int taskId) throws SQLException {
        Map<Integer, Integer> expectedHours = new HashMap<>();
        String query = "SELECT month_index, hours FROM task_expected_hours WHERE task_id = ? ORDER BY month_index ASC";

        // monthly expected hours are extracted and mapped
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, taskId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    expectedHours.put(rs.getInt("month_index"), rs.getInt("hours"));
                }
            }
        }
        return expectedHours;
    }

}
