<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Transfer History - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/transfer-history.css">
</head>

<body>
<%@ include file="/WEB-INF/jspf/navigation.jspf" %>

<main class="transfer-history-page">
    <section class="transfer-history-hero" aria-labelledby="transferHistoryTitle">
        <p class="transfer-history-eyebrow">Team management</p>

        <h1 id="transferHistoryTitle">Transfer History</h1>

        <p class="transfer-history-description">
            View your team's previous transfers, player changes, values, penalties, and transfer status.
        </p>

        <p class="transfer-history-back-link">
            <a href="${pageContext.request.contextPath}/transfers">&larr; Back to Transfers</a>
        </p>
    </section>

    <c:choose>
        <c:when test="${not empty error}">
            <p class="error-message transfer-history-error" role="alert">${error}</p>
        </c:when>

        <c:when test="${empty history}">
            <section id="emptyTransferHistory" class="transfer-history-empty">
                <h2>No Transfer History Yet</h2>
                <p>No transfers have been recorded for this team yet.</p>
            </section>
        </c:when>

        <c:otherwise>
            <section id="transferHistorySection" class="transfer-history-card">
                <h2>Completed Transfers</h2>

                <div class="transfer-history-table-wrap">
                    <table id="transferHistoryTable">
                        <thead>
                        <tr>
                            <th>Date</th>
                            <th>Player Out</th>
                            <th>Out Value</th>
                            <th>Player In</th>
                            <th>In Value</th>
                            <th>Value Difference</th>
                            <th>Penalty</th>
                            <th>Status</th>
                        </tr>
                        </thead>

                        <tbody>
                        <c:forEach var="transfer" items="${history}">
                            <tr>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty transfer.transferDate}">
                                            ${transfer.transferDate}
                                        </c:when>
                                        <c:when test="${not empty transfer.confirmationDate}">
                                            ${transfer.confirmationDate}
                                        </c:when>
                                        <c:otherwise>
                                            Not recorded
                                        </c:otherwise>
                                    </c:choose>
                                </td>

                                <td>
                                    <c:choose>
                                        <c:when test="${not empty transfer.removedPlayerName}">
                                            ${transfer.removedPlayerName}
                                        </c:when>
                                        <c:otherwise>
                                            Unknown player
                                        </c:otherwise>
                                    </c:choose>
                                </td>

                                <td>
                                    <c:choose>
                                        <c:when test="${not empty transfer.removedPlayerValue}">
                                            <t:money value="${transfer.removedPlayerValue}" />
                                        </c:when>
                                        <c:otherwise>
                                            -
                                        </c:otherwise>
                                    </c:choose>
                                </td>

                                <td>
                                    <c:choose>
                                        <c:when test="${not empty transfer.addedPlayerName}">
                                            ${transfer.addedPlayerName}
                                        </c:when>
                                        <c:otherwise>
                                            Unknown player
                                        </c:otherwise>
                                    </c:choose>
                                </td>

                                <td>
                                    <c:choose>
                                        <c:when test="${not empty transfer.addedPlayerValue}">
                                            <t:money value="${transfer.addedPlayerValue}" />
                                        </c:when>
                                        <c:otherwise>
                                            -
                                        </c:otherwise>
                                    </c:choose>
                                </td>

                                <td>
                                    <c:choose>
                                        <c:when test="${not empty transfer.valueDifference}">
                                            <t:money value="${transfer.valueDifference}" />
                                        </c:when>
                                        <c:otherwise>
                                            -
                                        </c:otherwise>
                                    </c:choose>
                                </td>

                                <td>
                                    <c:choose>
                                        <c:when test="${transfer.penaltyPoints > 0}">
                                            ${transfer.penaltyPoints} points
                                        </c:when>
                                        <c:otherwise>
                                            No penalty
                                        </c:otherwise>
                                    </c:choose>
                                </td>

                                <td>
                                    <c:choose>
                                        <c:when test="${not empty transfer.status}">
                                            ${transfer.status}
                                        </c:when>
                                        <c:otherwise>
                                            Unknown
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </section>
        </c:otherwise>
    </c:choose>
</main>

</body>
</html>