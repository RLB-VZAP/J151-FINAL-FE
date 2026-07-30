package za.ac.vzap.trytons.frontend.servlet.message;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.message.ConversationThreadResponse;
import za.ac.vzap.trytons.frontend.client.message.CreateMessageRequestRequest;
import za.ac.vzap.trytons.frontend.client.message.DirectMessageResponse;
import za.ac.vzap.trytons.frontend.client.message.MessageContactResponse;
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

    @Inject
    private MessageRestClient messageRestClient;

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
            case "requestAccess" -> createRequest(request, response);
            case "acceptRequest" -> respondToRequest(request, response, true);
            case "declineRequest" -> respondToRequest(request, response, false);
            default -> {
                request.setAttribute("error", "Unknown message action requested");
                renderPage(request, response);
            }
        }
    }

    private void createRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Optional<UUID> addresseeId = parseUuid(request.getParameter("addresseeUserId"));
        if (addresseeId.isEmpty()) {
            request.setAttribute("error", "A valid user is required to send a message request");
            renderPage(request, response);
            return;
        }

        Optional<MessageRequestResponse> created = messageRestClient.createRequest(
                new CreateMessageRequestRequest(addresseeId.get(), request.getParameter("introMessage")));
        if (created.isEmpty()) {
            if (sessionExpiredRedirect(request, response)) {
                return;
            }
            // The backend's own wording is more useful than a generic failure
            // here: it distinguishes "already asked", "they asked you first" and
            // "you can already message this user".
            request.setAttribute("error", apiCallStatus.getMessage("Unable to send your message request"));
            renderPage(request, response);
            return;
        }

        flashSuccess(request, "Message request sent to " + created.get().getAddresseeUsername());
        redirectTo(response, request, "/messages?tab=requests");
    }

    private void respondToRequest(HttpServletRequest request, HttpServletResponse response, boolean accept) throws ServletException, IOException {
        Optional<UUID> requestId = parseUuid(request.getParameter("requestId"));
        if (requestId.isEmpty()) {
            request.setAttribute("error", "A valid message request is required");
            renderPage(request, response);
            return;
        }

        Optional<MessageRequestResponse> answered = accept
                ? messageRestClient.acceptRequest(requestId.get())
                : messageRestClient.declineRequest(requestId.get());
        if (answered.isEmpty()) {
            if (sessionExpiredRedirect(request, response)) {
                return;
            }
            request.setAttribute("error", apiCallStatus.getMessage("Unable to answer that message request"));
            renderPage(request, response);
            return;
        }

        String counterpart = answered.get().getRequesterUsername();
        if (accept) {
            // Accepting opens the conversation, so land straight in it rather
            // than back on a requests list the request has just left. The name
            // rides along because a just-accepted conversation has no messages
            // yet and so no thread for resolveCounterpartName to read it from.
            flashSuccess(request, "You can now message " + counterpart);
            redirectTo(response, request, "/messages?with=" + answered.get().getRequesterUserId()
                    + "&name=" + URLEncoder.encode(counterpart, StandardCharsets.UTF_8));
            return;
        }
        flashInfo(request, "Declined the request from " + counterpart);
        redirectTo(response, request, "/messages?tab=requests");
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

        // Requests are always loaded: the badge on the Requests tab has to be
        // right whichever tab is showing, and the count is small.
        request.setAttribute("incomingRequests", messageRestClient.listIncomingRequests().orElse(List.of()));
        request.setAttribute("outgoingRequests", messageRestClient.listOutgoingRequests().orElse(List.of()));

        // The directory is only needed by the New message tab, and it is the one
        // call here that grows with the user table, so it is loaded on demand.
        String tab = request.getParameter("tab");
        if ("new".equals(tab)) {
            String searchTerm = request.getParameter("q");
            request.setAttribute("contacts", messageRestClient.listContacts(searchTerm).orElse(List.of()));
            request.setAttribute("contactSearch", searchTerm);
        }
        request.setAttribute("activeTab", tab == null || tab.isBlank() ? "threads" : tab);

        String with = request.getParameter("with");
        Optional<UUID> counterpart = parseUuid(with);
        if (counterpart.isPresent()) {
            messageRestClient.markThreadRead(counterpart.get());
            Optional<List<DirectMessageResponse>> conversation =
                    messageRestClient.getConversation(counterpart.get(), null);
            request.setAttribute("conversation", conversation.orElse(List.of()));
            request.setAttribute("activeCounterpartId", counterpart.get().toString());
            request.setAttribute("activeCounterpartName", resolveCounterpartName(request, counterpart.get()));
        }

        request.getRequestDispatcher(VIEW).forward(request, response);
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

    @Override
    public String getServletInfo() {
        return "Messages Servlet: private conversations, sending, blocking, and AJAX polling";
    }
}
