package za.ac.vzap.trytons.frontend.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.ws.rs.ext.ContextResolver;

public class ObjectMapperProvider implements ContextResolver<ObjectMapper> {
    private final ObjectMapper mapper;
    public ObjectMapperProvider() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // The two WARs deploy independently, so the backend routinely ships a DTO
        // field before the matching client bean exists. Jackson's default is to
        // throw on an unknown property, and APIClient converts any throw into an
        // empty Optional -- which every servlet renders as a friendly "no data"
        // panel. A single added backend field therefore blanked a whole page with
        // no error anywhere: adding stageLabel to FixtureResponseDTO silently
        // emptied /fixtures. Only a minority of the client beans carry
        // @JsonIgnoreProperties, so tolerate unknown fields globally instead of
        // relying on 50-odd annotations staying in place.
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }
}
