package za.ac.vzap.trytons.frontend.servlet;

import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import za.ac.vzap.trytons.frontend.client.AdminFixtureRestClient;

@WebServlet(name= "" , urlPatterns = "")
public class AdminFixtureServlet extends AbstractServlet {
    @Inject
    private AdminFixtureRestClient adminFixtureRestClient;
}
