package za.ac.vzap.trytons.frontend.servlet;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import za.ac.vzap.trytons.frontend.client.AuthRestClient;
import za.ac.vzap.trytons.frontend.client.LoginRequest;
import za.ac.vzap.trytons.frontend.client.LoginResponse;
import za.ac.vzap.trytons.frontend.client.RegisteredUserRequest;
import za.ac.vzap.trytons.frontend.client.RegisteredUserResponse;
import za.ac.vzap.trytons.frontend.session.SessionAuthContext;

import java.io.IOException;
import java.util.Optional;

@WebServlet(name = "AuthServlet", urlPatterns = {"/login", "/register", "/logout"})
public class AuthServlet extends HttpServlet {

    @Inject
    private AuthRestClient authRestClient;

    @Inject
    private SessionAuthContext authContext;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        switch (request.getServletPath()) {
            case "/register" -> request.getRequestDispatcher("/pages/register.jsp").forward(request, response);
            case "/logout" -> performLogout(request, response);
            default -> {
                if (authContext.isAuthenticated()) {
                    response.sendRedirect(request.getContextPath() + "/players?submit=players");
                } else {
                    request.getRequestDispatcher("/pages/login.jsp").forward(request, response);
                }
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("submit");
        if (action == null || action.isBlank()) {
            action = request.getServletPath().replace("/", "");
        }

        switch (action) {
            case "login" -> login(request, response);
            case "register" -> register(request, response);
            case "logout" -> performLogout(request, response);
            default -> response.sendRedirect(request.getContextPath() + "/login");
        }
    }

    private void login(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        LoginRequest loginRequest = new LoginRequest(
                request.getParameter("identifier"),
                request.getParameter("password")
        );

        Optional<LoginResponse> loginResponse = authRestClient.login(loginRequest);
        if (loginResponse.isEmpty() || loginResponse.get().getToken() == null) {
            request.setAttribute("error", "Invalid login credentials or the backend is unavailable.");
            request.getRequestDispatcher("/pages/login.jsp").forward(request, response);
            return;
        }

        LoginResponse loggedInUser = loginResponse.get();
        authContext.signIn(loggedInUser);

        HttpSession session = request.getSession(true);
        session.setAttribute("userId", loggedInUser.getUserId());
        session.setAttribute("username", loggedInUser.getUsername());
        session.setAttribute("email", loggedInUser.getEmail());
        session.setAttribute("role", loggedInUser.getRole());
        session.setAttribute("authToken", loggedInUser.getToken());

        response.sendRedirect(request.getContextPath() + "/players?submit=players");
    }

    private void register(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        RegisteredUserRequest registerRequest = new RegisteredUserRequest(
                request.getParameter("email"),
                request.getParameter("username"),
                request.getParameter("rawPassword")
        );

        Optional<RegisteredUserResponse> registerResponse = authRestClient.register(registerRequest);
        if (registerResponse.isPresent()) {
            request.setAttribute("message", "Registration successful. You can now log in.");
            request.getRequestDispatcher("/pages/login.jsp").forward(request, response);
            return;
        }

        request.setAttribute("error", "Registration failed. Check the details or try another email/username.");
        request.getRequestDispatcher("/pages/register.jsp").forward(request, response);
    }

    private void performLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        authContext.clear();

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.sendRedirect(request.getContextPath() + "/login");
    }

    @Override
    public String getServletInfo() {
        return "Handles frontend registration, login and logout.";
    }
}
