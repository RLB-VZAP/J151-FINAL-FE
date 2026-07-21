<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Dashboard - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/dashboard.css">
</head>
<body class="dashboard">
<%@ include file="/WEB-INF/jspf/navigation.jspf" %>

<main id="dashboard">

    <c:choose>
        <c:when test="${not empty sessionScope.username}">
            <h1>Welcome, ${fn:escapeXml(sessionScope.username)}</h1>
        </c:when>
        <c:otherwise>
            <h1>Welcome to Fantasy TryTons</h1>
        </c:otherwise>
    </c:choose>
    <p>Jump back into your league from here.</p>

    <section id="dashboardShortcuts">
        <a class="dashboard-tile" href="${pageContext.request.contextPath}/pages/create-team.jsp">
            <h2>Create Team</h2>
            <p>Build your squad and get ready to compete.</p>
        </a>
        <a class="dashboard-tile" href="${pageContext.request.contextPath}/leagues">
            <h2>Leagues</h2>
            <p>Browse, create or join a fantasy league.</p>
        </a>
        <a class="dashboard-tile" href="${pageContext.request.contextPath}/fixtures?submit=fixtures">
            <h2>Fixtures</h2>
            <p>See upcoming and completed fixtures.</p>
        </a>
        <a class="dashboard-tile" href="${pageContext.request.contextPath}/transfers">
            <h2>Transfers</h2>
            <p>Manage your squad's transfers.</p>
        </a>
        <a class="dashboard-tile" href="${pageContext.request.contextPath}/notifications">
            <h2>Notifications</h2>
            <p>Check your latest notifications.</p>
        </a>
        <a class="dashboard-tile" href="${pageContext.request.contextPath}/profile">
            <h2>Profile</h2>
            <p>View and update your account details.</p>
        </a>
    </section>

</main>
</body>
</html>
