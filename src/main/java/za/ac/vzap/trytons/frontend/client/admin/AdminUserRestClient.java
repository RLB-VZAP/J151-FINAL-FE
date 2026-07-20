package za.ac.vzap.trytons.frontend.client.admin;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import za.ac.vzap.trytons.frontend.client.shared.APIClient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Dependent
public class AdminUserRestClient {

    private static final String ADMIN_USERS_PATH = "/admin/users";

    @Inject
    private APIClient apiClient;

    public Optional<List<AdminUserSearchResponse>> searchUsers(String searchTerm) {
        // TODO [W4-FE-FIXES-12]: Call GET /admin/users?searchTerm={searchTerm} via the APIClient and return the matching users.
    }

    public Optional<AdminUserStatusResponse> updateUserStatus(UUID targetUserId, AdminUserStatusRequest request) {
        // TODO [W4-FE-FIXES-12]: Call PUT /admin/users/{targetUserId}/status via the APIClient with the given request body and return the updated user status.
    }
}
