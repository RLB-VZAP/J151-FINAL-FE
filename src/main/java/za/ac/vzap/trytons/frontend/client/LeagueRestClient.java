package za.ac.vzap.trytons.frontend.client;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.util.logging.Logger;

@Dependent
public class LeagueRestClient {
    // TODO [W4-FE-FIXES-21]: unimplemented skeleton — implement League browsing client per W3 flow
    //   Logger + injected APIClient only, zero methods
    // TODO [W4-FE-FIXES-20]: logger bound to the wrong class — Logger.getLogger(AuthRestClient.class)
    //   in LeagueRestClient; once implemented its log lines will be misattributed to AuthRestClient
    private static final Logger LOG = Logger.getLogger(AuthRestClient.class.getName());
    //STUB
    @Inject
    private APIClient apiClient;
}
