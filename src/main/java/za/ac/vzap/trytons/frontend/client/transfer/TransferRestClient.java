package za.ac.vzap.trytons.frontend.client.transfer;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.GenericType;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import za.ac.vzap.trytons.frontend.client.shared.APIClient;
import za.ac.vzap.trytons.frontend.client.fixture.LockStatusResponse;

@Dependent
public class TransferRestClient {
    private static final String TRANSFERS_PATH = "/transfers";
    private static final String LOCK_STATUS_PATH = "/lock-status";
    private static final String RECOMMENDATIONS_PATH = "/recommendations";

    private static final Logger LOG = Logger.getLogger(TransferRestClient.class.getName());

    @Inject
    private APIClient apiClient;

    public Optional<TransferResponse> executeTransfer(TransferRequest request) {
        if (TransferRequestValidator.isValid(request)) {
            LOG.log(Level.WARNING, "Transfer request is invalid.");
            return Optional.empty();
        }
        Optional<TransferResponse> response = apiClient.post(TRANSFERS_PATH, request, TransferResponse.class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to execute transfer.");
        }
        return response;
    }

    public Optional<List<TransferHistoryResponse>> getTransferHistory(String teamId) {
        if (isBlank(teamId)) {
            LOG.log(Level.WARNING, "Team id is required to get transfer history.");
            return Optional.empty();
        }

        // TODO [W4-FE-FIXES-17]: teamId concatenated into path unencoded — encode per
        //   PlayerRestClient.listPlayers pattern (see W4-CR-FE-08)
        String path = TRANSFERS_PATH + "/" + teamId + "/history";
        Optional<List<TransferHistoryResponse>> response =
                apiClient.getList(path, new GenericType<List<TransferHistoryResponse>>() {});

        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to get transfer history.");
        }
        return response;
    }

    public Optional<LockStatusResponse> getLockStatus(String roundId) {
        if (isBlank(roundId)) {
            LOG.log(Level.WARNING, "Round id is required to get lock status.");
            return Optional.empty();
        }

        String path = LOCK_STATUS_PATH + "/" + roundId;
        Optional<LockStatusResponse> response = apiClient.get(path, LockStatusResponse.class);

        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to get lock status.");
        }
        return response;
    }

    public Optional<TransferRecommendationResponse> getTransferRecommendation(String teamId, String roundId) {
        if(isBlank(teamId)){
            LOG.log(Level.WARNING, "Team id is required to get recommendation.");
            return Optional.empty();
        }

        // TODO [W4-FE-FIXES-29]: no such backend route — GET /transfers/{teamId}/recommendations?roundId=
        //   does not exist; the real endpoint is POST /transfer-recommendations with a
        //   TransferRecommendationRequestDTO body; every load 404s and recommendations are silently
        //   always empty (see W4-CR-FE-03)
        String path = TRANSFERS_PATH + "/" + teamId + RECOMMENDATIONS_PATH;
        if(roundId != null && !roundId.isBlank()){
            // TODO [W4-FE-FIXES-17]: roundId concatenated into query unencoded — encode per
            //   PlayerRestClient.listPlayers pattern (see W4-CR-FE-08)
            path += "?roundId=" + roundId;
        }

        Optional<TransferRecommendationResponse> response = apiClient.get(path, TransferRecommendationResponse.class);

        if(response.isEmpty()){
            LOG.log(Level.WARNING, "Unable to get recommendation.");
        }
        return response;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}