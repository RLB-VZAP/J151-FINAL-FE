package za.ac.vzap.trytons.frontend.client;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Dependent
public class AdminFixtureRestClient {
    private String UPDATE_STATUS = "/fixtures";
    private String CREATE_FIXTURE = "/fixtures";
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
    public Optional<FixtureResponse> updateFixtureStatus(String fixtureId, String status){
        String path = UPDATE_STATUS + "/" + fixtureId + "/status?status=" + status;
        Optional<FixtureResponse> response = apiClient.put(path,null,FixtureResponse.class);
        if(response.isEmpty()){
            LOG.log(Level.SEVERE, "Unable to update fixture");
        }
        return response;
    }

    }

