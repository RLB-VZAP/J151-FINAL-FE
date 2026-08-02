package za.ac.vzap.trytons.frontend.client.tournament;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Body of PUT /tournaments/leagues/{leagueId}/rounds/{roundId}/match-day.
 *
 * <p>Mirrors the backend's MatchDayUpdateRequestDTO: the whole round moves, and
 * kickoff travels with it: a round's kickoff is its lock deadline, so a
 * fixture's date always derives from its round's lock deadline.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MatchDayUpdateRequest {

    /** Must be a Monday, Wednesday, Friday, Saturday or Sunday, and still in the future. */
    private LocalDate matchDay;

    /** Kickoff for every fixture in the round; null keeps the current time. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm[:ss]")
    private LocalTime kickoff;
}
