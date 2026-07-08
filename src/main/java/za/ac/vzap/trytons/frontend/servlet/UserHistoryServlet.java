package za.ac.vzap.trytons.frontend.servlet;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServlet;
import za.ac.vzap.trytons.frontend.client.UserHistoryRestClient;

public class UserHistoryServlet extends HttpServlet {

    @Inject
    private UserHistoryRestClient userHistoryRestClient;
}