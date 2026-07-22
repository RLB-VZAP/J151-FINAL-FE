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

@WebServlet(name ="FixtureServlet", urlPatterns = {"/fixtures","/fixture"} )
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

