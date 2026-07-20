<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Notifications - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/notifications.css">
</head>
<body class="notifications">
<%@ include file="/WEB-INF/jspf/navigation.jspf" %>

<main id="notifications">

    <h1>Notifications</h1>

    <%-- TODO: Feedback section - show success/error messages set by NotificationServlet. --%>

    <%-- TODO: Notification list section - each notification's type, body, created date and read status. --%>

    <%-- TODO: Mark-as-read / mark-all-as-read action section. --%>

</main>
</body>
</html>
