package za.ac.vzap.trytons.frontend.client.leaderboard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.UUID;

/**
 * Tolerates unknown JSON fields so an additive backend field never breaks
 * deserialisation (Jackson defaults to FAIL_ON_UNKNOWN_PROPERTIES=true).
 * The backend DTO's field names (including "scoreDifference") now match
 * this bean's exactly -- they previously disagreed ("pointsDifference" vs
 * "scoreDifference"), which this annotation silently swallowed, so the DIFF
 * column always rendered 0. Keep the two in sync; don't rely on this to
 * paper over a future rename the way it papered over that one.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class LeaderboardEntryResponse {
    private UUID teamId;
    private String teamName;
    private String owner;
    private int rank;
    private Integer rankMovement;
    private Integer previousRanking;
    private int matchesPlayed, matchesWon, matchesDrawn, matchesLost, pointsFor, pointsAgainst,scoreDifference,leaguePoints,totalFantasyPoints;
}
