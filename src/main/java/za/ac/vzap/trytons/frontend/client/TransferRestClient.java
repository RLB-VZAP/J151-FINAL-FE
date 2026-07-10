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

    private static final Logger LOG = Logger.getLogger(TransferRestClient.class.getName());

    @Inject
    private APIClient apiClient;

    public Optional<TransferResponse> executeTransfer(TransferRequest request) {
        if (!isValidTransferRequest(request)) {
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

    private boolean isValidTransferRequest(TransferRequest request) {
        return request != null
                && !isBlank(request.getTeamId())
                && !isBlank(request.getRoundId())
                && !isBlank(request.getRemovedPlayerId())
                && !isBlank(request.getAddedPlayerId())
                && !request.getRemovedPlayerId().equals(request.getAddedPlayerId());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}