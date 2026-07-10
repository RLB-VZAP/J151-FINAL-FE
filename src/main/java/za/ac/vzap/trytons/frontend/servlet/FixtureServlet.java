package za.ac.vzap.trytons.frontend.servlet;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.FixtureResponse;
import za.ac.vzap.trytons.frontend.client.FixtureRestClient;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@WebServlet(name ="FixtureServlet", urlPatterns = {"/fixtures", "/fixture"} )
public class FixtureServlet extends HttpServlet {
    @Inject
    private FixtureRestClient fixtureRestClient;
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String submit = request.getParameter("submit");
        if (submit == null) {
            submit = "";
    }
        String destination = switch (submit){
        case "fixtures"->{
            String statusFilter = request.getParameter("status");
            Optional<List<FixtureResponse>> fixtures = fixtureRestClient.listFixtures(statusFilter);
            if (fixtures.isPresent()) {
                request.setAttribute("fixtures", fixtures.get());
            } else {
                request.setAttribute("error", "Unable to load players");
                request.setAttribute("fixtures", List.of());
            }
            request.setAttribute("statusFilter", statusFilter);
            yield "/pages/fixtures.jsp";
        }
        case "fixture"->{
            String fixtureId = request.getParameter("fixtureId");
            if (fixtureId == null || fixtureId.isBlank()) {
                request.setAttribute("error", "Invalid or missing fixtureId");
                yield "/pages/fixtures.jsp";
            }
            Optional<FixtureResponse> fixture = fixtureRestClient.getFixture(fixtureId);
            if (fixture.isPresent()) {
                request.setAttribute("fixture", fixture.get());
                yield "/pages/fixture-details.jsp";
            }
            request.setAttribute("error", "Fixture not found");
            yield "/pages/fixtures.jsp";
        }
            default -> "/index.jsp";
        };
        request.getRequestDispatcher(destination).forward(request, response);
    }
}
