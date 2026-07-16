package za.ac.vzap.trytons.frontend.servlet;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import za.ac.vzap.trytons.frontend.util.SessionAuthContext;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AbstractServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(AbstractServlet.class.getName());
    @Inject
    protected SessionAuthContext authContext;

    //protected route guard
    protected boolean requireAuthenticated(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if(!authContext.isAuthenticated()) {
            LOG.warning("Unauthenticated access attempt to " + req.getRequestURI());
            resp.sendRedirect(req.getContextPath() + "/login");
            return false;
        }
        return true;
    }

    //Admin guard
    protected boolean requireAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if(!authContext.isAdmin()) {
            LOG.warning("Non-admin access attempt to " + req.getRequestURI());
            resp.sendRedirect(req.getContextPath() + "/login");
            return false;
        }
        return true;
    }

    //UUID parser we all stole for Jaunte
    protected Optional<UUID> parseUuid(String value) {
        if(value == null || value.isBlank()){
            return Optional.empty();
        }
        try{
            return Optional.of(UUID.fromString(value.trim()));
        }catch(IllegalArgumentException e){
            LOG.log(Level.WARNING, "Invalid UUID format: {0} " + value);
            return Optional.empty();
        }
    }
    //I think this was the biggest bug, this refreshes session attributes.
    // for login and token refresh
    protected void syncSessionAttributes(HttpServletRequest req){
        HttpSession session = req.getSession(false);
        if(session == null || !authContext.isAuthenticated()) {
            return;
        }
        try{
            session.setAttribute("userId", authContext.getUserId());
            session.setAttribute("username", authContext.getUsername());
            session.setAttribute("email", authContext.getEmail());
            session.setAttribute("role", authContext.getRole());
            session.setAttribute("authToken", authContext.getToken());
            session.setAttribute("isAdmin", authContext.isAdmin());
        }catch(Exception e){
            LOG.log(Level.WARNING, "Failed to sync session attributes ",e);
        }
    }

    //used for logout
    protected void clearAuthSession(HttpServletRequest req){
        authContext.clear();
        HttpSession session = req.getSession(false);
        if(session != null){
            session.removeAttribute("userId");
            session.removeAttribute("username");
            session.removeAttribute("email");
            session.removeAttribute("role");
            session.removeAttribute("authToken");
            session.removeAttribute("isAdmin");
            session.invalidate();
        }
    }

    //A method recommended to add - common JSP forward with message
    protected void forwardWithError(HttpServletRequest req, HttpServletResponse resp, String errorMessage, String jspPath) throws ServletException,IOException {
        req.setAttribute("error", errorMessage);
        req.getRequestDispatcher(jspPath).forward(req, resp);
    }

    //Another helper recommended for success
    protected void forwardWithMessage(HttpServletRequest req, HttpServletResponse resp, String message, String jspPath) throws ServletException,IOException {
        req.setAttribute("message", message);
        req.getRequestDispatcher(jspPath).forward(req, resp);
    }

}
