package za.ac.vzap.trytons.frontend.client;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import za.ac.vzap.trytons.frontend.util.APIConfig;
import za.ac.vzap.trytons.frontend.util.SessionAuthContext;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Dependent
public class APIClient {
    private static final Logger LOG = Logger.getLogger(APIClient.class.getName());

    @Inject
    private SessionAuthContext authContext;

    public<T> Optional<T> handle(Response response, Class<T> responseType) {
        int status = response.getStatus();
        if(status >= 200 && status < 300) {
            if(responseType == Void.class || status == Response.Status.NO_CONTENT.getStatusCode()) {

                return Optional.empty();
            }
            return Optional.ofNullable(response.readEntity(responseType));
        }
        LOG.log(Level.WARNING, "Backend returned status: {0}", status);
        return Optional.empty();
    }

    public <T> Optional<T> post(String path, Object body, Class<T> responseType) {
        Client client = ClientBuilder.newClient();
        try{
            WebTarget target = client.target(APIConfig.getBaseUrl() + path);
            try(Response response = request(target).post(Entity.json(body))){
                return handle(response, responseType);
            }

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
            try(Response response = request(target).get();) {
                return handle(response, responseType);
            }
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
            try(Response response = request(target).put(Entity.json(body))){
                return handle(response, responseType);
            }
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
            try(Response response = request(target).delete()){
                return handle(response, responseType);
            }
        }catch(ProcessingException e){
            LOG.log(Level.SEVERE,"DELETE " + path + " failed", e);
            return Optional.empty();
        }finally {
            client.close();
        }
    }

    //Added method that accepts a GenericType (overloads the existing handle() method).
    public <T>Optional<T> handleList(Response response, GenericType<T> responseGenericType) {
        int status = response.getStatus();
        if(status >= 200 && status < 300) {
            return Optional.of(response.readEntity(responseGenericType));
        }
        LOG.log(Level.WARNING, "Backend returned status: {0}", status);
        return Optional.empty();
    }

    //Added method that accepts a GenericType (overloads the existing get() method).
    public <T>Optional<T> getList(String path, GenericType<T> responseGenericType) {
        Client client = ClientBuilder.newClient();
        try{
            WebTarget target = client.target(APIConfig.getBaseUrl() + path);
            try(Response response = request(target).get()){
                return handleList(response, responseGenericType);
            }
        }catch(ProcessingException e){
            LOG.log(Level.SEVERE,"GET " + path + " failed", e);
            return Optional.empty();
        }finally {
            client.close();
        }
    }
    private Invocation.Builder request(WebTarget target) {
        Invocation.Builder builder = target.request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE);
        if(authContext != null && authContext.isAuthenticated()) {
            builder.header(HttpHeaders.AUTHORIZATION, "Bearer " + authContext.getToken());
        }
        return builder;
    }
}
