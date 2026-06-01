package it.polimi.tiw.controllers.auth;

import com.google.gson.Gson;
import it.polimi.tiw.beans.UserLogin;
import it.polimi.tiw.dao.UserDAO;
import it.polimi.tiw.utils.DatabaseConnection;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet("/login")
public class Login extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        ServletContext context = getServletContext();

        // validation of input parameters is performed
        if (username == null || username.trim().isEmpty()) {
            sendTextError(response, HttpServletResponse.SC_BAD_REQUEST, "Username cannot be empty");
            return;
        }
        if (password == null || password.trim().isEmpty()) {
            sendTextError(response, HttpServletResponse.SC_BAD_REQUEST, "Password cannot be empty");
            return;
        }

        UserLogin user;

        // database connection and user lookup are executed
        try (Connection connection = DatabaseConnection.getInstance(context).getConnection()) {
            UserDAO userDAO = new UserDAO(connection);
            user = userDAO.getUserByCredentials(username, password);
        } catch (SQLException e) {
            sendTextError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database connection failure");
            return;
        }

        if (user == null) {
            sendTextError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid credentials");
            return;
        }

        // session is established; navigation is handled by the JavaScript client.
        HttpSession session = request.getSession();
        session.setAttribute("user", user);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        String json = new Gson().toJson(user);
        response.getWriter().write(json);
    }

    private void sendTextError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write(message);
    }
}
