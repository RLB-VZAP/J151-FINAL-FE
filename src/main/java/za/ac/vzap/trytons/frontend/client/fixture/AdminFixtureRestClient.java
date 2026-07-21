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

    public Optional<FixtureResponse> createFixture(FixtureRequest request){
        Optional<FixtureResponse> response = apiClient.post(CREATE_FIXTURE,request,FixtureResponse.class);
        if(response.isEmpty()){
            LOG.log(Level.SEVERE, "Unable to create fixture");
        }
        return response;
    }
    public Optional<FixtureResponse> updateFixtureStatus(String fixtureId, String status){
        String path = UPDATE_STATUS + "/" + encode(fixtureId) + "/status?status=" + encode(status);
        // TODO [W4-FE-FIXES-10]: null body passed to put() — APIClient.jsonEntity turns null into an
        //   EmptyRequestBody serialized as {} with Content-Type application/json, so the backend sees
        //   an all-null DTO instead of an absent body (see W4-CR-FE-11). Harmless here since the backend
        //   updateFixtureStatus service method reads only the "status" query param, not the request body,
        //   but there's no null-body-safe put() overload on APIClient today to avoid it cleanly.
        Optional<FixtureResponse> response = apiClient.put(path,null,FixtureResponse.class);
        if(response.isEmpty()){
            LOG.log(Level.SEVERE, "Unable to update fixture");
        }
        return response;
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    }

