<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="za.ac.vzap.trytons.frontend.client.message.PendingLeagueMessageResponse" %>
<%@ page import="za.ac.vzap.trytons.frontend.client.message.BlockedPhraseResponse" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Message Moderation - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-message-moderation.css">
</head>
<body class="admin-message-moderation">

<c:set var="activeNav" value="admin-message-moderation" scope="request" />
<%@ include file="/WEB-INF/jspf/sidebar.jspf" %>

<main id="messageModeration">
    <h1>Message Moderation</h1>

    <c:if test="${not empty error}">
        <p class="error-message alert alert-danger" role="alert"><c:out value="${error}"/></p>
    </c:if>

    <section id="pendingQueue" class="card">
        <h2>Flagged messages awaiting review</h2>
        <%
            List<PendingLeagueMessageResponse> pending =
                    (List<PendingLeagueMessageResponse>) request.getAttribute("pendingMessages");
        %>
        <% if (pending == null || pending.isEmpty()) { %>
            <p class="empty-state">There are no flagged messages to review.</p>
        <% } else { %>
            <table class="moderation-table table table-dark table-hover align-middle">
                <thead>
                    <tr>
                        <th>League</th>
                        <th>Sender</th>
                        <th>Message</th>
                        <th>Flagged for</th>
                        <th>Received</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <%
                        for (PendingLeagueMessageResponse message : pending) {
                            if (message == null) { continue; }
                            pageContext.setAttribute("message", message);
                    %>
                    <tr>
                        <td><c:out value="${message.leagueName}"/></td>
                        <td><c:out value="${message.senderUsername}"/></td>
                        <td class="message-body"><c:out value="${message.body}"/></td>
                        <td><span class="flag-reason badge"><c:out value="${message.flaggedReason}"/></span></td>
                        <td><c:out value="${message.createdAt}"/></td>
                        <td class="actions d-flex gap-2">
                            <form method="post" action="${pageContext.request.contextPath}/admin/message-moderation">
                                <%@ include file="/WEB-INF/jspf/csrf-field.jspf" %>
                                <input type="hidden" name="action" value="approve">
                                <input type="hidden" name="messageId" value="<%= message.getMessageId() %>">
                                <button type="submit" class="btn btn-gold btn-sm">Approve</button>
                            </form>
                            <form method="post" action="${pageContext.request.contextPath}/admin/message-moderation"
                                  onsubmit="return confirm('Reject and permanently remove this message?');">
                                <%@ include file="/WEB-INF/jspf/csrf-field.jspf" %>
                                <input type="hidden" name="action" value="reject">
                                <input type="hidden" name="messageId" value="<%= message.getMessageId() %>">
                                <button type="submit" class="btn btn-outline-danger btn-sm">Reject</button>
                            </form>
                        </td>
                    </tr>
                    <% } %>
                </tbody>
            </table>
        <% } %>
    </section>

    <section id="blocklistManager" class="card">
        <h2>Blocked words</h2>

        <form method="post" action="${pageContext.request.contextPath}/admin/message-moderation" class="add-phrase-form d-flex gap-2 mb-4">
            <%@ include file="/WEB-INF/jspf/csrf-field.jspf" %>
            <input type="hidden" name="action" value="addPhrase">
            <label for="phrase" class="visually-hidden">Phrase</label>
            <input type="text" id="phrase" name="phrase" maxlength="100" placeholder="Add a word or phrase…" required class="form-control">
            <button type="submit" class="btn btn-gold">Add</button>
        </form>

        <%
            List<BlockedPhraseResponse> blocklist =
                    (List<BlockedPhraseResponse>) request.getAttribute("blocklist");
        %>
        <% if (blocklist == null || blocklist.isEmpty()) { %>
            <p class="empty-state">The blocklist is empty.</p>
        <% } else { %>
            <ul class="blocklist">
                <%
                    for (BlockedPhraseResponse phrase : blocklist) {
                        if (phrase == null) { continue; }
                        pageContext.setAttribute("phrase", phrase);
                %>
                <li>
                    <span class="phrase"><c:out value="${phrase.phrase}"/></span>
                    <form method="post" action="${pageContext.request.contextPath}/admin/message-moderation">
                        <%@ include file="/WEB-INF/jspf/csrf-field.jspf" %>
                        <input type="hidden" name="action" value="removePhrase">
                        <input type="hidden" name="blocklistId" value="<%= phrase.getBlocklistId() %>">
                        <button type="submit" class="remove">Remove</button>
                    </form>
                </li>
                <% } %>
            </ul>
        <% } %>
    </section>
</main>
</body>
</html>
