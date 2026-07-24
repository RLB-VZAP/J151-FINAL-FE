package za.ac.vzap.trytons.frontend.servlet.admin;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.admin.AdminReportRestClient;
import za.ac.vzap.trytons.frontend.client.admin.LogRestClient;
import za.ac.vzap.trytons.frontend.client.admin.LogResponse;
import za.ac.vzap.trytons.frontend.client.admin.SystemReportRequest;
import za.ac.vzap.trytons.frontend.client.admin.SystemReportResponse;
import za.ac.vzap.trytons.frontend.servlet.shared.AbstractServlet;
import java.io.IOException;
import java.util.*;

@WebServlet(name = "AdminReportServlet", urlPatterns = {"/admin/reports"})
public class AdminReportServlet extends AbstractServlet {

    private static final String VIEW = "/pages/admin-reports.jsp";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Inject
    private AdminReportRestClient adminReportRestClient;

    @Inject
    private LogRestClient logRestClient;

    private static final int LOG_LIMIT = 100;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if(!requireAdmin(request,response)) {
            return;
        }
        String viewId = request.getParameter("view");
        String downloadId = request.getParameter("download");
        if(viewId != null && downloadId != null) {
            streamReport(viewId !=null ? viewId: downloadId,downloadId != null, response);
            return;
        }
        loadReports(request);
        loadLogs(request);
        forward(request, response);
    }
    private void streamReport(String reportId, boolean asAttachment, HttpServletResponse response) throws IOException {
        UUID id;
        try {
            id = UUID.fromString(reportId);
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid report id");
            return;
        }
        Optional<SystemReportResponse> reportOpt = adminReportRestClient.getReportById(id);
        if(reportOpt.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Report not found");
            return;
        }
        String json = OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(reportOpt.get().getResultJson());
        response.setContentType("application/json;charset=UTF-8");
        if(asAttachment) {
            response.setHeader("Content-Disposition", "attachment; filename=\"report-" + id + ".json\"");
        }
        response.getWriter().write(json);
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if(!requireAdmin(request,response)) {
            return;
        }
        String reportType = request.getParameter("reportType");
        String reportTitle = request.getParameter("reportTitle");
        String season = request.getParameter("season");
        String limit = request.getParameter("limit");
        String roundId = request.getParameter("roundId");

        if(reportType == null || reportType.isBlank() || reportTitle == null || reportTitle.isBlank()) {
            request.setAttribute("error","Report type and title are required");
            loadReports(request);
            loadLogs(request);
            forward(request, response);
            return;
        }
        SystemReportRequest reportRequest = new SystemReportRequest();
        reportRequest.setReportType(reportType.trim());
        reportRequest.setReportTitle(reportTitle.trim());

        Map<String, Object> parameters = new LinkedHashMap<>();
        if(season != null && !season.isBlank()) parameters.put("season", season.trim());
        if(limit != null && !limit.isBlank()) parameters.put("limit", limit.trim());
        if(roundId != null && !roundId.isBlank()) parameters.put("roundId", roundId.trim());
        reportRequest.setParametersJson(parameters);

        Optional<SystemReportResponse> generatedReport = adminReportRestClient.generateReport(reportRequest);
        if(generatedReport.isPresent()) {
            response.sendRedirect(request.getContextPath() + "/admin/reports");
            return;
        }
        request.setAttribute("error","Unable to generate System Report");
        loadReports(request);
        loadLogs(request);
        forward(request, response);

    }

    private void loadReports(HttpServletRequest request) {
        Optional<List<SystemReportResponse>> reportsOptional = adminReportRestClient.getReports();
        if(reportsOptional.isPresent()) {
            request.setAttribute("reports", reportsOptional.get());
        }else{
            request.setAttribute("reports", List.of());
            if(request.getAttribute("error") == null) {
                request.setAttribute("error","Unable to load reports");
            }
        }
    }

    private void loadLogs(HttpServletRequest request) {
        Optional<List<LogResponse>> logsOptional = logRestClient.getRecentLogs(LOG_LIMIT);
        if(logsOptional.isPresent()) {
            request.setAttribute("logs", logsOptional.get());
        }else{
            request.setAttribute("logs", List.of());
        }
    }

    private void forward(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher(VIEW).forward(request, response);
    }
}
