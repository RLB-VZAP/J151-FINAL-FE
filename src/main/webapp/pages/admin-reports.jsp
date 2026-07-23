<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>System Reports - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/catalog.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-reports.css">
</head>
<body class="catalog-page arep-page">

<c:set var="activeNav" value="admin-reports" scope="request" />
<%@ include file="/WEB-INF/jspf/sidebar.jspf" %>

<main class="catalog-main" id="adminReports">
    <div class="catalog-content">

        <header class="catalog-header">
            <div>
                <p class="catalog-eyebrow">Administration</p>
                <h1 class="brand-font">System Reports</h1>
            </div>
        </header>

        <c:if test="${not empty success}">
            <p class="arep-alert arep-alert-success" role="status"><c:out value="${success}" /></p>
        </c:if>
        <c:if test="${not empty error}">
            <p class="arep-alert arep-alert-error" role="alert"><c:out value="${error}" /></p>
        </c:if>

        <%-- ---------- Generated reports + generate form ---------- --%>
        <div class="arep-grid">

            <section id="reportList">
                <div class="arep-section-head">
                    <h2 class="arep-section-title">Previously generated reports</h2>
                    <span class="arep-rule-line"></span>
                    <span class="arep-count">${fn:length(reports)} report${fn:length(reports) == 1 ? '' : 's'}</span>
                </div>

                <c:choose>
                    <c:when test="${empty reports}">
                        <p class="arep-empty">No reports have been generated yet.</p>
                    </c:when>
                    <c:otherwise>
                        <div class="rtbl2">
                            <div class="rtbl2-scroll">
                                <div class="rtbl2-head">
                                    <span>Type</span>
                                    <span>Title</span>
                                    <span>Generated at</span>
                                </div>
                                <c:forEach var="report" items="${reports}">
                                    <div class="rtbl2-row">
                                        <span><span class="arep-chip"><c:out value="${report.reportType}" /></span></span>
                                        <span class="rtbl2-title" title="${fn:escapeXml(report.reportTitle)}"><c:out value="${report.reportTitle}" /></span>
                                        <span class="rtbl2-when">${fn:substring(fn:replace(report.generatedAt, 'T', ' '), 0, 16)}</span>
                                    </div>
                                </c:forEach>
                            </div>
                        </div>
                    </c:otherwise>
                </c:choose>
            </section>

            <section class="arep-panel" id="reportGeneration">
                <h2 class="arep-panel-title">Generate report</h2>
                <form action="${pageContext.request.contextPath}/admin/reports" method="post" id="generateReportForm">
                    <div class="arep-field">
                        <label class="arep-label" for="reportType">Report type</label>
                        <div class="arep-select-wrap">
                            <select class="arep-select" id="reportType" name="reportType" required>
                                <option value="">&mdash; Select report type &mdash;</option>
                                <option value="ACTIVE_USERS" ${param.reportType == 'ACTIVE_USERS' ? 'selected' : ''}>Active Users</option>
                                <option value="ACTIVE_LEAGUES" ${param.reportType == 'ACTIVE_LEAGUES' ? 'selected' : ''}>Active Leagues</option>
                                <option value="TOP_FANTASY_TEAMS" ${param.reportType == 'TOP_FANTASY_TEAMS' ? 'selected' : ''}>Top Fantasy Teams</option>
                                <option value="TOP_RUGBY_PLAYERS" ${param.reportType == 'TOP_RUGBY_PLAYERS' ? 'selected' : ''}>Top Rugby Players</option>
                                <option value="MOST_SELECTED_PLAYERS" ${param.reportType == 'MOST_SELECTED_PLAYERS' ? 'selected' : ''}>Most Selected Players</option>
                                <option value="UNAVAILABLE_PLAYERS" ${param.reportType == 'UNAVAILABLE_PLAYERS' ? 'selected' : ''}>Unavailable Players</option>
                                <option value="COMPLETED_FIXTURES" ${param.reportType == 'COMPLETED_FIXTURES' ? 'selected' : ''}>Completed Fixtures</option>
                                <option value="FIXTURE_RESULTS" ${param.reportType == 'FIXTURE_RESULTS' ? 'selected' : ''}>Fixture Results</option>
                                <option value="TRANSFER_ACTIVITY" ${param.reportType == 'TRANSFER_ACTIVITY' ? 'selected' : ''}>Transfer Activity</option>
                                <option value="LEAGUE_CHAT_ACTIVITY" ${param.reportType == 'LEAGUE_CHAT_ACTIVITY' ? 'selected' : ''}>League Chat Activity</option>
                                <option value="SYSTEM_ACTIVITY" ${param.reportType == 'SYSTEM_ACTIVITY' ? 'selected' : ''}>System Activity</option>
                            </select>
                            <svg class="arep-select-caret" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M6 9l6 6 6-6"/></svg>
                        </div>
                    </div>

                    <div class="arep-field">
                        <label class="arep-label" for="reportTitle">Report title</label>
                        <input class="arep-input" type="text" id="reportTitle" name="reportTitle"
                               value="${fn:escapeXml(param.reportTitle)}" placeholder="e.g. Weekly Active Users Report" required>
                    </div>

                    <div class="arep-field">
                        <label class="arep-label" for="parametersJson">Parameters (JSON &mdash; optional)</label>
                        <textarea class="arep-textarea" id="parametersJson" name="parametersJson" rows="4"
                                  placeholder='{"season":2026,"limit":50}'><c:out value="${param.parametersJson}" /></textarea>
                        <p class="arep-help">Optional JSON object containing report parameters.</p>
                    </div>

                    <button type="submit" class="btn-gold arep-submit">Generate report</button>
                </form>
            </section>

        </div>

        <%-- ---------- System activity logs ---------- --%>
        <section id="systemLogs">
            <div class="arep-section-head">
                <h2 class="arep-section-title">Recent system activity logs</h2>
                <span class="arep-rule-line"></span>
                <span class="arep-count">${fn:length(logs)} entr${fn:length(logs) == 1 ? 'y' : 'ies'}</span>
            </div>

            <c:choose>
                <c:when test="${empty logs}">
                    <p class="arep-empty">No logs are available.</p>
                </c:when>
                <c:otherwise>
                    <div class="ltbl">
                        <div class="ltbl-scroll">
                            <div class="ltbl-head">
                                <span>Created at</span>
                                <span>Action</span>
                                <span>Entity type</span>
                                <span>Description</span>
                            </div>
                            <c:forEach var="log" items="${logs}">
                                <div class="ltbl-row">
                                    <span class="ltbl-when">${fn:substring(fn:replace(log.createdAt, 'T', ' '), 0, 16)}</span>
                                    <span><span class="arep-chip"><c:out value="${log.actionType}" /></span></span>
                                    <span class="ltbl-entity"><c:out value="${log.entityType}" /></span>
                                    <span class="ltbl-desc" title="${fn:escapeXml(log.description)}"><c:out value="${log.description}" /></span>
                                </div>
                            </c:forEach>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>

    </div>
</main>

</body>
</html>
