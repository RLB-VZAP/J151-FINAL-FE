package za.ac.vzap.trytons.frontend.servlet.fixture;

import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import za.ac.vzap.trytons.frontend.client.fixture.AdminFixtureRestClient;
import za.ac.vzap.trytons.frontend.servlet.shared.AbstractServlet;

@WebServlet(name= "" , urlPatterns = "")
public class AdminFixtureServlet extends AbstractServlet {
    @Inject
    private AdminFixtureRestClient adminFixtureRestClient;
}
