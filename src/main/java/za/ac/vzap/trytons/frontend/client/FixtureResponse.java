package za.ac.vzap.trytons.frontend.client;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FixtureResponse {
    // TODO [DTO-ALIGNMENT]: Match FixtureResponseDTO's UUID/Java-time/FixtureStatus types and
    //   add its createdAt field. The current all-String representation hides contract drift.
    private String fixtureId;
    private String leagueId;
    private String roundId;
    private String teamAId;
    private String teamAName;
    private String teamBId;
    private String teamBName;
    private String fixtureDate;
    private String fixtureTime;
    private String fixtureStatus;
    private String simulationDate;
}
