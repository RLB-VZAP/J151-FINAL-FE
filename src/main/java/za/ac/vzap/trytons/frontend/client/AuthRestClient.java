package za.ac.vzap.trytons.frontend.client;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Dependent
public class AuthRestClient {
    private String LOGIN_PATH = "/auth/login";
    private String LOGOUT_PATH = "/auth/logout";
    private String STATUS_PATH = "/auth/status";
    private String REGISTER_PATH = "/users";

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
        String path = STATUS_PATH + "?requestingUserId=" + requestingUserId;
        return apiClient.get(path, AuthStatusResponse.class);
    }

}
