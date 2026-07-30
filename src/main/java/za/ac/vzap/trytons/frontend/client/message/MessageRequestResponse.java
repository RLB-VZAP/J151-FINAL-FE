package za.ac.vzap.trytons.frontend.client.message;

import java.util.UUID;

/** Mirrors the backend MessageRequestResponseDTO. */
public class MessageRequestResponse {

    private UUID requestId;
    private UUID requesterUserId;
    private String requesterUsername;
    private UUID addresseeUserId;
    private String addresseeUsername;
    private String status;
    private String introMessage;
    private String createdAt;
    private String respondedAt;
    private boolean mine;

    public MessageRequestResponse() {
    }

    public UUID getRequestId() {
        return requestId;
    }

    public void setRequestId(UUID requestId) {
        this.requestId = requestId;
    }

    public UUID getRequesterUserId() {
        return requesterUserId;
    }

    public void setRequesterUserId(UUID requesterUserId) {
        this.requesterUserId = requesterUserId;
    }

    public String getRequesterUsername() {
        return requesterUsername;
    }

    public void setRequesterUsername(String requesterUsername) {
        this.requesterUsername = requesterUsername;
    }

    public UUID getAddresseeUserId() {
        return addresseeUserId;
    }

    public void setAddresseeUserId(UUID addresseeUserId) {
        this.addresseeUserId = addresseeUserId;
    }

    public String getAddresseeUsername() {
        return addresseeUsername;
    }

    public void setAddresseeUsername(String addresseeUsername) {
        this.addresseeUsername = addresseeUsername;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIntroMessage() {
        return introMessage;
    }

    public void setIntroMessage(String introMessage) {
        this.introMessage = introMessage;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(String respondedAt) {
        this.respondedAt = respondedAt;
    }

    public boolean isMine() {
        return mine;
    }

    public void setMine(boolean mine) {
        this.mine = mine;
    }
}
