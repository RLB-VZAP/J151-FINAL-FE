
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Join League - Fantasy TryTons</title>
</head>
<body>
<%@ include file="/WEB-INF/jspf/navigation.jspf" %>

<h1>Join League</h1>

<c:if test="${not empty error}">
    <p class="error-message" role="alert"><c:out value="${error}" /></p>
</c:if>

<p>
    Don't know a league's ID yet?
    <a href="${pageContext.request.contextPath}/leagues" id="browseLeaguesLink">Browse public leagues here</a>.
</p>

<%-- Both public and private joins go through the same submission; the join code
     is only required when the league you're joining is private. --%>
<form method="post" action="${pageContext.request.contextPath}/league/join" id="joinLeagueForm">
    <input type="hidden" name="submit" value="league/join" />

    <section id="publicJoinSection">
        <h2>Join a public league</h2>
        <p>Enter the League ID of a public league (found on the Leagues page). No join code needed.</p>
        <div>
            <label for="leagueId">League ID</label>
            <input type="text" id="leagueId" name="leagueId"
                   value="${param.leagueId}" required>
        </div>
    </section>

    <br>

    <section id="privateJoinSection">
        <h2>Join a private league</h2>
        <p>Private leagues also need the join code your league manager shared with you.</p>
        <div>
            <label for="leagueCode">Join code</label>
            <%-- Join code is sensitive: never pre-filled from a URL, and this form always posts, never appears in a query string. --%>
            <input type="text" id="leagueCode" name="leagueCode" autocomplete="off">
        </div>
    </section>

    <br>

    <button type="submit" name="submit" value="league/join">Join league</button>
</form>

</body>
</html>
