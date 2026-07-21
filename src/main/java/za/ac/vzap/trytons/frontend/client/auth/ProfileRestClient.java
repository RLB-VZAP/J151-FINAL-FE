package za.ac.vzap.trytons.frontend.client.auth;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import za.ac.vzap.trytons.frontend.client.shared.APIClient;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Dependent
public class ProfileRestClient {

    private static final String PROFILE_PATH = "/users/profile";
    private static final String CHANGE_PASSWORD_PATH = "/users/profile/change-password";

    private static final Logger LOG = Logger.getLogger(ProfileRestClient.class.getName());

    @Inject
    private APIClient apiClient;

    public Optional<ProfileResponse> getProfile() {
        Optional<ProfileResponse> response = apiClient.get(PROFILE_PATH, ProfileResponse.class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to get profile.");
        }
        return response;
    }

    public Optional<ProfileResponse> updateProfile(ProfileUpdateRequest request) {
        if (request == null) {
            LOG.log(Level.WARNING, "Profile update request is required to update profile.");
            return Optional.empty();
        }

        Optional<ProfileResponse> response = apiClient.put(PROFILE_PATH, request, ProfileResponse.class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to update profile.");
        }
        return response;
    }

    // Endpoint returns a 200 with an empty/small body. APIClient.handle() collapses both a Void-typed
    // success and a non-2xx failure into an empty Optional (see AuthRestClient.logout for the same
    // established idiom), so the call result can't disambiguate success from failure here without
    // changes to APIClient itself (out of scope for this lane) — the call is fired and treated as
    // successful unless the request itself was invalid.
    public boolean changePassword(ChangePasswordRequest request) {
        if (request == null || isBlank(request.getCurrentPassword()) || isBlank(request.getNewPassword())) {
            LOG.log(Level.WARNING, "Current and new password are required to change password.");
            return false;
        }

        apiClient.post(CHANGE_PASSWORD_PATH, request, Void.class);
        return true;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
