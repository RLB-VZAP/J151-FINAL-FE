package za.ac.vzap.trytons.frontend.servlet.auth;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.auth.ProfileRestClient;
import za.ac.vzap.trytons.frontend.servlet.shared.AbstractServlet;

import java.io.IOException;

@WebServlet(name = "ProfileServlet", urlPatterns = {"/profile", "/profile/change-password"})
public class ProfileServlet extends AbstractServlet {

    private static final String PROFILE_VIEW = "/pages/profile.jsp";
    private static final String CHANGE_PASSWORD_VIEW = "/pages/change-password.jsp";

    @Inject
    private ProfileRestClient profileRestClient;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO: Require an authenticated user, load the current user's profile via ProfileRestClient, and forward to the profile or change-password view based on the requested servlet path.
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO: Require an authenticated user, read the submitted profile-update or password-change form data, call ProfileRestClient, and forward back to the relevant view with a success or error message.
    }
}
