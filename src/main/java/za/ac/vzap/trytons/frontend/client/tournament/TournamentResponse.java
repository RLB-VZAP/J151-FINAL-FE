package za.ac.vzap.trytons.frontend.client.tournament;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * A league's tournament. status is one of POOL_STAGE, KNOCKOUT_STAGE,
 * COMPLETED, CANCELLED; the champion / runner-up / third-place fields are only
 * populated once the tournament has completed.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TournamentResponse {
    private UUID tournamentId;
    private UUID leagueId;
    private String leagueName;
    private String season;
    private String status;
    private int managerCount;
    private int poolCount;
    private int poolMatchdays;
    private int bracketSize;
    private boolean thirdPlacePlayoff;
    private UUID championTeamId;
    private String championTeamName;
    private UUID runnerUpTeamId;
    private String runnerUpTeamName;
    private UUID thirdPlaceTeamId;
    private String thirdPlaceTeamName;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private List<TournamentPoolResponse> pools;
}
