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
  String successMessage = (String) request.getAttribute("message");
  if (successMessage != null && !successMessage.isBlank()) {
%>
<%-- TODO [W4-FE-FIXES-40]: successMessage (line 20) and errorMessage (line 29) written via unescaped scriptlet <%= %> on an unauthenticated page — reflected XSS if the text ever echoes the submitted identifier; escape output --%>
<p class="success-message" role="status"><%= successMessage %></p>
<%
  }
%>
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
<a href="${pageContext.request.contextPath}/register">
Register here my brodie, get cooking. You're missing all the action!
</a>.
</p>
</main>
</body>
</html>