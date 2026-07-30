package za.ac.vzap.trytons.frontend.client.message;

import java.util.UUID;

/** Body for "ask this user for permission to message them". */
public class CreateMessageRequestRequest {

    private UUID addresseeUserId;
    private String introMessage;

    public CreateMessageRequestRequest() {
    }

    public CreateMessageRequestRequest(UUID addresseeUserId, String introMessage) {
        this.addresseeUserId = addresseeUserId;
        this.introMessage = introMessage;
    }

    public UUID getAddresseeUserId() {
        return addresseeUserId;
    }

    public void setAddresseeUserId(UUID addresseeUserId) {
        this.addresseeUserId = addresseeUserId;
    }

    public String getIntroMessage() {
        return introMessage;
    }

    public void setIntroMessage(String introMessage) {
        this.introMessage = introMessage;
    }
}
