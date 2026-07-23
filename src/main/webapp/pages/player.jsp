<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="za.ac.vzap.trytons.frontend.client.pricing.PlayerPriceHistoryResponse" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <%-- TODO [W4-FE-FIXES-43]: unescaped ${player.playerName} (title line 7 and <h1> line 14) — stored XSS via admin data; use c:out/fn:escapeXml --%>
    <title>${player.playerName} - TryTons</title>
</head>
<body>
<%@ include file="/WEB-INF/jspf/navigation.jspf" %>

<main>
    <p><a href="${pageContext.request.contextPath}/players?submit=players">&larr; Back to players</a></p>
    <h1>${player.playerName}</h1>

    <dl>
        <dt>Club</dt><dd>${clubNamesById[player.clubId]}</dd>
        <dt>Position</dt><dd>${positionNamesById[player.positionId]}</dd>
        <dt>Value</dt><dd>${player.value}</dd>
        <dt>Current form</dt><dd>${player.currentForm}</dd>
        <dt>Status</dt><dd>${player.active ? 'Active' : 'Inactive'}</dd>
    </dl>

    <h2>Recent price changes</h2>
    <%
        List<PlayerPriceHistoryResponse> priceHistory =
                (List<PlayerPriceHistoryResponse>) request.getAttribute("priceHistory");
        if (priceHistory == null || priceHistory.isEmpty()) {
    %>
        <p>No price changes recorded yet.</p>
    <%
        } else {
    %>
        <table class="price-history">
            <thead>
            <tr><th>When</th><th>From</th><th>To</th><th>Change</th><th>Reason</th></tr>
            </thead>
            <tbody>
                <%
                    for (PlayerPriceHistoryResponse row : priceHistory) {
                        if (row == null) { continue; }
                        pageContext.setAttribute("row", row);
                        boolean up = row.getDelta() != null && row.getDelta().signum() > 0;
                %>
                <tr>
                    <td><c:out value="${row.createdAt}"/></td>
                    <td>${row.oldValue}</td>
                    <td>${row.newValue}</td>
                    <td style="color: <%= up ? "#15803d" : "#dc2626" %>;"><%= up ? "+" : "" %>${row.delta}</td>
                    <td><c:out value="${row.reason}"/></td>
                </tr>
                <% } %>
            </tbody>
        </table>
    <% } %>

    <h2>Set availability</h2>
    <c:if test="${not empty error}"><p>${error}</p></c:if>
    <c:if test="${not empty availabilityMessage}"><p>${availabilityMessage}</p></c:if>
    <c:if test="${not empty availability}">
        <p>Current: ${availability.status} (from ${availability.effectiveDate}<c:if test="${not empty availability.endDate}"> to ${availability.endDate}</c:if>)</p>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/player/availability">
        <input type="hidden" name="submit" value="player/availability"/>
        <input type="hidden" name="playerId" value="${player.playerId}"/>
        <p>
            <label for="status">Status</label>
            <select id="status" name="status">
                <option value="ACTIVE">ACTIVE</option>
                <option value="INJURED">INJURED</option>
                <option value="SUSPENDED">SUSPENDED</option>
                <option value="UNAVAILABLE">UNAVAILABLE</option>
            </select>
        </p>
        <p>
            <label for="effectiveDate">Effective date</label>
            <input type="date" id="effectiveDate" name="effectiveDate" required/>
        </p>
        <p>
            <label for="endDate">End date (optional)</label>
            <input type="date" id="endDate" name="endDate"/>
        </p>
        <p>
            <label for="notes">Notes</label>
            <textarea id="notes" name="notes"></textarea>
        </p>
        <p><button type="submit">Save availability</button></p>
    </form>
</main>
</body>
</html>
