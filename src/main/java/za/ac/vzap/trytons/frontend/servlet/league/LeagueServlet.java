package za.ac.vzap.trytons.frontend.servlet.league;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.league.JoinLeagueRequest;
import za.ac.vzap.trytons.frontend.client.league.LeagueMemberResponse;
import za.ac.vzap.trytons.frontend.client.league.LeagueRequest;
import za.ac.vzap.trytons.frontend.client.league.LeagueResponse;
import za.ac.vzap.trytons.frontend.client.league.LeagueRestClient;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
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

                request.setAttribute("myLeagues",
                        authContext.isAuthenticated()
                                ? leagueRestClient.listMyLeagues().orElse(List.of())
                                : List.of());
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

        String destination = switch (submit) {
            case "league/create" -> {
                Optional<LeagueResponse> created = leagueRestClient.createLeague(buildLeagueRequest(request));
                if (created.isPresent()) {
                    request.setAttribute("success", "League created successfully");
                    request.setAttribute("league", created.get());
                } else {
                    request.setAttribute("error", "Unable to create league. Check your details and try again.");
                }
                yield "/pages/create-league.jsp";
            }

            case "league/join" -> {
                JoinLeagueRequest joinRequest = new JoinLeagueRequest(
                        request.getParameter("leagueId"),
                        request.getParameter("leagueCode"));
                Optional<LeagueResponse> joined = leagueRestClient.joinLeague(joinRequest);
                if (joined.isPresent()) {
                    request.setAttribute("success", "You've joined the league");
                    request.setAttribute("league", joined.get());
                    yield "/pages/leagues.jsp";
                }

                request.setAttribute("error", "Unable to join that league. Check the code or ID and try again.");
                yield "/pages/join-league.jsp";
            }

            case "league/members/remove" -> {
                String leagueId = request.getParameter("leagueId");
                String membershipId = request.getParameter("membershipId");
                boolean removed = leagueId != null && membershipId != null
                        && leagueRestClient.removeMember(leagueId, membershipId);
                request.setAttribute(removed ? "success" : "error",
                        removed ? "Member removed" : "Unable to remove member");
                request.setAttribute("leagueId", leagueId);
                request.setAttribute("members", reloadMembers(leagueId));
                yield "/pages/league-members.jsp";
            }

            default -> "/index.jsp";
        };

        request.getRequestDispatcher(destination).forward(request, response);
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
