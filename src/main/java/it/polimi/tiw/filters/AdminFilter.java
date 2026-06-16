package it.polimi.tiw.filters;

import it.polimi.tiw.beans.UserLogin;
import it.polimi.tiw.beans.UserRole;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

public class AdminFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // retrieve current session
        HttpSession session = req.getSession(false);

        // verify authentication status
        if (session == null || session.getAttribute("user") == null) {
            sendLoginLocation(req, res);
            return;
        }

        // verify administrative role
        UserLogin user = (UserLogin) session.getAttribute("user");
        UserRole role = user.getRole();
        if (!UserRole.admin.equals(role)) {
            sendLoginLocation(req, res);
            return;
        }

        // pass request along the chain
        chain.doFilter(request, response);
    }

    private void sendLoginLocation(HttpServletRequest req, HttpServletResponse res) {
        String loginPath = req.getServletContext().getContextPath() + "/login.html";
        res.setStatus(HttpServletResponse.SC_FORBIDDEN);
        res.setHeader("Location", loginPath);
    }
}
