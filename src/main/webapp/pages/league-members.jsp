<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>League Members - Fantasy TryTons</title>
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
</head>
<body>
<%@ include file="/WEB-INF/jspf/navigation.jspf" %>

<h1>League Members</h1>

<c:if test="${not empty error}">
    <p class="error-message" role="alert"><c:out value="${error}" /></p>
</c:if>

<c:if test="${not empty success}">
    <p class="success-message" role="status"><c:out value="${success}" /></p>
</c:if>

<c:choose>
    <c:when test="${empty leagueId}">
        <p id="noLeagueSelectedState">No league was selected. Go back to your leagues and pick one to view its members.</p>
    </c:when>
    <c:otherwise>
        <p>
            <a href="${pageContext.request.contextPath}/league?leagueId=${leagueId}" id="backToLeagueLink">Back to league</a>
        </p>

        <section id="memberListSection">
            <c:choose>
                <c:when test="${empty members}">
                    <p id="membersEmptyState">This league doesn't have any members yet.</p>
                </c:when>
                <c:otherwise>
                    <table id="membersTable">
                        <thead>
                        <tr>
                            <th>Manager</th>
                            <th>Team</th>
                            <th></th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="member" items="${members}">
                            <tr>
                                <td><c:out value="${member.userDisplayName}" /></td>
                                <td><c:out value="${member.teamDisplayName}" /></td>
                                <td>
                                    <%-- Remove control is only shown to the league manager as a UI convenience;
                                        the backend remains the sole judge of whether the current user is
                                        actually allowed to remove this member, and will reject the request
                                        with a safe error message if not. --%>
                                    <c:if test="${isLeagueManager}">
                                        <form method="post" action="${pageContext.request.contextPath}/league/members"
                                            class="member-remove-form">
                                            <input type="hidden" name="submit" value="league/members/remove" />
                                            <input type="hidden" name="leagueId" value="${leagueId}" />
                                            <input type="hidden" name="membershipId" value="${member.membershipId}" />
                                            <button type="submit">Remove</button>
                                        </form>
                                    </c:if>
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
