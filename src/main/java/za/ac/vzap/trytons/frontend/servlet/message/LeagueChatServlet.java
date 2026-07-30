package za.ac.vzap.trytons.frontend.servlet.message;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.league.LeagueResponse;
import za.ac.vzap.trytons.frontend.client.league.LeagueRestClient;
import za.ac.vzap.trytons.frontend.client.message.LeagueMessageResponse;
import za.ac.vzap.trytons.frontend.client.message.LeagueMessageRestClient;
import za.ac.vzap.trytons.frontend.client.message.SendLeagueMessageRequest;
import za.ac.vzap.trytons.frontend.servlet.shared.AbstractServlet;
import za.ac.vzap.trytons.frontend.util.JsonSupport;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@WebServlet(name = "LeagueChatServlet", urlPatterns = {"/league-chat"})
public class LeagueChatServlet extends AbstractServlet {

    private static final String VIEW = "/pages/league-chat.jsp";

    @Inject
    private LeagueMessageRestClient leagueMessageRestClient;

    @Inject
    private LeagueRestClient leagueRestClient;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!requireAuthenticated(request, response)) {
            return;
        }

        String leagueId = request.getParameter("leagueId");
        if (leagueId == null || leagueId.isBlank()) {
            // Landing here with no league is the normal case — the sidebar links
            // to /league-chat with no parameter. Offer the user's leagues to pick
            // from rather than reporting an error they cannot act on.
            loadMyLeagues(request);
            request.getRequestDispatcher(VIEW).forward(request, response);
            return;
        }

        if ("json".equalsIgnoreCase(request.getParameter("format"))) {
            handlePoll(request, response, leagueId);
            return;
        }

        renderPage(request, response, leagueId);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!requireAuthenticated(request, response)) {
            return;
        }

        String leagueId = request.getParameter("leagueId");
        String body = request.getParameter("body");
        if (leagueId == null || leagueId.isBlank() || body == null || body.isBlank()) {
            request.setAttribute("error", "A league and a message are required");
            renderPage(request, response, leagueId);
            return;
        }

        Optional<LeagueMessageResponse> posted =
                leagueMessageRestClient.post(leagueId, new SendLeagueMessageRequest(body.trim()));
        if (posted.isEmpty()) {
            if (sessionExpiredRedirect(request, response)) {
                return;
            }
            request.setAttribute("error", apiCallStatus.getMessage("Unable to post your message"));
            renderPage(request, response, leagueId);
            return;
        }

        String flash = "PENDING_REVIEW".equalsIgnoreCase(posted.get().getStatus())
                ? "held"
                : "sent";
        response.sendRedirect(request.getContextPath() + "/league-chat?leagueId="
                + encode(leagueId) + "&flash=" + flash);
    }

    private void handlePoll(HttpServletRequest request, HttpServletResponse response, String leagueId) throws IOException {
        Optional<List<LeagueMessageResponse>> feed =
                leagueMessageRestClient.getFeed(leagueId, request.getParameter("since"));
        if (feed.isEmpty()) {
            if (apiCallStatus.isUnauthorized()) {
                JsonSupport.writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, List.of());
                return;
            }
            JsonSupport.writeJson(response, HttpServletResponse.SC_BAD_GATEWAY, List.of());
            return;
        }
        JsonSupport.writeJson(response, feed.get());
    }

    /**
     * The leagues this user belongs to, for the chat picker. Every league the
     * user is a member of gets a group chat — public and private alike —
     * because membership is itself the permission to post. That is the whole
     * difference from direct messages, which need the other person's consent.
     */
    private void loadMyLeagues(HttpServletRequest request) {
        Optional<List<LeagueResponse>> myLeagues = leagueRestClient.listMyLeagues();
        if (myLeagues.isEmpty() && request.getAttribute("error") == null) {
            request.setAttribute("error", "Unable to load your leagues right now");
        }
        request.setAttribute("myLeagues", myLeagues.orElse(List.of()));
    }

    private void renderPage(HttpServletRequest request, HttpServletResponse response, String leagueId) throws ServletException, IOException {
        Optional<LeagueResponse> league = leagueRestClient.getLeague(leagueId);
        if (league.isEmpty() && sessionExpiredRedirect(request, response)) {
            return;
        }
        loadMyLeagues(request);
        request.setAttribute("leagueId", leagueId);
        request.setAttribute("leagueName", league.map(LeagueResponse::getLeagueName).orElse("League chat"));
        request.setAttribute("leagueType", league.map(LeagueResponse::getLeagueType).orElse(null));

        Optional<List<LeagueMessageResponse>> feed = leagueMessageRestClient.getFeed(leagueId, null);
        if (feed.isEmpty()) {
            if (sessionExpiredRedirect(request, response)) {
                return;
            }
            if (request.getAttribute("error") == null) {
                request.setAttribute("error", "Unable to load the chat feed right now");
            }
            request.setAttribute("messages", List.of());
        } else {
            request.setAttribute("messages", feed.get());
        }

        String flash = request.getParameter("flash");
        if ("held".equals(flash)) {
            request.setAttribute("info", "Your message was flagged and is awaiting review by an administrator.");
        } else if ("sent".equals(flash)) {
            request.setAttribute("success", "Message posted.");
        }

        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    @Override
    public String getServletInfo() {
        return "League Chat Servlet: moderated league messaging with AJAX polling";
    }
}
