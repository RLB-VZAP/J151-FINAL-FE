<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="za.ac.vzap.trytons.frontend.client.message.LeagueMessageResponse" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>League Chat - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/league-chat.css">
</head>
<body class="league-chat">
<%@ include file="/WEB-INF/jspf/navigation.jspf" %>

<main id="leagueChat">
    <h1>Chat &mdash; <c:out value="${leagueName}"/></h1>

    <c:if test="${not empty error}">
        <p class="error-message" role="alert"><c:out value="${error}"/></p>
    </c:if>
    <c:if test="${not empty info}">
        <p class="info-message" role="status"><c:out value="${info}"/></p>
    </c:if>
    <c:if test="${not empty success}">
        <p class="success-message" role="status"><c:out value="${success}"/></p>
    </c:if>

    <c:if test="${not empty leagueId}">
        <div id="leagueFeed"
             data-context-path="${pageContext.request.contextPath}"
             data-league-id="${fn:escapeXml(leagueId)}"
             data-current-user="${fn:escapeXml(sessionScope.userId)}">
            <%
                List<LeagueMessageResponse> messages =
                        (List<LeagueMessageResponse>) request.getAttribute("messages");
                String currentUserId = String.valueOf(session.getAttribute("userId"));
                if (messages == null || messages.isEmpty()) {
            %>
                <p class="empty-state">No messages yet. Say hello!</p>
            <%
                } else {
                    for (LeagueMessageResponse message : messages) {
                        if (message == null) { continue; }
                        pageContext.setAttribute("message", message);
                        boolean mine = currentUserId != null
                                && currentUserId.equals(String.valueOf(message.getSenderUserId()));
            %>
                <div class="bubble <%= mine ? "mine" : "theirs" %>" data-created-at="<%= message.getCreatedAt() %>">
                    <span class="bubble-author"><c:out value="${message.senderUsername}"/></span>
                    <p class="bubble-body"><c:out value="${message.body}"/></p>
                    <span class="bubble-time"><c:out value="${message.createdAt}"/></span>
                </div>
            <%      }
                }
            %>
        </div>

        <form id="leagueSendForm" method="post" action="${pageContext.request.contextPath}/league-chat" class="send-form">
            <input type="hidden" name="action" value="send">
            <input type="hidden" name="leagueId" value="${fn:escapeXml(leagueId)}">
            <label for="leagueMessageBody" class="visually-hidden">Message</label>
            <textarea id="leagueMessageBody" name="body" rows="2" placeholder="Message the league…" required></textarea>
            <button type="submit">Send</button>
        </form>
    </c:if>
</main>

<script src="${pageContext.request.contextPath}/assets/js/league-chat.js" defer></script>
</body>
</html>
