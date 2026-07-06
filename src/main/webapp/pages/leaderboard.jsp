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
                <th>Weekly Points</th>
                <th>Total Points</th>
                <th>Rank Movement</th>
            </tr>
            <c:forEach var="entry" items="${leaderboard}">
                <tr>
                    <td>${entry.rank}</td>
                    <td>${entry.teamName}</td>
                    <td>${entry.owner}</td>
                    <td>${entry.weeklyPoints}</td>
                    <td>${entry.totalPoints}</td>
                    <td>${entry.rankMovement}</td>
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
                <th>Weekly Points</th>
                <th>Total Points</th>
                <th>Rank Movement</th>
            </tr>
            <tr>
                <td>${ranking.rank}</td>
                <td>${ranking.teamName}</td>
                <td>${ranking.owner}</td>
                <td>${ranking.weeklyPoints}</td>
                <td>${ranking.totalPoints}</td>
                <td>${ranking.rankMovement}</td>
            </tr>
        </table>
    </c:if>

</body>
</html>