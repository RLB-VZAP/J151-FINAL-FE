package za.ac.vzap.trytons.frontend.servlet.simulation;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.simulation.SimulationSettingRequest;
import za.ac.vzap.trytons.frontend.client.simulation.SimulationSettingResponse;
import za.ac.vzap.trytons.frontend.client.simulation.SimulationSettingRestClient;
import za.ac.vzap.trytons.frontend.servlet.shared.AbstractServlet;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@WebServlet(name = "AdminSimulationServlet", urlPatterns = {"/admin/simulation"})
public class AdminSimulationServlet extends AbstractServlet {

    private static final String VIEW = "/pages/admin-simulation.jsp";

    @Inject
    private SimulationSettingRestClient simulationSettingRestClient;

    private enum Outcome { SUCCESS, FAILURE, HANDLED }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!requireAdmin(request, response)) {
            return;
        }
        loadPage(request);
        request.getRequestDispatcher(VIEW).forward(request, response);
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

        Outcome outcome;
        switch (action) {
            case "saveSettings" -> outcome = saveSettings(request, response);
            default -> {
                request.setAttribute("error", "Unknown simulation action requested");
                outcome = Outcome.FAILURE;
            }
        }

        if (outcome == Outcome.HANDLED) {
            return;
        }

        if (outcome == Outcome.SUCCESS) {
            flashSuccess(request, "Simulation settings saved");
            response.sendRedirect(request.getContextPath() + "/admin/simulation");
            return;
        }

        loadPage(request);
        request.getRequestDispatcher(VIEW).forward(request, response);
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

    private void loadPage(HttpServletRequest request) {
        Optional<SimulationSettingResponse> active = simulationSettingRestClient.getActiveSimulationSetting();
        if (active.isPresent()) {
            request.setAttribute("activeSetting", active.get());
        } else {
            request.setAttribute("activeSettingError", "No active simulation settings are configured");
        }

        Optional<List<SimulationSettingResponse>> settings = simulationSettingRestClient.listSimulationSettings();
        request.setAttribute("simulationSettings", settings.orElse(List.of()));
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
        return "Admin Simulation Servlet, handles simulation settings capture";
    }
}
