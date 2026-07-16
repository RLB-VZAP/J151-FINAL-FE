package za.ac.vzap.trytons.frontend.client;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LockStatusResponse {
    // TODO [DTO-ALIGNMENT]: Align with backend LockStatusResponseDTO. This class expects
    //   transfer-window/deadline timestamp fields, while the backend sends lockedPlayerIds,
    //   lockedTeamIds, and snapshotsCreated; callers therefore receive incomplete lock data.
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
