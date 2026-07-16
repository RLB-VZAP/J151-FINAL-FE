<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Fixture Details - Fantasy TryTons</title>
</head>
<body>
<%@ include file="/WEB-INF/jspf/navigation.jspf" %>

<h1>Fixture Details</h1>

<c:if test="${not empty error}">
    <p class="error-message" role="alert">${error}</p>
</c:if>

<c:if test="${not empty fixture}">
    <table id="fixtureDetailsTable">
        <tr>
            <th>Matchup</th>
            <%-- TODO [W4-FE-FIXES-36]: unescaped user-chosen fantasy team names (${fixture.teamAName}/${fixture.teamBName}) — stored XSS; use c:out/fn:escapeXml --%>
            <td>${fixture.teamAName} vs ${fixture.teamBName}</td>
        </tr>
        <tr>
            <th>Date</th>
            <td>${fixture.fixtureDate}</td>
        </tr>
        <tr>
            <th>Time</th>
            <td>${fixture.fixtureTime}</td>
        </tr>
        <tr>
            <th>Status</th>
            <td>${fixture.fixtureStatus}</td>
        </tr>
    </table>

    <c:if test="${fixture.fixtureStatus == 'COMPLETED'}">
        <p id="fixtureResult">Simulated on ${fixture.simulationDate}</p>
    </c:if>
</c:if>

<p>
        <a href="${pageContext.request.contextPath}/fixtures?submit=fixtures" id="backToFixturesLink">
            Back to Fixtures
        </a>
</p>

</body>
</html>