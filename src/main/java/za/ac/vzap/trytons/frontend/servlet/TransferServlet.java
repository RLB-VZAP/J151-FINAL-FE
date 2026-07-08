package za.ac.vzap.trytons.frontend.servlet;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServlet;
import za.ac.vzap.trytons.frontend.client.TransferRestClient;

public class TransferServlet extends HttpServlet {

    @Inject
    private TransferRestClient transferRestClient;
}