package za.ac.vzap.trytons.frontend.client;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Dependent
public class FixtureRestClient {
    private String LIST_FIXTURE ="/fixtures";
    private String GET_FIXTURE = "/fixture";
   private static final Logger LOG = Logger.getLogger(FixtureRestClient.class.getName());

    @Inject
    private APIClient apiClient;
    public Optional<List<FixtureResponse>> listFixtures(String stausFilter) {
        StringBuilder path = new StringBuilder(LIST_FIXTURE);
        if(stausFilter != null && !stausFilter.isEmpty()) {
            path.append("?staus=").append(stausFilter);
        }
        Optional<FixtureResponse[]> response = apiClient.get(path.toString(), FixtureResponse[].class);
        if(response.isEmpty()){
            LOG.log(Level.SEVERE, "Unable list fixtures");
        }
        return response.map(fixtures -> new ArrayList<>(Arrays.asList(fixtures)));
    }

    public Optional<FixtureResponse> getFixture(String fixtureId) {
        String path = GET_FIXTURE + "/" + fixtureId;
        Optional<FixtureResponse> response = apiClient.get(path,FixtureResponse.class);
        if(response.isEmpty()){
            LOG.log(Level.SEVERE, "Unable find fixture");
        }
        return response;
    }
}
