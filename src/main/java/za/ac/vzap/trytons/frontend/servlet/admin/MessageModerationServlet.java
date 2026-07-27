package za.ac.vzap.trytons.frontend.servlet.admin;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.message.BlockedPhraseResponse;
import za.ac.vzap.trytons.frontend.client.message.DirectMessageResponse;
import za.ac.vzap.trytons.frontend.client.message.MessageModerationRestClient;
import za.ac.vzap.trytons.frontend.client.message.PendingLeagueMessageResponse;
import za.ac.vzap.trytons.frontend.servlet.shared.AbstractServlet;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@WebServlet(name = "MessageModerationServlet", urlPatterns = {"/admin/message-moderation"})
public class MessageModerationServlet extends AbstractServlet {

    private static final String VIEW = "/pages/admin-message-moderation.jsp";

    @Inject
    private MessageModerationRestClient moderationRestClient;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!requireAdmin(request, response)) {
            return;
        }
        loadAndForward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!requireAdmin(request, response)) {
            return;
        }

        String action = request.getParameter("action");
        if (action == null) {
            action = "";
        }

        boolean ok;
        switch (action) {
            case "approve" -> ok = withMessageId(request, moderationRestClient::approve);
            case "reject" -> ok = withMessageId(request, moderationRestClient::reject);
            case "addPhrase" -> ok = moderationRestClient.addBlocklistPhrase(request.getParameter("phrase"));
            case "removePhrase" -> {
                Optional<UUID> id = parseUuid(request.getParameter("blocklistId"));
                ok = id.isPresent() && moderationRestClient.removeBlocklistPhrase(id.get());
            }
            default -> ok = false;
        }

        if (!ok && sessionExpiredRedirect(request, response)) {
            return;
        }

        response.sendRedirect(request.getContextPath() + "/admin/message-moderation");
    }

    private boolean withMessageId(HttpServletRequest request, java.util.function.Predicate<UUID> op) {
        Optional<UUID> id = parseUuid(request.getParameter("messageId"));
        return id.isPresent() && op.test(id.get());
    }

    private void loadAndForward(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Optional<List<PendingLeagueMessageResponse>> pending = moderationRestClient.listPending();
        if (pending.isEmpty() && sessionExpiredRedirect(request, response)) {
            return;
        }
        request.setAttribute("pendingMessages", pending.orElse(List.of()));

        Optional<List<BlockedPhraseResponse>> blocklist = moderationRestClient.listBlocklist();
        request.setAttribute("blocklist", blocklist.orElse(List.of()));

        if (pending.isEmpty() && blocklist.isEmpty() && request.getAttribute("error") == null) {
            request.setAttribute("error", "Unable to load moderation data right now");
        }

        loadDirectMessageWindow(request);

        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    // Rule E: admins have no listing of reported direct messages today (no such
    // endpoint exists), so the entry point is a manual message id lookup rather
    // than a queue row. A blank id means the lookup form was never submitted, so
    // nothing is set and the section renders its default (empty) state.
    private void loadDirectMessageWindow(HttpServletRequest request) {
        String rawMessageId = request.getParameter("directMessageId");
        if (rawMessageId == null || rawMessageId.isBlank()) {
            return;
        }
        request.setAttribute("directMessageIdQuery", rawMessageId);

        Optional<UUID> messageId = parseUuid(rawMessageId);
        if (messageId.isEmpty()) {
            request.setAttribute("directMessageWindowError", "That is not a valid message id.");
            return;
        }

        Optional<List<DirectMessageResponse>> window = moderationRestClient.getDirectMessageWindow(messageId.get());
        if (window.isEmpty()) {
            if (apiCallStatus.isForbidden()) {
                // Contract: 403 means the message carries no report — an explicit
                // denied state, not a generic failure.
                request.setAttribute("directMessageWindowDenied", true);
            } else {
                request.setAttribute("directMessageWindowError", apiCallStatus.getMessage("Unable to load that conversation"));
            }
            return;
        }

        // The backend returns newest-first (the reported message, then each
        // preceding one); reverse so the page reads top-to-bottom like a chat.
        List<DirectMessageResponse> orderedWindow = window.get();
        Collections.reverse(orderedWindow);
        request.setAttribute("directMessageWindow", orderedWindow);
        request.setAttribute("directMessageAnchorId", rawMessageId);
    }

    @Override
    public String getServletInfo() {
        return "Message Moderation Servlet: league chat approval queue and blocklist management";
    }
}
