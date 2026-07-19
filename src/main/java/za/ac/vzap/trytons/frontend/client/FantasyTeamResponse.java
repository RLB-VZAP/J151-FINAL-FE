package za.ac.vzap.trytons.frontend.client;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class FantasyTeamResponse {
    private UUID teamID;
    private String teamName;
    private UUID managerId;
    private String managerUsername;
    private BigDecimal totalTeamValue;
    private BigDecimal remainingBudget;
    private Integer weeklyPoints;
    private Integer totalPoints;
    private Boolean valid;
    private List<FantasyTeamPlayerSelectionResponse> selectedPlayers;
}
