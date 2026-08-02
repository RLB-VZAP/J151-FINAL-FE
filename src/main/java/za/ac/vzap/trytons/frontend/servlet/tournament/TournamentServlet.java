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
import za.ac.vzap.trytons.frontend.client.tournament.MatchDayResponse;
import za.ac.vzap.trytons.frontend.client.tournament.StartLeagueResponse;
import za.ac.vzap.trytons.frontend.client.tournament.TournamentFixtureResponse;
import za.ac.vzap.trytons.frontend.client.tournament.TournamentResponse;
import za.ac.vzap.trytons.frontend.client.tournament.TournamentRestClient;
import za.ac.vzap.trytons.frontend.servlet.shared.AbstractServlet;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
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
        request.setAttribute("canEditMatchDays", Boolean.FALSE);
        league.ifPresent(found -> {
            request.setAttribute("league", found);
            request.setAttribute("isLeagueManager", isManagerOf(found));
            request.setAttribute("canEditMatchDays", canEditMatchDays(found));
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

    /**
     * One card per fantasy round. Grouped by roundId rather than by round
     * number because the round is what the match-day editor moves, and because
     * rounds are minted per league now — the season-wide roundNumber is still a
     * fine sort key (it increases in mint order) but it is no longer what the
     * page shows.
     */
    private List<RoundFixtureGroup> groupByFantasyRound(List<FixtureResponse> fixtures) {
        Map<UUID, List<FixtureResponse>> byRound = new LinkedHashMap<>();
        List<FixtureResponse> unknownRound = new ArrayList<>();
        for (FixtureResponse fixture : fixtures) {
            if (fixture == null) continue;
            if (fixture.getRoundId() == null) {
                unknownRound.add(fixture);
                continue;
            }
            byRound.computeIfAbsent(fixture.getRoundId(), key -> new ArrayList<>()).add(fixture);
        }

        Integer currentRound = determineCurrentRound(fixtures);

        List<RoundFixtureGroup> groups = new ArrayList<>();
        byRound.forEach((roundId, group) -> groups.add(toGroup(roundId, group, currentRound)));
        // Playing order: the season-wide round number, which increases with each
        // minted round. Unnumbered rounds sort last rather than being dropped.
        groups.sort(Comparator.comparing(
                group -> group.getRoundNumber() == null ? Integer.MAX_VALUE : group.getRoundNumber()));

        if (!unknownRound.isEmpty()) {
            groups.add(toGroup(null, unknownRound, currentRound));
        }
        return groups;
    }

    private RoundFixtureGroup toGroup(UUID roundId, List<FixtureResponse> group, Integer currentRound) {
        FixtureResponse first = group.get(0);
        Integer roundNumber = first.getRoundNumber();
        return new RoundFixtureGroup(
                roundId == null ? null : roundId.toString(),
                roundNumber,
                first.getMatchdayNumber(),
                first.getStage(),
                stageLabel(group),
                matchDayIso(group),
                isEditable(group),
                hasMixedStages(group),
                group,
                roundNumber != null && roundNumber.equals(currentRound));
    }

    /**
     * The backend now names its own stages ({@code stageLabel} on the DTO), so
     * that is what the page shows. KNOCKOUT_STAGES stays only as the fallback
     * for a response that predates the field — one label, decided in one place,
     * rather than the frontend quietly inventing a second vocabulary.
     */
    /**
     * Stage precedence, most important first. Used only to order a heading that
     * has to name more than one stage.
     */
    private static final List<String> STAGE_ORDER = List.of(
            "FINAL", "THIRD_PLACE", "SEMI_FINAL", "QUARTER_FINAL",
            "ROUND_OF_16", "ROUND_OF_32", "POOL");

    /**
     * A matchday can legitimately hold two different stages: the beaten
     * semi-finalists play the bronze final on the same day as the final, so
     * that round contains both FINAL and THIRD_PLACE fixtures.
     *
     * <p>This used to label the whole group from {@code group.get(0)}, which
     * meant the championship final was silently filed under a "Bronze Final"
     * heading and a reader of this list would never know a final had been
     * played. Name every distinct stage present instead, most important first.
     */
    private String stageLabel(List<FixtureResponse> group) {
        List<String> distinct = distinctStageLabels(group);
        if (distinct.size() > 1) {
            return String.join(" & ", distinct);
        }

        FixtureResponse first = group.isEmpty() ? null : group.get(0);
        if (first == null) return "Fixtures";

        String label = first.getStageLabel();
        if (label != null && !label.isBlank()) return label;

        String stage = first.getStage();
        if (stage == null) return "Fixtures";
        if ("POOL".equalsIgnoreCase(stage)) return "Pool Stage";
        return KNOCKOUT_STAGES.getOrDefault(stage.toUpperCase(Locale.ROOT), stage);
    }

    /**
     * Distinct stage labels in a group, most important stage first. Empty when
     * the group carries no stage at all (ordinary non-tournament fixtures).
     */
    private List<String> distinctStageLabels(List<FixtureResponse> group) {
        Map<String, String> byStage = new LinkedHashMap<>();
        for (FixtureResponse fixture : group) {
            if (fixture == null || fixture.getStage() == null) continue;
            String stage = fixture.getStage().toUpperCase(Locale.ROOT);
            String label = fixture.getStageLabel();
            if (label == null || label.isBlank()) {
                label = "POOL".equals(stage) ? "Pool Stage" : KNOCKOUT_STAGES.getOrDefault(stage, stage);
            }
            byStage.putIfAbsent(stage, label);
        }

        List<String> ordered = new ArrayList<>(byStage.keySet());
        ordered.sort(Comparator.comparingInt(stage -> {
            int index = STAGE_ORDER.indexOf(stage);
            return index < 0 ? STAGE_ORDER.size() : index;
        }));

        List<String> labels = new ArrayList<>();
        for (String stage : ordered) {
            labels.add(byStage.get(stage));
        }
        return labels;
    }

    /** True when this round holds fixtures from more than one stage. */
    private boolean hasMixedStages(List<FixtureResponse> group) {
        return distinctStageLabels(group).size() > 1;
    }

    /** The round's match day as yyyy-MM-dd, for the date input. */
    private String matchDayIso(List<FixtureResponse> group) {
        for (FixtureResponse fixture : group) {
            if (fixture.getFixtureDate() != null) {
                return fixture.getFixtureDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
            }
        }
        return null;
    }

    /**
     * Whether the editor is worth showing for this round. The decision is the
     * backend's ({@code TournamentServiceImpl.updateMatchDay}); this only
     * mirrors the two conditions the page can actually see — nothing played
     * yet, and the kickoff still ahead — so a hopeless form is not offered.
     * A round that has opened will be refused by the backend regardless.
     */
    private boolean isEditable(List<FixtureResponse> group) {
        LocalDate today = LocalDate.now();
        for (FixtureResponse fixture : group) {
            if (!"UPCOMING".equalsIgnoreCase(fixture.getFixtureStatus())) return false;
            if (fixture.getFixtureDate() == null || !fixture.getFixtureDate().isAfter(today)) return false;
        }
        return !group.isEmpty();
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
        private final String roundId;
        private final Integer roundNumber;
        private final Integer matchdayNumber;
        private final String stage;
        private final String stageLabel;
        private final String matchDayIso;
        private final boolean editable;
        private final List<FixtureResponse> fixtures;
        private final boolean current;
        private final boolean mixedStages;

        RoundFixtureGroup(String roundId,
                          Integer roundNumber,
                          Integer matchdayNumber,
                          String stage,
                          String stageLabel,
                          String matchDayIso,
                          boolean editable,
                          boolean mixedStages,
                          List<FixtureResponse> fixtures,
                          boolean current) {
            this.roundId = roundId;
            this.roundNumber = roundNumber;
            this.matchdayNumber = matchdayNumber;
            this.stage = stage;
            this.stageLabel = stageLabel;
            this.matchDayIso = matchDayIso;
            this.editable = editable;
            this.mixedStages = mixedStages;
            this.fixtures = fixtures;
            this.current = current;
        }

        public String getRoundId() { return roundId; }
        /** The season-wide sequence. Kept for sorting; not shown to managers. */
        public Integer getRoundNumber() { return roundNumber; }
        /** The league's own matchday, 1..n. What the card is titled with. */
        public Integer getMatchdayNumber() { return matchdayNumber; }
        public String getStage() { return stage; }
        public String getStageLabel() { return stageLabel; }
        /** True when this round mixes stages (final + bronze final share a day). */
        public boolean isMixedStages() { return mixedStages; }
        /** yyyy-MM-dd, for &lt;input type="date"&gt;. */
        public String getMatchDayIso() { return matchDayIso; }
        public boolean isEditable() { return editable; }
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

        // Action dispatch. Anything other than "matchday" — including no action
        // parameter at all — falls through to the start path, so the existing
        // Start League form (which posts no action) keeps working untouched.
        if ("matchday".equals(request.getParameter("action"))) {
            handleMatchDayUpdate(request, response);
            return;
        }

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
     * Moves one round's match day. POST-redirect-GET, so a refresh cannot
     * resubmit and the toast survives the redirect; the anchor drops the manager
     * back at the section they were editing.
     */
    private void handleMatchDayUpdate(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String leagueId = request.getParameter("leagueId");
        String roundId = request.getParameter("roundId");
        String matchDayParam = request.getParameter("matchDay");

        if (leagueId == null || leagueId.isBlank()) {
            flashError(request, "A league id is required to move a match day.");
            redirectTo(response, request, "/leagues");
            return;
        }

        String back = "/tournament?leagueId=" + encode(leagueId) + "#rounds-fixtures";

        if (roundId == null || roundId.isBlank()) {
            flashError(request, "A round is required to move a match day.");
            redirectTo(response, request, back);
            return;
        }

        LocalDate matchDay;
        try {
            matchDay = LocalDate.parse(matchDayParam, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (RuntimeException e) {
            flashError(request, "Pick a match day first — Wednesday, Saturday or Sunday.");
            redirectTo(response, request, back);
            return;
        }

        Optional<MatchDayResponse> moved =
                tournamentRestClient.updateMatchDay(leagueId, roundId, matchDay);
        if (moved.isPresent()) {
            MatchDayResponse result = moved.get();
            flashSuccess(request, "Match day moved to "
                    + result.getMatchDay().format(FIXTURE_DATE) + ". "
                    + result.getFixturesMoved() + " fixture"
                    + (result.getFixturesMoved() == 1 ? "" : "s") + " moved with it.");
            redirectTo(response, request, back);
            return;
        }

        if (sessionExpiredRedirect(request, response)) return;
        // APIClient never throws, so the reason for the empty Optional lives on
        // apiCallStatus — a validation refusal reads as the backend's own message.
        flashError(request, apiCallStatus.getMessage("Unable to move this match day."));
        redirectTo(response, request, back);
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
            // The backend's own label where it has one, so the bracket headings
            // and the round cards cannot end up naming the same stage
            // differently ("Quarter-finals" here, "Quarter-Finals" there).
            String label = inStage.get(0).getStageLabel();
            grouped.put(label == null || label.isBlank() ? stage.getValue() : label, inStage);
        }
        return grouped;
    }

    /**
     * Whether to offer the match-day editor at all. Mirrors the backend's
     * {@code TournamentServiceImpl.requireMatchDayEditor} exactly — an
     * administrator may reschedule any league, a PRIVATE league's own manager
     * may reschedule theirs, and nobody else may. This is display only; the
     * decision is still the backend's, and the two must not drift apart (this
     * codebase has broken twice by letting one authorisation path disagree with
     * another — see LESSONS.md).
     */
    private boolean canEditMatchDays(LeagueResponse league) {
        if (authContext.isAdmin()) return true;
        return "PRIVATE".equalsIgnoreCase(league.getLeagueType()) && isManagerOf(league);
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
