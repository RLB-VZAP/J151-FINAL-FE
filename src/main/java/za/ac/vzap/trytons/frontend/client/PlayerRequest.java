package za.ac.vzap.trytons.frontend.client;

import lombok.Getter;

import java.math.BigDecimal;
@Getter
public class PlayerRequest {
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
    private ClubResponse club;
    private PositionResponse position;
}
