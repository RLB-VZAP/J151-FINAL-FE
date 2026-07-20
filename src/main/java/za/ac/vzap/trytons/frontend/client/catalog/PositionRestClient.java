package za.ac.vzap.trytons.frontend.client.catalog;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import za.ac.vzap.trytons.frontend.client.shared.APIClient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Dependent
public class PositionRestClient {

    private static final String POSITIONS_PATH = "/position";

    @Inject
    private APIClient apiClient;

    public Optional<List<PositionResponse>> getAllPositions() {
        // TODO: Call GET /position via the APIClient and return the list of positions.
    }

    public Optional<PositionResponse> getPositionById(UUID positionId) {
        // TODO: Call GET /position/{positionId} via the APIClient and return the matching position.
    }

    public Optional<PositionResponse> createPosition(PositionRequest request) {
        // TODO: Call POST /position via the APIClient with the given request body and return the created position.
    }

    public Optional<PositionResponse> updatePosition(UUID positionId, PositionRequest request) {
        // TODO: Call PUT /position/{positionId} via the APIClient with the given request body and return the updated position.
    }
}
