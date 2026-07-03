package za.ac.vzap.trytons.frontend.client;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
@Getter
@Setter
public class ClubResponse {
    private UUID clubId;
    private String clubName;
    private String location;
    private String homeVenue;
    private int strengthRating;
    private boolean isActive;
}
