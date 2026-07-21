package za.ac.vzap.trytons.frontend.servlet.admin;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.admin.AdminUserRestClient;
import za.ac.vzap.trytons.frontend.servlet.shared.AbstractServlet;

import java.io.IOException;

@WebServlet(name = "AdminUserServlet", urlPatterns = {"/admin/users"})
public class AdminUserServlet extends AbstractServlet {

    private static final String VIEW = "/pages/admin-users.jsp";

    @Inject
    private AdminUserRestClient adminUserRestClient;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO [W4-FE-FIXES-03]: Require an admin user, read the optional searchTerm query parameter, call AdminUserRestClient.searchUsers, and forward the results to the admin users view.
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO [W4-FE-FIXES-03]: Require an admin user, read the target user id and new active-status flag from the submitted form, call AdminUserRestClient.updateUserStatus, and forward back to the admin users view with a success or error message.
    }
}
