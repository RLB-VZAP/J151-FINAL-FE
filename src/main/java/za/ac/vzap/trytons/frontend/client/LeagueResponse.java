package za.ac.vzap.trytons.frontend.client;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeagueResponse {
    private String leagueId;
    private String leagueName;
    private String description;
    private String leagueType;
    private int maxMembers;
    private String managerDisplayName;
    private String leagueCode;
}