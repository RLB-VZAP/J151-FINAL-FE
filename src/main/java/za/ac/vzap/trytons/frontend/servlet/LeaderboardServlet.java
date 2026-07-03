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
        String submit =  request.getParameter("submit");
        if (submit == null) {
            submit = "";
        }
        String destination = switch (submit){
            case "getLeaderboardForLeague" -> {
                Optional<UUID> leagueId = parseUuid(request.getParameter("leagueId"));
                if (leagueId.isEmpty()) {
                    request.setAttribute("error", "Invalid league id");
                    yield "leagues.jsp";
                }
                Optional<LeaderboardEntryResponse>  result = leaderboardRestClient.getLeaderboardForLeague(leagueId.get());
                if (result.isPresent()) {
                    request.setAttribute("leaderboard", result.get());
                    yield "leaderboard.jsp";
                }
                request.setAttribute("error", "No leaderboard found");
                yield "leaderboard.jsp";
            }
            case "getRankingForTeam" -> {
                Optional<UUID> teamId = parseUuid(request.getParameter("teamId"));
                if (teamId.isEmpty()) {
                    request.setAttribute("error", "Invalid team id");
                    yield "leaderboard.jsp";
                }
                Optional<LeaderboardEntryResponse> result = leaderboardRestClient.getRankingForTeam(teamId.get());
                if (result.isPresent()) {
                    request.setAttribute("leaderboard", result.get());
                    yield "leaderboard.jsp";
                }
                request.setAttribute("error", "No ranking found");
                yield "leaderboard.jsp";
            }
            default -> "index.jsp";
        };
        request.getRequestDispatcher(destination).forward(request, response);
    }

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

    private int parseInt(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private boolean parseCheckbox(String value) {
        return "on".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value);
    }
}
