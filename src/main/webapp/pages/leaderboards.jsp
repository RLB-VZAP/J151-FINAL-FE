<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<html>
<head>
    <title>Overall Leaderboard</title>
</head>
<body>
<%@ include file="/WEB-INF/jspf/navigation.jspf" %>

    <c:if test="${not empty error}">
        <p><c:out value="${error}" /></p>
    </c:if>

    <c:if test="${empty error and empty leaderboard}">
        <p>No leaderboard entries found.</p>
    </c:if>

    <c:if test="${not empty leaderboard}">
        <table>
            <tr>
                <th>Rank</th>
                <th>Movement</th>
                <th>Team</th>
                <th>Owner</th>
                <th>Played</th>
                <th>Won</th>
                <th>Drawn</th>
                <th>Lost</th>
                <th>Points For</th>
                <th>Points Against</th>
                <th>Difference</th>
                <th>League Points</th>
                <th>Total Fantasy Points</th>
            </tr>
            <c:forEach var="entry" items="${leaderboard}">
                <tr>
                    <td>${entry.rank}</td>
                    <td>${entry.rankMovement}</td>
                    <td><c:out value="${entry.teamName}" /></td>
                    <td><c:out value="${entry.owner}" /></td>
                    <td>${entry.matchesPlayed}</td>
                    <td>${entry.matchesWon}</td>
                    <td>${entry.matchesDrawn}</td>
                    <td>${entry.matchesLost}</td>
                    <td>${entry.pointsFor}</td>
                    <td>${entry.pointsAgainst}</td>
                    <td>${entry.scoreDifference}</td>
                    <td>${entry.leaguePoints}</td>
                    <td>${entry.totalFantasyPoints}</td>
                </tr>
            </c:forEach>
        </table>
    </c:if>
</body>
</html>
