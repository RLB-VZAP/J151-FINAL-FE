package za.ac.vzap.trytons.frontend.client.results;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

/**
 * How one team's match score was arrived at. The events sum to playerPoints,
 * then playerPoints + captainBonus - transferPenalty = totalPoints, which is
 * the score already shown for that side. Events come back sorted by
 * pointsEarned descending; the event types are admin-configurable, so nothing
 * here assumes a fixed set.
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TeamPointsBreakdownResponse {
    private UUID teamId;
    private String teamName;
    private int playerPoints;
    private int captainBonus;
    private int transferPenalty;
    private int totalPoints;
    private List<PointsByEventResponse> events;
}
