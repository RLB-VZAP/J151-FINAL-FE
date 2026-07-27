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
import java.util.UUID;

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
            request.setAttribute("error", "A league is required to open chat");
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

        // The original send form predates action dispatch and still posts with no
        // "action" field at all, so a blank/missing action keeps meaning "send"
        // rather than being rejected as unknown.
        if ("report".equals(request.getParameter("action"))) {
            reportMessage(request, response);
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

    // Rule C: reporting a single league chat message. Confirmation happens client
    // side (the report cannot be undone), the reason is optional.
    private void reportMessage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String leagueId = request.getParameter("leagueId");
        Optional<UUID> messageId = parseUuid(request.getParameter("messageId"));
        if (leagueId == null || leagueId.isBlank() || messageId.isEmpty()) {
            request.setAttribute("error", "A valid message is required to report it");
            renderPage(request, response, leagueId);
            return;
        }

        boolean ok = leagueMessageRestClient.report(leagueId, messageId.get(), blankToNull(request.getParameter("reason")));
        if (!ok) {
            if (sessionExpiredRedirect(request, response)) {
                return;
            }
            request.setAttribute("error", apiCallStatus.getMessage("Unable to report that message"));
            renderPage(request, response, leagueId);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/league-chat?leagueId=" + encode(leagueId) + "&flash=reported");
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
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

    private void renderPage(HttpServletRequest request, HttpServletResponse response, String leagueId) throws ServletException, IOException {
        Optional<LeagueResponse> league = leagueRestClient.getLeague(leagueId);
        if (league.isEmpty() && sessionExpiredRedirect(request, response)) {
            return;
        }
        request.setAttribute("leagueId", leagueId);
        request.setAttribute("leagueName", league.map(LeagueResponse::getLeagueName).orElse("League chat"));

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
        } else if ("reported".equals(flash)) {
            request.setAttribute("success", "Message reported. An administrator will review it.");
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
