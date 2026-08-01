package za.ac.vzap.trytons.frontend.client.tournament;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/** Mirrors the backend StartLeagueResponse returned by POST /tournaments/leagues/{leagueId}/start. */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StartLeagueResponse {
    private UUID leagueId;
    private UUID tournamentId;
    private String season;
    private int managerCount;
    private int poolCount;
    private int poolMatchdays;
    private int bracketSize;
    private int totalMatchdays;
    private int fixturesGenerated;
    private String message;
}
