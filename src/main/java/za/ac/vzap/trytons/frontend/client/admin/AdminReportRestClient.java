package za.ac.vzap.trytons.frontend.client.admin;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import za.ac.vzap.trytons.frontend.client.shared.APIClient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Dependent
public class AdminReportRestClient {

    private static final String SYSTEM_REPORTS_PATH = "/system-reports";

    @Inject
    private APIClient apiClient;

    public Optional<SystemReportResponse> generateReport(SystemReportRequest request) {
        // TODO: Call POST /system-reports via the APIClient with the given request body and return the generated report.
    }

    public Optional<List<SystemReportResponse>> getReports() {
        // TODO: Call GET /system-reports via the APIClient and return the list of generated reports.
    }

    public Optional<SystemReportResponse> getReportById(UUID reportId) {
        // TODO: Call GET /system-reports/{reportId} via the APIClient and return the matching report.
    }
}
