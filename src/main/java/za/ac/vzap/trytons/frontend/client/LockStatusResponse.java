package za.ac.vzap.trytons.frontend.client;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LockStatusResponse {
    private String roundId;
    private String roundStatus;
    private String transferWindowStatus;

    private Boolean locked;
    private Boolean deadlinePassed;
    private Boolean transfersAllowed;

    private String lockDeadline;
    private String deadlineAt;
    private String lockedAt;
    private String unlockAt;

    private String message;
}