package za.ac.vzap.trytons.frontend.client;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.util.logging.Logger;

@Dependent
public class LeagueRestClient {
    private static final Logger LOG = Logger.getLogger(AuthRestClient.class.getName());
    //STUB
    @Inject
    private APIClient apiClient;
}
