package za.ac.vzap.trytons.frontend.client.league;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JoinLeagueRequest {
    private UUID leagueId;
    private String leagueCode;
    private UUID teamId;

    public JoinLeagueRequest(String leagueId, String leagueCode) {
        this.leagueId = UUID.fromString(leagueId);
        this.leagueCode = leagueCode;
    }
}