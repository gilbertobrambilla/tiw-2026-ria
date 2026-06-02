package it.polimi.tiw.controllers.admin;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import it.polimi.tiw.beans.UserLogin;
import it.polimi.tiw.beans.UserRole;
import it.polimi.tiw.beans.admin.request.ProjectStructureBlueprint;
import it.polimi.tiw.beans.admin.request.TaskBlueprint;
import it.polimi.tiw.beans.admin.request.WPBlueprint;
import it.polimi.tiw.dao.ProjectDAO;
import it.polimi.tiw.dao.TaskDAO;
import it.polimi.tiw.dao.UserDAO;
import it.polimi.tiw.dao.WPDAO;
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
import java.util.ArrayList;
import java.util.List;

@WebServlet("/admin/create-project-structure")
public class CreateProjectStructure extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ProjectStructureBlueprint projectStructure;
        try {
            projectStructure = new Gson().fromJson(request.getReader(), ProjectStructureBlueprint.class);
        } catch (JsonSyntaxException e) {
            sendTextError(response, HttpServletResponse.SC_BAD_REQUEST, "Malformed JSON request");
            return;
        }

        List<String> errors = validateProjectStructure(projectStructure);
        if (!errors.isEmpty()) {
            sendTextError(response, HttpServletResponse.SC_BAD_REQUEST, String.join("\n", errors));
            return;
        }

        HttpSession session = request.getSession();
        UserLogin loggedInUser = (UserLogin) session.getAttribute("user");

        try (Connection connection = DatabaseConnection.getInstance(getServletContext()).getConnection()) {
            UserDAO userDAO = new UserDAO(connection);
            if (userDAO.getUserByIdAndRole(projectStructure.getManagedBy(), UserRole.technical) == null) {
                sendTextError(response, HttpServletResponse.SC_BAD_REQUEST, "Selected manager must be a technical user");
                return;
            }

            int projectId = createProjectStructure(connection, projectStructure, loggedInUser.getId());

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().print(projectId);
        } catch (SQLException e) {
            sendTextError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database failure occurred during project structure creation");
        }
    }

    private int createProjectStructure(Connection connection, ProjectStructureBlueprint projectStructure, int createdBy) throws SQLException {
        boolean previousAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);

        try {
            ProjectDAO projectDAO = new ProjectDAO(connection);
            WPDAO wpDAO = new WPDAO(connection);
            TaskDAO taskDAO = new TaskDAO(connection);

            int projectId = projectDAO.createProject(projectStructure.getTitle().trim(), projectStructure.getDurationMonths(), createdBy, projectStructure.getManagedBy());
            for (WPBlueprint workPackage : projectStructure.getWorkPackages()) {
                int wpId = wpDAO.createWP(projectId, workPackage.getTitle().trim(), workPackage.getStartMonth(), workPackage.getEndMonth());
                for (TaskBlueprint task : workPackage.getTasks()) {
                    taskDAO.createTask(wpId, task.getTitle().trim(), task.getDescription().trim(), task.getStartMonth(), task.getEndMonth());
                }
            }

            connection.commit();
            return projectId;
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(previousAutoCommit);
        }
    }

    private List<String> validateProjectStructure(ProjectStructureBlueprint projectStructure) {
        List<String> errors = new ArrayList<>();
        if (projectStructure == null) {
            errors.add("Project structure cannot be empty");
            return errors;
        }

        validateProjectData(projectStructure, errors);
        if (projectStructure.getWorkPackages() == null || projectStructure.getWorkPackages().isEmpty()) {
            errors.add("Project must contain at least one WP");
            return errors;
        }

        for (int i = 0; i < projectStructure.getWorkPackages().size(); i++) {
            validateWorkPackage(projectStructure, projectStructure.getWorkPackages().get(i), i + 1, errors);
        }
        return errors;
    }

    private void validateProjectData(ProjectStructureBlueprint projectStructure, List<String> errors) {
        if (isBlank(projectStructure.getTitle())) {
            errors.add("Project title cannot be empty");
        } else if (projectStructure.getTitle().length() > 100) {
            errors.add("Project title cannot exceed 100 characters");
        }

        if (projectStructure.getDurationMonths() <= 0) {
            errors.add("Project duration must be greater than zero");
        }

        if (projectStructure.getManagedBy() <= 0) {
            errors.add("Technical manager must be selected");
        }
    }

    private void validateWorkPackage(ProjectStructureBlueprint projectStructure, WPBlueprint workPackage, int position, List<String> errors) {
        if (workPackage == null) {
            errors.add("WP " + position + " cannot be empty");
            return;
        }

        if (isBlank(workPackage.getTitle())) {
            errors.add("WP " + position + " title cannot be empty");
        } else if (workPackage.getTitle().length() > 100) {
            errors.add("WP " + position + " title cannot exceed 100 characters");
        }

        if (workPackage.getStartMonth() <= 0 || workPackage.getEndMonth() <= 0) {
            errors.add("WP " + position + " months must be positive");
        } else if (workPackage.getStartMonth() > workPackage.getEndMonth()) {
            errors.add("WP " + position + " end month cannot precede start month");
        } else if (workPackage.getEndMonth() > projectStructure.getDurationMonths()) {
            errors.add("WP " + position + " duration cannot exceed project duration");
        }

        if (workPackage.getTasks() == null || workPackage.getTasks().isEmpty()) {
            errors.add("WP " + position + " must contain at least one task");
            return;
        }

        for (int i = 0; i < workPackage.getTasks().size(); i++) {
            validateTask(workPackage, workPackage.getTasks().get(i), position, i + 1, errors);
        }
    }

    private void validateTask(WPBlueprint workPackage, TaskBlueprint task, int wpPosition, int taskPosition, List<String> errors) {
        String taskLabel = "Task " + wpPosition + "." + taskPosition;
        if (task == null) {
            errors.add(taskLabel + " cannot be empty");
            return;
        }

        if (isBlank(task.getTitle())) {
            errors.add(taskLabel + " title cannot be empty");
        } else if (task.getTitle().length() > 100) {
            errors.add(taskLabel + " title cannot exceed 100 characters");
        }

        if (isBlank(task.getDescription())) {
            errors.add(taskLabel + " description cannot be empty");
        }

        if (task.getStartMonth() <= 0 || task.getEndMonth() <= 0) {
            errors.add(taskLabel + " months must be positive");
        } else if (task.getStartMonth() > task.getEndMonth()) {
            errors.add(taskLabel + " end month cannot precede start month");
        } else if (task.getStartMonth() < workPackage.getStartMonth() || task.getEndMonth() > workPackage.getEndMonth()) {
            errors.add(taskLabel + " duration must be contained in its WP duration");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void sendTextError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write(message);
    }
}
