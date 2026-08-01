package za.ac.vzap.trytons.frontend.client.tournament;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/** Global tournament scoring settings. Read by anyone, written by admins only. */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TournamentSettingsResponse {
    private UUID settingsId;
    private int winPoints;
    private int drawPoints;
    private int lossPoints;
    private int attackBonusThreshold;
    private int losingBonusMargin;
    private boolean thirdPlacePlayoff;
    private LocalDateTime updatedAt;
}
