package it.polimi.tiw.controllers.technical.collaborator;

import it.polimi.tiw.beans.UserLogin;
import it.polimi.tiw.beans.Task;
import it.polimi.tiw.dao.CollaboratorDAO;
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

@WebServlet("/technical/collaborator/update-worked-hours")
public class UpdateCollaboratorWorkedHours extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            sendTextError(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
            return;
        }

        UserLogin loggedInUser = (UserLogin) session.getAttribute("user");
        String taskIdParam = request.getParameter("taskId");
        String monthParam = request.getParameter("month");
        String workedHoursParam = request.getParameter("workedHours");

        if (taskIdParam == null || taskIdParam.trim().isEmpty() ||
                monthParam == null || monthParam.trim().isEmpty() ||
                workedHoursParam == null || workedHoursParam.trim().isEmpty()) {
            sendTextError(response, HttpServletResponse.SC_BAD_REQUEST, "Missing worked-hours update parameters");
            return;
        }

        int taskId;
        int month;
        int workedHours;

        try {
            taskId = Integer.parseInt(taskIdParam.trim());
            month = Integer.parseInt(monthParam.trim());
            workedHours = Integer.parseInt(workedHoursParam.trim());
        } catch (NumberFormatException e) {
            sendTextError(response, HttpServletResponse.SC_BAD_REQUEST, "Worked-hours update parameters must be integers");
            return;
        }

        if (workedHours < 0) {
            sendTextError(response, HttpServletResponse.SC_BAD_REQUEST, "Worked hours must be non-negative");
            return;
        }

        try (Connection connection = DatabaseConnection.getInstance(getServletContext()).getConnection()) {
            CollaboratorDAO collaboratorDAO = new CollaboratorDAO(connection);
            Task assignedTask = collaboratorDAO.getAssignedTaskForWorkedHoursUpdate(taskId, loggedInUser.getId());

            if (assignedTask == null) {
                sendTextError(response, HttpServletResponse.SC_FORBIDDEN, "Task is not assigned to the authenticated collaborator");
                return;
            }

            if (month < assignedTask.getStartMonth() || month > assignedTask.getEndMonth()) {
                sendTextError(response, HttpServletResponse.SC_BAD_REQUEST, "Month must be within task boundaries");
                return;
            }

            collaboratorDAO.saveCollaboratorWorkedHours(taskId, loggedInUser.getId(), month, workedHours);
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("Worked hours updated");
        } catch (SQLException e) {
            sendTextError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database failure occurred while updating worked hours");
        }
    }

    private void sendTextError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write(message);
    }
}
