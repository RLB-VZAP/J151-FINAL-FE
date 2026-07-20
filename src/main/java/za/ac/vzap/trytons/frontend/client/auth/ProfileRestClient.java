package za.ac.vzap.trytons.frontend.client.auth;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import za.ac.vzap.trytons.frontend.client.shared.APIClient;

// TODO: This client backs the user profile and change-password views (E2E-01). The backend currently exposes
// TODO: no profile-update or password-change endpoint or DTO, so no method signatures can be scaffolded from a
// TODO: proven contract yet. Once the backend profile/password lane is designed, add the request/response
// TODO: contract types and the matching client methods (view profile, update profile, change password) here.
@Dependent
public class ProfileRestClient {

    @Inject
    private APIClient apiClient;
}
