<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Create League - Fantasy TryTons</title>
</head>
<body>
<%@ include file="/WEB-INF/jspf/navigation.jspf" %>

<h1>Create League</h1>

<c:if test="${not empty error}">
    <p class="error-message" role="alert"><c:out value="${error}" /></p>
</c:if>

<c:choose>
    <c:when test="${not empty success}">
        <p class="success-message" role="status"><c:out value="${success}" /></p>
        <c:if test="${league != null}">
            <p>
                Your league "<c:out value="${league.leagueName}" />" is ready.
                <a href="${pageContext.request.contextPath}/league?leagueId=${league.leagueId}" id="viewCreatedLeagueLink">View it here</a>.
            </p>
        </c:if>
        <p><a href="${pageContext.request.contextPath}/leagues" id="backToLeaguesLink">Back to leagues</a></p>
    </c:when>
    <c:otherwise>
        <form method="post" action="${pageContext.request.contextPath}/league/create" id="createLeagueForm">
            <input type="hidden" name="submit" value="league/create" />

            <div>
                <label for="leagueName">League name</label>
                <input type="text" id="leagueName" name="leagueName"
                       value="${param.leagueName}" required>
            </div>
            <br>

            <div>
                <label for="description">Description</label>
                <textarea id="description" name="description" required><c:out value="${param.description}" /></textarea>
            </div>
            <br>

            <div>
                <label for="leagueType">Visibility</label>
                <select id="leagueType" name="leagueType" required>
                    <option value="PUBLIC" ${param.leagueType == 'PUBLIC' ? 'selected' : ''}>Public</option>
                    <option value="PRIVATE" ${param.leagueType == 'PRIVATE' ? 'selected' : ''}>Private</option>
                </select>
            </div>
            <br>

            <div>
                <label for="maxMembers">Max members</label>
                <input type="number" id="maxMembers" name="maxMembers" min="1"
                       value="${empty param.maxMembers ? 20 : param.maxMembers}" required>
            </div>
            <br>

            <button type="submit" name="submit" value="league/create">Create league</button>
        </form>
    </c:otherwise>
</c:choose>

</body>
</html>
