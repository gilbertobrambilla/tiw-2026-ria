package it.polimi.tiw.controllers.admin;

import com.google.gson.Gson;
import it.polimi.tiw.beans.Project;
import it.polimi.tiw.beans.UserLogin;
import it.polimi.tiw.beans.admin.AdminProjectStructureResponse;
import it.polimi.tiw.dao.AdminDAO;
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

@WebServlet("/admin/get-project-structure")
public class GetProjectStructure extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        UserLogin loggedInUser = (UserLogin) session.getAttribute("user");
        String projectIdParam = request.getParameter("projectId");

        int projectId;

        try {
            projectId = Integer.parseInt(projectIdParam);
        } catch (NumberFormatException e) {
            sendTextError(response, HttpServletResponse.SC_BAD_REQUEST, "Project id must be a integer");
            return;
        }

        try (Connection connection = DatabaseConnection.getInstance(getServletContext()).getConnection()) {
            ProjectDAO projectDAO = new ProjectDAO(connection);
            AdminDAO adminDAO = new AdminDAO(connection);

            Project project = projectDAO.getProjectByIdAndCreator(projectId, loggedInUser.getId());
            if (project == null) {
                sendTextError(response, HttpServletResponse.SC_NOT_FOUND, "Project not found");
                return;
            }

            AdminProjectStructureResponse report = adminDAO.getProjectStructure(project);

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(new Gson().toJson(report));
        } catch (SQLException e) {
            sendTextError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database failure occurred while retrieving project structure");
        }
    }

    private void sendTextError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write(message);
    }

}
