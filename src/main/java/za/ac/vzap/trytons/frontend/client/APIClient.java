package za.ac.vzap.trytons.frontend.client;
import jakarta.enterprise.context.Dependent;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import za.ac.vzap.trytons.frontend.util.APIConfig;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Dependent
public class APIClient {
    private static final Logger LOG = Logger.getLogger(APIClient.class.getName());

    public<T> Optional<T> handle(Response response, Class<T> ResponseType) {
        int status = response.getStatus();
        if(status >= 200 && status < 300) {
            if(ResponseType == Void.class) {
                return Optional.empty();
            }
            return Optional.of(response.readEntity(ResponseType));
        }
        LOG.log(Level.WARNING, "Backend returned status: {0}", status);
        return Optional.empty();
    }

    public <T> Optional<T> post(String path, Object body, Class<T> responseType) {
        Client client = ClientBuilder.newClient();
        try{
            WebTarget target = client.target(APIConfig.getBaseUrl() + path);
            Response response = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.json(body));
            return handle(response, responseType);
        }catch(Exception e){
            LOG.log(Level.SEVERE,"POST " + path + " failed", e);
            return Optional.empty();
        }finally {
            client.close();
        }
    }
    public <T>Optional<T> get(String path, Class<T> responseType) {
        Client client = ClientBuilder.newClient();
        try{
            WebTarget target = client.target(APIConfig.getBaseUrl() + path);
            Response response = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .get();
            return handle(response, responseType);
        }catch(ProcessingException e){
            LOG.log(Level.SEVERE,"GET " + path + " failed", e);
            return Optional.empty();
        }finally {
            client.close();
        }
    }

    public<T> Optional<T> put(String path, Object body, Class<T> responseType) {
        Client client = ClientBuilder.newClient();
        try{
            WebTarget target = client.target(APIConfig.getBaseUrl() + path);
            Response response = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.json(body));
            return handle(response, responseType);
        }catch(ProcessingException e){
            LOG.log(Level.SEVERE,"PUT " + path + " failed", e);
            return Optional.empty();
        }finally {
            client.close();
        }
    }

    public<T> Optional<T> delete(String path, Class<T> responseType) {
        Client client = ClientBuilder.newClient();
        try{
            WebTarget target = client.target(APIConfig.getBaseUrl() + path);
            Response response = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .delete();
            return handle(response, responseType);
        }catch(ProcessingException e){
            LOG.log(Level.SEVERE,"DELETE " + path + " failed", e);
            return Optional.empty();
        }finally {
            client.close();
        }
    }

}
