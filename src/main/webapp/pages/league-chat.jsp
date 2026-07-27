<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="za.ac.vzap.trytons.frontend.client.message.LeagueMessageResponse" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>League Chat - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/league-chat.css">
</head>
<body class="league-chat">

<c:set var="activeNav" value="leagues" scope="request" />
<%@ include file="/WEB-INF/jspf/sidebar.jspf" %>

<main id="leagueChat">
    <h1>Chat &mdash; <c:out value="${leagueName}"/></h1>

    <c:if test="${not empty error}">
        <p class="error-message alert alert-danger" role="alert"><c:out value="${error}"/></p>
    </c:if>
    <c:if test="${not empty info}">
        <p class="info-message alert alert-info" role="status"><c:out value="${info}"/></p>
    </c:if>
    <c:if test="${not empty success}">
        <p class="success-message alert alert-success" role="status"><c:out value="${success}"/></p>
    </c:if>

    <c:if test="${not empty leagueId}">
        <div id="leagueFeed" class="card"
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
                    <div class="bubble-footer">
                        <span class="bubble-time"><c:out value="${message.createdAt}"/></span>
                        <%-- Rule C: report a single message. Reporting cannot be undone, so the
                             browser confirms before this ever submits. --%>
                        <form method="post" action="${pageContext.request.contextPath}/league-chat" class="report-form"
                              onsubmit="return confirm('Report this message to an administrator? This cannot be undone.');">
                            <%@ include file="/WEB-INF/jspf/csrf-field.jspf" %>
                            <input type="hidden" name="action" value="report">
                            <input type="hidden" name="leagueId" value="${fn:escapeXml(leagueId)}">
                            <input type="hidden" name="messageId" value="${message.messageId}">
                            <input type="text" name="reason" maxlength="200" placeholder="Reason (optional)" class="report-reason">
                            <button type="submit" class="report-btn">Report</button>
                        </form>
                    </div>
                </div>
            <%      }
                }
            %>
        </div>

        <%-- Administrators can monitor any league's chat but are not members, so the
             backend rejects a post from them; the send box is withheld to match. --%>
        <c:if test="${sessionScope.role != 'ADMINISTRATOR'}">
            <form id="leagueSendForm" method="post" action="${pageContext.request.contextPath}/league-chat" class="d-flex gap-2 mt-3">
                <%@ include file="/WEB-INF/jspf/csrf-field.jspf" %>
                <input type="hidden" name="action" value="send">
                <input type="hidden" name="leagueId" value="${fn:escapeXml(leagueId)}">
                <label for="leagueMessageBody" class="visually-hidden">Message</label>
                <textarea id="leagueMessageBody" name="body" rows="2" placeholder="Message the league…" required class="form-control"></textarea>
                <button type="submit" class="btn btn-gold">Send</button>
            </form>
        </c:if>
    </c:if>
</main>

<script src="${pageContext.request.contextPath}/assets/js/league-chat.js" defer></script>
</body>
</html>
