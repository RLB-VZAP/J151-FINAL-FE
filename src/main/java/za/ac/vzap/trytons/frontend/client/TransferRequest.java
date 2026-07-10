package za.ac.vzap.trytons.frontend.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransferRequest {
    private String teamId;
    private String roundId;
    private String removedPlayerId;
    private String addedPlayerId;
    private boolean penaltyConfirmed;
}