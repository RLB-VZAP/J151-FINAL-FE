package za.ac.vzap.trytons.frontend.client.league;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JoinLeagueRequest {
    private String leagueId;
    private String leagueCode;
}