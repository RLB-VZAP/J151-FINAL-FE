<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Fixtures - Fantasy TryTons</title>
</head>
<body>
<%@ include file="/WEB-INF/jspf/navigation.jspf" %>

<h1>Fixtures</h1>

<c:if test="${not empty error}">
    <p class="error-message" role="alert">${error}</p>
</c:if>

<form action="${pageContext.request.contextPath}/fixtures" method="get" id="fixtureFilterForm">
    <input type="hidden" name="submit" value="fixtures" />

    <select name="status" id="statusFilter">
        <option value="">All Fixtures</option>
        <option value="UPCOMING" ${statusFilter == 'UPCOMING' ? 'selected' : ''}>Upcoming</option>
        <option value="LOCKED" ${statusFilter == 'LOCKED' ? 'selected' : ''}>Locked</option>
        <option value="SIMULATING" ${statusFilter == 'SIMULATING' ? 'selected' : ''}>Simulating</option>
        <option value="COMPLETED" ${statusFilter == 'COMPLETED' ? 'selected' : ''}>Completed</option>
        <option value="CANCELLED" ${statusFilter == 'CANCELLED' ? 'selected' : ''}>Cancelled</option>
    </select>

    <button type="submit">Filter</button>
</form>

<c:choose>
    <c:when test="${empty fixtures}">
        <p id="fixturesEmptyState">No fixtures found.</p>
    </c:when>
    <c:otherwise>
        <table id="fixturesTable">
            <thead>
            <tr>
                <th>Matchup</th>
                <th>Date</th>
                <th>Time</th>
                <th>Status</th>
                <th></th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="fixture" items="${fixtures}">
                <tr>
                    <%-- TODO [W4-FE-FIXES-37]: unescaped user-chosen fantasy team names (${fixture.teamAName}/${fixture.teamBName}) — stored XSS; use c:out/fn:escapeXml --%>
                    <td>${fixture.teamAName} vs ${fixture.teamBName}</td>
                    <td>${fixture.fixtureDate}</td>
                    <td>${fixture.fixtureTime}</td>
                    <td>${fixture.fixtureStatus}</td>
                    <td>
                        <a href="${pageContext.request.contextPath}/fixture?submit=fixture&fixtureId=${fixture.fixtureId}"
                            id="fixtureLink-${fixture.fixtureId}">
                            View
                        </a>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </c:otherwise>
</c:choose>

</body>
</html>