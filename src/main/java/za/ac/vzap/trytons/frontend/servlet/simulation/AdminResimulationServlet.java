package za.ac.vzap.trytons.frontend.servlet.simulation;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.fixture.FixtureRestClient;
import za.ac.vzap.trytons.frontend.client.fixture.FixtureResponse;
import za.ac.vzap.trytons.frontend.client.simulation.ResimulationRequest;
import za.ac.vzap.trytons.frontend.client.simulation.ResimulationResponse;
import za.ac.vzap.trytons.frontend.client.simulation.ResimulationRestClient;
import za.ac.vzap.trytons.frontend.servlet.shared.AbstractServlet;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@WebServlet(name = "AdminResimulationServlet", urlPatterns = {"/admin/resimulation"})
public class AdminResimulationServlet extends AbstractServlet {

    private static final String VIEW = "/pages/admin-resimulation.jsp";
    private static final String REDIRECT_PATH = "/admin/resimulation";

    @Inject
    private ResimulationRestClient resimulationRestClient;

    @Inject
    private FixtureRestClient fixtureRestClient;

    private enum Outcome { SUCCESS, FAILURE, HANDLED }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!requireAdmin(request, response)) {
            return;
        }
        loadPage(request, request.getParameter("fixtureId"));
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!requireAdmin(request, response)) {
            return;
        }

        String fixtureIdForReload = request.getParameter("fixtureId");
        Outcome outcome = resimulate(request, response);

        if (outcome == Outcome.HANDLED) {
            return;
        }

        if (outcome == Outcome.SUCCESS) {
            flashSuccess(request, "Resimulation triggered");
            response.sendRedirect(buildRedirectUrl(request, fixtureIdForReload));
            return;
        }

        loadPage(request, fixtureIdForReload);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private String buildRedirectUrl(HttpServletRequest request, String fixtureIdForReload) {
        String redirect = request.getContextPath() + REDIRECT_PATH;
        if (fixtureIdForReload != null && !fixtureIdForReload.isBlank()) {
            redirect += "?fixtureId=" + URLEncoder.encode(fixtureIdForReload, StandardCharsets.UTF_8);
        }
        return redirect;
    }

    private Outcome resimulate(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Optional<UUID> fixtureId = parseUuid(request.getParameter("fixtureId"));
        String reason = request.getParameter("resimulationReason");

        if (fixtureId.isEmpty()) {
            request.setAttribute("error", "A valid fixture is required to trigger a resimulation");
            return Outcome.FAILURE;
        }

        ResimulationRequest resimulationRequest = new ResimulationRequest();
        resimulationRequest.setFixtureId(fixtureId.get());
        resimulationRequest.setResimulationReason(reason);

        Optional<ResimulationResponse> result = resimulationRestClient.resimulateFixture(resimulationRequest);
        if (result.isPresent()) {
            return Outcome.SUCCESS;
        }
        return handleApiFailure(request, response, "Resimulation could not be triggered") ? Outcome.HANDLED : Outcome.FAILURE;
    }

    private void loadPage(HttpServletRequest request, String fixtureIdParam) {
        Optional<List<FixtureResponse>> fixtures = fixtureRestClient.listFixtures(null);
        request.setAttribute("fixtures", fixtures.orElse(List.of()));

        request.setAttribute("selectedFixtureId", fixtureIdParam);
        if (fixtureIdParam == null || fixtureIdParam.isBlank()) {
            return;
        }

        Optional<UUID> fixtureId = parseUuid(fixtureIdParam);
        if (fixtureId.isEmpty()) {
            return;
        }

        Optional<List<ResimulationResponse>> resimulations =
                resimulationRestClient.listResimulationsForFixture(fixtureId.get());
        request.setAttribute("resimulations", resimulations.orElse(List.of()));
    }

    @Override
    public String getServletInfo() {
        return "Admin Resimulation Servlet, handles controlled resimulation triggers and history";
    }
}
