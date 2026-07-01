package za.ac.vzap.trytons.frontend.client;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
@Getter
@Setter
public class AuthStatusResponse {
    private boolean authenticated;
    private UUID userId;
    private String userName;
    private String email;
    private String role;


}
