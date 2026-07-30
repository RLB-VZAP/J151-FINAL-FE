<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>League Chat - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/catalog.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/messages.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/league-chat.css">
</head>
<body class="catalog-page msg-page">

<c:set var="activeNav" value="leagues" scope="request" />
<%@ include file="/WEB-INF/jspf/sidebar.jspf" %>

<main class="catalog-main" id="leagueChat">
    <div class="catalog-content">

        <header class="catalog-header msg-header">
            <div>
                <p class="catalog-eyebrow">Community</p>
                <h1 class="brand-font">League Chat</h1>
                <p class="msg-intro">
                    Every league you belong to has a group chat open to all of its members &mdash; no request needed,
                    because being in the league is the invitation. Messages are subject to moderation.
                </p>
            </div>
            <a class="msg-ghost msg-page-link" href="${pageContext.request.contextPath}/messages">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M19 12H5"/><path d="M11 18l-6-6 6-6"/></svg>
                Direct messages
            </a>
        </header>

        <c:if test="${not empty error}">
            <p class="msg-alert msg-alert-error" role="alert"><c:out value="${error}"/></p>
        </c:if>
        <c:if test="${not empty info}">
            <p class="msg-alert msg-alert-info" role="status"><c:out value="${info}"/></p>
        </c:if>
        <c:if test="${not empty success}">
            <p class="msg-alert msg-alert-info" role="status"><c:out value="${success}"/></p>
        </c:if>

        <%-- The picker is always present, including when no league is selected.
             The sidebar links here without a leagueId, so this is the landing
             state — previously it reported an error the user could not act on. --%>
        <section class="msg-section" id="leaguePickerSection">
            <div class="msg-section-head">
                <h2 class="msg-section-title">Choose a league</h2>
                <span class="msg-rule-line"></span>
            </div>

            <c:choose>
                <c:when test="${empty myLeagues}">
                    <p class="msg-empty" id="noLeaguesState">
                        You are not in any leagues yet. Join or create one from
                        <a href="${pageContext.request.contextPath}/leagues">Leagues</a>
                        to get a group chat.
                    </p>
                </c:when>
                <c:otherwise>
                    <form method="get" action="${pageContext.request.contextPath}/league-chat" class="msg-search-form" id="leaguePickerForm">
                        <div class="msg-field msg-field-grow">
                            <label class="msg-label" for="leaguePicker">League</label>
                            <select class="msg-input" id="leaguePicker" name="leagueId" required>
                                <option value="">&mdash; Select league &mdash;</option>
                                <c:forEach var="league" items="${myLeagues}">
                                    <option value="${league.leagueId}" ${league.leagueId eq leagueId ? 'selected' : ''}>
                                        <c:out value="${league.leagueName}"/>
                                        (${league.leagueType == 'PUBLIC' ? 'Public' : 'Private'})
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                        <button type="submit" class="msg-ghost">Open chat</button>
                    </form>
                </c:otherwise>
            </c:choose>
        </section>

        <c:if test="${not empty leagueId}">
            <section class="msg-section" id="leagueChatSection">
                <div class="msg-section-head">
                    <h2 class="msg-section-title"><c:out value="${leagueName}"/></h2>
                    <c:if test="${not empty leagueType}">
                        <span class="lch-type ${leagueType == 'PUBLIC' ? 'is-public' : 'is-private'}">
                            ${leagueType == 'PUBLIC' ? 'Public' : 'Private'}
                        </span>
                    </c:if>
                    <span class="msg-rule-line"></span>
                </div>

                <div class="msg-panel msg-conversation-panel">
                    <div id="leagueFeed" class="msg-bubbles"
                         data-context-path="${pageContext.request.contextPath}"
                         data-league-id="${fn:escapeXml(leagueId)}"
                         data-current-user="${fn:escapeXml(sessionScope.userId)}">
                        <c:choose>
                            <c:when test="${empty messages}">
                                <p class="msg-empty msg-empty-sm" id="leagueFeedEmptyState">No messages yet. Say hello!</p>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="message" items="${messages}">
                                    <%-- senderUserId is a UUID and the session attribute a String,
                                         so compare string forms. --%>
                                    <c:set var="mine" value="${sessionScope.userId eq message.senderUserId.toString()}" />
                                    <div class="msg-bubble ${mine ? 'is-mine' : 'is-theirs'}"
                                         data-created-at="${message.createdAt}">
                                        <span class="msg-bubble-author"><c:out value="${message.senderUsername}"/></span>
                                        <p class="msg-bubble-body"><c:out value="${message.body}"/></p>
                                        <span class="msg-bubble-time">${fn:substring(fn:replace(message.createdAt, 'T', ' '), 0, 16)}</span>
                                    </div>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <form id="leagueSendForm" method="post" action="${pageContext.request.contextPath}/league-chat" class="msg-send-form">
                        <%@ include file="/WEB-INF/jspf/csrf-field.jspf" %>
                        <input type="hidden" name="action" value="send">
                        <input type="hidden" name="leagueId" value="${fn:escapeXml(leagueId)}">
                        <label class="msg-visually-hidden" for="leagueMessageBody">Message</label>
                        <textarea class="msg-input msg-textarea" id="leagueMessageBody" name="body" rows="2"
                                  placeholder="Message the league…" required></textarea>
                        <button type="submit" class="btn-gold msg-send-btn">Send</button>
                    </form>
                </div>
            </section>
        </c:if>

    </div>
</main>

<script src="${pageContext.request.contextPath}/assets/js/league-chat.js" defer></script>
</body>
</html>
