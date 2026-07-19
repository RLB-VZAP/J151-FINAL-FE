package za.ac.vzap.trytons.frontend.servlet;

import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import za.ac.vzap.trytons.frontend.client.LeagueRestClient;

@WebServlet(name = "LeagueServlet", urlPatterns = {"/leagues"})
public class LeagueServlet extends HttpServlet {
    // TODO [W4-FE-FIXES-08]: unimplemented skeleton — implement League browsing per W3 flow
    //   @WebServlet("/leagues") maps this servlet but it has no doGet/doPost (injected
    //   LeagueRestClient only)
    @Inject
    private LeagueRestClient leagueRestClient;
}
