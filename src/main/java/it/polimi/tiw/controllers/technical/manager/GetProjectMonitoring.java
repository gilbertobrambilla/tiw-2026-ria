package it.polimi.tiw.controllers.technical.manager;

import com.google.gson.Gson;
import it.polimi.tiw.beans.Project;
import it.polimi.tiw.beans.ProjectStatus;
import it.polimi.tiw.beans.UserLogin;
import it.polimi.tiw.beans.manager.ManagerProjectMonitoringResponse;
import it.polimi.tiw.dao.ManagerDAO;
import it.polimi.tiw.dao.ProjectDAO;
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
 * A monthly report for a monitorable managed project is returned.
 */
@WebServlet("/technical/manager/get-project-monitoring")
public class GetProjectMonitoring extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        UserLogin loggedInUser = (UserLogin) session.getAttribute("user");
        String projectIdParam = request.getParameter("projectId");

        int projectId;
        try {
            projectId = Integer.parseInt(projectIdParam);
        } catch (NumberFormatException e) {
            sendTextError(response, HttpServletResponse.SC_BAD_REQUEST, "Project id must be an integer");
            return;
        }

        try (Connection connection = DatabaseConnection.getInstance(getServletContext()).getConnection()) {
            ProjectDAO projectDAO = new ProjectDAO(connection);
            ManagerDAO managerDAO = new ManagerDAO(connection);

            Project project = projectDAO.getProjectByIdAndManager(projectId, loggedInUser.getId());
            boolean monitorable = project != null &&
                    (project.getStatus() == ProjectStatus.assigned || project.getStatus() == ProjectStatus.completed);

            if (!monitorable) {
                sendTextError(response, HttpServletResponse.SC_NOT_FOUND, "Project not found");
                return;
            }

            boolean closable = project.getStatus() == ProjectStatus.assigned && projectDAO.isProjectClosable(projectId);
            ManagerProjectMonitoringResponse report = managerDAO.getProjectMonitoring(project, closable);

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(new Gson().toJson(report));
        } catch (SQLException e) {
            sendTextError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database failure occurred while retrieving project report");
        }
    }

    private void sendTextError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write(message);
    }
}
