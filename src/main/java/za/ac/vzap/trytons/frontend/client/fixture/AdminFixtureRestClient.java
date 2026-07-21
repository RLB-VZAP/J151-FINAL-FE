package za.ac.vzap.trytons.frontend.client.fixture;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import za.ac.vzap.trytons.frontend.client.shared.APIClient;

@Dependent
public class AdminFixtureRestClient {
    private String UPDATE_STATUS = "/fixtures";
    private String CREATE_FIXTURE = "/fixtures";
    private static final Logger LOG = Logger.getLogger(AdminFixtureRestClient.class.getName());
    @Inject
    private APIClient apiClient ;
    @Inject
    private FixtureRestClient fixtureRestClient;

    public Optional<FixtureResponse> createFixture(FixtureRequest request){
        Optional<FixtureResponse> response = apiClient.post(CREATE_FIXTURE,request,FixtureResponse.class);
        if(response.isEmpty()){
            LOG.log(Level.SEVERE, "Unable to create fixture");
        }
        return response;
    }
    public Optional<FixtureResponse> updateFixtureStatus(String fixtureId, String status){
        String path = UPDATE_STATUS;
        if(status != null && !status.isBlank()) {
            path += "?status=" + encode(status.trim());
        }
        if (fixtureId != null && !status.isBlank()){
            path += "?fixtureId=" + encode(fixtureId.trim());
        }
        Optional<FixtureResponse> response = apiClient.put(path, status, FixtureResponse.class);
        if(response.isEmpty()){
            LOG.log(Level.SEVERE, "Unable to update fixture");
        }
        return response;
    }

    public String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    }

