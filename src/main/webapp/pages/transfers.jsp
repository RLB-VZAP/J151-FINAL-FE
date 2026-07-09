<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Transfers - Fantasy TryTons</title>
</head>
<body>
<%@ include file="/WEB-INF/jspf/navigation.jspf" %>

<h1>Transfers</h1>

<p>Choose one player from your current squad to remove, then choose one available player to bring in.</p>

<c:if test="${not empty success}">
    <p class="success-message" role="status">${success}</p>
</c:if>

<c:if test="${not empty error}">
    <p class="error-message" role="alert">${error}</p>
</c:if>

<c:if test="${not empty transferError}">
    <p class="error-message" role="alert">${transferError}</p>
</c:if>

<c:if test="${not empty lockError}">
    <p class="error-message" role="alert">${lockError}</p>
</c:if>

<c:if test="${not empty transfer}">
    <section id="latestTransferResult">
        <h2>Latest Transfer</h2>
        <p>
            ${transfer.removedPlayerName} out,
            ${transfer.addedPlayerName} in.
        </p>

        <c:if test="${not empty transfer.valueDifference}">
            <p>Value difference: ${transfer.valueDifference}</p>
        </c:if>

        <c:if test="${transfer.penaltyApplied}">
            <p>Penalty applied: ${transfer.penaltyPoints} points</p>
        </c:if>

        <c:if test="${not empty transfer.newRemainingBudget}">
            <p>New remaining budget: ${transfer.newRemainingBudget}</p>
        </c:if>

        <c:if test="${not empty transfer.message}">
            <p>${transfer.message}</p>
        </c:if>
    </section>
</c:if>

<c:if test="${not empty lockStatus}">
    <section id="lockStatusBanner">
        <h2>Round Status</h2>

        <p>
            Status:
            <strong>
                <c:choose>
                    <c:when test="${lockStatus.locked or lockStatus.deadlinePassed or lockStatus.transfersAllowed == false}">
                        Transfers locked
                    </c:when>
                    <c:otherwise>
                        Transfers open
                    </c:otherwise>
                </c:choose>
            </strong>
        </p>

        <c:if test="${not empty lockStatus.roundStatus}">
            <p>Round status: ${lockStatus.roundStatus}</p>
        </c:if>

        <c:if test="${not empty lockStatus.transferWindowStatus}">
            <p>Transfer window: ${lockStatus.transferWindowStatus}</p>
        </c:if>

        <c:if test="${not empty lockStatus.lockDeadline}">
            <p>Lock deadline: ${lockStatus.lockDeadline}</p>
        </c:if>

        <c:if test="${not empty lockStatus.deadlineAt}">
            <p>Deadline: ${lockStatus.deadlineAt}</p>
        </c:if>

        <c:if test="${not empty lockStatus.message}">
            <p>${lockStatus.message}</p>
        </c:if>
    </section>
</c:if>

<section id="budgetSection">
    <h2>Budget</h2>

    <c:choose>
        <c:when test="${not empty remainingBudget}">
            <p>Remaining budget: ${remainingBudget}</p>
        </c:when>
        <c:when test="${not empty budget}">
            <p>Remaining budget: ${budget}</p>
        </c:when>
        <c:otherwise>
            <p>Budget information will show once your team details are available.</p>
        </c:otherwise>
    </c:choose>
</section>

<form action="${pageContext.request.contextPath}/transfers" method="post" id="transferForm">
    <input type="hidden" name="submit" value="transfer">

    <c:choose>
        <c:when test="${not empty teamId}">
            <input type="hidden" name="teamId" value="${teamId}">
        </c:when>
        <c:otherwise>
            <div>
                <label for="teamId">Team ID</label>
                <input type="text" id="teamId" name="teamId" required>
            </div>
        </c:otherwise>
    </c:choose>

    <c:choose>
        <c:when test="${not empty roundId}">
            <input type="hidden" name="roundId" value="${roundId}">
        </c:when>
        <c:otherwise>
            <div>
                <label for="roundId">Round ID</label>
                <input type="text" id="roundId" name="roundId" required>
            </div>
        </c:otherwise>
    </c:choose>

    <section id="squadSection">
        <h2>Your Current Squad</h2>

        <c:choose>
            <c:when test="${empty squad}">
                <p>Your current squad could not be loaded yet.</p>
            </c:when>
            <c:otherwise>
                <table id="squadTable">
                    <thead>
                    <tr>
                        <th>Remove</th>
                        <th>Player</th>
                        <th>Club</th>
                        <th>Position</th>
                        <th>Value</th>
                        <th>Fantasy Points</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="player" items="${squad}">
                        <tr>
                            <td>
                                <input
                                        type="radio"
                                        name="removedPlayerId"
                                        value="${player.playerId}"
                                        required>
                            </td>
                            <td>${player.playerName}</td>
                            <td>${player.club.clubName}</td>
                            <td>${player.position.positionName}</td>
                            <td>${player.value}</td>
                            <td>${player.totalFantasyPoints}</td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </c:otherwise>
        </c:choose>
    </section>

    <section id="incomingSection">
        <h2>Available Players</h2>

        <c:choose>
            <c:when test="${empty players}">
                <p>No available players could be loaded right now.</p>
            </c:when>
            <c:otherwise>
                <table id="incomingPlayersTable">
                    <thead>
                    <tr>
                        <th>Add</th>
                        <th>Player</th>
                        <th>Club</th>
                        <th>Position</th>
                        <th>Value</th>
                        <th>Form</th>
                        <th>Fantasy Points</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="player" items="${players}">
                        <tr>
                            <td>
                                <input
                                        type="radio"
                                        name="addedPlayerId"
                                        value="${player.playerId}"
                                        required>
                            </td>
                            <td>${player.playerName}</td>
                            <td>${player.club.clubName}</td>
                            <td>${player.position.positionName}</td>
                            <td>${player.value}</td>
                            <td>${player.currentForm}</td>
                            <td>${player.totalFantasyPoints}</td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </c:otherwise>
        </c:choose>
    </section>

    <%@ include file="/WEB-INF/jspf/transfer-recommendations.jspf" %>

    <section id="penaltySection">
        <h2>Penalty Confirmation</h2>
        <label for="penaltyConfirmed">
            <input type="checkbox" id="penaltyConfirmed" name="penaltyConfirmed" value="true">
            I understand this transfer may apply penalty points if the backend confirms it is outside my free transfer allowance.
        </label>
    </section>

    <button type="submit">Confirm Transfer</button>
</form>

<p>
    <a href="${pageContext.request.contextPath}/transfers/history?teamId=${teamId}">
        View transfer history
    </a>
</p>

</body>
</html>