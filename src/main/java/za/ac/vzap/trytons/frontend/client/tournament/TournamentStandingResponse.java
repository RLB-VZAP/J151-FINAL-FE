package za.ac.vzap.trytons.frontend.client.tournament;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * One row of a pool table. pointsFor / pointsAgainst are FANTASY points, not
 * rugby match points. position is null until the pool has been ordered.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TournamentStandingResponse {
    private UUID teamId;
    private String teamName;
    private String ownerUsername;
    private int seed;
    private int played;
    private int won;
    private int drawn;
    private int lost;
    private int pointsFor;
    private int pointsAgainst;
    private int pointsDifference;
    private int attackBonus;
    private int losingBonus;
    private int tournamentPoints;
    private Integer position;
    private boolean qualified;
}
