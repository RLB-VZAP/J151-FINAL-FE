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
        // TODO [W4-FE-FIXES-17]: fixtureId/status concatenated into URL unencoded — encode per
        //   PlayerRestClient.listPlayers pattern (see W4-CR-FE-08)
        String path = UPDATE_STATUS + "/" + fixtureId + "/status?status=" + status;
        // TODO [W4-FE-FIXES-10]: null body passed to put() — APIClient.jsonEntity turns null into an
        //   EmptyRequestBody serialized as {} with Content-Type application/json, so the backend sees
        //   an all-null DTO instead of an absent body (see W4-CR-FE-11)
        Optional<FixtureResponse> response = apiClient.put(path,null,FixtureResponse.class);
        if(response.isEmpty()){
            LOG.log(Level.SEVERE, "Unable to update fixture");
        }
        return response;
    }

    }

