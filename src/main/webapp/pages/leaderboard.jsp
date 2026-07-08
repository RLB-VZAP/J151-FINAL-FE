<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<html>
<head>
    <title>Leaderboard</title>
</head>
<body>

    <c:if test="${not empty error}">
        <p>${error}</p>
    </c:if>

    <c:if test="${not empty leaderboard}">
        <table>
            <tr>
                <th>Rank</th>
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
                    <td>${entry.teamName}</td>
                    <td>${entry.owner}</td>
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

    <c:if test="${not empty ranking}">
        <!-- a single entry, no loop needed — just print fields directly off ${ranking} -->
        <table>
            <tr>
                <th>Rank</th>
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
            <tr>
                <td>${ranking.rank}</td>
                <td>${ranking.teamName}</td>
                <td>${ranking.owner}</td>
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
        </table>
    </c:if>
</body>
</html>