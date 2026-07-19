package za.ac.vzap.trytons.frontend.servlet.fixture;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.fixture.FixtureResponse;
import za.ac.vzap.trytons.frontend.client.fixture.FixtureRestClient;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import za.ac.vzap.trytons.frontend.servlet.shared.AbstractServlet;

@WebServlet(name ="FixtureServlet", urlPatterns = {"/fixtures", "/fixture"} )
public class FixtureServlet extends AbstractServlet {
    @Inject
    private FixtureRestClient fixtureRestClient;
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String submit = request.getParameter("submit");
        if (submit == null) {
            submit = "";
        }
        switch (submit) {
            case "fixture" -> {
                String fixtureId = request.getParameter("fixtureId");
                if (fixtureId == null || fixtureId.isBlank()) {
                    forwardWithError(request, response, "Invalid or missing fixtureId", "/pages/fixtures.jsp");
                    return;
                }
                Optional<FixtureResponse> fixture = fixtureRestClient.getFixture(fixtureId);
                if (fixture.isPresent()) {
                    request.setAttribute("fixture", fixture.get());
                    request.getRequestDispatcher("/pages/fixture-details.jsp").forward(request, response);
                    return;
                }
                forwardWithError(request, response, "Fixture not found", "/pages/fixtures.jsp");
            }

            default -> {
                String statusFilter = request.getParameter("status");
                Optional<List<FixtureResponse>> fixtures = fixtureRestClient.listFixtures(statusFilter);
                request.setAttribute("statusFilter", statusFilter);
                if (fixtures.isPresent()) {
                    request.setAttribute("fixtures", fixtures.get());
                    request.getRequestDispatcher("/pages/fixtures.jsp").forward(request, response);
                } else {
                    request.setAttribute("fixtures", List.of());
                    forwardWithError(request, response, "Unable to load fixtures", "/pages/fixtures.jsp");
                }
            }
        }
    }
    }

