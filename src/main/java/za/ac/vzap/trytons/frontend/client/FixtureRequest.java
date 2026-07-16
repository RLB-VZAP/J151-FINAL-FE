package za.ac.vzap.trytons.frontend.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FixtureRequest {
    // TODO [DTO-ALIGNMENT]: Match FixtureRequestDTO's UUID identifiers, LocalDate/LocalTime,
    //   and FixtureStatus field before relying on this payload; this client model currently
    //   serializes every contract value as String.
    private String leagueId;
    private String roundId;
    private String teamAId;
    private String teamBId;
    private String fixtureDate;
    private String fixtureTime;
    private String fixtureStatus;

}
