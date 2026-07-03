package za.ac.vzap.trytons.frontend.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClubRequest {
    private String clubName;
    private String location;
    private String homeVenue;
    private int strengthRating;
    private boolean isActive;
}
