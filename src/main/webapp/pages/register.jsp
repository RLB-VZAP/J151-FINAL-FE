<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

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

<%
  String errorMessage = (String) request.getAttribute("error");
  if (errorMessage != null && !errorMessage.isBlank()) {
%>
<p class="error-message" role="alert">
<%-- TODO [W4-FE-FIXES-45]: errorMessage written via unescaped scriptlet <%= %> on the unauthenticated registration page — reflected XSS if a validation error echoes submitted email/username; escape output --%>
<%= errorMessage %>
</p>
<%
  }
%>
<form method="post" action="${pageContext.request.contextPath}/register">
<div>
<label for="email">Email Address</label>
<input
      type="email"
      id="email"
      name="email"
      required>
</div>
<br>
<div>
<label for = "username">Username</label>
<input
      type="text"
      id="username"
      name="username"
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
<a href="${pageContext.request.contextPath}/pages/login.jsp">
Login here my brodie, get cooking. You're missing all the action!
</a>.
</p>
</main>
</body>
</html>