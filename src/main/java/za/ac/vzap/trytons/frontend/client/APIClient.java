package za.ac.vzap.trytons.frontend.client;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import za.ac.vzap.trytons.frontend.session.SessionAuthContext;
import za.ac.vzap.trytons.frontend.util.APIConfig;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Dependent
public class APIClient {

    private static final Logger LOG = Logger.getLogger(APIClient.class.getName());

    @Inject
    private SessionAuthContext authContext;

    public <T> Optional<T> post(String path, Object body, Class<T> responseType) {
        Client client = ClientBuilder.newClient();
        try {
            WebTarget target = client.target(APIConfig.getBaseUrl() + path);
            try (Response response = request(target).post(jsonEntity(body))) {
                return handle(response, responseType);
            }
        } catch (ProcessingException e) {
            LOG.log(Level.SEVERE, "POST " + path + " failed", e);
            return Optional.empty();
        } finally {
            client.close();
        }
    }

    public boolean postForStatus(String path, Object body) {
        Client client = ClientBuilder.newClient();
        try {
            WebTarget target = client.target(APIConfig.getBaseUrl() + path);
            try (Response response = request(target).post(jsonEntity(body))) {
                if (isSuccessful(response)) {
                    return true;
                }
                logBackendFailure(path, response);
                return false;
            }
        } catch (ProcessingException e) {
            LOG.log(Level.SEVERE, "POST " + path + " failed", e);
            return false;
        } finally {
            client.close();
        }
    }

    public <T> Optional<T> get(String path, Class<T> responseType) {
        Client client = ClientBuilder.newClient();
        try {
            WebTarget target = client.target(APIConfig.getBaseUrl() + path);
            try (Response response = request(target).get()) {
                return handle(response, responseType);
            }
        } catch (ProcessingException e) {
            LOG.log(Level.SEVERE, "GET " + path + " failed", e);
            return Optional.empty();
        } finally {
            client.close();
        }
    }

    public <T> Optional<T> put(String path, Object body, Class<T> responseType) {
        Client client = ClientBuilder.newClient();
        try {
            WebTarget target = client.target(APIConfig.getBaseUrl() + path);
            try (Response response = request(target).put(jsonEntity(body))) {
                return handle(response, responseType);
            }
        } catch (ProcessingException e) {
            LOG.log(Level.SEVERE, "PUT " + path + " failed", e);
            return Optional.empty();
        } finally {
            client.close();
        }
    }

    public <T> Optional<T> delete(String path, Class<T> responseType) {
        Client client = ClientBuilder.newClient();
        try {
            WebTarget target = client.target(APIConfig.getBaseUrl() + path);
            try (Response response = request(target).delete()) {
                return handle(response, responseType);
            }
        } catch (ProcessingException e) {
            LOG.log(Level.SEVERE, "DELETE " + path + " failed", e);
            return Optional.empty();
        } finally {
            client.close();
        }
    }

    public <T> Optional<T> getList(String path, GenericType<T> responseType) {
        Client client = ClientBuilder.newClient();
        try {
            WebTarget target = client.target(APIConfig.getBaseUrl() + path);
            try (Response response = request(target).get()) {
                if (isSuccessful(response)) {
                    return Optional.ofNullable(response.readEntity(responseType));
                }
                logBackendFailure(path, response);
                return Optional.empty();
            }
        } catch (ProcessingException e) {
            LOG.log(Level.SEVERE, "GET " + path + " failed", e);
            return Optional.empty();
        } finally {
            client.close();
        }
    }

    private Entity<?> jsonEntity(Object body) {
        return Entity.entity(body == null ? new EmptyRequestBody() : body, MediaType.APPLICATION_JSON_TYPE);
    }

    private static final class EmptyRequestBody {
        public EmptyRequestBody() {
        }
    }

    private Invocation.Builder request(WebTarget target) {
        Invocation.Builder builder = target.request(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE);

        if (authContext != null && authContext.isAuthenticated()) {
            builder.header(HttpHeaders.AUTHORIZATION, "Bearer " + authContext.getToken());
        }
        return builder;
    }

    private <T> Optional<T> handle(Response response, Class<T> responseType) {
        if (!isSuccessful(response)) {
            logBackendFailure(response.getLocation() == null ? "backend request" : response.getLocation().toString(), response);
            return Optional.empty();
        }

        if (responseType == Void.class || response.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
            return Optional.empty();
        }
        return Optional.ofNullable(response.readEntity(responseType));
    }

    private boolean isSuccessful(Response response) {
        return response.getStatus() >= 200 && response.getStatus() < 300;
    }

    private void logBackendFailure(String path, Response response) {
        String body = "";
        try {
            if (response.hasEntity()) {
                body = response.readEntity(String.class);
            }
        } catch (RuntimeException ignored) {
            // Logging must not hide the original backend status.
        }
        LOG.log(Level.WARNING, "Backend request {0} returned HTTP {1}. {2}",
                new Object[]{path, response.getStatus(), body});
    }
}
