package it.polimi.tiw.controllers.admin;

import com.google.gson.Gson;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.beans.UserRole;
import it.polimi.tiw.dao.UserDAO;
import it.polimi.tiw.utils.DatabaseConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/admin/get-technical-users")
public class GetTechnicalUsers extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try (Connection connection = DatabaseConnection.getInstance(getServletContext()).getConnection()) {
            UserDAO userDAO = new UserDAO(connection);
            List<User> users = userDAO.getUsersByRole(UserRole.technical.name());

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(new Gson().toJson(users));
        } catch (SQLException e) {
            sendTextError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database failure occurred while retrieving technical users");
        }
    }

    private void sendTextError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write(message);
    }
}
