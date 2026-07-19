package za.ac.vzap.trytons.frontend.servlet.auth;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.auth.AuthRestClient;
import za.ac.vzap.trytons.frontend.client.auth.LoginRequest;
import za.ac.vzap.trytons.frontend.client.auth.LoginResponse;
import za.ac.vzap.trytons.frontend.client.auth.RegisteredUserRequest;
import za.ac.vzap.trytons.frontend.client.auth.RegisteredUserResponse;

import java.io.IOException;
import java.util.Optional;
import za.ac.vzap.trytons.frontend.servlet.shared.AbstractServlet;


@WebServlet (name = "AuthServlet" , urlPatterns = {"/login", "/register", "/logout"})
public class AuthServlet extends AbstractServlet {

    @Inject
    private AuthRestClient authRestClient;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String submit = request.getParameter("submit");
        if (submit == null) {
            submit = "";
        }
        String destination = switch (submit){
            case "login" -> {
                String identifier = request.getParameter("identifier");
                String password = request.getParameter("password");
                LoginRequest loginRequest = new LoginRequest(identifier, password);
                Optional<LoginResponse> loginResponse = authRestClient.login(loginRequest);
                if (loginResponse.isPresent()) {
                    establishAuthenticatedSession(request,loginResponse.get());
                    yield "/pages/register.jsp";
                }else {
                    request.setAttribute("error", "Invalid login credentials");
                    yield "/pages/login.jsp";
                }

            }
            case "register" ->{
                String email       = request.getParameter("email");
                String username    = request.getParameter("username");
                String rawPassword = request.getParameter("rawPassword");
                RegisteredUserRequest registerRequest = new RegisteredUserRequest(email, username, rawPassword);
                Optional<RegisteredUserResponse> registerResponse = authRestClient.register(registerRequest);
                if (registerResponse.isPresent()) {
                    yield "/pages/login.jsp";
                } else {
                    request.setAttribute("error", "Registration failed");
                    yield "/pages/register.jsp";
                }
            }
            case "logout" -> {
                authRestClient.logout();
                clearAuthSession(request);
                yield "/pages/login.jsp";
                }

            default ->"index.jsp";
        };
        request.getRequestDispatcher(destination).forward(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Auth Servlet, handles login request and register request and logout request";
    }

}

