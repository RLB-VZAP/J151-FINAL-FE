package za.ac.vzap.trytons.frontend.client.notification;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import za.ac.vzap.trytons.frontend.client.shared.APIClient;

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
public class NotificationRestClient {

    private static final String NOTIFICATIONS_PATH = "/notifications";
    private static final String UNREAD_COUNT_PATH = "/notifications/unread-count";
    private static final String READ_ALL_PATH = "/notifications/read-all";

    private static final Logger LOG = Logger.getLogger(NotificationRestClient.class.getName());

    @Inject
    private APIClient apiClient;

    public Optional<List<NotificationResponse>> getNotifications(boolean unreadOnly) {
        String path = NOTIFICATIONS_PATH + "?unreadOnly=" + encode(String.valueOf(unreadOnly));
        Optional<NotificationResponse[]> response = apiClient.get(path, NotificationResponse[].class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to get notifications.");
        }
        return response.map(notifications -> new ArrayList<>(Arrays.asList(notifications)));
    }

    // Backend wraps the count in a single-key JSON object ({"unreadCount": N}), not a bare integer —
    // deserialize into UnreadCountResponse and unwrap so the method can still return Optional<Integer>.
    public Optional<Integer> getUnreadCount() {
        Optional<UnreadCountResponse> response = apiClient.get(UNREAD_COUNT_PATH, UnreadCountResponse.class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to get unread notification count.");
        }
        return response.map(UnreadCountResponse::getUnreadCount);
    }

    public Optional<NotificationResponse> markAsRead(UUID notificationId) {
        if (notificationId == null) {
            LOG.log(Level.WARNING, "Notification id is required to mark a notification as read.");
            return Optional.empty();
        }

        String path = NOTIFICATIONS_PATH + "/" + encode(notificationId.toString()) + "/read";
        Optional<NotificationResponse> response = apiClient.put(path, null, NotificationResponse.class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to mark notification as read.");
        }
        return response;
    }

    // Backend wraps the count in a single-key JSON object ({"updatedCount": N}), not a bare integer —
    // deserialize into UpdatedCountResponse and unwrap so the method can still return Optional<Integer>.
    public Optional<Integer> markAllAsRead() {
        Optional<UpdatedCountResponse> response = apiClient.put(READ_ALL_PATH, null, UpdatedCountResponse.class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to mark all notifications as read.");
        }
        return response.map(UpdatedCountResponse::getUpdatedCount);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
