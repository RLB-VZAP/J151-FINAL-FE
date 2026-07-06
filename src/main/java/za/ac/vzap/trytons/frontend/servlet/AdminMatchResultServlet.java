package za.ac.vzap.trytons.frontend.servlet;

import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import za.ac.vzap.trytons.frontend.client.AdminMatchResultRestClient;

@WebServlet(name= "" , urlPatterns = "")
public class AdminMatchResultServlet extends HttpServlet {
    @Inject
    private AdminMatchResultRestClient adminMatchResultRestClient;
}
