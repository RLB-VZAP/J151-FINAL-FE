package za.ac.vzap.trytons.frontend.client.fixture;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/*
    ignoreUnknown because the shared JAX-RS ObjectMapper (ObjectMapperProvider)
    leaves FAIL_ON_UNKNOWN_PROPERTIES at its default of true: a field added to
    the backend DTO but not mirrored here would otherwise make every fixture
    call fail deserialisation, and APIClient never throws -- the page would just
    render as an empty state with no clue why. Mirroring the DTO is still the
    job; this only stops a mismatch from being silent and total.
*/
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
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
    /**
     * The backend's own name for that stage, e.g. "Quarter-Finals". It travels as
     * its own string because Jackson serialises the stage enum by name(), so the
     * label cannot ride along on the enum. Null for a non-tournament fixture.
     */
    private String stageLabel;
    private Integer roundNumber;
    private Integer matchdayNumber;
    /** Null until the fixture has been simulated. */
    private Integer teamAScore;
    private Integer teamBScore;
}
