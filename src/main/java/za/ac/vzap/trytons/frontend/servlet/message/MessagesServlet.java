package za.ac.vzap.trytons.frontend.servlet.message;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.auth.UserSearchRestClient;
import za.ac.vzap.trytons.frontend.client.auth.UserSearchResponse;
import za.ac.vzap.trytons.frontend.client.message.ConversationThreadResponse;
import za.ac.vzap.trytons.frontend.client.message.DirectMessageResponse;
import za.ac.vzap.trytons.frontend.client.message.MessageRequestOverviewResponse;
import za.ac.vzap.trytons.frontend.client.message.MessageRequestResponse;
import za.ac.vzap.trytons.frontend.client.message.MessageRestClient;
import za.ac.vzap.trytons.frontend.client.message.SendDirectMessageRequest;
import za.ac.vzap.trytons.frontend.servlet.shared.AbstractServlet;
import za.ac.vzap.trytons.frontend.util.JsonSupport;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@WebServlet(name = "MessagesServlet", urlPatterns = {"/messages"})
public class MessagesServlet extends AbstractServlet {

    private static final String VIEW = "/pages/messages.jsp";
    private static final String COMPOSE_FLAG = "1";

    private static final String REQUEST_STATUS_PENDING = "PENDING";
    private static final String REQUEST_STATUS_APPROVED = "APPROVED";

    // Drives which control messages.jsp shows for a selected counterpart: a normal
    // Send box, a "Send message request" button, or a pending/incoming notice.
    // See applyMessagingState().
    private static final String MESSAGING_STATE_ALLOWED = "ALLOWED";
    private static final String MESSAGING_STATE_NONE = "NONE";
    private static final String MESSAGING_STATE_PENDING_OUTGOING = "PENDING_OUTGOING";
    private static final String MESSAGING_STATE_PENDING_INCOMING = "PENDING_INCOMING";

    @Inject
    private MessageRestClient messageRestClient;

    // Backs the compose "pick a recipient" search for every logged-in user via
    // the privacy-scoped GET /api/users/search (username-only, no email/role).
    @Inject
    private UserSearchRestClient userSearchRestClient;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!requireAuthenticated(request, response)) {
            return;
        }

        if ("json".equalsIgnoreCase(request.getParameter("format"))) {
            handlePoll(request, response);
            return;
        }

        renderPage(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!requireAuthenticated(request, response)) {
            return;
        }

        String action = request.getParameter("action");
        if (action == null) {
            action = "";
        }

        switch (action) {
            case "send" -> sendMessage(request, response);
            case "block" -> toggleBlock(request, response, true);
            case "unblock" -> toggleBlock(request, response, false);
            case "request" -> sendMessageRequest(request, response);
            case "approveRequest" -> respondToRequest(request, response, true);
            case "rejectRequest" -> respondToRequest(request, response, false);
            case "report" -> reportMessage(request, response);
            default -> {
                request.setAttribute("error", "Unknown message action requested");
                renderPage(request, response);
            }
        }
    }

    private void handlePoll(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String with = request.getParameter("with");
        if (with != null && !with.isBlank()) {
            Optional<UUID> counterpart = parseUuid(with);
            if (counterpart.isEmpty()) {
                JsonSupport.writeJson(response, HttpServletResponse.SC_BAD_REQUEST, List.of());
                return;
            }
            Optional<List<DirectMessageResponse>> conversation =
                    messageRestClient.getConversation(counterpart.get(), request.getParameter("since"));
            writePollResult(response, conversation.orElse(null));
            return;
        }

        Optional<List<ConversationThreadResponse>> threads = messageRestClient.getThreads();
        writePollResult(response, threads.orElse(null));
    }

    private void writePollResult(HttpServletResponse response, Object payload) throws IOException {
        if (payload == null) {
            if (apiCallStatus.isUnauthorized()) {
                JsonSupport.writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, List.of());
                return;
            }
            JsonSupport.writeJson(response, HttpServletResponse.SC_BAD_GATEWAY, List.of());
            return;
        }
        JsonSupport.writeJson(response, payload);
    }

    private void renderPage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Optional<List<ConversationThreadResponse>> threads = messageRestClient.getThreads();
        if (threads.isEmpty()) {
            if (sessionExpiredRedirect(request, response)) {
                return;
            }
            if (request.getAttribute("error") == null) {
                request.setAttribute("error", "Unable to load your messages right now");
            }
            request.setAttribute("threads", List.of());
        } else {
            request.setAttribute("threads", threads.get());
        }

        Optional<MessageRequestOverviewResponse> overview = messageRestClient.getMessageRequests();
        populateRequestAttributes(request, overview);

        Optional<UUID> counterpart = parseUuid(resolveRecipientParam(request));
        if (counterpart.isPresent()) {
            messageRestClient.markThreadRead(counterpart.get());
            Optional<List<DirectMessageResponse>> conversation =
                    messageRestClient.getConversation(counterpart.get(), null);
            List<DirectMessageResponse> conversationMessages = conversation.orElse(List.of());
            request.setAttribute("conversation", conversationMessages);
            request.setAttribute("activeCounterpartId", counterpart.get().toString());
            request.setAttribute("activeCounterpartName", resolveCounterpartName(request, counterpart.get()));
            applyMessagingState(request, counterpart.get(), conversationMessages, overview.orElse(null));
        } else if (COMPOSE_FLAG.equals(request.getParameter("compose"))) {
            loadComposeResults(request);
        }

        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    // Surfaces the request inbox on every page load (Rule A) via plain request
    // attributes, plus a sidebar badge count following the same opt-in pattern
    // NotificationServlet uses for "sidebarUnreadCount" (see sidebar.jspf).
    private void populateRequestAttributes(HttpServletRequest request, Optional<MessageRequestOverviewResponse> overview) {
        List<MessageRequestResponse> incoming = overview.map(MessageRequestOverviewResponse::getIncoming).orElse(List.of());
        List<MessageRequestResponse> outgoing = overview.map(MessageRequestOverviewResponse::getOutgoing).orElse(List.of());
        request.setAttribute("incomingRequests", incoming);
        request.setAttribute("outgoingRequests", outgoing);

        long pendingIncomingCount = incoming.stream()
                .filter(candidate -> REQUEST_STATUS_PENDING.equals(candidate.getStatus()))
                .count();
        request.setAttribute("pendingIncomingRequestCount", pendingIncomingCount);
        request.setAttribute("sidebarMessageRequestCount", pendingIncomingCount);
    }

    /**
     * Decides, before any send is attempted, whether the active conversation can
     * send freely, must send a request first, or is waiting on one already in
     * flight (Rule B). A non-empty conversation is grandfathered (Rule 3) and
     * always wins; otherwise the requester/target relationship in the request
     * overview decides. If the overview could not be loaded, this defaults to
     * ALLOWED so the existing Send box still renders — a genuine permission
     * problem then surfaces through the 422 fallback in sendMessage() instead.
     */
    private void applyMessagingState(HttpServletRequest request, UUID counterpartId,
                                      List<DirectMessageResponse> conversation,
                                      MessageRequestOverviewResponse overview) {
        if (!conversation.isEmpty()) {
            request.setAttribute("messagingState", MESSAGING_STATE_ALLOWED);
            return;
        }
        if (overview == null) {
            request.setAttribute("messagingState", MESSAGING_STATE_ALLOWED);
            return;
        }

        for (MessageRequestResponse outgoing : overview.getOutgoing()) {
            if (!counterpartId.equals(outgoing.getTargetUserId())) {
                continue;
            }
            if (REQUEST_STATUS_APPROVED.equals(outgoing.getStatus())) {
                request.setAttribute("messagingState", MESSAGING_STATE_ALLOWED);
                return;
            }
            if (REQUEST_STATUS_PENDING.equals(outgoing.getStatus())) {
                request.setAttribute("messagingState", MESSAGING_STATE_PENDING_OUTGOING);
                return;
            }
        }
        for (MessageRequestResponse incoming : overview.getIncoming()) {
            if (!counterpartId.equals(incoming.getRequesterUserId())) {
                continue;
            }
            if (REQUEST_STATUS_APPROVED.equals(incoming.getStatus())) {
                request.setAttribute("messagingState", MESSAGING_STATE_ALLOWED);
                return;
            }
            if (REQUEST_STATUS_PENDING.equals(incoming.getStatus())) {
                request.setAttribute("messagingState", MESSAGING_STATE_PENDING_INCOMING);
                request.setAttribute("incomingRequestId", incoming.getRequestId().toString());
                return;
            }
        }
        request.setAttribute("messagingState", MESSAGING_STATE_NONE);
    }

    // "with" is the historical param used when following a conversation from
    // the thread list; "recipientUserId" is the deep-link param used by the
    // "Message" action on admin-users.jsp and league-members.jsp. Both open
    // the same compose/reply pane for a counterpart, existing thread or not.
    private String resolveRecipientParam(HttpServletRequest request) {
        String with = request.getParameter("with");
        if (with != null && !with.isBlank()) {
            return with;
        }
        return request.getParameter("recipientUserId");
    }

    private void loadComposeResults(HttpServletRequest request) {
        request.setAttribute("composeMode", true);

        String searchTerm = request.getParameter("searchTerm");
        request.setAttribute("composeSearchTerm", searchTerm);

        // Only search once a term has been typed. A blank term matches every
        // user, which would list the whole directory to anyone opening compose.
        if (searchTerm == null || searchTerm.isBlank()) {
            return;
        }

        Optional<List<UserSearchResponse>> results = userSearchRestClient.searchUsers(searchTerm);
        if (results.isEmpty()) {
            request.setAttribute("composeResults", List.of());
            if (request.getAttribute("error") == null) {
                request.setAttribute("error", "Unable to search users right now");
            }
            return;
        }
        request.setAttribute("composeResults", results.get());
    }

    @SuppressWarnings("unchecked")
    private String resolveCounterpartName(HttpServletRequest request, UUID counterpartId) {
        String provided = request.getParameter("name");
        if (provided != null && !provided.isBlank()) {
            return provided;
        }
        Object threadsAttr = request.getAttribute("threads");
        if (threadsAttr instanceof List<?> threads) {
            for (ConversationThreadResponse thread : (List<ConversationThreadResponse>) threads) {
                if (counterpartId.equals(thread.getCounterpartUserId())) {
                    return thread.getCounterpartUsername();
                }
            }
        }
        return "Conversation";
    }

    private void sendMessage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Optional<UUID> recipientId = parseUuid(request.getParameter("recipientUserId"));
        String body = request.getParameter("body");
        if (recipientId.isEmpty() || body == null || body.isBlank()) {
            request.setAttribute("error", "A recipient and a message are required");
            renderPage(request, response);
            return;
        }

        Optional<DirectMessageResponse> sent =
                messageRestClient.send(new SendDirectMessageRequest(recipientId.get(), body.trim()));
        if (sent.isEmpty()) {
            if (sessionExpiredRedirect(request, response)) {
                return;
            }
            request.setAttribute("error", apiCallStatus.getMessage("Unable to send your message"));
            renderPage(request, response);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/messages?with=" + recipientId.get());
    }

    private void toggleBlock(HttpServletRequest request, HttpServletResponse response, boolean block) throws ServletException, IOException {
        Optional<UUID> userId = parseUuid(request.getParameter("userId"));
        if (userId.isEmpty()) {
            request.setAttribute("error", "A valid user is required");
            renderPage(request, response);
            return;
        }

        boolean ok = block ? messageRestClient.block(userId.get()) : messageRestClient.unblock(userId.get());
        if (!ok && sessionExpiredRedirect(request, response)) {
            return;
        }
        response.sendRedirect(request.getContextPath() + "/messages");
    }

    // Rule B primary action for a counterpart with no approved relationship yet.
    private void sendMessageRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Optional<UUID> targetId = parseUuid(request.getParameter("targetUserId"));
        if (targetId.isEmpty()) {
            flashError(request, "A recipient is required to send a message request");
            response.sendRedirect(request.getContextPath() + "/messages");
            return;
        }

        Optional<MessageRequestResponse> created = messageRestClient.createMessageRequest(targetId.get());
        String redirectUrl = conversationRedirect(request, targetId.get());
        if (created.isEmpty()) {
            if (sessionExpiredRedirect(request, response)) {
                return;
            }
            flashError(request, apiCallStatus.getMessage("Unable to send your message request"));
            response.sendRedirect(redirectUrl);
            return;
        }

        flashSuccess(request, "Message request sent.");
        response.sendRedirect(redirectUrl);
    }

    private void respondToRequest(HttpServletRequest request, HttpServletResponse response, boolean approve) throws IOException {
        Optional<UUID> requestId = parseUuid(request.getParameter("requestId"));
        if (requestId.isEmpty()) {
            flashError(request, "A valid request is required");
            redirectAfterRequestAction(request, response);
            return;
        }

        boolean ok = approve
                ? messageRestClient.approveMessageRequest(requestId.get())
                : messageRestClient.rejectMessageRequest(requestId.get());
        if (!ok) {
            if (sessionExpiredRedirect(request, response)) {
                return;
            }
            flashError(request, apiCallStatus.getMessage("Unable to update that request"));
            redirectAfterRequestAction(request, response);
            return;
        }

        flashSuccess(request, approve ? "Message request approved." : "Message request rejected.");
        redirectAfterRequestAction(request, response);
    }

    // Rule C: reporting a single direct message. The reason is optional and the
    // JSP confirms with the user before this ever submits, since a report cannot
    // be undone.
    private void reportMessage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Optional<UUID> messageId = parseUuid(request.getParameter("messageId"));
        if (messageId.isEmpty()) {
            flashError(request, "A valid message is required to report it");
            redirectAfterRequestAction(request, response);
            return;
        }

        boolean ok = messageRestClient.reportDirectMessage(messageId.get(), blankToNull(request.getParameter("reason")));
        if (!ok) {
            if (sessionExpiredRedirect(request, response)) {
                return;
            }
            flashError(request, apiCallStatus.getMessage("Unable to report that message"));
            redirectAfterRequestAction(request, response);
            return;
        }

        flashSuccess(request, "Message reported. An administrator will review it.");
        redirectAfterRequestAction(request, response);
    }

    // approveRequest/rejectRequest/report all carry optional returnWith/returnName
    // hidden fields when they are submitted from inside an open conversation, so
    // the user lands back where they were rather than always on the plain
    // conversation list.
    private void redirectAfterRequestAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String returnWith = request.getParameter("returnWith");
        if (returnWith == null || returnWith.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/messages");
            return;
        }
        String returnName = request.getParameter("returnName");
        String suffix = (returnName == null || returnName.isBlank()) ? "" : "&name=" + encode(returnName);
        response.sendRedirect(request.getContextPath() + "/messages?with=" + encode(returnWith) + suffix);
    }

    private String conversationRedirect(HttpServletRequest request, UUID counterpartId) {
        String name = request.getParameter("targetUsername");
        String suffix = (name == null || name.isBlank()) ? "" : "&name=" + encode(name);
        return request.getContextPath() + "/messages?with=" + counterpartId + suffix;
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    @Override
    public String getServletInfo() {
        return "Messages Servlet: private conversations, sending, blocking, and AJAX polling";
    }
}
