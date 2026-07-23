<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Scoring Rule Management - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/catalog.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-scoring-rules.css">
</head>
<body class="catalog-page asr-page">

<c:set var="activeNav" value="admin-scoring-rules" scope="request" />
<%@ include file="/WEB-INF/jspf/sidebar.jspf" %>

<main class="catalog-main" id="adminScoringRules">
    <div class="catalog-content">

        <header class="catalog-header">
            <div>
                <p class="catalog-eyebrow">Administration</p>
                <h1 class="brand-font">Scoring Rule Management</h1>
                <p class="asr-intro">
                    Manage the fantasy points awarded, or deducted, for each match event, scoped per season.
                </p>
            </div>
        </header>

        <c:if test="${not empty success}">
            <p class="asr-alert asr-alert-success" role="status"><c:out value="${success}" /></p>
        </c:if>
        <c:if test="${not empty error}">
            <p class="asr-alert asr-alert-error" role="alert"><c:out value="${error}" /></p>
        </c:if>
        <c:if test="${not empty rulesError}">
            <p class="asr-alert asr-alert-error" role="alert"><c:out value="${rulesError}" /></p>
        </c:if>

        <%-- ---------- Season selector ---------- --%>
        <section class="asr-panel" id="seasonSelection">
            <h2 class="asr-panel-title">Season</h2>
            <form action="${pageContext.request.contextPath}/admin/scoring-rules" method="get" id="seasonSelectForm" class="asr-season-form">
                <div class="asr-field asr-field-grow">
                    <label class="asr-label" for="seasonSelect">Season</label>
                    <input class="asr-input" type="text" id="seasonSelect" name="season"
                           value="${fn:escapeXml(selectedSeason)}" placeholder="e.g. 2025/26" required>
                </div>
                <button type="submit" class="asr-ghost">Load season</button>
            </form>
        </section>

        <div class="asr-grid">

            <%-- ---------- Rules table ---------- --%>
            <section id="scoringRuleListSection">
                <div class="asr-section-head">
                    <h2 class="asr-section-title">Rules for <c:out value="${selectedSeason}" /></h2>
                    <span class="asr-rule-line"></span>
                    <span class="asr-count">${fn:length(scoringRules)} rule${fn:length(scoringRules) == 1 ? '' : 's'}</span>
                </div>

                <c:choose>
                    <c:when test="${empty scoringRules}">
                        <p class="asr-empty" id="scoringRulesEmptyState">No scoring rules exist for this season yet.</p>
                    </c:when>
                    <c:otherwise>
                        <div class="srtbl" id="scoringRulesTable">
                            <div class="srtbl-scroll">
                                <div class="srtbl-head">
                                    <span>Event type</span>
                                    <span class="srtbl-c">Points</span>
                                    <span class="srtbl-c">Deduction</span>
                                    <span class="srtbl-c">Active</span>
                                    <span>Description</span>
                                    <span aria-hidden="true"></span>
                                </div>

                                <c:forEach var="rule" items="${scoringRules}">
                                    <%-- Points are stored as a magnitude with isDeduction carrying the
                                         sign, so the signed rendering here is presentational only. --%>
                                    <c:set var="mag" value="${rule.pointsAwarded < 0 ? -rule.pointsAwarded : rule.pointsAwarded}" />
                                    <div class="srtbl-row">
                                        <span class="srtbl-event"><c:out value="${rule.eventType}" /></span>
                                        <span class="srtbl-c">
                                            <span class="srtbl-pts ${rule.isDeduction ? 'is-ded' : 'is-award'}"><c:choose><c:when test="${rule.isDeduction}">&minus;</c:when><c:otherwise>+</c:otherwise></c:choose>${mag}</span>
                                        </span>
                                        <span class="srtbl-c"><span class="asr-flag ${rule.isDeduction ? 'is-yes' : 'is-no'}">${rule.isDeduction ? 'Yes' : 'No'}</span></span>
                                        <span class="srtbl-c"><span class="asr-flag ${rule.active ? 'is-yes' : 'is-no'}">${rule.active ? 'Yes' : 'No'}</span></span>
                                        <span class="srtbl-desc" title="${fn:escapeXml(rule.description)}"><c:out value="${rule.description}" /></span>
                                        <span class="srtbl-action">
                                            <a class="srtbl-edit" href="${pageContext.request.contextPath}/admin/scoring-rules?season=${fn:escapeXml(selectedSeason)}&amp;ruleId=${rule.ruleId}">Edit</a>
                                        </span>
                                    </div>
                                </c:forEach>
                            </div>
                        </div>
                    </c:otherwise>
                </c:choose>
            </section>

            <%-- ---------- Create / edit form ---------- --%>
            <section class="asr-panel" id="scoringRuleFormSection">
                <h2 class="asr-panel-title">
                    <c:choose>
                        <c:when test="${not empty editingRule}">Edit scoring rule</c:when>
                        <c:otherwise>Create scoring rule</c:otherwise>
                    </c:choose>
                </h2>

                <form method="post" action="${pageContext.request.contextPath}/admin/scoring-rules" id="scoringRuleForm">
                    <c:if test="${not empty editingRule}">
                        <input type="hidden" name="ruleId" value="${editingRule.ruleId}">
                    </c:if>

                    <div class="asr-field">
                        <label class="asr-label" for="eventType">Event type</label>
                        <input class="asr-input" type="text" id="eventType" name="eventType"
                               value="${not empty editingRule ? fn:escapeXml(editingRule.eventType) : ''}"
                               placeholder="e.g. TRY" required>
                    </div>

                    <div class="asr-field">
                        <label class="asr-label" for="pointsAwarded">Points awarded</label>
                        <input class="asr-input" type="number" id="pointsAwarded" name="pointsAwarded" step="1"
                               value="${not empty editingRule ? editingRule.pointsAwarded : ''}" required>
                    </div>

                    <div class="asr-field">
                        <label class="asr-label" for="season">Season</label>
                        <input class="asr-input" type="text" id="season" name="season"
                               value="${not empty editingRule ? fn:escapeXml(editingRule.season) : fn:escapeXml(selectedSeason)}" required>
                    </div>

                    <div class="asr-field">
                        <label class="asr-label" for="description">Description</label>
                        <input class="asr-input" type="text" id="description" name="description"
                               value="${not empty editingRule ? fn:escapeXml(editingRule.description) : ''}"
                               placeholder="Optional">
                    </div>

                    <div class="asr-checks">
                        <label class="asr-check" for="active">
                            <input type="checkbox" id="active" name="active"
                                   ${(empty editingRule) or editingRule.active ? 'checked' : ''}>
                            <span>Active</span>
                        </label>
                        <label class="asr-check" for="isDeduction">
                            <input type="checkbox" id="isDeduction" name="isDeduction"
                                   ${(not empty editingRule) and editingRule.isDeduction ? 'checked' : ''}>
                            <span>Deduction</span>
                        </label>
                    </div>

                    <button type="submit" name="submit" value="save-scoring-rule" class="btn-gold asr-submit">
                        <c:choose>
                            <c:when test="${not empty editingRule}">Update scoring rule</c:when>
                            <c:otherwise>Create scoring rule</c:otherwise>
                        </c:choose>
                    </button>

                    <c:if test="${not empty editingRule}">
                        <a class="asr-cancel" href="${pageContext.request.contextPath}/admin/scoring-rules?season=${fn:escapeXml(selectedSeason)}">Cancel edit</a>
                    </c:if>
                </form>
            </section>

        </div>

    </div>
</main>

</body>
</html>
