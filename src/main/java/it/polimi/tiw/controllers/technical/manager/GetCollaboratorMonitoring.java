package it.polimi.tiw.controllers.technical.manager;

import com.google.gson.Gson;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.beans.UserLogin;
import it.polimi.tiw.beans.manager.ManagerCollaboratorReportResponse;
import it.polimi.tiw.dao.ManagerDAO;
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

/**
 * A collaborator hours report for projects managed by the authenticated manager is returned.
 */
@WebServlet("/technical/manager/get-collaborator-monitoring")
public class GetCollaboratorMonitoring extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        UserLogin loggedInUser = (UserLogin) session.getAttribute("user");
        String collaboratorIdParam = request.getParameter("collaboratorId");

        int collaboratorId;
        try {
            collaboratorId = Integer.parseInt(collaboratorIdParam);
        } catch (NumberFormatException e) {
            sendTextError(response, HttpServletResponse.SC_BAD_REQUEST, "Collaborator id must be an integer");
            return;
        }

        try (Connection connection = DatabaseConnection.getInstance(getServletContext()).getConnection()) {
            UserDAO userDAO = new UserDAO(connection);
            ManagerDAO managerDAO = new ManagerDAO(connection);

            User collaborator = userDAO.getCollaboratorByIdAndManager(collaboratorId, loggedInUser.getId());
            if (collaborator == null) {
                sendTextError(response, HttpServletResponse.SC_NOT_FOUND, "Collaborator not found");
                return;
            }

            ManagerCollaboratorReportResponse report = managerDAO.getCollaboratorReport(collaborator, loggedInUser.getId());

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(new Gson().toJson(report));
        } catch (SQLException e) {
            sendTextError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database failure occurred while retrieving collaborator report");
        }
    }

    private void sendTextError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write(message);
    }
}
