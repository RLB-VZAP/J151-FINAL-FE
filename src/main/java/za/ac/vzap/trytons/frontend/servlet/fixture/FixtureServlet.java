package za.ac.vzap.trytons.frontend.servlet.fixture;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.fixture.FixtureResponse;
import za.ac.vzap.trytons.frontend.client.fixture.FixtureRestClient;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import za.ac.vzap.trytons.frontend.servlet.shared.AbstractServlet;

import za.ac.vzap.trytons.frontend.client.results.AdminMatchResultRestClient;
import za.ac.vzap.trytons.frontend.client.results.MatchResultResponse;
import za.ac.vzap.trytons.frontend.client.results.MatchTeamScoreResponse;
import za.ac.vzap.trytons.frontend.client.results.MatchTeamScoreRestClient;
import za.ac.vzap.trytons.frontend.client.results.PlayerStatisticsResponse;
import za.ac.vzap.trytons.frontend.client.scoring.FantasyPointBreakdownResponse;
import za.ac.vzap.trytons.frontend.client.scoring.FantasyPointsResponse;
import za.ac.vzap.trytons.frontend.client.scoring.FantasyPointsRestClient;

@WebServlet(name ="FixtureServlet", urlPatterns = {"/fixtures","/fixture","/fixture/create","/fixture/update"} )
public class FixtureServlet extends AbstractServlet {
    @Inject
    private FixtureRestClient fixtureRestClient;
    @Inject
    private AdminMatchResultRestClient matchResultRestClient;
    @Inject
    private MatchTeamScoreRestClient matchTeamScoreRestClient;
    @Inject
    private FantasyPointsRestClient fantasyPointsRestClient;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String submit = request.getParameter("submit");
        if (submit == null) {
            submit = "";
        }
        switch (submit) {
            case "fixture" -> {
                String fixtureId = request.getParameter("fixtureId");
                if (fixtureId == null || fixtureId.isBlank()) {
                    forwardWithError(request, response, "Invalid or missing fixtureId", "/pages/fixtures.jsp");
                    return;
                }
                Optional<FixtureResponse> fixture = fixtureRestClient.getFixture(fixtureId);
                if (fixture.isPresent()) {
                    request.setAttribute("fixture", fixture.get());
                    loadMatchResultReadBack(request, fixtureId);
                    loadBreakdownDrillDown(request);
                    request.getRequestDispatcher("/pages/fixture-details.jsp").forward(request, response);
                    return;
                }
                forwardWithError(request, response, "Fixture not found", "/pages/fixtures.jsp");
            }

            default -> {
                String statusFilter = request.getParameter("status");
                Optional<List<FixtureResponse>> fixtures = fixtureRestClient.listFixtures(statusFilter);
                request.setAttribute("statusFilter", statusFilter);
                if (fixtures.isPresent()) {
                    request.setAttribute("fixtures", fixtures.get());
                    request.getRequestDispatcher("/pages/fixtures.jsp").forward(request, response);
                } else {
                    request.setAttribute("fixtures", List.of());
                    forwardWithError(request, response, "Unable to load fixtures", "/pages/fixtures.jsp");
                }
            }
        }
    }

    // Loads the match result read-back for a completed fixture: the result itself, both teams'
    // match-team-scores, and the player-statistics captured for that result. Silently leaves the
    // request attributes unset if no result exists yet (fixture not simulated) - the JSP treats an
    // absent "matchResult" attribute as "no result available".
    private void loadMatchResultReadBack(HttpServletRequest request, String fixtureId) {
        Optional<MatchResultResponse> matchResult = matchResultRestClient.getMatchResult(fixtureId);
        if (matchResult.isEmpty()) {
            return;
        }
        MatchResultResponse result = matchResult.get();
        request.setAttribute("matchResult", result);

        String resultId = result.getResultId().toString();

        Optional<List<MatchTeamScoreResponse>> teamScores = matchTeamScoreRestClient.listMatchTeamScoresForResult(resultId);
        teamScores.ifPresent(scores -> request.setAttribute("teamScores", scores));

        Optional<List<PlayerStatisticsResponse>> playerStats = matchResultRestClient.listResultStatistics(resultId);
        playerStats.ifPresent(stats -> request.setAttribute("playerStats", stats));
    }

    // Optional drill-down: when the page is reloaded with ?statId=<uuid> (a link next to a row in
    // the player-statistics table), resolves that stat's final fantasy points and the points'
    // breakdown lines, so the JSP can render a breakdown table for the selected player only.
    // Iterating every player's breakdown on every fixture-details load would be an N+1 fan-out over
    // fantasy-points and fantasy-point-breakdowns per player, so it is surfaced on-demand instead.
    private void loadBreakdownDrillDown(HttpServletRequest request) {
        String statId = request.getParameter("statId");
        if (statId == null || statId.isBlank()) {
            return;
        }
        Optional<FantasyPointsResponse> finalPoints = fantasyPointsRestClient.getFinalFantasyPointsForStat(statId);
        if (finalPoints.isEmpty()) {
            return;
        }
        request.setAttribute("selectedPoints", finalPoints.get());

        Optional<List<FantasyPointBreakdownResponse>> breakdowns =
                fantasyPointsRestClient.listBreakdownsForPoints(finalPoints.get().getPointsId().toString());
        breakdowns.ifPresent(list -> request.setAttribute("breakdowns", list));
    }
    }

