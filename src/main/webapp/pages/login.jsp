<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>TryTons - Login</title>
</head>

<body>
<main>
<h1>Login</h1>
<p>Please log in using your username or email address.</p>

<c:if test="${param.registered == '1'}">
<p class="success-message" role="status">
Registration successful. Please log in with your new account.
</p>
</c:if>
<c:if test="${param.expired == '1'}">
<p class="notice-message" role="alert">
Your session has expired. Please log in again.
</p>
</c:if>

<%
    String errorMessage = (String) request.getAttribute("error");
    if (errorMessage != null && !errorMessage.isBlank()) {
%>
<p class="error-message" role="alert">
<%= errorMessage %>
</p>
<%
    }
%>
<form method="post" action="${pageContext.request.contextPath}/login">
<div>
<label for="identifier">Username or email address</label>
<input
    type="text"
    id="identifier"
    name="identifier"
    value="${fn:escapeXml(identifier)}"
    required
    autocomplete="username">
</div>
<br>
<div>
<label for="password">Password</label>
<input
type="password"
id="password"
name="password"
required
autocomplete="current-password">
</div>
<br>
<button type="submit" name="submit" value="login">Log in</button>
</form>
<p>
Don't have an account?
<a href="${pageContext.request.contextPath}/register">
Register for a Fantasy TryTons account.
</a>.
</p>
</main>
</body>
</html>
