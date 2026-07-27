<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="za.ac.vzap.trytons.frontend.client.message.PendingLeagueMessageResponse" %>
<%@ page import="za.ac.vzap.trytons.frontend.client.message.BlockedPhraseResponse" %>
<%@ page import="za.ac.vzap.trytons.frontend.client.message.DirectMessageResponse" %>
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

    <%-- Rule E: no queue of reported direct messages exists yet (no such listing
         endpoint is in the backend contract), so the entry point here is a manual
         message id lookup rather than a row to click. The 10-message bound is
         called out explicitly so the privacy limit reads as a deliberate design
         choice, not a bug. --%>
    <section id="directMessageContext" class="card">
        <h2>Reported direct message context</h2>
        <p class="dmc-intro">
            Look up a reported direct message by id to see its bounded conversation context.
        </p>

        <form method="get" action="${pageContext.request.contextPath}/admin/message-moderation" class="dmc-lookup-form d-flex gap-2 mb-3">
            <label for="directMessageId" class="visually-hidden">Message id</label>
            <input type="text" id="directMessageId" name="directMessageId"
                   value="${fn:escapeXml(directMessageIdQuery)}"
                   placeholder="Message id (UUID)" class="form-control">
            <button type="submit" class="btn btn-gold btn-sm">View</button>
        </form>

        <c:if test="${directMessageWindowDenied}">
            <p class="empty-state dmc-denied">
                This message has no report on file, so admin access to its conversation is denied.
            </p>
        </c:if>

        <c:if test="${not empty directMessageWindowError}">
            <p class="error-message alert alert-danger" role="alert"><c:out value="${directMessageWindowError}"/></p>
        </c:if>

        <c:if test="${not empty directMessageWindow}">
            <p class="dmc-bound-note">
                Showing the reported message and up to 10 preceding — admins cannot view more.
            </p>
            <div class="dmc-window">
                <%
                    List<DirectMessageResponse> directMessageWindow =
                            (List<DirectMessageResponse>) request.getAttribute("directMessageWindow");
                    String anchorId = (String) request.getAttribute("directMessageAnchorId");
                    for (DirectMessageResponse dm : directMessageWindow) {
                        if (dm == null) { continue; }
                        pageContext.setAttribute("dm", dm);
                        boolean isAnchor = anchorId != null && anchorId.equals(String.valueOf(dm.getMessageId()));
                        boolean isRejected = "REJECTED".equals(dm.getStatus());
                %>
                <div class="dmc-message <%= isAnchor ? "is-anchor" : "" %> <%= isRejected ? "is-rejected" : "" %>">
                    <div class="dmc-message-meta">
                        <span class="dmc-sender">Sender: <c:out value="${dm.senderUserId}"/></span>
                        <span class="dmc-time"><c:out value="${dm.createdAt}"/></span>
                        <% if (isAnchor) { %>
                            <span class="dmc-tag dmc-tag-anchor">Reported message</span>
                        <% } %>
                        <% if (isRejected) { %>
                            <span class="dmc-tag dmc-tag-rejected">Blocked content — not delivered</span>
                        <% } %>
                    </div>
                    <p class="dmc-message-body"><c:out value="${dm.body}"/></p>
                </div>
                <% } %>
            </div>
        </c:if>
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
