<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Players - Fantasy TryTons</title>
</head>
<body>
<%@ include file="/WEB-INF/jspf/navigation.jspf" %>

<h1>Players</h1>

<c:if test="${not empty error}">
    <p class="error-message" role="alert">${error}</p>
</c:if>

<form action="${pageContext.request.contextPath}/players" method="get" id="playerSearchForm">
    <input type="hidden" name="submit" value="players" />
    <input type="text" name="search" placeholder="Search player name"
           value="${searchTerm}" id="playerSearchInput" />

    <select name="clubId" id="clubFilter">
        <option value="">All Clubs</option>
        <c:forEach var="club" items="${clubs}">
            <option value="${club.clubId}"
                    ${club.clubId == selectedClubId ? 'selected' : ''}>
                    ${club.clubName}
            </option>
        </c:forEach>
    </select>

    <select name="positionId" id="positionFilter">
        <option value="">All Positions</option>
        <c:forEach var="position" items="${positions}">
            <option value="${position.positionId}"
                    ${position.positionId == selectedPositionId ? 'selected' : ''}>
                    ${position.positionName}
            </option>
        </c:forEach>
    </select>

    <button type="submit">Search</button>
</form>

<c:choose>
    <c:when test="${empty players}">
        <p id="playersEmptyState">No players found matching your search.</p>
    </c:when>
    <c:otherwise>
        <table id="playersTable">
            <thead>
            <tr>
                <th>Name</th>
                <th>Club</th>
                <th>Position</th>
                <th>Value</th>
                <th>Form</th>
                <th>Fantasy Points</th>
                <th>Availability</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="player" items="${players}">
                <tr>
                    <td>${player.playerName}</td>
                    <td>${player.club.clubName}</td>
                    <td>${player.position.positionName}</td>
                    <td>${player.value}</td>
                    <td>${player.currentForm}</td>
                    <td>${player.totalFantasyPoints}</td>
                    <td>
                        <c:choose>
                            <c:when test="${player.active}">Active</c:when>
                            <c:otherwise>Unavailable</c:otherwise>
                        </c:choose>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </c:otherwise>
</c:choose>

<script src="${pageContext.request.contextPath}/assets/js/search-filter.js"></script>
</body>
</html>