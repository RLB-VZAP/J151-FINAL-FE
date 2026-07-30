<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Messages - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/catalog.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/messages.css">
</head>
<body class="catalog-page msg-page">

<c:set var="activeNav" value="messages" scope="request" />
<%@ include file="/WEB-INF/jspf/sidebar.jspf" %>

<main class="catalog-main" id="messages">
    <div class="catalog-content">

        <header class="catalog-header msg-header">
            <div>
                <p class="catalog-eyebrow">Community</p>
                <h1 class="brand-font">Messages</h1>
                <p class="msg-intro">
                    Direct messages need the other person's agreement first &mdash; ask, and once they accept you can
                    both message freely. Your leagues have their own group chat where no request is needed.
                </p>
            </div>
            <a class="msg-ghost msg-page-link" href="${pageContext.request.contextPath}/league-chat">
                League chat
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M5 12h14"/><path d="M13 6l6 6-6 6"/></svg>
            </a>
        </header>

        <c:if test="${not empty error}">
            <p class="msg-alert msg-alert-error" role="alert"><c:out value="${error}"/></p>
        </c:if>
        <c:if test="${not empty info}">
            <p class="msg-alert msg-alert-info" role="status"><c:out value="${info}"/></p>
        </c:if>

        <%-- Tabs are plain links rather than script: each one is a real URL the
             servlet can render on its own, so the back button and a page reload
             both behave. --%>
        <nav class="msg-tabs" aria-label="Messages sections">
            <a class="msg-tab ${activeTab == 'threads' ? 'is-active' : ''}"
               href="${pageContext.request.contextPath}/messages">Conversations</a>
            <a class="msg-tab ${activeTab == 'requests' ? 'is-active' : ''}"
               href="${pageContext.request.contextPath}/messages?tab=requests">
                Requests
                <c:if test="${fn:length(incomingRequests) > 0}">
                    <span class="msg-badge">${fn:length(incomingRequests)}</span>
                </c:if>
            </a>
            <a class="msg-tab ${activeTab == 'new' ? 'is-active' : ''}"
               href="${pageContext.request.contextPath}/messages?tab=new">New message</a>
        </nav>

        <c:choose>

            <%-- ---------- Requests ---------- --%>
            <c:when test="${activeTab == 'requests'}">
                <section class="msg-section" id="incomingRequestsSection">
                    <div class="msg-section-head">
                        <h2 class="msg-section-title">Asking to message you</h2>
                        <span class="msg-rule-line"></span>
                    </div>

                    <c:choose>
                        <c:when test="${empty incomingRequests}">
                            <p class="msg-empty" id="incomingRequestsEmptyState">No one is waiting on you.</p>
                        </c:when>
                        <c:otherwise>
                            <ul class="msg-request-list" id="incomingRequestsList">
                                <c:forEach var="req" items="${incomingRequests}">
                                    <li class="msg-request">
                                        <span class="msg-avatar">${fn:toUpperCase(fn:substring(req.requesterUsername, 0, 1))}</span>
                                        <div class="msg-request-text">
                                            <span class="msg-request-who"><c:out value="${req.requesterUsername}"/></span>
                                            <c:if test="${not empty req.introMessage}">
                                                <span class="msg-request-note"><c:out value="${req.introMessage}"/></span>
                                            </c:if>
                                        </div>
                                        <div class="msg-request-actions">
                                            <form method="post" action="${pageContext.request.contextPath}/messages">
                                                <%@ include file="/WEB-INF/jspf/csrf-field.jspf" %>
                                                <input type="hidden" name="action" value="acceptRequest">
                                                <input type="hidden" name="requestId" value="${req.requestId}">
                                                <button type="submit" class="btn-gold msg-btn-sm">Accept</button>
                                            </form>
                                            <form method="post" action="${pageContext.request.contextPath}/messages">
                                                <%@ include file="/WEB-INF/jspf/csrf-field.jspf" %>
                                                <input type="hidden" name="action" value="declineRequest">
                                                <input type="hidden" name="requestId" value="${req.requestId}">
                                                <button type="submit" class="msg-ghost msg-btn-sm">Decline</button>
                                            </form>
                                        </div>
                                    </li>
                                </c:forEach>
                            </ul>
                        </c:otherwise>
                    </c:choose>
                </section>

                <section class="msg-section" id="outgoingRequestsSection">
                    <div class="msg-section-head">
                        <h2 class="msg-section-title">Waiting on them</h2>
                        <span class="msg-rule-line"></span>
                    </div>

                    <c:choose>
                        <c:when test="${empty outgoingRequests}">
                            <p class="msg-empty" id="outgoingRequestsEmptyState">You have no pending requests out.</p>
                        </c:when>
                        <c:otherwise>
                            <ul class="msg-request-list" id="outgoingRequestsList">
                                <c:forEach var="req" items="${outgoingRequests}">
                                    <li class="msg-request">
                                        <span class="msg-avatar">${fn:toUpperCase(fn:substring(req.addresseeUsername, 0, 1))}</span>
                                        <div class="msg-request-text">
                                            <span class="msg-request-who"><c:out value="${req.addresseeUsername}"/></span>
                                            <span class="msg-request-note">Waiting for a reply</span>
                                        </div>
                                        <span class="msg-state msg-state-pending">Pending</span>
                                    </li>
                                </c:forEach>
                            </ul>
                        </c:otherwise>
                    </c:choose>
                </section>
            </c:when>

            <%-- ---------- New message ---------- --%>
            <c:when test="${activeTab == 'new'}">
                <section class="msg-section" id="newMessageSection">
                    <div class="msg-section-head">
                        <h2 class="msg-section-title">Find someone to message</h2>
                        <span class="msg-rule-line"></span>
                    </div>

                    <form method="get" action="${pageContext.request.contextPath}/messages" class="msg-search-form">
                        <input type="hidden" name="tab" value="new">
                        <div class="msg-field msg-field-grow">
                            <label class="msg-label" for="contactSearch">Search by username or email</label>
                            <input class="msg-input" type="text" id="contactSearch" name="q"
                                   value="${fn:escapeXml(contactSearch)}" placeholder="e.g. jarryd">
                        </div>
                        <button type="submit" class="msg-ghost">Search</button>
                    </form>

                    <c:choose>
                        <c:when test="${empty contacts}">
                            <p class="msg-empty" id="contactsEmptyState">
                                <c:choose>
                                    <c:when test="${not empty contactSearch}">No users match &ldquo;<c:out value="${contactSearch}"/>&rdquo;.</c:when>
                                    <c:otherwise>No other users to message yet.</c:otherwise>
                                </c:choose>
                            </p>
                        </c:when>
                        <c:otherwise>
                            <ul class="msg-contact-list" id="contactsList">
                                <c:forEach var="contact" items="${contacts}">
                                    <li class="msg-contact">
                                        <span class="msg-avatar">${fn:toUpperCase(fn:substring(contact.username, 0, 1))}</span>
                                        <span class="msg-contact-name"><c:out value="${contact.username}"/></span>

                                        <%-- One branch per MessageContactState, so the only
                                             action offered is the one that will actually work. --%>
                                        <c:choose>
                                            <c:when test="${contact.state == 'ACCEPTED'}">
                                                <a class="msg-ghost msg-btn-sm"
                                                   href="${pageContext.request.contextPath}/messages?with=${contact.userId}&name=${fn:escapeXml(contact.username)}">Open chat</a>
                                            </c:when>
                                            <c:when test="${contact.state == 'REQUEST_SENT'}">
                                                <span class="msg-state msg-state-pending">Request sent</span>
                                            </c:when>
                                            <c:when test="${contact.state == 'REQUEST_RECEIVED'}">
                                                <form method="post" action="${pageContext.request.contextPath}/messages">
                                                    <%@ include file="/WEB-INF/jspf/csrf-field.jspf" %>
                                                    <input type="hidden" name="action" value="acceptRequest">
                                                    <input type="hidden" name="requestId" value="${contact.requestId}">
                                                    <button type="submit" class="btn-gold msg-btn-sm">Accept their request</button>
                                                </form>
                                            </c:when>
                                            <c:when test="${contact.state == 'BLOCKED'}">
                                                <span class="msg-state msg-state-blocked">Blocked</span>
                                            </c:when>
                                            <c:otherwise>
                                                <%-- NONE or DECLINED: asking again is allowed. --%>
                                                <form method="post" action="${pageContext.request.contextPath}/messages" class="msg-request-form">
                                                    <%@ include file="/WEB-INF/jspf/csrf-field.jspf" %>
                                                    <input type="hidden" name="action" value="requestAccess">
                                                    <input type="hidden" name="addresseeUserId" value="${contact.userId}">
                                                    <label class="msg-visually-hidden" for="intro-${contact.userId}">Note</label>
                                                    <input class="msg-input msg-input-sm" type="text" id="intro-${contact.userId}"
                                                           name="introMessage" maxlength="255" placeholder="Optional note">
                                                    <button type="submit" class="btn-gold msg-btn-sm">
                                                        ${contact.state == 'DECLINED' ? 'Ask again' : 'Request'}
                                                    </button>
                                                </form>
                                            </c:otherwise>
                                        </c:choose>
                                    </li>
                                </c:forEach>
                            </ul>
                        </c:otherwise>
                    </c:choose>
                </section>
            </c:when>

            <%-- ---------- Conversations ---------- --%>
            <c:otherwise>
                <div class="msg-grid">

                    <section class="msg-panel msg-thread-panel" aria-label="Conversations">
                        <h2 class="msg-panel-title">Conversations</h2>

                        <c:choose>
                            <c:when test="${empty threads}">
                                <p class="msg-empty msg-empty-sm" id="threadsEmptyState">
                                    No conversations yet. Use
                                    <a href="${pageContext.request.contextPath}/messages?tab=new">New message</a>
                                    to ask someone first.
                                </p>
                            </c:when>
                            <c:otherwise>
                                <ul class="msg-thread-list" id="threadList">
                                    <c:forEach var="thread" items="${threads}">
                                        <li>
                                            <%-- activeCounterpartId is a String and counterpartUserId a UUID,
                                                 so compare their string forms; EL's == would never match. --%>
                                            <a class="msg-thread ${activeCounterpartId eq thread.counterpartUserId.toString() ? 'is-active' : ''}"
                                               href="${pageContext.request.contextPath}/messages?with=${thread.counterpartUserId}&name=${fn:escapeXml(thread.counterpartUsername)}">
                                                <span class="msg-avatar">${fn:toUpperCase(fn:substring(thread.counterpartUsername, 0, 1))}</span>
                                                <span class="msg-thread-text">
                                                    <span class="msg-thread-name"><c:out value="${thread.counterpartUsername}"/></span>
                                                    <span class="msg-thread-preview"><c:out value="${thread.lastMessageBody}"/></span>
                                                </span>
                                                <c:if test="${thread.unreadCount > 0}">
                                                    <span class="msg-badge">${thread.unreadCount}</span>
                                                </c:if>
                                            </a>
                                        </li>
                                    </c:forEach>
                                </ul>
                            </c:otherwise>
                        </c:choose>
                    </section>

                    <section class="msg-panel msg-conversation-panel" aria-label="Conversation">
                        <c:choose>
                            <c:when test="${empty activeCounterpartId}">
                                <p class="msg-empty msg-empty-sm" id="conversationEmptyState">Select a conversation to start messaging.</p>
                            </c:when>
                            <c:otherwise>
                                <header class="msg-conversation-head">
                                    <h2 class="msg-panel-title"><c:out value="${activeCounterpartName}"/></h2>
                                    <form method="post" action="${pageContext.request.contextPath}/messages"
                                          onsubmit="return confirm('Block this user? You will no longer be able to message each other.');">
                                        <%@ include file="/WEB-INF/jspf/csrf-field.jspf" %>
                                        <input type="hidden" name="action" value="block">
                                        <input type="hidden" name="userId" value="${activeCounterpartId}">
                                        <button type="submit" class="msg-ghost msg-btn-sm msg-btn-danger">Block</button>
                                    </form>
                                </header>

                                <div id="conversationMessages" class="msg-bubbles"
                                     data-context-path="${pageContext.request.contextPath}"
                                     data-counterpart="${activeCounterpartId}">
                                    <c:choose>
                                        <c:when test="${empty conversation}">
                                            <p class="msg-empty msg-empty-sm" id="noMessagesYetState">No messages yet &mdash; say hello.</p>
                                        </c:when>
                                        <c:otherwise>
                                            <c:forEach var="message" items="${conversation}">
                                                <div class="msg-bubble ${message.mine ? 'is-mine' : 'is-theirs'}"
                                                     data-created-at="${message.createdAt}">
                                                    <p class="msg-bubble-body"><c:out value="${message.body}"/></p>
                                                    <span class="msg-bubble-time">${fn:substring(fn:replace(message.createdAt, 'T', ' '), 0, 16)}</span>
                                                </div>
                                            </c:forEach>
                                        </c:otherwise>
                                    </c:choose>
                                </div>

                                <form id="sendForm" method="post" action="${pageContext.request.contextPath}/messages" class="msg-send-form">
                                    <%@ include file="/WEB-INF/jspf/csrf-field.jspf" %>
                                    <input type="hidden" name="action" value="send">
                                    <input type="hidden" name="recipientUserId" value="${activeCounterpartId}">
                                    <label class="msg-visually-hidden" for="messageBody">Message</label>
                                    <textarea class="msg-input msg-textarea" id="messageBody" name="body" rows="2"
                                              placeholder="Type a message…" required></textarea>
                                    <button type="submit" class="btn-gold msg-send-btn">Send</button>
                                </form>
                            </c:otherwise>
                        </c:choose>
                    </section>

                </div>
            </c:otherwise>
        </c:choose>

    </div>
</main>

<script src="${pageContext.request.contextPath}/assets/js/messages.js" defer></script>
</body>
</html>
