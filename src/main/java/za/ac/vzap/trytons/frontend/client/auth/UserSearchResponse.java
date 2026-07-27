package za.ac.vzap.trytons.frontend.client.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

// Mirrors the backend's privacy-scoped UserSearchResponseDTO: userId and
// username only, no email or role.
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserSearchResponse {
    private UUID userId;
    private String username;
}
