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
import za.ac.vzap.trytons.frontend.client.simulation.SimulationSettingRequest;
import za.ac.vzap.trytons.frontend.client.simulation.SimulationSettingResponse;
import za.ac.vzap.trytons.frontend.client.simulation.SimulationSettingRestClient;
import za.ac.vzap.trytons.frontend.servlet.shared.AbstractServlet;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Serves the two admin simulation screens. They were one page originally, which
 * left an admin scrolling past the whole settings editor to reach the
 * resimulation controls; splitting them also means each request only makes the
 * two or three API calls its own screen needs instead of all five.
 *
 * <ul>
 *   <li>{@code /admin/simulation} — season weighting settings (view, create, edit)</li>
 *   <li>{@code /admin/resimulation} — controlled resimulation (history, trigger)</li>
 * </ul>
 */
@WebServlet(name = "AdminSimulationServlet", urlPatterns = {"/admin/simulation", "/admin/resimulation"})
public class AdminSimulationServlet extends AbstractServlet {

    private static final String SETTINGS_PATH = "/admin/simulation";
    private static final String RESIMULATION_PATH = "/admin/resimulation";

    private static final String SETTINGS_VIEW = "/pages/admin-simulation.jsp";
    private static final String RESIMULATION_VIEW = "/pages/admin-resimulation.jsp";

    @Inject
    private SimulationSettingRestClient simulationSettingRestClient;

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
        if (RESIMULATION_PATH.equals(request.getServletPath())) {
            loadResimulationPage(request, request.getParameter("fixtureId"));
            request.getRequestDispatcher(RESIMULATION_VIEW).forward(request, response);
            return;
        }
        loadSettingsPage(request);
        request.getRequestDispatcher(SETTINGS_VIEW).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!requireAdmin(request, response)) {
            return;
        }
        String action = request.getParameter("action");
        if (action == null) {
            action = "";
        }

        // Which of the two screens the POST came from decides both the
        // redirect target on success and the view re-rendered on failure.
        boolean isResimulation = "resimulate".equals(action);
        String fixtureIdForReload = request.getParameter("fixtureId");
        Outcome outcome;
        switch (action) {
            case "saveSettings" -> outcome = saveSettings(request, response);
            case "resimulate" -> outcome = resimulate(request, response);
            default -> {
                request.setAttribute("error", "Unknown simulation action requested");
                outcome = Outcome.FAILURE;
                isResimulation = RESIMULATION_PATH.equals(request.getServletPath());
            }
        }

        if (outcome == Outcome.HANDLED) {
            return;
        }

        if (outcome == Outcome.SUCCESS) {
            flashSuccess(request, isResimulation ? "Resimulation triggered" : "Simulation settings saved");
            String redirect = request.getContextPath() + (isResimulation ? RESIMULATION_PATH : SETTINGS_PATH);
            // Only the resimulation screen is fixture-scoped: coming back with the
            // same fixture selected keeps its history table on screen.
            if (isResimulation && fixtureIdForReload != null && !fixtureIdForReload.isBlank()) {
                redirect += "?fixtureId=" + URLEncoder.encode(fixtureIdForReload, StandardCharsets.UTF_8);
            }
            response.sendRedirect(redirect);
            return;
        }

        if (isResimulation) {
            loadResimulationPage(request, fixtureIdForReload);
            request.getRequestDispatcher(RESIMULATION_VIEW).forward(request, response);
            return;
        }
        loadSettingsPage(request);
        request.getRequestDispatcher(SETTINGS_VIEW).forward(request, response);
    }

    private Outcome saveSettings(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String season = request.getParameter("season");
        BigDecimal playerAbilityWeight = parseDecimal(request.getParameter("playerAbilityWeight"));
        BigDecimal playerFormWeight = parseDecimal(request.getParameter("playerFormWeight"));
        BigDecimal teamBalanceWeight = parseDecimal(request.getParameter("teamBalanceWeight"));
        BigDecimal randomVariationWeight = parseDecimal(request.getParameter("randomVariationWeight"));

        if (season == null || season.isBlank() || playerAbilityWeight == null || playerFormWeight == null
                || teamBalanceWeight == null || randomVariationWeight == null) {
            request.setAttribute("error", "Season and all four weights are required to save simulation settings");
            return Outcome.FAILURE;
        }

        Integer maxResimulations = parseInt(request.getParameter("maxResimulations"));
        if (maxResimulations == null) {
            request.setAttribute("error", "Max resimulations must be a valid whole number");
            return Outcome.FAILURE;
        }

        SimulationSettingRequest settingRequest = new SimulationSettingRequest();
        settingRequest.setSeason(season.trim());
        settingRequest.setPlayerAbilityWeight(playerAbilityWeight);
        settingRequest.setPlayerFormWeight(playerFormWeight);
        settingRequest.setTeamBalanceWeight(teamBalanceWeight);
        settingRequest.setRandomVariationWeight(randomVariationWeight);
        settingRequest.setRequireAdminApproval(request.getParameter("requireAdminApproval") != null);
        settingRequest.setAllowResimulation(request.getParameter("allowResimulation") != null);
        settingRequest.setMaxResimulations(maxResimulations);
        settingRequest.setIsActive(request.getParameter("isActive") != null);

        Optional<UUID> settingsId = parseUuid(request.getParameter("settingsId"));
        Optional<SimulationSettingResponse> saved = settingsId.isPresent()
                ? simulationSettingRestClient.updateSimulationSetting(settingsId.get(), settingRequest)
                : simulationSettingRestClient.createSimulationSetting(settingRequest);

        if (saved.isPresent()) {
            return Outcome.SUCCESS;
        }
        return handleApiFailure(request, response, "Simulation settings could not be saved") ? Outcome.HANDLED : Outcome.FAILURE;
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

    /** Settings screen: the active setting summary plus every season's setting. */
    private void loadSettingsPage(HttpServletRequest request) {
        loadActiveSetting(request);

        Optional<List<SimulationSettingResponse>> settings = simulationSettingRestClient.listSimulationSettings();
        request.setAttribute("simulationSettings", settings.orElse(List.of()));
    }

    /**
     * Resimulation screen: the fixture list, and the selected fixture's run
     * history. The active setting comes along because its resimulation cap and
     * allow/approval flags govern whether a trigger will be accepted at all —
     * the admin needs those visible here, not on the other page.
     */
    private void loadResimulationPage(HttpServletRequest request, String fixtureIdParam) {
        loadActiveSetting(request);

        Optional<List<FixtureResponse>> fixtures = fixtureRestClient.listFixtures(null);
        request.setAttribute("fixtures", fixtures.orElse(List.of()));

        request.setAttribute("selectedFixtureId", fixtureIdParam);
        if (fixtureIdParam != null && !fixtureIdParam.isBlank()) {
            Optional<UUID> fixtureId = parseUuid(fixtureIdParam);
            if (fixtureId.isPresent()) {
                Optional<List<ResimulationResponse>> resimulations =
                        resimulationRestClient.listResimulationsForFixture(fixtureId.get());
                request.setAttribute("resimulations", resimulations.orElse(List.of()));
            }
        }
    }

    private void loadActiveSetting(HttpServletRequest request) {
        Optional<SimulationSettingResponse> active = simulationSettingRestClient.getActiveSimulationSetting();
        if (active.isPresent()) {
            request.setAttribute("activeSetting", active.get());
        } else {
            request.setAttribute("activeSettingError", "No active simulation settings are configured");
        }
    }

    private BigDecimal parseDecimal(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInt(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public String getServletInfo() {
        return "Admin Simulation Servlet, serves the simulation settings screen and the controlled resimulation screen";
    }
}
