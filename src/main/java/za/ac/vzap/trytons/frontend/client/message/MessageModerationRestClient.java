package za.ac.vzap.trytons.frontend.client.message;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import za.ac.vzap.trytons.frontend.client.shared.APIClient;
import za.ac.vzap.trytons.frontend.client.shared.ApiCallStatus;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Dependent
public class MessageModerationRestClient {

    private static final String BASE_PATH = "/admin/message-moderation";
    private static final String PENDING_PATH = BASE_PATH + "/pending";
    private static final String BLOCKLIST_PATH = BASE_PATH + "/blocklist";

    private static final Logger LOG = Logger.getLogger(MessageModerationRestClient.class.getName());

    @Inject
    private APIClient apiClient;

    @Inject
    private ApiCallStatus apiCallStatus;

    public Optional<List<PendingLeagueMessageResponse>> listPending() {
        Optional<PendingLeagueMessageResponse[]> response =
                apiClient.get(PENDING_PATH, PendingLeagueMessageResponse[].class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to load pending messages.");
        }
        return response.map(messages -> new ArrayList<>(Arrays.asList(messages)));
    }

    public boolean approve(UUID messageId) {
        if (messageId == null) {
            return false;
        }
        apiClient.put(BASE_PATH + "/" + encode(messageId.toString()) + "/approve", null, LeagueMessageResponse.class);
        return apiCallStatus.isSuccess();
    }

    public boolean reject(UUID messageId) {
        if (messageId == null) {
            return false;
        }
        apiClient.put(BASE_PATH + "/" + encode(messageId.toString()) + "/reject", null, Void.class);
        return apiCallStatus.isSuccess();
    }

    public Optional<List<BlockedPhraseResponse>> listBlocklist() {
        Optional<BlockedPhraseResponse[]> response =
                apiClient.get(BLOCKLIST_PATH, BlockedPhraseResponse[].class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to load blocklist.");
        }
        return response.map(phrases -> new ArrayList<>(Arrays.asList(phrases)));
    }

    public boolean addBlocklistPhrase(String phrase) {
        if (phrase == null || phrase.isBlank()) {
            return false;
        }
        apiClient.post(BLOCKLIST_PATH, new AddBlockedPhraseRequest(phrase.trim()), BlockedPhraseResponse.class);
        return apiCallStatus.isSuccess();
    }

    public boolean removeBlocklistPhrase(UUID blocklistId) {
        if (blocklistId == null) {
            return false;
        }
        apiClient.delete(BLOCKLIST_PATH + "/" + encode(blocklistId.toString()), Void.class);
        return apiCallStatus.isSuccess();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
