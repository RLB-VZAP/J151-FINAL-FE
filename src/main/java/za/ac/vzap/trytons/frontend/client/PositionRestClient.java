package za.ac.vzap.trytons.frontend.client;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Dependent
public class PositionRestClient {

    private static final String POSITION_PATH = "/position";
    private static final Logger LOG = Logger.getLogger(PositionRestClient.class.getName());

    @Inject
    private APIClient apiClient;

    public Optional<List<PositionResponse>> listPositions() {
        Optional<PositionResponse[]> response = apiClient.get(POSITION_PATH, PositionResponse[].class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to load positions.");
        }
        return response.map(items -> new ArrayList<>(Arrays.asList(items)));
    }
}
