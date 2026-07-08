package za.ac.vzap.trytons.frontend.client;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class LeaderboardEntryResponse {
    private UUID teamId;
    private String teamName;
    private String owner;
    private int rank;
    private int weeklyPoints;
    private int totalPoints;
    private Integer rankMovement;
}
