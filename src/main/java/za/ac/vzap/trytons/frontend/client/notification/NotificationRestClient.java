package za.ac.vzap.trytons.frontend.client.notification;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import za.ac.vzap.trytons.frontend.client.shared.APIClient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Dependent
public class NotificationRestClient {

    private static final String NOTIFICATIONS_PATH = "/notifications";
    private static final String UNREAD_COUNT_PATH = "/notifications/unread-count";
    private static final String READ_ALL_PATH = "/notifications/read-all";

    @Inject
    private APIClient apiClient;

    public Optional<List<NotificationResponse>> getNotifications(boolean unreadOnly) {
        // TODO: Call GET /notifications?unreadOnly={unreadOnly} via the APIClient and return the caller's notifications.
    }

    public Optional<Integer> getUnreadCount() {
        // TODO: Call GET /notifications/unread-count via the APIClient and return the unread notification count.
    }

    public Optional<NotificationResponse> markAsRead(UUID notificationId) {
        // TODO: Call PUT /notifications/{notificationId}/read via the APIClient and return the updated notification.
    }

    public Optional<Integer> markAllAsRead() {
        // TODO: Call PUT /notifications/read-all via the APIClient and return the number of notifications updated.
    }
}
