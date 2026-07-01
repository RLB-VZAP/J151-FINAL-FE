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

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

@WebServlet (name = "AuthServlet" , urlPatterns = {"/login", "/register", "/logout"})
public class AuthServlet extends HttpServlet {
    private final Logger LOGGER = Logger.getLogger(AuthServlet.class.getName());

    @Inject
    private AuthRestClient authRestClient;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String submit = request.getParameter("submit");
        String destination = switch (submit){
            case "login" -> {

                yield null;
            }
            case "register" ->{

                yield null;
            }
            case "logout" -> {
                yield null;
            }
            default ->"index.jsp";
        };
    }

}

