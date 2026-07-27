package za.ac.vzap.trytons.frontend.client.message;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MessageRequestOverviewResponse {
    private List<MessageRequestResponse> incoming;
    private List<MessageRequestResponse> outgoing;
}
