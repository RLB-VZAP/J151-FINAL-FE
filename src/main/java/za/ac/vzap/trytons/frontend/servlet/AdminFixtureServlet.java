package za.ac.vzap.trytons.frontend.servlet;

import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import za.ac.vzap.trytons.frontend.client.AdminFixtureRestClient;

@WebServlet(name= "" , urlPatterns = "")
public class AdminFixtureServlet extends HttpServlet {
    @Inject
    private AdminFixtureRestClient adminFixtureRestClient;
}
