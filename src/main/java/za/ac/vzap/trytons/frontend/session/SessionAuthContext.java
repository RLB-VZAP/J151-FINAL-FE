package za.ac.vzap.trytons.frontend.session;

import jakarta.enterprise.context.SessionScoped;
import za.ac.vzap.trytons.frontend.client.LoginResponse;

import java.io.Serializable;
import java.util.UUID;

@SessionScoped
public class SessionAuthContext implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID userId;
    private String username;
    private String email;
    private String role;
    private String token;

    public void signIn(LoginResponse response) {
        this.userId = response.getUserId();
        this.username = response.getUsername();
        this.email = response.getEmail();
        this.role = response.getRole();
        this.token = response.getToken();
    }

    public void clear() {
        userId = null;
        username = null;
        email = null;
        role = null;
        token = null;
    }

    public boolean isAuthenticated() {
        return userId != null && token != null && !token.isBlank();
    }

    public UUID getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getToken() {
        return token;
    }
}
