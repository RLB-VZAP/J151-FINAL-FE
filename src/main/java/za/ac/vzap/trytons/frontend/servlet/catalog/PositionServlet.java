package za.ac.vzap.trytons.frontend.servlet.catalog;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.catalog.PositionRestClient;
import za.ac.vzap.trytons.frontend.servlet.shared.AbstractServlet;

import java.io.IOException;

@WebServlet(name = "PositionServlet", urlPatterns = {"/admin/positions"})
public class PositionServlet extends AbstractServlet {

    private static final String VIEW = "/pages/admin-positions.jsp";

    @Inject
    private PositionRestClient positionRestClient;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO: Require an admin user, load the full list of positions via PositionRestClient, and forward to the admin positions view.
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO: Require an admin user, read the submitted create/update position form data, call PositionRestClient, and forward back to the admin positions view with a success or error message.
    }
}
