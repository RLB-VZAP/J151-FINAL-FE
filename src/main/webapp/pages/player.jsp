<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <%-- TODO [W4-FE-FIXES-43]: unescaped ${player.playerName} (title line 7 and <h1> line 14) — stored XSS via admin data; use c:out/fn:escapeXml --%>
    <title>${player.playerName} - TryTons</title>
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
</head>
<body>
<%@ include file="/WEB-INF/jspf/navigation.jspf" %>

<main>
    <p><a href="${pageContext.request.contextPath}/players?submit=players">&larr; Back to players</a></p>
    <h1>${player.playerName}</h1>

    <dl>
        <dt>Club</dt><dd>${clubNamesById[player.clubId]}</dd>
        <dt>Position</dt><dd>${positionNamesById[player.positionId]}</dd>
        <dt>Value</dt><dd><t:money value="${player.value}" /></dd>
        <dt>Current form</dt><dd><t:rating value="${player.currentForm}" /></dd>
        <dt>Status</dt><dd>${player.active ? 'Active' : 'Inactive'}</dd>
    </dl>

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
