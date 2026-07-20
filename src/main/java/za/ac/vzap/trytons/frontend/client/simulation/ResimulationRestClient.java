package za.ac.vzap.trytons.frontend.client.simulation;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import za.ac.vzap.trytons.frontend.client.shared.APIClient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Dependent
public class ResimulationRestClient {

    private static final String RESIMULATIONS_PATH = "/resimulations";

    @Inject
    private APIClient apiClient;

    public Optional<ResimulationResponse> resimulateFixture(ResimulationRequest request) {
        // TODO: Call POST /resimulations via the APIClient with the given request body and return the resulting resimulation record. The backend endpoint itself is still a stub (W3-BE-DATABASE-LOGIC-FIX-05A).
    }

    public Optional<List<ResimulationResponse>> listResimulationsForFixture(UUID fixtureId) {
        // TODO: Call GET /resimulations/fixture/{fixtureId} via the APIClient and return the resimulation history for that fixture. The backend endpoint itself is still a stub (W3-BE-DATABASE-LOGIC-FIX-05A).
    }
}
