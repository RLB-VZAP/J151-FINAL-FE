package za.ac.vzap.trytons.frontend.client;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Dependent
public class TransferRestClient {
    private String GET_TRANSFER_RECOMMENDATIONS_PATH = "/transfer";

    private static final Logger LOG = Logger.getLogger(TransferRestClient.class.getName());

    @Inject
    private APIClient apiClient;

    public Optional<TransferRecommendationResponse> getRecommendation(UUID teamId) {
        String path =  GET_TRANSFER_RECOMMENDATIONS_PATH + "/" + teamId + "/recommendations";

        Optional<TransferRecommendationResponse> response = apiClient.get(path, TransferRecommendationResponse.class);

        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Could not find transfer recommendations for team ");
        }
        return response;
    }
}