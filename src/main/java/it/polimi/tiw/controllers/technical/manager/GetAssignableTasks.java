package it.polimi.tiw.controllers.technical.manager;

import com.google.gson.Gson;
import it.polimi.tiw.beans.ManagedTask;
import it.polimi.tiw.beans.ProjectStatus;
import it.polimi.tiw.beans.UserLogin;
import it.polimi.tiw.dao.TaskDAO;
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

@WebServlet("/technical/manager/get-assignable-tasks")
public class GetAssignableTasks extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        UserLogin loggedInUser = (UserLogin) session.getAttribute("user");

        try (Connection connection = DatabaseConnection.getInstance(getServletContext()).getConnection()) {
            TaskDAO taskDAO = new TaskDAO(connection);
            List<ManagedTask> tasks = taskDAO.getManagedTasksByManagerAndProjectStatus(loggedInUser.getId(), ProjectStatus.created);

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(new Gson().toJson(tasks));
        } catch (SQLException e) {
            sendTextError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database failure occurred while retrieving managed tasks");
        }
    }

    private void sendTextError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write(message);
    }
}
