package it.polimi.tiw.controllers.technical.manager;

import com.google.gson.Gson;
import it.polimi.tiw.beans.ManagedTask;
import it.polimi.tiw.beans.ProjectStatus;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.beans.UserLogin;
import it.polimi.tiw.beans.manager.ManagerTaskAssignmentResponse;
import it.polimi.tiw.dao.TaskDAO;
import it.polimi.tiw.dao.TaskHoursDAO;
import it.polimi.tiw.dao.UserDAO;
import it.polimi.tiw.utils.DatabaseConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Assignment details for a created-project task managed by the authenticated manager are returned.
 */
@WebServlet("/technical/manager/get-task-assignment-details")
public class GetTaskAssignmentDetails extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        UserLogin loggedInUser = (UserLogin) session.getAttribute("user");
        String taskIdParam = request.getParameter("taskId");

        int taskId;
        try {
            taskId = Integer.parseInt(taskIdParam);
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

            List<User> availableCollaborators = userDAO.getTechnicalUsersExceptManager(loggedInUser.getId());
            Map<Integer, Integer> expectedHours = taskHoursDAO.getExpectedHoursByTask(taskId);
            List<Integer> collaboratorIds = userDAO.getCollaboratorIdsByTask(taskId);

            ManagerTaskAssignmentResponse details = new ManagerTaskAssignmentResponse(
                    managedTask,
                    availableCollaborators,
                    expectedHours,
                    collaboratorIds
            );

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(new Gson().toJson(details));
        } catch (SQLException e) {
            sendTextError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database failure occurred while retrieving task assignment details");
        }
    }

    private void sendTextError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write(message);
    }
}
