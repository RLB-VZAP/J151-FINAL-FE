package za.ac.vzap.trytons.frontend.client.tournament;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * A tournament fixture. stage is POOL for the pool phase, otherwise one of
 * ROUND_OF_32, ROUND_OF_16, QUARTER_FINAL, SEMI_FINAL, THIRD_PLACE, FINAL.
 * teamAScore / teamBScore stay null until the fixture has been simulated.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TournamentFixtureResponse {
    private UUID fixtureId;
    private UUID tournamentId;
    private String stage;
    /** The backend's own name for the stage, e.g. "Quarter-Finals". */
    private String stageLabel;
    private UUID poolId;
    private String poolName;
    private Integer bracketSlot;
    private Integer matchdayNumber;
    private Integer roundNumber;
    private UUID roundId;
    private UUID teamAId;
    private String teamAName;
    private UUID teamBId;
    private String teamBName;
    private Integer teamAScore;
    private Integer teamBScore;
    private String status;
    private LocalDate fixtureDate;
    private LocalTime fixtureTime;
}
