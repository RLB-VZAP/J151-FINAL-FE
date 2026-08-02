<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Fixture Administration - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/catalog.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-fixtures.css">
</head>
<body class="catalog-page afx-page">

<c:set var="activeNav" value="admin-fixtures" scope="request" />
<%@ include file="/WEB-INF/jspf/sidebar.jspf" %>

<c:set var="statusOptions" value="UPCOMING,LOCKED,SIMULATING,COMPLETED,PROCESSED,CANCELLED" />

<main class="catalog-main" id="adminFixtures">
    <div class="catalog-content">

        <header class="catalog-header">
            <div>
                <p class="catalog-eyebrow">Administration</p>
                <h1 class="brand-font">Fixture Administration</h1>
                <p class="afx-intro">Review fixtures and update their status as a round progresses.</p>
            </div>
        </header>

        <c:if test="${not empty success}">
            <p class="afx-alert afx-alert-success" role="status"><c:out value="${success}" /></p>
        </c:if>
        <c:if test="${not empty error}">
            <p class="afx-alert afx-alert-error" role="alert"><c:out value="${error}" /></p>
        </c:if>
        <c:if test="${not empty fixturesError}">
            <p class="afx-alert afx-alert-error" role="alert"><c:out value="${fixturesError}" /></p>
        </c:if>

        <div class="afx-grid">
            <div class="afx-col">

                <section id="fixtureListSection">
                    <div class="afx-section-head">
                        <h2 class="afx-section-title">Fixtures</h2>
                        <span class="afx-rule-line"></span>
                        <span class="afx-count">${fn:length(fixtures)} fixture${fn:length(fixtures) == 1 ? '' : 's'}</span>
                    </div>

                    <form action="${pageContext.request.contextPath}/admin/fixtures" method="get" id="fixtureFilterForm" class="afx-filter">
                        <div class="afx-select-wrap afx-filter-select">
                            <select class="afx-select" id="statusFilter" name="status" aria-label="Filter by status">
                                <option value="" ${empty statusFilter ? 'selected' : ''}>All statuses</option>
                                <c:forEach var="s" items="${fn:split(statusOptions, ',')}">
                                    <option value="${s}" ${statusFilter eq s ? 'selected' : ''}>${fn:substring(s, 0, 1)}${fn:toLowerCase(fn:substring(s, 1, fn:length(s)))}</option>
                                </c:forEach>
                            </select>
                            <svg class="afx-select-caret" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M6 9l6 6 6-6"/></svg>
                        </div>
                        <button type="submit" class="afx-ghost">Apply filter</button>
                    </form>

                    <c:choose>
                        <c:when test="${empty fixtures}">
                            <p class="afx-empty" id="fixturesEmptyState">No fixtures were found.</p>
                        </c:when>
                        <c:otherwise>
                            <div class="ftbl">
                                <div class="ftbl-scroll">
                                    <div class="ftbl-head">
                                        <span>League</span>
                                        <span>Round</span>
                                        <span>Team A</span>
                                        <span>Team B</span>
                                        <span>Date</span>
                                        <span>Status</span>
                                        <span>Update status</span>
                                    </div>

                                    <c:forEach var="fixture" items="${fixtures}">
                                        <c:set var="fxStatus" value="${empty fixture.fixtureStatus ? 'UPCOMING' : fixture.fixtureStatus}" />
                                        <div class="ftbl-row">
                                            <span class="ftbl-league">${empty leagueNamesById[fixture.leagueId.toString()] ? '&mdash;' : fn:escapeXml(leagueNamesById[fixture.leagueId.toString()])}</span>
                                            <span class="ftbl-round">${empty roundLabelsById[fixture.roundId.toString()] ? '&mdash;' : fn:escapeXml(roundLabelsById[fixture.roundId.toString()])}</span>
                                            <span class="ftbl-team" title="${fn:escapeXml(fixture.teamAName)}"><c:out value="${fixture.teamAName}" /></span>
                                            <span class="ftbl-team" title="${fn:escapeXml(fixture.teamBName)}"><c:out value="${fixture.teamBName}" /></span>
                                            <span class="ftbl-date">${fixture.fixtureDate}</span>
                                            <span><span class="afx-pill afx-pill-${fn:toLowerCase(fxStatus)}">${fn:substring(fxStatus, 0, 1)}${fn:toLowerCase(fn:substring(fxStatus, 1, fn:length(fxStatus)))}</span></span>
                                            <span class="ftbl-update">
                                                <%-- Three separate writes, not one dropdown.

                                                     The status dropdown offers only the purely administrative moves the
                                                     backend still accepts (see AdminFixtureServlet.statusTransitions).
                                                     Completed and Processed are gone from it: they are derived facts, and
                                                     selecting them used to set the column while writing none of the rows
                                                     behind it. They are now reached through the actions that do the work:

                                                       Simulate  (locked fixture)    plays the match, writing the
                                                                                     matchResult and playerStatistics
                                                       Process   (completed fixture) awards fantasy points, writes both
                                                                                     match_team_score rows, refreshes the
                                                                                     leaderboard

                                                     Both refuse for real reasons (lock deadline not reached, squad short
                                                     of 20 players, ...); the servlet surfaces the backend's message. --%>
                                                <c:set var="nextStatuses" value="${statusTransitions[fxStatus]}" />
                                                <c:set var="canSimulate" value="${fxStatus eq 'LOCKED'}" />
                                                <c:set var="canProcess" value="${fxStatus eq 'COMPLETED'}" />

                                                <c:if test="${canSimulate}">
                                                    <form method="post" action="${pageContext.request.contextPath}/admin/fixtures" class="simulateFixtureForm afx-update-form">
                                                        <%@ include file="/WEB-INF/jspf/csrf-field.jspf" %>
                                                        <input type="hidden" name="action" value="simulate">
                                                        <input type="hidden" name="fixtureId" value="${fixture.fixtureId}">
                                                        <button type="submit" class="afx-update-btn" title="Play this match and generate its result">Simulate</button>
                                                    </form>
                                                </c:if>

                                                <c:if test="${canProcess}">
                                                    <form method="post" action="${pageContext.request.contextPath}/admin/fixtures" class="processFixtureForm afx-update-form">
                                                        <%@ include file="/WEB-INF/jspf/csrf-field.jspf" %>
                                                        <input type="hidden" name="action" value="process">
                                                        <input type="hidden" name="fixtureId" value="${fixture.fixtureId}">
                                                        <button type="submit" class="afx-update-btn" title="Award fantasy points and refresh the leaderboard">Process</button>
                                                    </form>
                                                </c:if>

                                                <c:if test="${not empty nextStatuses}">
                                                    <form method="post" action="${pageContext.request.contextPath}/admin/fixtures" class="statusUpdateForm afx-update-form">
                                                        <%@ include file="/WEB-INF/jspf/csrf-field.jspf" %>
                                                        <input type="hidden" name="action" value="status">
                                                        <input type="hidden" name="fixtureId" value="${fixture.fixtureId}">
                                                        <div class="afx-select-wrap afx-update-select">
                                                            <select class="afx-select afx-select-sm" name="status" aria-label="New status">
                                                                <c:forEach var="s" items="${nextStatuses}">
                                                                    <option value="${s}">${fn:substring(s, 0, 1)}${fn:toLowerCase(fn:substring(s, 1, fn:length(s)))}</option>
                                                                </c:forEach>
                                                            </select>
                                                            <svg class="afx-select-caret" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M6 9l6 6 6-6"/></svg>
                                                        </div>
                                                        <button type="submit" class="afx-update-btn">Update</button>
                                                    </form>
                                                </c:if>

                                                <c:if test="${empty nextStatuses and not canSimulate and not canProcess}">
                                                    <span class="afx-update-none">No further changes</span>
                                                </c:if>
                                            </span>
                                        </div>
                                    </c:forEach>
                                </div>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </section>
            </div>

        </div>

    </div>
</main>

</body>
</html>
