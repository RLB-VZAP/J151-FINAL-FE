package za.ac.vzap.trytons.frontend.client.league;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeagueMemberResponse {
    private String membershipId;
    private String teamDisplayName;
    private String userDisplayName;
}