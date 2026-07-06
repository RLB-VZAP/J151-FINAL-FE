package za.ac.vzap.trytons.frontend.servlet;

import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import za.ac.vzap.trytons.frontend.client.AdminMatchResultRestClient;
import za.ac.vzap.trytons.frontend.client.AuthRestClient;

@WebServlet(name ="")
public class AdminMatchResultServlet {
    @Inject
    private AdminMatchResultRestClient adminMatchResultRestClient;
}
