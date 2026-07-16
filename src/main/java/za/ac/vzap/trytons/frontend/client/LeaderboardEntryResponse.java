package za.ac.vzap.trytons.frontend.client;

import lombok.*;

import java.util.UUID;

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
    // TODO [W4-FE-FIXES-18]: DTO field names drift from the backend JSON contract — `previousRank`
    //   vs backend `previousRanking`, and `total_fantasy_points` (line 19, snake_case) vs backend
    //   `totalFantasyPoints`; rankMovement is also missing. With no snake_case Jackson strategy these
    //   deserialize null/0 on every leaderboard row — verify against LeaderboardEntryResponseDTO.
    private Integer previousRank;
    private int matchesPlayed, matchesWon, matchesDrawn, matchesLost, pointsFor, pointsAgainst,scoreDifference,leaguePoints,total_fantasy_points;
}
