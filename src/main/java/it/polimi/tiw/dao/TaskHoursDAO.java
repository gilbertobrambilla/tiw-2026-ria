package it.polimi.tiw.dao;

import it.polimi.tiw.beans.Task;
import it.polimi.tiw.beans.manager.monthly.ExpectedHoursMonth;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

    public List<ExpectedHoursMonth> getExpectedHoursByTask(Task task) throws SQLException {
        List<ExpectedHoursMonth> expectedHours = initializeExpectedHours(task.getStartMonth(), task.getEndMonth());
        String query = "SELECT month_index, hours FROM task_expected_hours WHERE task_id = ? ORDER BY month_index ASC";

        // existing monthly expected hours are applied over the full task month range
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, task.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    setExpectedHours(expectedHours, task.getStartMonth(), task.getEndMonth(),
                            rs.getInt("month_index"), rs.getInt("hours"));
                }
            }
        }
        return expectedHours;
    }

    private List<ExpectedHoursMonth> initializeExpectedHours(int startMonth, int endMonth) {
        List<ExpectedHoursMonth> expectedHours = new ArrayList<>();
        for (int month = startMonth; month <= endMonth; month++) {
            expectedHours.add(new ExpectedHoursMonth(month, null));
        }
        return expectedHours;
    }

    private void setExpectedHours(List<ExpectedHoursMonth> expectedHours, int startMonth, int endMonth,
                                  int month, int hours) {
        if (month < startMonth || month > endMonth) {
            return;
        }
        expectedHours.set(month - startMonth, new ExpectedHoursMonth(month, hours));
    }

}
