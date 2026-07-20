package za.ac.vzap.trytons.frontend.servlet.simulation;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.simulation.ResimulationRestClient;
import za.ac.vzap.trytons.frontend.client.simulation.SimulationSettingRestClient;
import za.ac.vzap.trytons.frontend.servlet.shared.AbstractServlet;

import java.io.IOException;

@WebServlet(name = "AdminSimulationServlet", urlPatterns = {"/admin/simulation"})
public class AdminSimulationServlet extends AbstractServlet {

    private static final String VIEW = "/pages/admin-simulation.jsp";

    @Inject
    private SimulationSettingRestClient simulationSettingRestClient;

    @Inject
    private ResimulationRestClient resimulationRestClient;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO: Require an admin user, load the active simulation settings via SimulationSettingRestClient, and forward to the admin simulation view.
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO: Require an admin user, read the submitted settings-update or resimulation-request form data, call SimulationSettingRestClient or ResimulationRestClient accordingly, and forward back to the admin simulation view with a success or error message.
    }
}
