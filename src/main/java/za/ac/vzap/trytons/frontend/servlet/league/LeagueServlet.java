package za.ac.vzap.trytons.frontend.servlet.league;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.league.JoinLeagueRequest;
import za.ac.vzap.trytons.frontend.client.league.JoinLeagueResponse;
import za.ac.vzap.trytons.frontend.client.league.LeagueMemberResponse;
import za.ac.vzap.trytons.frontend.client.league.LeagueRequest;
import za.ac.vzap.trytons.frontend.client.league.LeagueResponse;
import za.ac.vzap.trytons.frontend.client.league.LeagueRestClient;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import za.ac.vzap.trytons.frontend.servlet.shared.AbstractServlet;

@WebServlet(name = "LeagueServlet", urlPatterns = {"/leagues", "/league", "/league/create", "/league/join", "/league/members"})
public class LeagueServlet extends AbstractServlet {

    @Inject
    private LeagueRestClient leagueRestClient;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getServletPath();

        if ("/league/members".equals(path) && !requireAuthenticated(request, response)) return;

        String destination = switch (path) {
            case "/leagues" -> {
                Optional<List<LeagueResponse>> publicLeagues = leagueRestClient.listPublicLeagues();
                request.setAttribute("publicLeagues", publicLeagues.orElseGet(List::of));
                if (publicLeagues.isEmpty()) {
                    request.setAttribute("error", "Unable to load leagues right now");
                }

                request.setAttribute("myLeagues", loadMyLeagues());
                yield "/pages/leagues.jsp";
            }

            case "/league" -> {
                String leagueId = request.getParameter("leagueId");
                if (leagueId == null || leagueId.isBlank()) {
                    request.setAttribute("error", "A league id is required");
                    yield "/pages/leagues.jsp";
                }
                leagueRestClient.getLeague(leagueId).ifPresentOrElse(
                        league -> request.setAttribute("league", league),
                        () -> request.setAttribute("error", "League not found, or you don't have access to view it")
                );
                yield "/pages/leagues.jsp";
            }

            case "/league/create" -> "/pages/create-league.jsp";

            case "/league/join" -> "/pages/join-league.jsp";

            case "/league/members" -> {
                String leagueId = request.getParameter("leagueId");
                request.setAttribute("leagueId", leagueId);
                request.setAttribute("members", reloadMembers(leagueId));
                request.setAttribute("isLeagueManager", isCurrentUserManager(leagueId));
                yield "/pages/league-members.jsp";
            }

            default -> "/index.jsp";
        };

        request.getRequestDispatcher(destination).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!requireAuthenticated(request, response)) return;

        String submit = request.getParameter("submit");
        if (submit == null) submit = "";

        switch (submit) {
            case "league/create" -> {
                Optional<LeagueResponse> created = leagueRestClient.createLeague(buildLeagueRequest(request));
                if (created.isPresent()) {
                    response.sendRedirect(request.getContextPath() + "/league?leagueId=" + created.get().getLeagueId());
                    return;
                }
                if (handleApiFailure(request, response, "Unable to create league. Check your details and try again.")) return;
                request.getRequestDispatcher("/pages/create-league.jsp").forward(request, response);
            }

            case "league/join" -> {
                JoinLeagueRequest joinRequest = new JoinLeagueRequest(
                        request.getParameter("leagueId"),
                        request.getParameter("leagueCode"));
                Optional<JoinLeagueResponse> joined = leagueRestClient.joinLeague(joinRequest);
                if (joined.isPresent()) {
                    response.sendRedirect(request.getContextPath() + "/league?leagueId=" + joined.get().getLeagueId());
                    return;
                }
                if (handleApiFailure(request, response, "Unable to join that league. Check the code or ID and try again.")) return;
                request.getRequestDispatcher("/pages/join-league.jsp").forward(request, response);
            }

            case "league/members/remove" -> {
                String leagueId = request.getParameter("leagueId");
                String membershipId = request.getParameter("membershipId");
                boolean removed = false;
                if (leagueId != null && membershipId != null) {
                    leagueRestClient.removeMember(leagueId, membershipId);
                    removed = apiCallStatus.isSuccess();
                }
                if (removed) {
                    response.sendRedirect(request.getContextPath() + "/league/members?leagueId=" + leagueId);
                    return;
                }
                if (handleApiFailure(request, response, "Unable to remove member")) return;
                request.setAttribute("leagueId", leagueId);
                request.setAttribute("members", reloadMembers(leagueId));
                request.setAttribute("isLeagueManager", isCurrentUserManager(leagueId));
                request.getRequestDispatcher("/pages/league-members.jsp").forward(request, response);
            }

            default -> request.getRequestDispatcher("/index.jsp").forward(request, response);
        }
    }

    private LeagueRequest buildLeagueRequest(HttpServletRequest request) {
        return new LeagueRequest(
                request.getParameter("leagueName"),
                request.getParameter("description"),
                request.getParameter("leagueType"),
                parseIntOrZero(request.getParameter("maxMembers")));
    }

    private List<LeagueMemberResponse> reloadMembers(String leagueId) {
        if (leagueId == null || leagueId.isBlank()) return List.of();
        return leagueRestClient.listMembers(leagueId).orElse(List.of());
    }

    private List<LeagueResponse> loadMyLeagues() {
        if (!authContext.isAuthenticated()) return List.of();
        UUID currentUserId = authContext.getUserId();
        if (currentUserId == null) return List.of();
        return leagueRestClient.listMyLeagues().orElse(List.of()).stream()
                .filter(league -> currentUserId.equals(league.getManagerUserId()))
                .collect(Collectors.toList());
    }

    private boolean isCurrentUserManager(String leagueId) {
        if (leagueId == null || leagueId.isBlank() || !authContext.isAuthenticated()) return false;
        UUID currentUserId = authContext.getUserId();
        if (currentUserId == null) return false;
        return leagueRestClient.getLeague(leagueId)
                .map(LeagueResponse::getManagerUserId)
                .map(currentUserId::equals)
                .orElse(false);
    }

    private int parseIntOrZero(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public String getServletInfo() {
        return "League Servlet, handles league browsing, detail, create, join, and member management";
    }
}
