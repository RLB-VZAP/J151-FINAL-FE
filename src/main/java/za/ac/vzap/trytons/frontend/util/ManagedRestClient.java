package za.ac.vzap.trytons.frontend.util;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.jackson.JacksonFeature;

/**
 * Owns a single, shared JAX-RS {@link Client}. A Client is thread-safe and
 * expensive to build (its own connection pool and thread infrastructure), so
 * it must be created once per application lifecycle rather than per request.
 */
@ApplicationScoped
public class ManagedRestClient {

    private static final int CONNECT_TIMEOUT_MILLIS = 5000;
    private static final int READ_TIMEOUT_MILLIS = 15000;

    private Client client;

    @PostConstruct
    public void init() {
        client = ClientBuilder.newClient()
                .register(JacksonFeature.class)
                .register(ObjectMapperProvider.class);
        client.property(ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT_MILLIS);
        client.property(ClientProperties.READ_TIMEOUT, READ_TIMEOUT_MILLIS);
    }

    @PreDestroy
    public void destroy() {
        if (client != null) {
            client.close();
        }
    }

    public Client getClient() {
        return client;
    }
}
