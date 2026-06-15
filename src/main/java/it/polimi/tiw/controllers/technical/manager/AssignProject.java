package it.polimi.tiw.controllers.technical.manager;

import it.polimi.tiw.beans.Project;
import it.polimi.tiw.beans.ProjectStatus;
import it.polimi.tiw.beans.UserLogin;
import it.polimi.tiw.dao.ProjectDAO;
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

@WebServlet("/technical/manager/assign-project")
@MultipartConfig
public class AssignProject extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        UserLogin loggedInUser = (UserLogin) session.getAttribute("user");

        int projectId;
        try {
            projectId = parseRequiredInt(request, "projectId");
        } catch (NumberFormatException e) {
            sendTextError(response, HttpServletResponse.SC_BAD_REQUEST, "Project id must be an integer");
            return;
        }

        try (Connection connection = DatabaseConnection.getInstance(getServletContext()).getConnection()) {
            ProjectDAO projectDAO = new ProjectDAO(connection);
            Project project = projectDAO.getProjectByIdAndManager(projectId, loggedInUser.getId());
            if (project == null) {
                sendTextError(response, HttpServletResponse.SC_NOT_FOUND, "Project not found");
                return;
            }

            if (project.getStatus() != ProjectStatus.created) {
                sendTextError(response, HttpServletResponse.SC_BAD_REQUEST, "Project must be in created status");
                return;
            }

            if (!projectDAO.isProjectAssignable(projectId)) {
                sendTextError(response, HttpServletResponse.SC_BAD_REQUEST, "Project has tasks without collaborators or expected hours");
                return;
            }

            projectDAO.updateProjectStatus(projectId, ProjectStatus.assigned);

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("Project assigned");
        } catch (SQLException e) {
            sendTextError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database failure occurred while assigning project");
        }
    }

    private int parseRequiredInt(HttpServletRequest request, String parameterName) {
        String value = request.getParameter(parameterName);
        if (value == null || value.trim().isEmpty()) {
            throw new NumberFormatException();
        }
        return Integer.parseInt(value);
    }

    private void sendTextError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write(message);
    }
}
