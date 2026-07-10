package za.ac.vzap.trytons.frontend.client;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Dependent
public class AdminFixtureRestClient {
    private String UPDATE_FIXTURE = "/fixture";
    private String LIST_FIXTURE = "/fixtures";
    private String CREATE_FIXTURE = "/fixture";
    private static final Logger LOG = Logger.getLogger(AdminFixtureRestClient.class.getName());
    @Inject
    private APIClient apiClient ;

    public Optional<FixtureResponse> createFixture(FixtureRequest request){
        Optional<FixtureResponse> response = apiClient.post(CREATE_FIXTURE,request,FixtureResponse.class);
        if(response.isEmpty()){
            LOG.log(Level.SEVERE, "Unable to create fixture");
        }
        return response;
    }
    public Optional<FixtureResponse> updateFixture(String fixtureId, FixtureRequest request){
        String path = UPDATE_FIXTURE + "/" + fixtureId;
        Optional<FixtureResponse> response = apiClient.post(UPDATE_FIXTURE,request,FixtureResponse.class);
        if(response.isEmpty()){
            LOG.log(Level.SEVERE, "Unable to update fixture");
        }
        return response;
    }
    public Optional<List<FixtureResponse>> listFixtures(String statusFilter){
        StringBuilder path = new StringBuilder(LIST_FIXTURE);
        if(statusFilter != null && !statusFilter.isEmpty()) {
            path.append("?status=").append(statusFilter);
        }
        Optional<FixtureResponse[]> response = apiClient.get(path.toString(), FixtureResponse[].class);
        if(response.isEmpty()){
            LOG.log(Level.SEVERE, "Unable list fixtures");
        }
        return response.map(fixtures -> new ArrayList<>(Arrays.asList(fixtures)));

    }
}
