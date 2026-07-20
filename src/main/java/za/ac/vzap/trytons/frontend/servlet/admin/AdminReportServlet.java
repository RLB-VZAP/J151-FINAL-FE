package za.ac.vzap.trytons.frontend.servlet.admin;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.admin.AdminReportRestClient;
import za.ac.vzap.trytons.frontend.servlet.shared.AbstractServlet;

import java.io.IOException;

@WebServlet(name = "AdminReportServlet", urlPatterns = {"/admin/reports"})
public class AdminReportServlet extends AbstractServlet {

    private static final String VIEW = "/pages/admin-reports.jsp";

    @Inject
    private AdminReportRestClient adminReportRestClient;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO: Require an admin user, load the list of generated system reports via AdminReportRestClient, and forward to the admin reports view.
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO: Require an admin user, read the submitted report-generation form data, call AdminReportRestClient to generate the report, and forward back to the admin reports view with a success or error message.
    }
}
