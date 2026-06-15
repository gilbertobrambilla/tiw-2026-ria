package it.polimi.tiw.controllers.technical.manager;

import it.polimi.tiw.beans.ManagedTask;
import it.polimi.tiw.beans.ProjectStatus;
import it.polimi.tiw.beans.Task;
import it.polimi.tiw.beans.UserLogin;
import it.polimi.tiw.beans.UserRole;
import it.polimi.tiw.beans.manager.monthly.ExpectedHoursMonth;
import it.polimi.tiw.dao.TaskDAO;
import it.polimi.tiw.dao.TaskHoursDAO;
import it.polimi.tiw.dao.UserDAO;
import it.polimi.tiw.utils.DatabaseConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@WebServlet("/technical/manager/save-task-assignment")
@MultipartConfig
public class SaveTaskAssignment extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        UserLogin loggedInUser = (UserLogin) session.getAttribute("user");

        int taskId;
        try {
            taskId = parseRequiredInt(request, "taskId");
        } catch (NumberFormatException e) {
            sendTextError(response, HttpServletResponse.SC_BAD_REQUEST, "Task id must be an integer");
            return;
        }

        try (Connection connection = DatabaseConnection.getInstance(getServletContext()).getConnection()) {
            TaskDAO taskDAO = new TaskDAO(connection);
            TaskHoursDAO taskHoursDAO = new TaskHoursDAO(connection);
            UserDAO userDAO = new UserDAO(connection);

            ManagedTask managedTask = taskDAO.getManagedTaskByTaskAndManagerAndProjectStatus(taskId, loggedInUser.getId(), ProjectStatus.created);
            if (managedTask == null) {
                sendTextError(response, HttpServletResponse.SC_NOT_FOUND, "Task not found");
                return;
            }

            List<String> errors = new ArrayList<>();
            List<ExpectedHoursMonth> expectedHours = parseExpectedHours(request, managedTask.getTask(), errors);
            List<Integer> collaboratorIds = parseCollaboratorIds(request, loggedInUser.getId(), userDAO, errors);
            if (!errors.isEmpty()) {
                sendTextError(response, HttpServletResponse.SC_BAD_REQUEST, String.join("\n", errors));
                return;
            }

            saveTaskAssignment(connection, taskDAO, taskHoursDAO, taskId, expectedHours, collaboratorIds);

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("Task assignment saved");
        } catch (SQLException e) {
            sendTextError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database failure occurred while saving task assignment");
        }
    }

    private List<ExpectedHoursMonth> parseExpectedHours(HttpServletRequest request, Task task, List<String> errors) {
        List<ExpectedHoursMonth> expectedHours = new ArrayList<>();
        for (int month = task.getStartMonth(); month <= task.getEndMonth(); month++) {
            String value = request.getParameter("expectedHours_" + month);

            if (value == null || value.trim().isEmpty()) {
                errors.add("Expected hours for month M" + month + " are required");
                continue;
            }

            try {
                int hours = Integer.parseInt(value.trim());
                if (hours < 0) {
                    errors.add("Expected hours for month M" + month + " cannot be negative");
                } else {
                    expectedHours.add(new ExpectedHoursMonth(month, hours));
                }
            } catch (NumberFormatException e) {
                errors.add("Expected hours for month M" + month + " must be an integer");
            }
        }
        return expectedHours;
    }

    private List<Integer> parseCollaboratorIds(HttpServletRequest request, int managerId, UserDAO userDAO, List<String> errors) throws SQLException {
        String[] collaboratorParams = request.getParameterValues("collaboratorIds");
        if (collaboratorParams == null || collaboratorParams.length == 0) {
            errors.add("At least one collaborator must be selected");
            return List.of();
        }

        Set<Integer> collaboratorIds = new LinkedHashSet<>();
        for (String collaboratorParam : collaboratorParams) {
            try {
                int collaboratorId = Integer.parseInt(collaboratorParam);
                if (collaboratorId == managerId) {
                    errors.add("Manager cannot assign themselves as collaborator");
                } else if (userDAO.getUserByIdAndRole(collaboratorId, UserRole.technical) == null) {
                    errors.add("Selected collaborator " + collaboratorId + " is not a technical user");
                } else {
                    collaboratorIds.add(collaboratorId);
                }
            } catch (NumberFormatException e) {
                errors.add("Collaborator id must be an integer");
            }
        }

        if (collaboratorIds.isEmpty() && errors.isEmpty()) {
            errors.add("At least one collaborator must be selected");
        }
        return new ArrayList<>(collaboratorIds);
    }

    private void saveTaskAssignment(Connection connection, TaskDAO taskDAO, TaskHoursDAO taskHoursDAO, int taskId,
                                    List<ExpectedHoursMonth> expectedHours, List<Integer> collaboratorIds) throws SQLException {
        boolean previousAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);

        try {
            for (ExpectedHoursMonth expectedHour : expectedHours) {
                taskHoursDAO.saveExpectedHours(taskId, expectedHour.getMonth(), expectedHour.getExpectedHours());
            }

            taskDAO.deleteWorkersByTask(taskId);
            for (Integer collaboratorId : collaboratorIds) {
                taskDAO.addWorkerToTask(taskId, collaboratorId);
            }

            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(previousAutoCommit);
        }
    }

    private int parseRequiredInt(HttpServletRequest request, String parameterName) {
        String value = request.getParameter(parameterName);
        if (value == null || value.trim().isEmpty()) {
            throw new NumberFormatException();
        }
        return Integer.parseInt(value.trim());
    }

    private void sendTextError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write(message);
    }
}
