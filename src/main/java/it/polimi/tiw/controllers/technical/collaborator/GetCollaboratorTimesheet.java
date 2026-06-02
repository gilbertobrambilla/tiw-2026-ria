package it.polimi.tiw.controllers.technical.collaborator;

import com.google.gson.Gson;
import it.polimi.tiw.beans.UserLogin;
import it.polimi.tiw.beans.collaborator.CollaboratorHomeResponse;
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

@WebServlet("/technical/collaborator/get-timesheet")
public class GetCollaboratorTimesheet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            sendTextError(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
            return;
        }

        UserLogin loggedInUser = (UserLogin) session.getAttribute("user");

        try (Connection connection = DatabaseConnection.getInstance(getServletContext()).getConnection()) {
            CollaboratorDAO collaboratorDAO = new CollaboratorDAO(connection);
            CollaboratorHomeResponse timesheet = collaboratorDAO.getCollaboratorHomeData(loggedInUser.getId());

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(new Gson().toJson(timesheet));
        } catch (SQLException e) {
            sendTextError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database failure occurred while retrieving collaborator timesheet");
        }
    }

    private void sendTextError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write(message);
    }
}
