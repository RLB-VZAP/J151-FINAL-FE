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
    <title>Dynamic Pricing - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-pricing.css">
</head>
<body class="admin-pricing">
<%@ include file="/WEB-INF/jspf/navigation.jspf" %>

<main id="pricing">
    <h1>Dynamic Player Pricing</h1>

    <c:if test="${not empty error}">
        <p class="error-message" role="alert"><c:out value="${error}"/></p>
    </c:if>
    <c:if test="${not empty info}">
        <p class="info-message" role="status"><c:out value="${info}"/></p>
    </c:if>
    <c:if test="${not empty success}">
        <p class="success-message" role="status"><c:out value="${success}"/></p>
    </c:if>

    <p class="intro">
        Prices adjust automatically after each round is processed, using form, popularity,
        fantasy points, injuries, transfer demand and availability. You can also preview or
        apply a run manually below. Existing team budgets are never changed retroactively.
    </p>

    <section id="pricingSettings">
        <h2>Weighting &amp; bounds</h2>
        <form method="post" action="${pageContext.request.contextPath}/admin/pricing" class="settings-form">
            <input type="hidden" name="action" value="saveSettings">
            <div class="settings-grid">
                <label>Form weight
                    <input type="number" step="0.0001" min="0" name="weightForm" value="${settings.weightForm}">
                </label>
                <label>Popularity weight
                    <input type="number" step="0.0001" min="0" name="weightPopularity" value="${settings.weightPopularity}">
                </label>
                <label>Fantasy points weight
                    <input type="number" step="0.0001" min="0" name="weightPoints" value="${settings.weightPoints}">
                </label>
                <label>Injury weight
                    <input type="number" step="0.0001" min="0" name="weightInjury" value="${settings.weightInjury}">
                </label>
                <label>Transfer demand weight
                    <input type="number" step="0.0001" min="0" name="weightDemand" value="${settings.weightDemand}">
                </label>
                <label>Availability weight
                    <input type="number" step="0.0001" min="0" name="weightAvailability" value="${settings.weightAvailability}">
                </label>
                <label>Max change per run (fraction)
                    <input type="number" step="0.0001" min="0" name="maxDeltaPct" value="${settings.maxDeltaPct}">
                </label>
                <label>Minimum value
                    <input type="number" step="0.01" min="0" name="minValue" value="${settings.minValue}">
                </label>
                <label>Maximum value
                    <input type="number" step="0.01" min="0" name="maxValue" value="${settings.maxValue}">
                </label>
            </div>
            <button type="submit" class="save-button">Save settings</button>
        </form>
    </section>

    <section id="pricingActions">
        <h2>Run pricing</h2>
        <div class="action-buttons">
            <form method="post" action="${pageContext.request.contextPath}/admin/pricing">
                <input type="hidden" name="action" value="preview">
                <button type="submit" class="preview-button">Preview changes</button>
            </form>
            <form method="post" action="${pageContext.request.contextPath}/admin/pricing"
                  onsubmit="return confirm('Apply these price changes to all players now?');">
                <input type="hidden" name="action" value="run">
                <button type="submit" class="run-button">Apply now</button>
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
                <table class="changes-table">
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
