<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Players - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/players.css">
</head>
<body class="players-page">

<c:set var="activeNav" value="players" scope="request" />
<%@ include file="/WEB-INF/jspf/sidebar.jspf" %>

<main class="players-main">
    <%-- Photo layer sits under everything, behind a heavy dark-green wash. --%>
    <div class="players-backdrop" aria-hidden="true"></div>

    <div class="players-content">

        <header class="players-header">
            <div>
                <p class="players-eyebrow">Fantasy TryTons League</p>
                <h1 class="brand-font">Players</h1>
            </div>
            <%-- players-filter.js keeps this in step with the filtered list. --%>
            <p class="players-count" id="playersCount">${fn:length(players)} players &middot; season 2025/26</p>
        </header>

        <c:if test="${not empty error}">
            <p class="players-error" role="alert">${fn:escapeXml(error)}</p>
        </c:if>

        <div class="players-toolbar">
            <label class="search-wrap" for="playerSearchInput">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><circle cx="11" cy="11" r="7"/><path d="M21 21l-4.3-4.3"/></svg>
                <input type="search" id="playerSearchInput" name="search"
                       placeholder="Search player name" autocomplete="off"
                       aria-label="Search player name"
                       value="${fn:escapeXml(searchTerm)}">
            </label>

            <select class="sel sel-filter" id="clubFilter" aria-label="Filter by club">
                <option value="">All clubs</option>
                <c:forEach var="club" items="${clubs}">
                    <option value="${fn:escapeXml(club.clubId)}" ${club.clubId == selectedClubId ? 'selected' : ''}>${fn:escapeXml(club.clubName)}</option>
                </c:forEach>
            </select>

            <select class="sel sel-filter" id="positionFilter" aria-label="Filter by position">
                <option value="">All positions</option>
                <c:forEach var="position" items="${positions}">
                    <option value="${fn:escapeXml(position.positionId)}" ${position.positionId == selectedPositionId ? 'selected' : ''}>${fn:escapeXml(position.positionName)}</option>
                </c:forEach>
            </select>

            <div class="sort-wrap">
                <label class="sort-label" for="playerSort">Sort by</label>
                <select class="sel" id="playerSort">
                    <option value="name">Name</option>
                    <option value="club">Club</option>
                    <option value="value">Value</option>
                    <option value="form">Form</option>
                </select>
            </div>
        </div>

        <div class="ptable" id="playersTable">
            <div class="prow phead">
                <span>Name</span>
                <span>Club</span>
                <span>Position</span>
                <span>Value</span>
                <span>Form</span>
                <span>Availability</span>
            </div>
            <div class="pbody" id="playersBody">
                <c:forEach var="player" items="${players}">
                    <c:set var="clubName" value="${clubNamesById[player.clubId]}" />
                    <c:set var="positionName" value="${positionNamesById[player.positionId]}" />
                    <%-- The servlet exposes position names but not categories, so read the
                         forward/back split off the ${positions} list it already supplies.
                         Nine positions, so the inner scan costs nothing. --%>
                    <c:set var="isForward" value="${false}" />
                    <c:forEach var="pos" items="${positions}">
                        <c:if test="${pos.positionId == player.positionId}">
                            <c:set var="isForward" value="${pos.positionCategory == 'FORWARD'}" />
                        </c:if>
                    </c:forEach>
                    <%-- Drives the arrow thresholds below; the rating tag renders the number. --%>
                    <c:set var="formScore" value="${player.currentForm / 10}" />
                    <c:set var="playerName" value="${empty player.playerName ? '' : player.playerName}" />
                    <c:set var="nameParts" value="${fn:split(playerName, ' ')}" />

                    <div class="prow"
                         data-name="${fn:escapeXml(fn:toLowerCase(playerName))}"
                         data-club="${fn:escapeXml(player.clubId)}"
                         data-club-name="${fn:escapeXml(clubName)}"
                         data-position="${fn:escapeXml(player.positionId)}"
                         data-value="${player.value}"
                         data-form="${player.currentForm}">

                        <span class="p-name">
                            <span class="p-avatar" aria-hidden="true"><c:if test="${fn:length(nameParts) > 0}">${fn:toUpperCase(fn:substring(nameParts[0], 0, 1))}<c:if test="${fn:length(nameParts) > 1}">${fn:toUpperCase(fn:substring(nameParts[fn:length(nameParts) - 1], 0, 1))}</c:if></c:if></span>
                            <span class="p-name-text" title="${fn:escapeXml(playerName)}">${fn:escapeXml(playerName)}</span>
                        </span>

                        <span class="p-club" title="${fn:escapeXml(clubName)}">${fn:escapeXml(clubName)}</span>

                        <span>
                            <span class="pos-pill ${isForward ? 'pos-fwd' : 'pos-back'}">${fn:escapeXml(positionName)}</span>
                        </span>

                        <span class="p-value"><t:money value="${player.value}" /></span>

                        <span>
                            <c:choose>
                                <c:when test="${formScore >= 7}">
                                    <span class="form-chip form-up"><span aria-hidden="true">&uarr;</span><t:rating value="${player.currentForm}" /></span>
                                </c:when>
                                <c:when test="${formScore < 5}">
                                    <span class="form-chip form-down"><span aria-hidden="true">&darr;</span><t:rating value="${player.currentForm}" /></span>
                                </c:when>
                                <c:otherwise>
                                    <span class="form-chip"><span aria-hidden="true">&ndash;</span><t:rating value="${player.currentForm}" /></span>
                                </c:otherwise>
                            </c:choose>
                        </span>

                        <%-- The list endpoint only exposes isActive, so this is a two-state
                             read of the three availability treatments the design defines. --%>
                        <span>
                            <c:choose>
                                <c:when test="${player.active}">
                                    <span class="avail avail-ok"><span class="avail-label">Available</span></span>
                                </c:when>
                                <c:otherwise>
                                    <span class="avail avail-out"><span class="avail-label">Unavailable</span></span>
                                </c:otherwise>
                            </c:choose>
                        </span>
                    </div>
                </c:forEach>
            </div>
        </div>

        <p class="players-empty" id="playersEmptyState" hidden>No players match your search.</p>

    </div>
</main>

<script src="${pageContext.request.contextPath}/assets/js/players-filter.js"></script>
</body>
</html>
