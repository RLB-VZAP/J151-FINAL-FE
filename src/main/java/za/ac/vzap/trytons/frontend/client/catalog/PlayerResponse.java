package za.ac.vzap.trytons.frontend.client.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.UUID;
@Getter
@Setter
public class PlayerResponse {
    private UUID playerId;
    private UUID clubId;
    private UUID positionId;
    private String playerName;
    private BigDecimal value;
    private int attackingAbility;
    private int defensiveAbility;
    private int kickingAbility;
    private int discipline;
    private int consistency;
    private int fitness;
    private int currentForm;
    @JsonProperty("isActive")
    private boolean isActive;

    /**
     * Current availability as the backend enum name — ACTIVE, INJURED, SUSPENDED.
     * Separate from {@code isActive}, which only says the player is still on a
     * roster: a rostered player can be injured and so cannot be picked.
     */
    private String availabilityStatus;
}
