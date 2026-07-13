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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/pages/admin-fixtures.jsp").forward(request, response);
    }
}
