<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Fixture Administration - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-fixtures.css">
</head>
<body class="admin-fixtures">
<%@ include file="/WEB-INF/jspf/navigation.jspf" %>

<main id="adminFixtures">

    <h1>Fixture Administration</h1>

    <%-- TODO [W4-FE-FIXES-30]: Feedback section - show success/error messages set by AdminFixtureServlet. --%>

    <%-- TODO [W4-FE-FIXES-30]: Fixture list section - table of fixtures with league, round, teams and status. --%>

    <%-- TODO [W4-FE-FIXES-30]: Fixture status update section - a form/action to update a fixture's status. --%>

</main>
</body>
</html>
