package za.ac.vzap.trytons.frontend.client.results;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

/**
 * One scoring-rule line of a team's points breakdown. pointsEarned is already
 * signed, so a deduction arrives negative and is rendered as-is.
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PointsByEventResponse {
    private String eventType;
    private String description;
    private int eventCount;
    private int pointsEarned;
    private boolean deduction;
}
