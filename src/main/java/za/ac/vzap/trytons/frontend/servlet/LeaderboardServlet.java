package za.ac.vzap.trytons.frontend.servlet;

import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import za.ac.vzap.trytons.frontend.client.ClubRestClient;

@WebServlet(name ="", urlPatterns = {""} )
public class LeaderboardServlet extends HttpServlet {
    @Inject
    private ClubRestClient clubRestClient;
}
