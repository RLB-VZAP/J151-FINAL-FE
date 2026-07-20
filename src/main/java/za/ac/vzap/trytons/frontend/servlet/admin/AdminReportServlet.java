package za.ac.vzap.trytons.frontend.servlet.admin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.admin.AdminReportRestClient;
import za.ac.vzap.trytons.frontend.client.admin.SystemReportRequest;
import za.ac.vzap.trytons.frontend.client.admin.SystemReportResponse;
import za.ac.vzap.trytons.frontend.servlet.shared.AbstractServlet;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@WebServlet(name = "AdminReportServlet", urlPatterns = {"/admin/reports"})
public class AdminReportServlet extends AbstractServlet {

    private static final String VIEW = "/pages/admin-reports.jsp";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Inject
    private AdminReportRestClient adminReportRestClient;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if(!requireAdmin(request,response)) {
            return;
        }
        loadReports(request);
        forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if(!requireAdmin(request,response)) {
            return;
        }
        String reportType = request.getParameter("reportType");
        String reportTitle = request.getParameter("reportTitle");
        String parametersJson = request.getParameter("parametersJson");
        if(reportType == null || reportType.isBlank() || reportTitle == null || reportTitle.isBlank()) {
            request.setAttribute("error","Report type and title are required");
            loadReports(request);
            forward(request, response);
            return;
        }
        SystemReportRequest reportRequest = new SystemReportRequest();
        reportRequest.setReportType(reportType.trim());
        reportRequest.setReportTitle(reportTitle.trim());
        if(parametersJson != null && !parametersJson.isBlank()) {
            try{
                Map<String, Object> parameters = OBJECT_MAPPER.readValue(parametersJson, new TypeReference<Map<String, Object>>(){});
                reportRequest.setParametersJson(parameters);
            }catch(IOException e){
                request.setAttribute("error","Invalid JSON format");
                loadReports(request);
                forward(request, response);
                return;
            }
        }else{
            reportRequest.setParametersJson(Map.of());
        }

        Optional<SystemReportResponse> generatedReport = adminReportRestClient.generateReport(reportRequest);
        if(generatedReport.isPresent()) {
            SystemReportResponse report = generatedReport.get();
            request.setAttribute("success","System report '" + report.getReportTitle() + "' generated successfully");
            request.setAttribute("newReport", report);
        }else{
            request.setAttribute("error","Unable to generate System Report");
        }
        loadReports(request);
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

    private void forward(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher(VIEW).forward(request, response);
    }
}
