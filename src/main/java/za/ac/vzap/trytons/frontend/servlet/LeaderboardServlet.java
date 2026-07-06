package za.ac.vzap.trytons.frontend.servlet;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.LeaderboardEntryResponse;
import za.ac.vzap.trytons.frontend.client.LeaderboardRestClient;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@WebServlet(name = "LeaderboardServlet", urlPatterns = {"/leaderboard"})
public class LeaderboardServlet extends HttpServlet {
    @Inject
    private LeaderboardRestClient leaderboardRestClient;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String destination;

        //Request contains both 'leagueId' and 'teamId':
        if (request.getParameter("leagueId") != null && request.getParameter("teamId") != null){
            request.setAttribute("error", "Invalid request");
            destination = "/leaderboard.jsp";

            //Request contains 'leagueId':
        } else if (request.getParameter("leagueId") != null) {
            Optional<UUID> leagueId = parseUuid(request.getParameter("leagueId"));
            if (leagueId.isEmpty()){
                request.setAttribute("error", "Invalid league ID");
                destination = "/leaderboard.jsp";
            }else {
                Optional<LeaderboardEntryResponse> result = leaderboardRestClient.getLeaderboardForLeague(leagueId.get());
                if (result.isPresent()) {
                    request.setAttribute("leaderboard", result.get());
                    destination = "/leaderboard.jsp";
                } else {
                    request.setAttribute("error", "No leaderboard found");
                    destination = "/leaderboard.jsp";
                }
            }

            //Request contains 'teamId':
        } else if (request.getParameter("teamId") != null) {
            Optional<UUID> teamId = parseUuid(request.getParameter("teamId"));
            if (teamId.isEmpty()){
                request.setAttribute("error", "Invalid team ID");
                destination = "/leaderboard.jsp";
            }else {
                Optional<UUID> leaderboardId = parseUuid(request.getParameter("leaderboardId"));
                if (leaderboardId.isEmpty()) {
                    request.setAttribute("error", "Invalid leaderboard ID");
                    destination = "/leaderboard.jsp";
                }else {
                    Optional<LeaderboardEntryResponse> result = leaderboardRestClient.getRankingForTeam(teamId.get(), leaderboardId.get());
                    if (result.isPresent()) {
                        request.setAttribute("ranking", result.get());
                        destination =  "/leaderboard.jsp";
                    } else {
                        request.setAttribute("error", "No ranking found");
                        destination = "/leaderboard.jsp";
                    }
                }
            }

            //Request contains neither (leaderboard path with no parameters):
        }else {
            request.setAttribute("error", "Invalid request");
            destination = "/leaderboard.jsp";
        }
        request.getRequestDispatcher(destination).forward(request, response);
    }

    //Credit goes to Jaunte Garcia for writing this helper method.
    private Optional<UUID> parseUuid(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(value.trim()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
