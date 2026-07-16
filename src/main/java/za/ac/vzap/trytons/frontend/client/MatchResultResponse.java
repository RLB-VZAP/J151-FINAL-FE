package za.ac.vzap.trytons.frontend.client;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class MatchResultResponse {
    // TODO [DTO-ALIGNMENT]: Match MatchResultResponseDTO: replace approvedAt with UUID
    //   approvedByAdminUserId and align winnerSide with the backend MatchTeamSide enum.
    private UUID resultId;
    private UUID fixtureId;
    private UUID teamAId;
    private UUID teamBId;
    private int simulationRunNumber;
    private int teamAScore;
    private int teamBScore;
    private String winnerSide;
    private boolean draw;
    private boolean approved;
    private boolean current;
    private LocalDateTime resultDate;
    private UUID approvedByAdminUserId;
}
