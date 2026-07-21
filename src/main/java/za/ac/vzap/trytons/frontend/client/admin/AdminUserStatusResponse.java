package za.ac.vzap.trytons.frontend.client.admin;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AdminUserStatusResponse {
    private UUID userId;
    private String email;
    private String username;
    private String role;
    private Boolean isActive;
}
