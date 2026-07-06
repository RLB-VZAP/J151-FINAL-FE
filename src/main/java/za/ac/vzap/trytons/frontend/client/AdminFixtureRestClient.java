package za.ac.vzap.trytons.frontend.client;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.util.logging.Logger;

@Dependent
public class AdminFixtureRestClient {
    private static final Logger LOG = Logger.getLogger(AdminFixtureRestClient.class.getName());
    @Inject
    private APIClient apiClient ;
}
