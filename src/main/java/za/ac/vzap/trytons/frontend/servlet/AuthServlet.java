package za.ac.vzap.trytons.frontend.servlet;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.AuthRestClient;

import java.io.IOException;
import java.util.logging.Logger;

@WebServlet (name = "AuthServlet" , urlPatterns = {"/login", "/register", "/logout"})
public class AuthServlet extends HttpServlet {
    private final Logger LOGGER = Logger.getLogger(AuthServlet.class.getName());

    @Inject
    private AuthRestClient authRestClient;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if("/login".equals(request.getServletPath())) {
            login(request,response);
        }else if("/register".equals(request.getServletPath())) {
            register(request,response);
        }else if("/logout".equals(request.getServletPath())) {
            logout(request,response);
        }
    }

    private void login (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    private void register (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    private void logout(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}

