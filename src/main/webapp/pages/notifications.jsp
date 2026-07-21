<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="za.ac.vzap.trytons.frontend.client.notification.NotificationResponse" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

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

    <section id="notificationActions">
        <form method="get" action="${pageContext.request.contextPath}/notifications" id="unreadFilterForm">
            <label>
                <input type="checkbox" name="unreadOnly" value="true" onchange="this.form.submit()" <%= Boolean.TRUE.equals(request.getAttribute("unreadOnly")) ? "checked" : "" %>>
                Show unread only
            </label>
        </form>
        <form method="post" action="${pageContext.request.contextPath}/notifications" id="markAllForm">
            <input type="hidden" name="action" value="markAllAsRead">
            <button type="submit">Mark all as read</button>
        </form>
    </section>

    <section id="notificationListSection">
        <%
            List<NotificationResponse> notifications = (List<NotificationResponse>) request.getAttribute("notifications");
        %>
        <% if (notifications == null || notifications.isEmpty()) { %>
        <p id="notificationsEmptyState">You have no notifications yet.</p>
        <% } else { %>
        <table id="notificationsTable">
            <thead>
                <tr>
                    <th>Type</th>
                    <th>Message</th>
                    <th>Received</th>
                    <th>Status</th>
                    <th></th>
                </tr>
            </thead>
            <tbody>
                <%
                    for (NotificationResponse notification : notifications) {
                        if (notification == null) {
                            continue;
                        }
                        pageContext.setAttribute("notification", notification);
                %>
                <tr class="<%= notification.isRead() ? "is-read" : "is-unread" %>">
                    <td><c:out value="${notification.type}" /></td>
                    <td><c:out value="${notification.body}" /></td>
                    <td><c:out value="${notification.createdAt}" /></td>
                    <td><%= notification.isRead() ? "Read" : "Unread" %></td>
                    <td>
                        <% if (!notification.isRead()) { %>
                        <form method="post" action="${pageContext.request.contextPath}/notifications">
                            <input type="hidden" name="action" value="markAsRead">
                            <input type="hidden" name="notificationId" value="<%= notification.getNotificationId() %>">
                            <button type="submit">Mark as read</button>
                        </form>
                        <% } %>
                    </td>
                </tr>
                <%
                    }
                %>
            </tbody>
        </table>
        <% } %>
    </section>

</main>
</body>
</html>
