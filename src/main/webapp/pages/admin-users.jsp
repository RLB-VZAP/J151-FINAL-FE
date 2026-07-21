<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>User Management - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-users.css">
</head>
<body class="admin-users">
<%@ include file="/WEB-INF/jspf/navigation.jspf" %>

<main id="adminUsers">

    <h1>User Management</h1>

    <%-- TODO [W4-FE-FIXES-32]: Feedback section - show success/error messages set by AdminUserServlet. --%>

    <%-- TODO [W4-FE-FIXES-32]: Search section - a form to search users by search term. --%>

    <%-- TODO [W4-FE-FIXES-32]: User list section - table of matching users with email, username, role and active status, plus an activate/deactivate action per row. --%>

</main>
</body>
</html>
