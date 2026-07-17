package za.ac.vzap.trytons.frontend.client;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;
@Getter
@Setter
public class PlayerResponse {
    private UUID playerId;
    private String playerName;
    private BigDecimal value;
    private int attackingAbility;
    private int defensiveAbility;
    private int kickingAbility;
    private int discipline;
    private int consistency;
    private int fitness;
    private int currentForm;
    private int totalFantasyPoints;
    private boolean isActive;
    // TODO [W4-FE-FIXES-22]: DTO drift — backend PlayerResponseDTO sends flat clubId/positionId
    //   UUIDs, not nested club/position objects (lines 23-24), so Jackson never populates them and
    //   create-team.jsp renders "-" for every player's club/position; also totalFantasyPoints
    //   (line 21) has no backend counterpart (always 0) — align with PlayerResponseDTO
    private ClubResponse club;
    private PositionResponse position;
    private PlayerResponse player;
    private boolean isCaptain;
    private boolean isViceCaptain;
    private boolean isBench;
}
