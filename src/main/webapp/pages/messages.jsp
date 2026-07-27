<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="za.ac.vzap.trytons.frontend.client.message.ConversationThreadResponse" %>
<%@ page import="za.ac.vzap.trytons.frontend.client.message.DirectMessageResponse" %>
<%@ page import="za.ac.vzap.trytons.frontend.client.message.MessageRequestResponse" %>
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
        <div class="col-md-4 d-flex flex-column gap-3 messages-sidebar-col">
        <section class="thread-list card" aria-label="Conversations">
            <div class="thread-list-head">
                <h2>Conversations</h2>
                <a class="btn btn-gold btn-sm" href="${pageContext.request.contextPath}/messages?compose=1">New message</a>
            </div>
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

        <%-- Rule A: request inbox lives alongside Conversations rather than as its
             own nav entry. Collapsed by <details> when there is nothing pending,
             expanded automatically when an incoming request needs a decision. --%>
        <%
            List<MessageRequestResponse> incomingRequests =
                    (List<MessageRequestResponse>) request.getAttribute("incomingRequests");
            List<MessageRequestResponse> outgoingRequests =
                    (List<MessageRequestResponse>) request.getAttribute("outgoingRequests");
            Long pendingIncomingCountAttr = (Long) request.getAttribute("pendingIncomingRequestCount");
            long pendingIncomingCount = pendingIncomingCountAttr == null ? 0L : pendingIncomingCountAttr;
        %>
        <details class="requests-panel card" <%= pendingIncomingCount > 0 ? "open" : "" %>>
            <summary class="requests-summary">
                <h2>Requests</h2>
                <% if (pendingIncomingCount > 0) { %>
                    <span class="unread-badge badge"><%= pendingIncomingCount %></span>
                <% } %>
            </summary>

            <div class="requests-body">
                <p class="requests-subhead">Incoming</p>
                <% if (incomingRequests == null || incomingRequests.isEmpty()) { %>
                    <p class="empty-state">No incoming requests.</p>
                <% } else {
                    for (MessageRequestResponse incoming : incomingRequests) {
                        if (incoming == null) { continue; }
                        pageContext.setAttribute("req", incoming);
                %>
                    <div class="request-item">
                        <div class="request-item-info">
                            <span class="request-name"><c:out value="${req.requesterUsername}"/></span>
                            <span class="request-status status-${fn:toLowerCase(req.status)}"><c:out value="${req.status}"/></span>
                        </div>
                        <c:if test="${req.status == 'PENDING'}">
                            <div class="request-actions">
                                <form method="post" action="${pageContext.request.contextPath}/messages">
                                    <%@ include file="/WEB-INF/jspf/csrf-field.jspf" %>
                                    <input type="hidden" name="action" value="approveRequest">
                                    <input type="hidden" name="requestId" value="<%= incoming.getRequestId() %>">
                                    <button type="submit" class="btn btn-gold btn-sm">Approve</button>
                                </form>
                                <form method="post" action="${pageContext.request.contextPath}/messages">
                                    <%@ include file="/WEB-INF/jspf/csrf-field.jspf" %>
                                    <input type="hidden" name="action" value="rejectRequest">
                                    <input type="hidden" name="requestId" value="<%= incoming.getRequestId() %>">
                                    <button type="submit" class="btn btn-outline-danger btn-sm">Reject</button>
                                </form>
                            </div>
                        </c:if>
                    </div>
                <% } } %>

                <p class="requests-subhead">Outgoing</p>
                <% if (outgoingRequests == null || outgoingRequests.isEmpty()) { %>
                    <p class="empty-state">No outgoing requests.</p>
                <% } else {
                    for (MessageRequestResponse outgoing : outgoingRequests) {
                        if (outgoing == null) { continue; }
                        pageContext.setAttribute("req", outgoing);
                %>
                    <div class="request-item">
                        <div class="request-item-info">
                            <span class="request-name"><c:out value="${req.targetUsername}"/></span>
                            <span class="request-status status-${fn:toLowerCase(req.status)}"><c:out value="${req.status}"/></span>
                        </div>
                    </div>
                <% } } %>
            </div>
        </details>
        </div>

        <section class="conversation card col-md-8" aria-label="Conversation">
            <% if (activeId == null && !Boolean.TRUE.equals(request.getAttribute("composeMode"))) { %>
                <p class="empty-state">Select a conversation to start messaging.</p>
            <% } else if (activeId == null) { %>
                <div class="compose-panel">
                    <h2>New message</h2>
                    <form method="get" action="${pageContext.request.contextPath}/messages" class="compose-search-form">
                        <input type="hidden" name="compose" value="1">
                        <label for="composeSearchTerm" class="visually-hidden">Search by username</label>
                        <input type="text" id="composeSearchTerm" name="searchTerm"
                               value="${fn:escapeXml(composeSearchTerm)}"
                               placeholder="Search by username" class="form-control">
                        <button type="submit" class="btn btn-gold btn-sm">Search</button>
                    </form>
                    <%-- Results appear only once a term has been searched. Listing the
                         directory unprompted would expose every user to anyone opening
                         compose, so the pre-search state is a prompt, not a roster.
                         Results carry username only — the backend search endpoint
                         (GET /api/users/search) never returns email or role. --%>
                    <c:choose>
                        <c:when test="${empty composeSearchTerm}">
                            <p class="empty-state">Search for someone by username to start a conversation.</p>
                        </c:when>
                        <c:when test="${empty composeResults}">
                            <p class="empty-state">No users match &ldquo;<c:out value="${composeSearchTerm}"/>&rdquo;.</p>
                        </c:when>
                        <c:otherwise>
                            <ul class="compose-results">
                                <c:forEach var="candidate" items="${composeResults}">
                                    <c:if test="${candidate.userId ne sessionScope.userId}">
                                        <li class="compose-result">
                                            <span class="compose-result-name"><c:out value="${candidate.username}"/></span>
                                            <a class="btn btn-outline-light btn-sm"
                                               href="${pageContext.request.contextPath}/messages?recipientUserId=${candidate.userId}&name=${fn:escapeXml(candidate.username)}">Message</a>
                                        </li>
                                    </c:if>
                                </c:forEach>
                            </ul>
                        </c:otherwise>
                    </c:choose>
                </div>
            <% } else {
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
                            <div class="bubble-footer">
                                <span class="bubble-time"><c:out value="${message.createdAt}"/></span>
                                <%-- Rule C: report a single message. Reporting cannot be undone, so the
                                     browser confirms before this ever submits. --%>
                                <form method="post" action="${pageContext.request.contextPath}/messages" class="report-form"
                                      onsubmit="return confirm('Report this message to an administrator? This cannot be undone.');">
                                    <%@ include file="/WEB-INF/jspf/csrf-field.jspf" %>
                                    <input type="hidden" name="action" value="report">
                                    <input type="hidden" name="messageId" value="${message.messageId}">
                                    <input type="hidden" name="returnWith" value="${activeCounterpartId}">
                                    <input type="hidden" name="returnName" value="${fn:escapeXml(activeCounterpartName)}">
                                    <input type="text" name="reason" maxlength="200" placeholder="Reason (optional)" class="report-reason">
                                    <button type="submit" class="report-btn">Report</button>
                                </form>
                            </div>
                        </div>
                    <%      }
                        }
                    %>
                </div>

                <%-- Rule B: the primary action for this counterpart depends on whether
                     there is an approved relationship yet — computed server-side in
                     MessagesServlet.applyMessagingState() from the request overview and
                     the conversation itself (an existing thread is grandfathered). --%>
                <c:choose>
                    <c:when test="${messagingState == 'PENDING_OUTGOING'}">
                        <div class="messaging-state-notice">
                            <p>Message request sent — waiting for <c:out value="${activeCounterpartName}"/> to approve it.</p>
                        </div>
                    </c:when>
                    <c:when test="${messagingState == 'PENDING_INCOMING'}">
                        <div class="messaging-state-notice">
                            <p><c:out value="${activeCounterpartName}"/> has asked to message you. Approve their
                                request to start chatting.</p>
                            <div class="request-actions">
                                <form method="post" action="${pageContext.request.contextPath}/messages">
                                    <%@ include file="/WEB-INF/jspf/csrf-field.jspf" %>
                                    <input type="hidden" name="action" value="approveRequest">
                                    <input type="hidden" name="requestId" value="${incomingRequestId}">
                                    <input type="hidden" name="returnWith" value="${activeCounterpartId}">
                                    <input type="hidden" name="returnName" value="${fn:escapeXml(activeCounterpartName)}">
                                    <button type="submit" class="btn btn-gold btn-sm">Approve</button>
                                </form>
                                <form method="post" action="${pageContext.request.contextPath}/messages">
                                    <%@ include file="/WEB-INF/jspf/csrf-field.jspf" %>
                                    <input type="hidden" name="action" value="rejectRequest">
                                    <input type="hidden" name="requestId" value="${incomingRequestId}">
                                    <input type="hidden" name="returnWith" value="${activeCounterpartId}">
                                    <input type="hidden" name="returnName" value="${fn:escapeXml(activeCounterpartName)}">
                                    <button type="submit" class="btn btn-outline-danger btn-sm">Reject</button>
                                </form>
                            </div>
                        </div>
                    </c:when>
                    <c:when test="${messagingState == 'NONE'}">
                        <form method="post" action="${pageContext.request.contextPath}/messages" class="request-send-form">
                            <%@ include file="/WEB-INF/jspf/csrf-field.jspf" %>
                            <input type="hidden" name="action" value="request">
                            <input type="hidden" name="targetUserId" value="${activeCounterpartId}">
                            <input type="hidden" name="targetUsername" value="${fn:escapeXml(activeCounterpartName)}">
                            <p class="messaging-state-hint">You need this person's permission before you can message them.</p>
                            <button type="submit" class="btn btn-gold">Send message request</button>
                        </form>
                    </c:when>
                    <c:otherwise>
                        <form id="sendForm" method="post" action="${pageContext.request.contextPath}/messages" class="d-flex gap-2 mt-3">
                            <%@ include file="/WEB-INF/jspf/csrf-field.jspf" %>
                            <input type="hidden" name="action" value="send">
                            <input type="hidden" name="recipientUserId" value="<%= activeId %>">
                            <label for="messageBody" class="visually-hidden">Message</label>
                            <textarea id="messageBody" name="body" rows="2" placeholder="Type a message…" required class="form-control"></textarea>
                            <button type="submit" class="btn btn-gold">Send</button>
                        </form>
                    </c:otherwise>
                </c:choose>
            <% } %>
        </section>
    </div>
</main>

<script src="${pageContext.request.contextPath}/assets/js/messages.js" defer></script>
</body>
</html>
