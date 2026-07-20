<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>System Reports - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-reports.css">
</head>
<body class="admin-reports">
<%@ include file="/WEB-INF/jspf/navigation.jspf" %>

<main id="adminReports">

    <h1>System Reports</h1>

    <%-- TODO: Feedback section - show success/error messages set by AdminReportServlet. --%>

    <%-- TODO: Report generation section - choose a report type and generate a new system report. --%>

    <%-- TODO: Report list section - table of previously generated reports with type, title and generated date. --%>

</main>
</body>
</html>
