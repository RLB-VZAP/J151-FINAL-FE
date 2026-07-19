<%@ page contentType="text/html;charset=UTF-8" language="java" %>
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
        <dt>Club</dt><dd>${player.club.clubName}</dd>
        <dt>Position</dt><dd>${player.position.positionName}</dd>
        <dt>Value</dt><dd>${player.value}</dd>
        <dt>Current form</dt><dd>${player.currentForm}</dd>
        <dt>Fantasy points</dt><dd>${player.totalFantasyPoints}</dd>
        <dt>Status</dt><dd>${player.active ? 'Active' : 'Inactive'}</dd>
    </dl>
</main>
</body>
</html>
