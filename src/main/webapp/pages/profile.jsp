<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="za.ac.vzap.trytons.frontend.client.auth.ProfileResponse" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>My Profile - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/profile.css">
</head>
<body class="profile">
<%@ include file="/WEB-INF/jspf/navigation.jspf" %>

<main id="profile">

    <h1>My Profile</h1>

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

    <%
        ProfileResponse profile = (ProfileResponse) request.getAttribute("profile");
    %>
    <% if (profile == null) { %>
    <p>Your profile could not be loaded right now. Please try again later.</p>
    <% } else { %>

    <section id="profileDetails">
        <h2>Account details</h2>
        <dl>
            <dt>Email</dt>
            <dd><c:out value="${profile.email}" /></dd>
            <dt>Username</dt>
            <dd><c:out value="${profile.username}" /></dd>
            <dt>Role</dt>
            <dd><c:out value="${profile.role}" /></dd>
            <dt>Account status</dt>
            <dd><%= Boolean.TRUE.equals(profile.getIsActive()) ? "Active" : "Inactive" %></dd>
            <dt>Registration status</dt>
            <dd><c:out value="${profile.registrationStatus}" /></dd>
            <dt>Registered on</dt>
            <dd><c:out value="${profile.registrationDate}" /></dd>
            <dt>Last login</dt>
            <dd><c:out value="${profile.lastLoginAt}" /></dd>
        </dl>
    </section>

    <section id="profileUpdateSection">
        <h2>Update profile</h2>
        <%-- The backend treats a null field as "leave unchanged", so every input is pre-filled with
                the current profile value - submitting without editing a field must not blank it out. --%>
        <form method="post" action="${pageContext.request.contextPath}/profile" id="profileUpdateForm">
            <div>
                <label for="username">Username</label>
                <input type="text" id="username" name="username" value="${fn:escapeXml(profile.username)}" required>
            </div>
            <div>
                <label for="email">Email</label>
                <input type="email" id="email" name="email" value="${fn:escapeXml(profile.email)}" required>
            </div>
            <div>
                <label for="profilePic">Profile picture URL</label>
                <input type="text" id="profilePic" name="profilePic" value="${fn:escapeXml(profile.profilePic)}">
            </div>
            <button type="submit">Save changes</button>
        </form>
    </section>

    <% } %>

    <p>
        Want to change your password instead?
        <a href="${pageContext.request.contextPath}/profile/change-password">
            Go to change password
        </a>.
    </p>

</main>
</body>
</html>
