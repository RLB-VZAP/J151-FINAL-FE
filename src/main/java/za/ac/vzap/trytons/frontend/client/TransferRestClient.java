package za.ac.vzap.trytons.frontend.client;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.GenericType;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Dependent
public class TransferRestClient {
    private static final String TRANSFERS_PATH = "/transfers";
    private static final String LOCK_STATUS_PATH = "/lock-status";
    private static final String RECOMMENDATIONS_PATH = "/recommendations";

    private static final Logger LOG = Logger.getLogger(TransferRestClient.class.getName());

    @Inject
    private APIClient apiClient;

    public Optional<TransferResponse> executeTransfer(TransferRequest request) {
        if (!TransferRequestValidator.isValid(request)) {
            LOG.log(Level.WARNING, "Transfer request is invalid.");
            return Optional.empty();
        }

        // TODO [W4-FE-FIXES-26]: executeTransfer deserializes the POST /transfers response straight into
        //   TransferResponse, but the backend wraps it in an ApiResponseDTO envelope — every field maps
        //   null and the "Latest Transfer" panel renders blank despite success (see W4-CR-FE-02)
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
        // TODO [W4-FE-FIXES-27]: getTransferHistory reads a bare JSON array via GenericType<List<...>>,
        //   but backend TransferResource.listTransferHistory wraps it in an ApiResponseDTO envelope
        //   {"success","message","data":[...]} — history page always shows "Unable to load" (see W4-CR-FE-02)
        Optional<List<TransferHistoryResponse>> response =
                apiClient.getList(path, new GenericType<List<TransferHistoryResponse>>() {});

        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to get transfer history.");
        }
        return response;
    }

    // TODO [W4-FE-FIXES-28]: BLOCKED BY BACKEND — getLockStatus calls GET /lock-status/{roundId},
    //   which resolves to LockStatusResource (marked //stub) and always returns 501; this call can
    //   never succeed until the backend lock-status endpoint is implemented
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