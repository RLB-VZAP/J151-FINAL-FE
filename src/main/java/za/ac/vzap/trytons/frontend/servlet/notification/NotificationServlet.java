package za.ac.vzap.trytons.frontend.servlet.notification;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.notification.NotificationRestClient;
import za.ac.vzap.trytons.frontend.client.notification.NotificationResponse;
import za.ac.vzap.trytons.frontend.servlet.shared.AbstractServlet;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@WebServlet(name = "NotificationServlet", urlPatterns = {"/notifications"})
public class NotificationServlet extends AbstractServlet {

    private static final String VIEW = "/pages/notifications.jsp";

    @Inject
    private NotificationRestClient notificationRestClient;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!requireAuthenticated(request, response)) {
            return;
        }

        boolean unreadOnly = Boolean.parseBoolean(request.getParameter("unreadOnly"));
        if (!loadNotifications(request, response, unreadOnly)) {
            return;
        }

        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!requireAuthenticated(request, response)) {
            return;
        }

        String action = request.getParameter("action");
        if (action == null) {
            action = "";
        }

        switch (action) {
            case "markAsRead" -> {
                if (!markAsRead(request, response)) {
                    return;
                }
            }
            case "markAllAsRead" -> {
                if (!markAllAsRead(request, response)) {
                    return;
                }
            }
            default -> request.setAttribute("error", "Unknown notification action requested");
        }

        if (request.getAttribute("success") != null) {
            response.sendRedirect(request.getContextPath() + "/notifications");
            return;
        }

        if (!loadNotifications(request, response, false)) {
            return;
        }

        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    // Loads the notification list into the "notifications" attribute. Returns false if a session
    // expiry redirect was already sent (caller must return immediately in that case).
    private boolean loadNotifications(HttpServletRequest request, HttpServletResponse response, boolean unreadOnly) throws IOException {
        Optional<List<NotificationResponse>> notifications = notificationRestClient.getNotifications(unreadOnly);
        if (notifications.isEmpty()) {
            if (sessionExpiredRedirect(request, response)) {
                return false;
            }
            if (request.getAttribute("error") == null) {
                request.setAttribute("error", "Unable to load notifications right now");
            }
            request.setAttribute("notifications", List.of());
        } else {
            request.setAttribute("notifications", notifications.get());
        }
        request.setAttribute("unreadOnly", unreadOnly);
        return true;
    }

    private boolean markAsRead(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Optional<UUID> notificationId = parseUuid(request.getParameter("notificationId"));
        if (notificationId.isEmpty()) {
            request.setAttribute("error", "A valid notification is required");
            return true;
        }

        Optional<NotificationResponse> updated = notificationRestClient.markAsRead(notificationId.get());
        if (updated.isEmpty()) {
            if (sessionExpiredRedirect(request, response)) {
                return false;
            }
            request.setAttribute("error", "Unable to mark notification as read");
        } else {
            request.setAttribute("success", "Notification marked as read");
        }
        return true;
    }

    private boolean markAllAsRead(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Optional<Integer> updatedCount = notificationRestClient.markAllAsRead();
        if (updatedCount.isEmpty()) {
            if (sessionExpiredRedirect(request, response)) {
                return false;
            }
            request.setAttribute("error", "Unable to mark all notifications as read");
        } else {
            request.setAttribute("success", updatedCount.get() + " notification(s) marked as read");
        }
        return true;
    }

    @Override
    public String getServletInfo() {
        return "Notification Servlet, handles listing notifications and marking them as read";
    }
}
