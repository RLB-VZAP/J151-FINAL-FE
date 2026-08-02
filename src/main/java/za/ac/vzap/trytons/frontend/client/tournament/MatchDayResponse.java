package za.ac.vzap.trytons.frontend.client.tournament;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/** Where a round ended up after being moved. Mirrors MatchDayResponseDTO. */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MatchDayResponse {

    private UUID roundId;
    private Integer matchdayNumber;

    /** POOL, or a knockout stage. */
    private String stage;
    /** The backend's own name for that stage, e.g. "Quarter-Finals". */
    private String stageLabel;

    private LocalDate matchDay;
    private LocalTime kickoff;

    /** How many fixtures moved with the round. */
    private int fixturesMoved;
}
