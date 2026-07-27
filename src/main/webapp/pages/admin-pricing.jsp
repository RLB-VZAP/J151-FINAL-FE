<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="za.ac.vzap.trytons.frontend.client.pricing.PricingRunSummaryResponse" %>
<%@ page import="za.ac.vzap.trytons.frontend.client.pricing.PriceChangeResponse" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dynamic Pricing - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-pricing.css">
</head>
<body class="admin-pricing">

<c:set var="activeNav" value="admin-pricing" scope="request" />
<%@ include file="/WEB-INF/jspf/sidebar.jspf" %>

<main id="pricing">
    <h1>Dynamic Player Pricing</h1>

    <c:if test="${not empty error}">
        <p class="error-message alert alert-danger" role="alert"><c:out value="${error}"/></p>
    </c:if>
    <c:if test="${not empty info}">
        <p class="info-message alert alert-info" role="status"><c:out value="${info}"/></p>
    </c:if>
    <c:if test="${not empty success}">
        <p class="success-message alert alert-success" role="status"><c:out value="${success}"/></p>
    </c:if>

    <p class="intro">
        Prices adjust automatically after each round is processed, using form, popularity,
        fantasy points, injuries, transfer demand and availability. You can also preview or
        apply a run manually below. Existing team budgets are never changed retroactively.
    </p>

    <section id="pricingSettings" class="card">
        <h2>Weighting &amp; bounds</h2>
        <form method="post" action="${pageContext.request.contextPath}/admin/pricing" class="settings-form">
            <%@ include file="/WEB-INF/jspf/csrf-field.jspf" %>
            <input type="hidden" name="action" value="saveSettings">
            <div class="row g-3">
                <div class="col-md-4">
                    <label for="weightForm" class="form-label">Form weight</label>
                    <input type="number" step="0.0001" min="0" id="weightForm" name="weightForm" value="${settings.weightForm}" class="form-control">
                </div>
                <div class="col-md-4">
                    <label for="weightPopularity" class="form-label">Popularity weight</label>
                    <input type="number" step="0.0001" min="0" id="weightPopularity" name="weightPopularity" value="${settings.weightPopularity}" class="form-control">
                </div>
                <div class="col-md-4">
                    <label for="weightPoints" class="form-label">Fantasy points weight</label>
                    <input type="number" step="0.0001" min="0" id="weightPoints" name="weightPoints" value="${settings.weightPoints}" class="form-control">
                </div>
                <div class="col-md-4">
                    <label for="weightInjury" class="form-label">Injury weight</label>
                    <input type="number" step="0.0001" min="0" id="weightInjury" name="weightInjury" value="${settings.weightInjury}" class="form-control">
                </div>
                <div class="col-md-4">
                    <label for="weightDemand" class="form-label">Transfer demand weight</label>
                    <input type="number" step="0.0001" min="0" id="weightDemand" name="weightDemand" value="${settings.weightDemand}" class="form-control">
                </div>
                <div class="col-md-4">
                    <label for="weightAvailability" class="form-label">Availability weight</label>
                    <input type="number" step="0.0001" min="0" id="weightAvailability" name="weightAvailability" value="${settings.weightAvailability}" class="form-control">
                </div>
                <div class="col-md-4">
                    <label for="maxDeltaPct" class="form-label">Max change per run (fraction)</label>
                    <input type="number" step="0.0001" min="0" id="maxDeltaPct" name="maxDeltaPct" value="${settings.maxDeltaPct}" class="form-control">
                </div>
                <div class="col-md-4">
                    <label for="minValue" class="form-label">Minimum value</label>
                    <input type="number" step="0.01" min="0" id="minValue" name="minValue" value="${settings.minValue}" class="form-control">
                </div>
                <div class="col-md-4">
                    <label for="maxValue" class="form-label">Maximum value</label>
                    <input type="number" step="0.01" min="0" id="maxValue" name="maxValue" value="${settings.maxValue}" class="form-control">
                </div>
            </div>
            <button type="submit" class="btn btn-gold mt-4">Save settings</button>
        </form>
    </section>

    <section id="pricingActions" class="card">
        <h2>Run pricing</h2>
        <div class="d-flex gap-3 mb-4">
            <form method="post" action="${pageContext.request.contextPath}/admin/pricing">
                <%@ include file="/WEB-INF/jspf/csrf-field.jspf" %>
                <input type="hidden" name="action" value="preview">
                <button type="submit" class="btn btn-gold">Preview changes</button>
            </form>
            <form method="post" action="${pageContext.request.contextPath}/admin/pricing"
                  onsubmit="return confirm('Apply these price changes to all players now?');">
                <%@ include file="/WEB-INF/jspf/csrf-field.jspf" %>
                <input type="hidden" name="action" value="run">
                <button type="submit" class="btn btn-outline-danger">Apply now</button>
            </form>
        </div>

        <%
            PricingRunSummaryResponse summary = (PricingRunSummaryResponse) request.getAttribute("summary");
            if (summary != null) {
                pageContext.setAttribute("summary", summary);
        %>
            <div class="run-summary">
                <p>
                    <c:choose>
                        <c:when test="${summary.applied}"><strong>Applied.</strong></c:when>
                        <c:otherwise><strong>Preview.</strong></c:otherwise>
                    </c:choose>
                    Evaluated ${summary.playersEvaluated} players, ${summary.playersRepriced} repriced.
                    Total increase ${summary.totalIncrease}, total decrease ${summary.totalDecrease}.
                </p>

                <%
                    List<PriceChangeResponse> changes = summary.getChanges();
                    if (changes != null && !changes.isEmpty()) {
                %>
                <table class="changes-table table table-dark table-hover">
                    <thead>
                    <tr><th>Player</th><th>Old</th><th>New</th><th>Change</th></tr>
                    </thead>
                    <tbody>
                        <%
                            for (PriceChangeResponse change : changes) {
                                if (change == null) { continue; }
                                pageContext.setAttribute("change", change);
                                boolean up = change.getDelta() != null && change.getDelta().signum() > 0;
                        %>
                        <tr class="<%= up ? "up" : "down" %>">
                            <td><c:out value="${change.playerName}"/></td>
                            <td>${change.oldValue}</td>
                            <td>${change.newValue}</td>
                            <td class="delta"><%= up ? "+" : "" %>${change.delta}</td>
                        </tr>
                        <% } %>
                    </tbody>
                </table>
                <% } else { %>
                    <p class="empty-state">No players changed price in this run.</p>
                <% } %>
            </div>
        <% } %>
    </section>
</main>
</body>
</html>
