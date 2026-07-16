<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

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
<a href="${pageContext.request.contextPath}/pages/register.jsp">
Register here my brodie, get cooking. You're missing all the action!
</a>.
</p>
</main>
</body>
</html>