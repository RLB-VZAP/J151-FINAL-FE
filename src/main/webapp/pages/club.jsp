<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <%-- TODO [W4-FE-FIXES-33]: unescaped admin-supplied club data (${club.clubName} here and lines 14,17,18: name, location, homeVenue) — stored XSS; use c:out/fn:escapeXml --%>
    <title>${club.clubName} - TryTons</title>
</head>
<body>
<%@ include file="/WEB-INF/jspf/navigation.jspf" %>

<main>
    <p><a href="${pageContext.request.contextPath}/clubs?submit=clubs">&larr; Back to clubs</a></p>
    <h1>${club.clubName}</h1>

    <dl>
        <dt>Location</dt><dd>${empty club.location ? 'Not supplied' : club.location}</dd>
        <dt>Home venue</dt><dd>${empty club.homeVenue ? 'Not supplied' : club.homeVenue}</dd>
        <dt>Status</dt><dd>${club.active ? 'Active' : 'Inactive'}</dd>
    </dl>
</main>
</body>
</html>
