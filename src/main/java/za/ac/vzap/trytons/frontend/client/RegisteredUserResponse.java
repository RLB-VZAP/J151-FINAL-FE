package za.ac.vzap.trytons.frontend.client;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
@Getter
@Setter
public class RegisteredUserResponse {
    private UUID userId;
    private String username;
    private String role;
    private String status;
}
