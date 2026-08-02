package za.ac.vzap.trytons.frontend.servlet.tournament;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.fantasyteam.FantasyTeamRestClient;
import za.ac.vzap.trytons.frontend.client.fixture.FixtureResponse;
import za.ac.vzap.trytons.frontend.client.fixture.FixtureRestClient;
import za.ac.vzap.trytons.frontend.client.league.LeagueResponse;
import za.ac.vzap.trytons.frontend.client.league.LeagueRestClient;
import za.ac.vzap.trytons.frontend.client.tournament.StartLeagueResponse;
import za.ac.vzap.trytons.frontend.client.tournament.TournamentFixtureResponse;
import za.ac.vzap.trytons.frontend.client.tournament.TournamentResponse;
import za.ac.vzap.trytons.frontend.client.tournament.TournamentRestClient;
import za.ac.vzap.trytons.frontend.servlet.shared.AbstractServlet;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;

/**
 * The league tournament view, plus the action that starts one.
 *
 * A league only has a tournament once its manager has started it, so a 404 from
 * the backend is an expected state rather than an error: the page is forwarded
 * with a "notStarted" flag and explains what to do instead.
 */
@WebServlet(name = "TournamentServlet", urlPatterns = {"/tournament", "/league/start"})
public class TournamentServlet extends AbstractServlet {

    private static final String VIEW = "/pages/tournament.jsp";

    /** Knockout rounds in the order they are played, with the labels the page shows. */
    private static final Map<String, String> KNOCKOUT_STAGES = new LinkedHashMap<>();

    static {
        KNOCKOUT_STAGES.put("ROUND_OF_32", "Round of 32");
        KNOCKOUT_STAGES.put("ROUND_OF_16", "Round of 16");
        KNOCKOUT_STAGES.put("QUARTER_FINAL", "Quarter-finals");
        KNOCKOUT_STAGES.put("SEMI_FINAL", "Semi-finals");
        KNOCKOUT_STAGES.put("FINAL", "Final");
        KNOCKOUT_STAGES.put("THIRD_PLACE", "Third-place playoff");
    }

    @Inject
    private TournamentRestClient tournamentRestClient;

    @Inject
    private LeagueRestClient leagueRestClient;

    @Inject
    private FixtureRestClient fixtureRestClient;

    @Inject
    private FantasyTeamRestClient fantasyTeamRestClient;

    private static final DateTimeFormatter FIXTURE_DATE =
            DateTimeFormatter.ofPattern("EEE d MMM yyyy", Locale.UK);
    private static final DateTimeFormatter FIXTURE_TIME =
            DateTimeFormatter.ofPattern("HH:mm", Locale.UK);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!requireAuthenticated(request, response)) return;

        String leagueId = request.getParameter("leagueId");
        request.setAttribute("leagueId", leagueId);

        if (leagueId == null || leagueId.isBlank()) {
            request.setAttribute("notStarted", Boolean.TRUE);
            forwardWithError(request, response, "A league id is required to view a tournament.", VIEW);
            return;
        }

        // The league itself supplies the name and the manager check for the
        // not-started panel, neither of which the tournament endpoint can give
        // us when there is no tournament yet.
        Optional<LeagueResponse> league = leagueRestClient.getLeague(leagueId);
        league.ifPresent(found -> {
            request.setAttribute("league", found);
            request.setAttribute("isLeagueManager", isManagerOf(found));
            // Private leagues are friendlies — real fixtures, but excluded from the
            // master leaderboard, seeding and pricing. Surfaced here so it is visible
            // wherever this league's fixtures are browsed.
            request.setAttribute("isPrivateLeague", "PRIVATE".equalsIgnoreCase(found.getLeagueType()));
        });

        // League-scoped, not tournament-scoped, so rounds and fixtures render even for
        // a FORMING league that has no tournament yet.
        loadRoundsAndFixtures(request, leagueId);

        Optional<TournamentResponse> tournament = tournamentRestClient.getTournamentForLeague(leagueId);
        if (tournament.isEmpty()) {
            if (sessionExpiredRedirect(request, response)) return;
            // A 404 simply means the league has never been started.
            request.setAttribute("notStarted", Boolean.TRUE);
            if (!apiCallStatus.isNotFound()) {
                request.setAttribute("error",
                        apiCallStatus.getMessage("Unable to load the tournament for this league."));
            }
            request.getRequestDispatcher(VIEW).forward(request, response);
            return;
        }

        TournamentResponse found = tournament.get();
        request.setAttribute("tournament", found);
        loadFixtures(request, found);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    /**
     * The league's own Rounds & fixtures section: every fixture the league holds
     * (pool, knockout and any non-tournament round), grouped by fantasy round
     * rather than matchday so a knockout tie appears here too, not just in the
     * bracket. Reuses the league-scoped endpoint from FixtureServlet's approach,
     * but the scores now travel on the DTO, so there is no per-fixture read-back.
     */
    private void loadRoundsAndFixtures(HttpServletRequest request, String leagueId) {
        List<FixtureResponse> fixtures = fixtureRestClient.listFixturesForLeague(leagueId, null).orElse(List.of());
        request.setAttribute("roundGroups", groupByFantasyRound(fixtures));

        request.setAttribute("myTeamId", fantasyTeamRestClient.getMyTeam()
                .map(team -> team.getTeamId() == null ? null : team.getTeamId().toString())
                .orElse(null));

        Map<UUID, String> dateLabels = new HashMap<>();
        Map<UUID, String> timeLabels = new HashMap<>();
        for (FixtureResponse fixture : fixtures) {
            if (fixture == null || fixture.getFixtureId() == null) continue;
            if (fixture.getFixtureDate() != null) {
                dateLabels.put(fixture.getFixtureId(), fixture.getFixtureDate().format(FIXTURE_DATE));
            }
            if (fixture.getFixtureTime() != null) {
                timeLabels.put(fixture.getFixtureId(), fixture.getFixtureTime().format(FIXTURE_TIME));
            }
        }
        request.setAttribute("fixtureDateById", dateLabels);
        request.setAttribute("fixtureTimeById", timeLabels);
    }

    private List<RoundFixtureGroup> groupByFantasyRound(List<FixtureResponse> fixtures) {
        Map<Integer, List<FixtureResponse>> byRound = new TreeMap<>();
        for (FixtureResponse fixture : fixtures) {
            if (fixture == null) continue;
            Integer number = fixture.getRoundNumber();
            byRound.computeIfAbsent(number == null ? Integer.MAX_VALUE : number, key -> new ArrayList<>())
                    .add(fixture);
        }

        Integer currentRound = determineCurrentRound(fixtures);

        List<RoundFixtureGroup> groups = new ArrayList<>();
        byRound.forEach((number, group) -> {
            boolean unknownRound = number.equals(Integer.MAX_VALUE);
            groups.add(new RoundFixtureGroup(
                    unknownRound ? null : number,
                    stageLabel(group),
                    group,
                    !unknownRound && number.equals(currentRound)));
        });
        return groups;
    }

    private String stageLabel(List<FixtureResponse> group) {
        String stage = group.isEmpty() ? null : group.get(0).getStage();
        if (stage == null) return "Fixtures";
        if ("POOL".equalsIgnoreCase(stage)) return "Pool";
        return KNOCKOUT_STAGES.getOrDefault(stage.toUpperCase(Locale.ROOT), stage);
    }

    /**
     * The round to expand by default: the earliest round with a fixture still to
     * be played, or the highest round number when the whole league is done.
     */
    private Integer determineCurrentRound(List<FixtureResponse> fixtures) {
        Optional<Integer> nextUnplayed = fixtures.stream()
                .filter(fixture -> fixture != null && fixture.getRoundNumber() != null
                        && !isFinished(fixture.getFixtureStatus()))
                .map(FixtureResponse::getRoundNumber)
                .min(Comparator.naturalOrder());
        if (nextUnplayed.isPresent()) return nextUnplayed.get();

        return fixtures.stream()
                .filter(fixture -> fixture != null && fixture.getRoundNumber() != null)
                .map(FixtureResponse::getRoundNumber)
                .max(Comparator.naturalOrder())
                .orElse(null);
    }

    private boolean isFinished(String status) {
        return "COMPLETED".equalsIgnoreCase(status)
                || "PROCESSED".equalsIgnoreCase(status)
                || "CANCELLED".equalsIgnoreCase(status);
    }

    /** View model for one fantasy-round card in the Rounds & fixtures section. */
    public static class RoundFixtureGroup {
        private final Integer roundNumber;
        private final String stageLabel;
        private final List<FixtureResponse> fixtures;
        private final boolean current;

        RoundFixtureGroup(Integer roundNumber, String stageLabel, List<FixtureResponse> fixtures, boolean current) {
            this.roundNumber = roundNumber;
            this.stageLabel = stageLabel;
            this.fixtures = fixtures;
            this.current = current;
        }

        public Integer getRoundNumber() { return roundNumber; }
        public String getStageLabel() { return stageLabel; }
        public List<FixtureResponse> getFixtures() { return fixtures; }
        public boolean isCurrent() { return current; }
        public int getCount() { return fixtures.size(); }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Starting a tournament is a league manager's action, not an admin one —
        // the backend authorises the league's own manager (or an admin).
        if (!requireAuthenticated(request, response)) return;

        String leagueId = request.getParameter("leagueId");
        if (leagueId == null || leagueId.isBlank()) {
            flashError(request, "A league id is required to start a league.");
            redirectTo(response, request, "/leagues");
            return;
        }

        Optional<StartLeagueResponse> started = tournamentRestClient.startLeague(leagueId);
        if (started.isPresent()) {
            String message = started.get().getMessage();
            flashSuccess(request, (message == null || message.isBlank())
                    ? "League started. The tournament draw has been made."
                    : message);
            redirectTo(response, request, "/tournament?leagueId=" + encode(leagueId));
            return;
        }

        if (sessionExpiredRedirect(request, response)) return;
        flashError(request, apiCallStatus.getMessage("Unable to start this league right now."));
        redirectTo(response, request, "/leagues");
    }

    /**
     * The knockout bracket only now — pool and knockout fixtures together are
     * covered by the league-scoped Rounds & fixtures section (loadRoundsAndFixtures),
     * grouped by stage in playing order, ordered by bracket slot.
     */
    private void loadFixtures(HttpServletRequest request, TournamentResponse tournament) {
        if (tournament.getTournamentId() == null) return;

        List<TournamentFixtureResponse> fixtures =
                tournamentRestClient.listFixtures(tournament.getTournamentId().toString()).orElse(List.of());
        request.setAttribute("fixtures", fixtures);
        request.setAttribute("bracketGroups", groupByStage(fixtures));
    }

    private Map<String, List<TournamentFixtureResponse>> groupByStage(List<TournamentFixtureResponse> fixtures) {
        Map<String, List<TournamentFixtureResponse>> grouped = new LinkedHashMap<>();
        for (Map.Entry<String, String> stage : KNOCKOUT_STAGES.entrySet()) {
            List<TournamentFixtureResponse> inStage = new ArrayList<>();
            for (TournamentFixtureResponse fixture : fixtures) {
                if (fixture != null && stage.getKey().equalsIgnoreCase(fixture.getStage())) {
                    inStage.add(fixture);
                }
            }
            if (inStage.isEmpty()) continue;
            // Bracket slot fixes the vertical order of a round; an unslotted
            // fixture sorts last rather than being dropped.
            inStage.sort(Comparator.comparing(
                    fixture -> fixture.getBracketSlot() == null ? Integer.MAX_VALUE : fixture.getBracketSlot()));
            grouped.put(stage.getValue(), inStage);
        }
        return grouped;
    }

    private boolean isManagerOf(LeagueResponse league) {
        UUID currentUserId = authContext.getUserId();
        return currentUserId != null && currentUserId.equals(league.getManagerUserId());
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    @Override
    public String getServletInfo() {
        return "Tournament Servlet, renders a league's tournament and starts a league's tournament";
    }
}
