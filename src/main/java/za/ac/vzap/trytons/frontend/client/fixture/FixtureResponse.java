package za.ac.vzap.trytons.frontend.client.fixture;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FixtureResponse {
    private UUID fixtureId;
    private UUID leagueId;
    private UUID roundId;
    private UUID teamAId;
    private String teamAName;
    private UUID teamBId;
    private String teamBName;
    private LocalDate fixtureDate;
    private LocalTime fixtureTime;
    private String fixtureStatus;
    private LocalDateTime simulationDate;
    private LocalDateTime createdAt;
    /** POOL, or a knockout stage (QUARTER_FINAL, SEMI_FINAL, FINAL, ...). Null for a
     *  non-tournament fixture. */
    private String stage;
    private Integer roundNumber;
    private Integer matchdayNumber;
    /** Null until the fixture has been simulated. */
    private Integer teamAScore;
    private Integer teamBScore;
}
