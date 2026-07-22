<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Points History - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/user-history.css">
</head>

<body>
<%@ include file="/WEB-INF/jspf/navigation.jspf" %>

<main class="user-history-page">
    <section class="user-history-hero" aria-labelledby="userHistoryTitle">
        <p class="user-history-eyebrow">Your season</p>
        <h1 id="userHistoryTitle">Points History</h1>
        <p class="user-history-description">
            Track your fantasy team's total points, current ranking, and how each round played out.
        </p>
    </section>

    <section class="user-history-summary" aria-labelledby="summaryTitle">
        <h2 id="summaryTitle">Summary</h2>

        <c:choose>
            <c:when test="${not empty pointsHistoryError}">
                <p class="error-message user-history-error" role="alert">${pointsHistoryError}</p>
            </c:when>

            <c:when test="${empty pointsHistory}">
                <div class="user-history-empty">
                    <h3>No Points Yet</h3>
                    <p>Your fantasy team hasn't earned any points yet. Check back after the next round is processed.</p>
                </div>
            </c:when>

            <c:otherwise>
                <div class="user-history-summary-cards">
                    <div class="user-history-summary-card">
                        <span class="user-history-summary-label">Total Points</span>
                        <span class="user-history-summary-value">${pointsHistory.totals}</span>
                    </div>
                    <div class="user-history-summary-card">
                        <span class="user-history-summary-label">Current Rank</span>
                        <span class="user-history-summary-value">
                            <c:choose>
                                <c:when test="${not empty pointsHistory.ranking}">
                                    #${pointsHistory.ranking}
                                </c:when>
                                <c:otherwise>
                                    Not available
                                </c:otherwise>
                            </c:choose>
                        </span>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
    </section>

    <section class="user-history-breakdown" aria-labelledby="breakdownTitle">
        <h2 id="breakdownTitle">Round-by-Round Breakdown</h2>

        <c:choose>
            <c:when test="${not empty weeklyPerformanceError}">
                <p class="error-message user-history-error" role="alert">${weeklyPerformanceError}</p>
            </c:when>

            <c:when test="${empty weeklyPerformance}">
                <div class="user-history-empty">
                    <h3>No Rounds Played Yet</h3>
                    <p>Once fixtures are processed, your round-by-round results will appear here.</p>
                </div>
            </c:when>

            <c:otherwise>
                <div class="user-history-table-wrap">
                    <table id="weeklyPerformanceTable">
                        <thead>
                        <tr>
                            <th>Round</th>
                            <th>Fixture</th>
                            <th>Points</th>
                            <th>Result</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="round" items="${weeklyPerformance}">
                            <tr>
                                <td>${round.roundId}</td>
                                <td>${round.fixtureId}</td>
                                <td>${round.pointsScored}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${round.result == 'WIN'}">
                                            <span class="user-history-badge user-history-badge-win">Win</span>
                                        </c:when>
                                        <c:when test="${round.result == 'DRAW'}">
                                            <span class="user-history-badge user-history-badge-draw">Draw</span>
                                        </c:when>
                                        <c:when test="${round.result == 'LOSS'}">
                                            <span class="user-history-badge user-history-badge-loss">Loss</span>
                                        </c:when>
                                        <c:otherwise>
                                            ${round.result}
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </section>
</main>

</body>
</html>
