<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="za.ac.vzap.trytons.frontend.client.message.ConversationThreadResponse" %>
<%@ page import="za.ac.vzap.trytons.frontend.client.message.DirectMessageResponse" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Messages - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/messages.css">
</head>
<body class="messages">

<c:set var="activeNav" value="messages" scope="request" />
<%@ include file="/WEB-INF/jspf/sidebar.jspf" %>

<main id="messages">
    <h1>Messages</h1>

    <c:if test="${not empty error}">
        <p class="error-message alert alert-danger" role="alert"><c:out value="${error}"/></p>
    </c:if>

    <div class="row g-3">
        <section class="thread-list card col-md-4" aria-label="Conversations">
            <h2>Conversations</h2>
            <%
                List<ConversationThreadResponse> threads =
                        (List<ConversationThreadResponse>) request.getAttribute("threads");
                String activeId = (String) request.getAttribute("activeCounterpartId");
            %>
            <% if (threads == null || threads.isEmpty()) { %>
                <p class="empty-state">No conversations yet.</p>
            <% } else {
                for (ConversationThreadResponse thread : threads) {
                    if (thread == null) { continue; }
                    pageContext.setAttribute("thread", thread);
                    boolean active = activeId != null && activeId.equals(String.valueOf(thread.getCounterpartUserId()));
            %>
                <a class="thread <%= active ? "is-active" : "" %>"
                   href="${pageContext.request.contextPath}/messages?with=${thread.counterpartUserId}&name=${fn:escapeXml(thread.counterpartUsername)}">
                    <span class="thread-name"><c:out value="${thread.counterpartUsername}"/></span>
                    <c:if test="${thread.unreadCount > 0}">
                        <span class="unread-badge badge">${thread.unreadCount}</span>
                    </c:if>
                    <span class="thread-preview"><c:out value="${thread.lastMessageBody}"/></span>
                </a>
            <%  } } %>
        </section>

        <section class="conversation card col-md-8" aria-label="Conversation">
            <% if (activeId == null) { %>
                <p class="empty-state">Select a conversation to start messaging.</p>
            <% } else {
                String counterpartName = String.valueOf(request.getAttribute("activeCounterpartName"));
                List<DirectMessageResponse> conversation =
                        (List<DirectMessageResponse>) request.getAttribute("conversation");
            %>
                <header class="conversation-header">
                    <h2><c:out value="${activeCounterpartName}"/></h2>
                    <form method="post" action="${pageContext.request.contextPath}/messages"
                          onsubmit="return confirm('Block this user? You will no longer be able to message each other.');">
                        <%@ include file="/WEB-INF/jspf/csrf-field.jspf" %>
                        <input type="hidden" name="action" value="block">
                        <input type="hidden" name="userId" value="<%= activeId %>">
                        <button type="submit" class="btn btn-outline-danger btn-sm">Block</button>
                    </form>
                </header>

                <div id="conversationMessages"
                     data-context-path="${pageContext.request.contextPath}"
                     data-counterpart="<%= activeId %>">
                    <%
                        if (conversation != null) {
                            for (DirectMessageResponse message : conversation) {
                                if (message == null) { continue; }
                                pageContext.setAttribute("message", message);
                    %>
                        <div class="bubble <%= message.isMine() ? "mine" : "theirs" %>"
                             data-created-at="<%= message.getCreatedAt() %>">
                            <p class="bubble-body"><c:out value="${message.body}"/></p>
                            <span class="bubble-time"><c:out value="${message.createdAt}"/></span>
                        </div>
                    <%      }
                        }
                    %>
                </div>

                <form id="sendForm" method="post" action="${pageContext.request.contextPath}/messages" class="d-flex gap-2 mt-3">
                    <%@ include file="/WEB-INF/jspf/csrf-field.jspf" %>
                    <input type="hidden" name="action" value="send">
                    <input type="hidden" name="recipientUserId" value="<%= activeId %>">
                    <label for="messageBody" class="visually-hidden">Message</label>
                    <textarea id="messageBody" name="body" rows="2" placeholder="Type a message…" required class="form-control"></textarea>
                    <button type="submit" class="btn btn-gold">Send</button>
                </form>
            <% } %>
        </section>
    </div>
</main>

<script src="${pageContext.request.contextPath}/assets/js/messages.js" defer></script>
</body>
</html>
