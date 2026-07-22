<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Leagues - Fantasy TryTons</title>
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
</head>
<body>
<%@ include file="/WEB-INF/jspf/navigation.jspf" %>

<h1>Leagues</h1>

<c:if test="${not empty error}">
    <p class="error-message" role="alert"><c:out value="${error}" /></p>
</c:if>

<c:if test="${not empty success}">
    <p class="success-message" role="status"><c:out value="${success}" /></p>
</c:if>

<p>
    <a href="${pageContext.request.contextPath}/league/create" id="createLeagueLink">Create a league</a>
    &nbsp;|&nbsp;
    <a href="${pageContext.request.contextPath}/league/join" id="joinLeagueLink">Join a league</a>
</p>

<c:choose>
    <%-- Single league detail view (GET /league?leagueId=... or after a successful join) --%>
    <c:when test="${league != null}">
        <section id="leagueDetail">
            <h2><c:out value="${league.leagueName}" /></h2>
            <p><c:out value="${league.description}" /></p>
            <p>Type: <c:out value="${league.leagueType}" /></p>
            <p>Max members: <c:out value="${league.maxMembers}" /></p>
            <p>Manager: <c:out value="${league.managerDisplayName}" /></p>
            <c:if test="${not empty league.leagueCode}">
                <p>League code: <c:out value="${league.leagueCode}" /></p>
            </c:if>
            <p>
                <a href="${pageContext.request.contextPath}/league/members?leagueId=${league.leagueId}" id="viewMembersLink">View members</a>
            </p>
        </section>
        <p><a href="${pageContext.request.contextPath}/leagues" id="backToLeaguesLink">Back to all leagues</a></p>
    </c:when>

    <%-- Browse view (GET /leagues) --%>
    <c:otherwise>
        <section id="publicLeaguesSection">
            <h2>Public Leagues</h2>
            <c:choose>
                <c:when test="${empty publicLeagues}">
                    <p id="publicLeaguesEmptyState">No public leagues are available right now.</p>
                </c:when>
                <c:otherwise>
                    <table id="publicLeaguesTable">
                        <thead>
                        <tr>
                            <th>League Name</th>
                            <th>Description</th>
                            <th>Max Members</th>
                            <th>Manager</th>
                            <th></th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="publicLeague" items="${publicLeagues}">
                            <tr>
                                <td><c:out value="${publicLeague.leagueName}" /></td>
                                <td><c:out value="${publicLeague.description}" /></td>
                                <td><c:out value="${publicLeague.maxMembers}" /></td>
                                <td><c:out value="${publicLeague.managerDisplayName}" /></td>
                                <td>
                                    <a href="${pageContext.request.contextPath}/league?leagueId=${publicLeague.leagueId}">View</a>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </c:otherwise>
            </c:choose>
        </section>

        <section id="myLeaguesSection">
            <h2>My Leagues</h2>
            <c:choose>
                <c:when test="${empty myLeagues}">
                    <p id="myLeaguesEmptyState">You haven't joined or created any leagues yet.</p>
                </c:when>
                <c:otherwise>
                    <table id="myLeaguesTable">
                        <thead>
                        <tr>
                            <th>League Name</th>
                            <th>Description</th>
                            <th>Type</th>
                            <th>Max Members</th>
                            <th></th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="myLeague" items="${myLeagues}">
                            <tr>
                                <td><c:out value="${myLeague.leagueName}" /></td>
                                <td><c:out value="${myLeague.description}" /></td>
                                <td><c:out value="${myLeague.leagueType}" /></td>
                                <td><c:out value="${myLeague.maxMembers}" /></td>
                                <td>
                                    <a href="${pageContext.request.contextPath}/league?leagueId=${myLeague.leagueId}">View</a>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </c:otherwise>
            </c:choose>
        </section>
    </c:otherwise>
</c:choose>

</body>
</html>

