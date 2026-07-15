package za.ac.vzap.trytons.frontend.client;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiEnvelope<T> {
    private boolean success;
    private String message;
    private T data;
}
