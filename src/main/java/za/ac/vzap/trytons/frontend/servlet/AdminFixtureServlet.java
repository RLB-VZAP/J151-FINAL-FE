package za.ac.vzap.trytons.frontend.servlet;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.AdminFixtureRestClient;

import java.io.IOException;

@WebServlet(name = "AdminFixtureServlet", urlPatterns = "/admin/fixtures")
public class AdminFixtureServlet extends HttpServlet {

    @Inject
    private AdminFixtureRestClient adminFixtureRestClient;

    // TODO [W4-FE-FIXES-01]: admin doGet forwards with no SessionAuthContext.isAuthenticated()/role gate
    //   gate /admin/* on an authenticated admin before forwarding (see W4-CR-FE-05)
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/pages/admin-fixtures.jsp").forward(request, response);
    }
}
