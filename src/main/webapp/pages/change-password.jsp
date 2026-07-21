<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Change Password - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/change-password.css">
</head>
<body class="change-password">
<%@ include file="/WEB-INF/jspf/navigation.jspf" %>

<main id="changePassword">

    <h1>Change Password</h1>

    <c:if test="${not empty error}">
    <p class="error-message" role="alert">
        <c:out value="${error}" />
    </p>
    </c:if>

    <c:if test="${not empty success}">
    <p class="success-message" role="status">
        <c:out value="${success}" />
    </p>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/profile/change-password" id="changePasswordForm">
        <div>
            <label for="currentPassword">Current password</label>
            <input
                type="password"
                id="currentPassword"
                name="currentPassword"
                required
                autocomplete="current-password">
        </div>
        <div>
            <label for="newPassword">New password</label>
            <input
                type="password"
                id="newPassword"
                name="newPassword"
                required
                autocomplete="new-password">
        </div>
        <div>
            <label for="confirmPassword">Confirm new password</label>
            <%-- Confirmation is client-side only - only currentPassword/newPassword are sent to the server. --%>
            <input
                type="password"
                id="confirmPassword"
                name="confirmPassword"
                required
                autocomplete="new-password">
        </div>
        <p class="error-message" role="alert" id="passwordMismatchWarning" hidden>
            New password and confirmation do not match.
        </p>
        <button type="submit">Change password</button>
    </form>

    <p>
        <a href="${pageContext.request.contextPath}/profile">Back to profile</a>
    </p>

</main>
<script>
    document.getElementById('changePasswordForm').addEventListener('submit', function (event) {
        var newPassword = document.getElementById('newPassword').value;
        var confirmPassword = document.getElementById('confirmPassword').value;
        var warning = document.getElementById('passwordMismatchWarning');
        if (newPassword !== confirmPassword) {
            event.preventDefault();
            warning.hidden = false;
        } else {
            warning.hidden = true;
        }
    });
</script>
</body>
</html>
