package za.ac.vzap.trytons.frontend.client.results;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Mirrors the backend's {@code MatchProcessingResultDTO}, returned by
 * {@code POST /match-processing/fixtures/{fixtureId}}.
 *
 * <p>Every field is a count of work that actually happened — points calculated,
 * team score rows written, leaderboard refreshed — which is what separates a real
 * PROCESSED fixture from a status column that merely says so.
 */
@Getter
@Setter
public class MatchProcessingResponse {
    private UUID fixtureId;
    private int pointsCalculated;
    private int teamsUpdated;
    private boolean leaderboardsRefreshed;
    private String status;
}
