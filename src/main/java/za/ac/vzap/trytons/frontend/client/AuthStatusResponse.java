package za.ac.vzap.trytons.frontend.client;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
@Getter
@Setter
public class AuthStatusResponse {
    // TODO [DTO-ALIGNMENT]: Rename userName to username to match AuthStatusResponseDTO JSON;
    //   the current field is not populated when the backend returns the authenticated user.
    private boolean authenticated;
    private UUID userId;
    private String userName;
    private String email;
    private String role;


}
