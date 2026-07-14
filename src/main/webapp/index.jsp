<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    if (session.getAttribute("userId") == null) {
        response.sendRedirect(request.getContextPath() + "/login");
    } else {
        response.sendRedirect(request.getContextPath() + "/players?submit=players");
    }
%>
