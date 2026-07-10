<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Transfer History - Fantasy TryTons</title>
</head>

<body>
<%@ include file="/WEB-INF/jspf/navigation.jspf" %>

<main>
    <h1>Transfer History</h1>

    <p>View your team's previous transfers, player changes, values, penalties, and transfer status.</p>

    <p>
        <a href="${pageContext.request.contextPath}/transfers">Back to Transfers</a>
    </p>

    <c:choose>
        <c:when test="${not empty error}">
            <p class="error-message" role="alert">${error}</p>
        </c:when>

        <c:when test="${empty history}">
            <section id="emptyTransferHistory">
                <h2>No Transfer History Yet</h2>
                <p>No transfers have been recorded for this team yet.</p>
            </section>
        </c:when>

        <c:otherwise>
            <section id="transferHistorySection">
                <h2>Completed Transfers</h2>

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
                                    <c:when test="${not empty transfer.confirmedAt}">
                                        ${transfer.confirmedAt}
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
                                        ${transfer.removedPlayerValue}
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
                                        ${transfer.addedPlayerValue}
                                    </c:when>
                                    <c:otherwise>
                                        -
                                    </c:otherwise>
                                </c:choose>
                            </td>

                            <td>
                                <c:choose>
                                    <c:when test="${not empty transfer.valueDifference}">
                                        ${transfer.valueDifference}
                                    </c:when>
                                    <c:otherwise>
                                        -
                                    </c:otherwise>
                                </c:choose>
                            </td>

                            <td>
                                <c:choose>
                                    <c:when test="${transfer.penaltyApplied}">
                                        <c:choose>
                                            <c:when test="${not empty transfer.penaltyPoints}">
                                                ${transfer.penaltyPoints} points
                                            </c:when>
                                            <c:otherwise>
                                                Penalty applied
                                            </c:otherwise>
                                        </c:choose>
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
                                    <c:when test="${not empty transfer.transferWindowStatus}">
                                        ${transfer.transferWindowStatus}
                                    </c:when>
                                    <c:otherwise>
                                        Unknown
                                    </c:otherwise>
                                </c:choose>

                                <c:if test="${not empty transfer.message}">
                                    <br>
                                    <small>${transfer.message}</small>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </section>
        </c:otherwise>
    </c:choose>
</main>

</body>
</html>