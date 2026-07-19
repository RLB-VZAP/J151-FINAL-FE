package za.ac.vzap.trytons.frontend.servlet;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.LeaderboardEntryResponse;
import za.ac.vzap.trytons.frontend.client.LeaderboardRestClient;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@WebServlet(name = "LeaderboardServlet", urlPatterns = {"/leaderboard"})
public class LeaderboardServlet extends AbstractServlet {
    @Inject
    private LeaderboardRestClient leaderboardRestClient;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if(!requireAuthenticated(request,response)) {
            return;
        }

        //Request contains both 'leagueId' and 'teamId':
        if (request.getParameter("leagueId") != null && request.getParameter("teamId") != null){
            request.setAttribute("error", "Invalid request");

            //Request contains 'leagueId':
        } else if (request.getParameter("leagueId") != null) {
            Optional<UUID> leagueId = parseUuid(request.getParameter("leagueId"));
            if (leagueId.isEmpty()){
                request.setAttribute("error", "Invalid league ID");
            }else {
                Optional<List<LeaderboardEntryResponse>> result = leaderboardRestClient.getLeaderboardForLeague(leagueId.get());
                if (result.isPresent()) {
                    request.setAttribute("leaderboard", result.get());
                } else {
                    request.setAttribute("error", "No leaderboard found");
                }
            }
            //Request contains 'teamId':
        } else if (request.getParameter("teamId") != null) {
            Optional<UUID> teamId = parseUuid(request.getParameter("teamId"));
            if (teamId.isEmpty()){
                request.setAttribute("error", "Invalid team ID");
            }else {
                Optional<UUID> leaderboardId = parseUuid(request.getParameter("leaderboardId"));
                if (leaderboardId.isEmpty()) {
                    request.setAttribute("error", "Invalid leaderboard ID");
                }else {
                    Optional<LeaderboardEntryResponse> result = leaderboardRestClient.getRankingForTeam(teamId.get(), leaderboardId.get());
                    if (result.isPresent()) {
                        request.setAttribute("ranking", result.get());
                    } else {
                        request.setAttribute("error", "No ranking found");
                    }
                }
            }
            //Request contains neither (leaderboard path with no parameters):
        }else {
            request.setAttribute("error", "Invalid request");

        }
        request.getRequestDispatcher("/pages/leaderboard.jsp").forward(request, response);
    }

}
