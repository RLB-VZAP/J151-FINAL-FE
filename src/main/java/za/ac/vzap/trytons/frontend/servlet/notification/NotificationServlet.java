package za.ac.vzap.trytons.frontend.servlet.notification;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.notification.NotificationRestClient;
import za.ac.vzap.trytons.frontend.servlet.shared.AbstractServlet;

import java.io.IOException;

@WebServlet(name = "NotificationServlet", urlPatterns = {"/notifications"})
public class NotificationServlet extends AbstractServlet {

    private static final String VIEW = "/pages/notifications.jsp";

    @Inject
    private NotificationRestClient notificationRestClient;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO: Require an authenticated user, load the caller's notifications via NotificationRestClient, and forward to the notifications view.
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO: Require an authenticated user, read the submitted mark-as-read or mark-all-as-read action, call NotificationRestClient, and forward back to the notifications view.
    }
}
