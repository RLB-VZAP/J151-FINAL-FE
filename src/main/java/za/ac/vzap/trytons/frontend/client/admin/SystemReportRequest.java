package za.ac.vzap.trytons.frontend.client.admin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SystemReportRequest {
    private String reportType;
    private String reportTitle;
    private String parametersJson;
}
