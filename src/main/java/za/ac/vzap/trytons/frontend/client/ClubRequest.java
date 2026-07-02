package za.ac.vzap.trytons.frontend.client;

import lombok.Getter;

@Getter
public class ClubRequest {
    private String clubName;
    private String location;
    private String homeVenue;
    private int strengthRating;
    private boolean isActive;
}
