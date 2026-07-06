package za.ac.vzap.trytons.frontend.client;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.util.logging.Logger;

@Dependent
public class LeaderboardRestClient {
    private static final Logger LOG = Logger.getLogger(ClubRestClient.class.getName());

    @Inject
    private APIClient apiClient;
}
