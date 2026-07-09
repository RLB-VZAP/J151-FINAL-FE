package za.ac.vzap.trytons.frontend.servlet;

import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import za.ac.vzap.trytons.frontend.client.LeagueRestClient;

@WebServlet(name = "LeagueServlet", urlPatterns = {"/leagues"})
public class LeagueServlet extends HttpServlet {
    @Inject
    private LeagueRestClient leagueRestClient;
}
