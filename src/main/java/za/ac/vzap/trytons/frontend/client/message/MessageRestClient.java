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
public class MessageRestClient {

    private static final String MESSAGES_PATH = "/messages";
    private static final String THREADS_PATH = "/messages/threads";
    private static final String DIRECT_PATH = "/messages/direct";
    private static final String UNREAD_COUNT_PATH = "/messages/unread-count";
    private static final String BLOCK_PATH = "/messages/block";
    private static final String BLOCKED_PATH = "/messages/blocked";

    private static final Logger LOG = Logger.getLogger(MessageRestClient.class.getName());

    @Inject
    private APIClient apiClient;

    @Inject
    private ApiCallStatus apiCallStatus;

    public Optional<List<ConversationThreadResponse>> getThreads() {
        Optional<ConversationThreadResponse[]> response = apiClient.get(THREADS_PATH, ConversationThreadResponse[].class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to load conversation threads.");
        }
        return response.map(threads -> new ArrayList<>(Arrays.asList(threads)));
    }

    public Optional<List<DirectMessageResponse>> getConversation(UUID counterpartUserId, String since) {
        if (counterpartUserId == null) {
            LOG.log(Level.WARNING, "A conversation partner is required.");
            return Optional.empty();
        }
        String path = DIRECT_PATH + "/" + encode(counterpartUserId.toString());
        if (since != null && !since.isBlank()) {
            path += "?since=" + encode(since);
        }
        Optional<DirectMessageResponse[]> response = apiClient.get(path, DirectMessageResponse[].class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to load conversation.");
        }
        return response.map(messages -> new ArrayList<>(Arrays.asList(messages)));
    }

    public Optional<DirectMessageResponse> send(SendDirectMessageRequest request) {
        if (request == null || request.getRecipientUserId() == null) {
            LOG.log(Level.WARNING, "A recipient is required to send a message.");
            return Optional.empty();
        }
        Optional<DirectMessageResponse> response = apiClient.post(DIRECT_PATH, request, DirectMessageResponse.class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to send direct message.");
        }
        return response;
    }

    public Optional<Integer> markThreadRead(UUID counterpartUserId) {
        if (counterpartUserId == null) {
            return Optional.empty();
        }
        String path = DIRECT_PATH + "/" + encode(counterpartUserId.toString()) + "/read";
        Optional<UpdatedCountResponse> response = apiClient.put(path, null, UpdatedCountResponse.class);
        return response.map(UpdatedCountResponse::getUpdatedCount);
    }

    public Optional<Integer> getUnreadCount() {
        Optional<UnreadCountResponse> response = apiClient.get(UNREAD_COUNT_PATH, UnreadCountResponse.class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to load unread message count.");
        }
        return response.map(UnreadCountResponse::getUnreadCount);
    }

    public boolean block(UUID userId) {
        if (userId == null) {
            return false;
        }
        apiClient.post(BLOCK_PATH + "/" + encode(userId.toString()), null, Void.class);
        return apiCallStatus.isSuccess();
    }

    public boolean unblock(UUID userId) {
        if (userId == null) {
            return false;
        }
        apiClient.delete(BLOCK_PATH + "/" + encode(userId.toString()), Void.class);
        return apiCallStatus.isSuccess();
    }

    public Optional<List<BlockedUserResponse>> listBlocked() {
        Optional<BlockedUserResponse[]> response = apiClient.get(BLOCKED_PATH, BlockedUserResponse[].class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to load blocked users.");
        }
        return response.map(users -> new ArrayList<>(Arrays.asList(users)));
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
