package za.ac.vzap.trytons.frontend.client;

public class AuthRestClient {
    private String LOGIN_PATH = "/auth/login";
    private String LOGOUT_PATH = "/auth/logout";
    private String STATUS_PATH = "/auth/status";
    private String REGISTER_PATH = "/users";
    private APIClient apiClient = new  APIClient();

    public AuthRestClient() {
        this.apiClient = new APIClient();
    }
    public APIClient.ApiResult<RegisteredUserResponse> register(RegisteredUserRequest request){
        return apiClient.post(REGISTER_PATH, request, RegisteredUserResponse.class);
    }
    public APIClient.ApiResult<LoginResponse> login(LoginRequest request){
        return apiClient.post(LOGIN_PATH, request, LoginResponse.class);
    }
    public APIClient.ApiResult<Void> logout(){
        return apiClient.post(LOGOUT_PATH, null, Void.class);
    }
    public APIClient.ApiResult<AuthStatusResponse> getAuthStatus(String requestingUserId){
        String path = STATUS_PATH + "?requestingUserId=" + requestingUserId;
        return apiClient.get(path, AuthStatusResponse.class);
    }

}
