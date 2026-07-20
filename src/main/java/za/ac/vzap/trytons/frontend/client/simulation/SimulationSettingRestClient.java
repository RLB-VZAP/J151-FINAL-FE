package za.ac.vzap.trytons.frontend.client.simulation;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import za.ac.vzap.trytons.frontend.client.shared.APIClient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Dependent
public class SimulationSettingRestClient {

    private static final String SIMULATION_SETTINGS_PATH = "/simulation-settings";

    @Inject
    private APIClient apiClient;

    public Optional<SimulationSettingResponse> createSimulationSetting(SimulationSettingRequest request) {
        // TODO: Call POST /simulation-settings via the APIClient with the given request body and return the created settings.
    }

    public Optional<SimulationSettingResponse> updateSimulationSetting(UUID simulationSettingsId, SimulationSettingRequest request) {
        // TODO: Call PUT /simulation-settings/{simulationSettingsId} via the APIClient with the given request body and return the updated settings.
    }

    public Optional<SimulationSettingResponse> getSimulationSettingById(UUID simulationSettingsId) {
        // TODO: Call GET /simulation-settings/{simulationSettingsId} via the APIClient and return the matching settings.
    }

    public Optional<SimulationSettingResponse> getActiveSimulationSetting() {
        // TODO: Call GET /simulation-settings/active via the APIClient and return the currently active settings.
    }

    public Optional<List<SimulationSettingResponse>> listSimulationSettings() {
        // TODO: Call GET /simulation-settings via the APIClient and return the full list of settings.
    }
}
