package za.ac.vzap.trytons.frontend.servlet;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.AdminMatchResultRestClient;
import za.ac.vzap.trytons.frontend.client.FixtureRestClient;
import za.ac.vzap.trytons.frontend.client.FixtureResponse;
import za.ac.vzap.trytons.frontend.client.MatchResultRequest;
import za.ac.vzap.trytons.frontend.client.MatchResultResponse;
import za.ac.vzap.trytons.frontend.client.PlayerStatisticsRequest;
import za.ac.vzap.trytons.frontend.client.PlayerStatisticsResponse;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@WebServlet(name = "AdminMatchResultServlet", urlPatterns = {"/admin/match-results"})
public class AdminMatchResultServlet extends HttpServlet {

    private static final String VIEW = "/pages/admin-match-results.jsp";

    @Inject
    private AdminMatchResultRestClient adminMatchResultRestClient;

    @Inject
    private FixtureRestClient fixtureRestClient;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        loadPage(request);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    // TODO [W4-FE-FIXES-01]: admin action runs with no SessionAuthContext.isAuthenticated()/role gate
    //   gate all /admin/* servlet entry points on an authenticated admin before doing work;
    //   backend @Authenticated rejects it but the frontend must not reach the call unguarded
    //   (see W4-CR-FE-05)
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) {
            action = "";
        }

        switch (action) {
            case "matchResult", "" -> submitMatchResult(request);
            case "playerStatistics" -> submitPlayerStatistics(request);
            default -> request.setAttribute("error", "Unknown capture action requested");
        }

        loadPage(request);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private void submitMatchResult(HttpServletRequest request) {
        String fixtureId = request.getParameter("fixtureId");
        UUID fixtureUuid = parseUuid(fixtureId);

        String actorId = request.getParameter("actorId");
        UUID actorUuid = parseUuid(actorId);

        if (fixtureUuid == null) {
            request.setAttribute("error", "Please select a valid fixture before capturing a result");
            return;
        }

        // TODO [W4-FE-FIXES-02]: invalid actorId is silently accepted — sets "error" but falls through
        //   unlike the fixtureUuid guard above (which returns), this branch continues and submits the
        //   match result with actorUuid=null; a later success attribute overwrites the error message
        //   so the user never learns the actor was dropped — return/short-circuit here
        if (actorUuid == null) {
            request.setAttribute("error", "Actor Id can't be null");
        }

        int teamAScore = parseNonNegativeInt(request.getParameter("teamAScore"));
        int teamBScore = parseNonNegativeInt(request.getParameter("teamBScore"));

        if (teamAScore < 0 || teamBScore < 0) {
            request.setAttribute("error", "Team scores are required and cannot be negative");
            return;
        }

        MatchResultRequest matchResultRequest = new MatchResultRequest();
        matchResultRequest.setFixtureId(fixtureUuid);
        matchResultRequest.setActorId(actorUuid);
        matchResultRequest.setTeamAScore(teamAScore);
        matchResultRequest.setTeamBScore(teamBScore);
        matchResultRequest.setSimulationReason(request.getParameter("simulationReason"));

        Optional<MatchResultResponse> result = adminMatchResultRestClient.submitMatchResult(fixtureId, matchResultRequest);

        if (result.isPresent()) {
            request.setAttribute("success", "Match result captured successfully");
            request.setAttribute("matchResult", result.get());
        } else {
            request.setAttribute("error", "Match result could not be captured");
        }
    }

    private void submitPlayerStatistics(HttpServletRequest request) {
        String fixtureId = request.getParameter("fixtureId");
        UUID fixtureUuid = parseUuid(fixtureId);
        UUID resultId = parseUuid(request.getParameter("resultId"));
        UUID teamId = parseUuid(request.getParameter("teamId"));
        UUID playerId = parseUuid(request.getParameter("playerId"));

        if (fixtureUuid == null || teamId == null || playerId == null) {
            request.setAttribute("error", "A valid fixture, team, and player are required to capture statistics");
            return;
        }

        int[] counts = {
            parseNonNegativeInt(request.getParameter("tries")),
            parseNonNegativeInt(request.getParameter("assists")),
            parseNonNegativeInt(request.getParameter("tackles")),
            parseNonNegativeInt(request.getParameter("missedTackles")),
            parseNonNegativeInt(request.getParameter("conversions")),
            parseNonNegativeInt(request.getParameter("penalties")),
            parseNonNegativeInt(request.getParameter("metersGained")),
            parseNonNegativeInt(request.getParameter("yellowCards")),
            parseNonNegativeInt(request.getParameter("redCards"))
        };

        for (int count : counts) {
            if (count < 0) {
                request.setAttribute("error", "Statistic counts cannot be negative");
                return;
            }
        }

        PlayerStatisticsRequest statisticsRequest = new PlayerStatisticsRequest();
        statisticsRequest.setResultId(resultId);
        statisticsRequest.setTeamId(teamId);
        statisticsRequest.setPlayerId(playerId);
        statisticsRequest.setTries(counts[0]);
        statisticsRequest.setAssists(counts[1]);
        statisticsRequest.setTackles(counts[2]);
        statisticsRequest.setMissedTackles(counts[3]);
        statisticsRequest.setConversions(counts[4]);
        statisticsRequest.setPenalties(counts[5]);
        statisticsRequest.setMetersGained(counts[6]);
        statisticsRequest.setYellowCards(counts[7]);
        statisticsRequest.setRedCards(counts[8]);

        Optional<PlayerStatisticsResponse> statistics =
                adminMatchResultRestClient.submitPlayerStatistics(fixtureId, statisticsRequest);

        if (statistics.isPresent()) {
            request.setAttribute("success", "Player statistics captured successfully");
            request.setAttribute("playerStatistics", statistics.get());
        } else {
            request.setAttribute("error", "Player statistics could not be captured");
        }
    }

    private void loadPage(HttpServletRequest request) {
        Optional<List<FixtureResponse>> fixtures = fixtureRestClient.listFixtures(null);

        if (fixtures.isPresent()) {
            request.setAttribute("fixtures", fixtures.get());
        } else {
            request.setAttribute("fixturesError", "Unable to load fixtures for capture");
            request.setAttribute("fixtures", List.of());
        }

        String fixtureId = request.getParameter("fixtureId");
        request.setAttribute("selectedFixtureId", fixtureId);

        if (fixtureId != null && !fixtureId.isBlank() && request.getAttribute("matchResult") == null) {
            Optional<MatchResultResponse> matchResult = adminMatchResultRestClient.getMatchResult(fixtureId);
            matchResult.ifPresent(result -> request.setAttribute("matchResult", result));
        }
    }

    // TODO [W4-FE-FIXES-09]: duplicated parseUuid (non-Optional variant) — extract shared util/ helper (see W4-CR-FE-12)
    private UUID parseUuid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private int parseNonNegativeInt(String value) {
        if (value == null || value.isBlank()) {
            return -1;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @Override
    public String getServletInfo() {
        return "Admin Match Result Servlet, handles fixture selection, match result capture, and player statistics capture";
    }
}
