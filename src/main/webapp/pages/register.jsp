<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>TryTons - Register</title>
</head>

<body>
<main>
<h1>Register</h1>
<p>Please register a new account with your details.</p>

<c:if test="${not empty error}">
<p class="error-message" role="alert">
<c:out value="${error}"/>
</p>
</c:if>

<form method="post" action="${pageContext.request.contextPath}/register">
<div>
<label for="email">Email Address</label>
<input
      type="email"
      id="email"
      name="email"
      value="${fn:escapeXml(email)}"
      required>
</div>
<br>
<div>
<label for = "username">Username</label>
<input
      type="text"
      id="username"
      name="username"
      value="${fn:escapeXml(username)}"
      required>
</div>

<br>
<div>
<label for="rawPassword">Password</label>
<input
type="password"
id="rawPassword"
name="rawPassword"
required
autocomplete="new-password">
</div>
<br>
<button type="submit" name="submit" value="register">Register</button>
</form>
<p>
Already have an account?
<a href="${pageContext.request.contextPath}/login">
Log in to your Fantasy TryTons account.
</a>.
</p>
</main>
</body>
</html>
