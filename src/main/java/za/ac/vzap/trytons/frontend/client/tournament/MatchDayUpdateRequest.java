package za.ac.vzap.trytons.frontend.client.tournament;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Body of PUT /tournaments/leagues/{leagueId}/rounds/{roundId}/match-day.
 *
 * <p>Mirrors the backend's MatchDayUpdateRequestDTO: the whole round moves, and
 * kickoff is not editable -- it is fixed at MatchdayCalendar.KICKOFF so a
 * fixture's date always derives from its round's lock deadline.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MatchDayUpdateRequest {

    /** Must be a Wednesday, Saturday or Sunday, and still in the future. */
    private LocalDate matchDay;
}
