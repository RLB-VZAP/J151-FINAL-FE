package za.ac.vzap.trytons.frontend.client.catalog;

import lombok.Getter;

@Getter
public class PositionRequest {
    private String positionName;
    private String positionCategory;
    private int minRequired;
    private int maxAllowed;
}
