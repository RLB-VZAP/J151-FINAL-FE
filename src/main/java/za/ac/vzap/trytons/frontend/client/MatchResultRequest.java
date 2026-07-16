package za.ac.vzap.trytons.frontend.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MatchResultRequest {
    // TODO [DTO-ALIGNMENT]: Match MatchResultRequestDTO, which accepts only fixtureId,
    //   teamAScore, and teamBScore. actorId and simulationReason are frontend-only fields that
    //   should not be emitted in this endpoint's request body.
    private UUID actorId;
    private UUID fixtureId;
    private int teamAScore;
    private int teamBScore;
    private String simulationReason;
}
