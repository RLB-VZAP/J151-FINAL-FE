package za.ac.vzap.trytons.frontend.client.auth;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import za.ac.vzap.trytons.frontend.client.shared.APIClient;

@Dependent
public class AuthRestClient {
    private static final String LOGIN_PATH = "/auth/login";
    private static final String LOGOUT_PATH = "/auth/logout";
    private static final String STATUS_PATH = "/auth/status";
    private static final String REGISTER_PATH = "/auth/register";

    private static final Logger LOG = Logger.getLogger(AuthRestClient.class.getName());
    @Inject
    private APIClient apiClient ;

    public Optional<RegisteredUserResponse> register(RegisteredUserRequest request){
        Optional<RegisteredUserResponse> response = apiClient.post(REGISTER_PATH,request,RegisteredUserResponse.class);
        if(response.isEmpty()){
            LOG.log(Level.SEVERE, "Unable to register user");
        }
        return response;
    }
    public Optional<LoginResponse> login(LoginRequest request){
        Optional<LoginResponse> response = apiClient.post(LOGIN_PATH,request,LoginResponse.class);
        if(response.isEmpty()){
            LOG.log(Level.SEVERE, "Unable to login user");
        }
        return response;
    }
    public boolean logout(){
        apiClient.post(LOGOUT_PATH, null, Void.class);
        return true;
    }
    public Optional<AuthStatusResponse> getAuthStatus(String requestingUserId){
        String path = STATUS_PATH + "?requestingUserId=" + encode(requestingUserId);
        return apiClient.get(path, AuthStatusResponse.class);
    }

    public String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

}
