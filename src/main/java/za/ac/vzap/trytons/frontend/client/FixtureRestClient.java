package za.ac.vzap.trytons.frontend.client;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Dependent
public class FixtureRestClient {
    // TODO [W4-FE-FIXES-16]: LIST_FIXTURE/GET_FIXTURE are non-static, non-final instance fields
    //   holding the same literal "/fixtures" — every other client uses static final (see W4-CR-FE-17)
    private String LIST_FIXTURE ="/fixtures";
    private String GET_FIXTURE = "/fixtures";
   private static final Logger LOG = Logger.getLogger(FixtureRestClient.class.getName());

    @Inject
    private APIClient apiClient;
    public Optional<List<FixtureResponse>> listFixtures(String statusFilter) {
        StringBuilder path = new StringBuilder(LIST_FIXTURE);
        if(statusFilter != null && !statusFilter.isEmpty()) {
            // TODO [W4-FE-FIXES-17]: query value concatenated into the URL with no encoding — a space/&/=
            //   makes ClientBuilder.target() throw IllegalArgumentException, uncaught by the
            //   ProcessingException-only catch; follow PlayerRestClient.listPlayers' encoding pattern
            //   (see W4-CR-FE-08)
            path.append("?status=").append(statusFilter);
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
