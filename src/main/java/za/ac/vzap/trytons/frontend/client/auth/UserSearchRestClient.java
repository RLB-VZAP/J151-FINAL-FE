package za.ac.vzap.trytons.frontend.client.auth;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import za.ac.vzap.trytons.frontend.client.shared.APIClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

// Backs the "find someone to message" search available to every logged-in
// user (see GET /api/users/search). Unlike AdminUserRestClient, this never
// carries email or role — the endpoint itself doesn't return them.
@Dependent
public class UserSearchRestClient {

    private static final String USER_SEARCH_PATH = "/users/search";

    private static final Logger LOG = Logger.getLogger(UserSearchRestClient.class.getName());

    @Inject
    private APIClient apiClient;

    public Optional<List<UserSearchResponse>> searchUsers(String searchTerm) {
        if (searchTerm == null || searchTerm.isBlank()) {
            return Optional.of(List.of());
        }

        String encodedSearchTerm = URLEncoder.encode(searchTerm.trim(), StandardCharsets.UTF_8);
        String path = USER_SEARCH_PATH + "?searchTerm=" + encodedSearchTerm;

        Optional<UserSearchResponse[]> response = apiClient.get(path, UserSearchResponse[].class);
        if (response.isEmpty()) {
            LOG.log(Level.WARNING, "Unable to search users");
        }
        return response.map(users -> new ArrayList<>(Arrays.asList(users)));
    }
}
