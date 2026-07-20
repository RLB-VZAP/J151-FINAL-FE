package za.ac.vzap.trytons.frontend.client.admin;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class SystemReportResponse {
    private UUID reportId;
    private String reportType;
    private String reportTitle;
    private String parametersJson;
    private String resultJson;
    private LocalDateTime generatedAt;
    private UUID generatedByAdminUserId;
}
