package za.ac.vzap.trytons.frontend.servlet.fixture;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.fixture.AdminFixtureRestClient;
import za.ac.vzap.trytons.frontend.client.fixture.FixtureResponse;
import za.ac.vzap.trytons.frontend.client.fixture.FixtureRestClient;
import za.ac.vzap.trytons.frontend.servlet.shared.AbstractServlet;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@WebServlet(name = "AdminFixtureServlet", urlPatterns = {"/admin/fixtures"})
public class AdminFixtureServlet extends AbstractServlet {

    private static final String VIEW = "/pages/admin-fixtures.jsp";

    @Inject
    private AdminFixtureRestClient adminFixtureRestClient;

    // AdminFixtureRestClient exposes no list method (only createFixture/updateFixtureStatus), so the
    // read-side listing for this admin table borrows FixtureRestClient.listFixtures, the same client
    // AdminMatchResultServlet already injects alongside its admin-specific client for the same reason.
    @Inject
    private FixtureRestClient fixtureRestClient;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!requireAdmin(request, response)) {
            return;
        }
        loadPage(request, request.getParameter("status"));
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!requireAdmin(request, response)) {
            return;
        }
        updateFixtureStatus(request);
        // The "status" POST parameter here is the new status just applied to one fixture, not a list
        // filter — reload unfiltered so the admin can see the updated row in context.
        loadPage(request, null);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private void updateFixtureStatus(HttpServletRequest request) {
        String fixtureId = request.getParameter("fixtureId");
        String status = request.getParameter("status");

        if (fixtureId == null || fixtureId.isBlank() || status == null || status.isBlank()) {
            request.setAttribute("error", "A valid fixture and status are required to update a fixture");
            return;
        }

        Optional<FixtureResponse> updated = adminFixtureRestClient.updateFixtureStatus(fixtureId.trim(), status.trim());
        if (updated.isPresent()) {
            request.setAttribute("success", "Fixture status updated successfully");
        } else {
            request.setAttribute("error", "Fixture status could not be updated");
        }
    }

    private void loadPage(HttpServletRequest request, String statusFilter) {
        Optional<List<FixtureResponse>> fixtures = fixtureRestClient.listFixtures(statusFilter);
        if (fixtures.isPresent()) {
            request.setAttribute("fixtures", fixtures.get());
        } else {
            request.setAttribute("fixturesError", "Unable to load fixtures");
            request.setAttribute("fixtures", List.of());
        }
        request.setAttribute("statusFilter", statusFilter);
    }

    @Override
    public String getServletInfo() {
        return "Admin Fixture Servlet, handles fixture listing and fixture status updates";
    }
}
