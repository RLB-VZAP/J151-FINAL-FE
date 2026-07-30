package za.ac.vzap.trytons.frontend.client.message;

import java.util.UUID;

/**
 * A user the caller could message, with where the two of them stand.
 * Mirrors the backend MessageContactDTO; {@code state} carries the backend
 * MessageContactState name (NONE, REQUEST_SENT, REQUEST_RECEIVED, ACCEPTED,
 * DECLINED, BLOCKED) as a String so the JSP can switch on it without the
 * frontend duplicating the enum.
 */
public class MessageContactResponse {

    private UUID userId;
    private String username;
    private String state;
    private UUID requestId;

    public MessageContactResponse() {
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public UUID getRequestId() {
        return requestId;
    }

    public void setRequestId(UUID requestId) {
        this.requestId = requestId;
    }
}
