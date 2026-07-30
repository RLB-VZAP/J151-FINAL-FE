<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Controlled Resimulation - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/catalog.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-simulation.css">
</head>
<body class="catalog-page asim-page">

<c:set var="activeNav" value="admin-resimulation" scope="request" />
<%@ include file="/WEB-INF/jspf/sidebar.jspf" %>

<main class="catalog-main" id="adminResimulation">
    <div class="catalog-content">

        <header class="catalog-header asim-header">
            <div>
                <p class="catalog-eyebrow">Administration</p>
                <h1 class="brand-font">Controlled Resimulation</h1>
                <p class="asim-intro">
                    Re-run the match simulation for an individual fixture and review every run recorded against it.
                    The caps that govern what is allowed come from the active simulation settings.
                </p>
            </div>
            <a class="asim-ghost asim-page-link" href="${pageContext.request.contextPath}/admin/simulation">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M19 12H5"/><path d="M11 18l-6-6 6-6"/></svg>
                Simulation settings
            </a>
        </header>

        <c:if test="${not empty success}">
            <p class="asim-alert asim-alert-success" role="status"><c:out value="${success}" /></p>
        </c:if>
        <c:if test="${not empty error}">
            <p class="asim-alert asim-alert-error" role="alert"><c:out value="${error}" /></p>
        </c:if>
        <c:if test="${not empty activeSettingError}">
            <p class="asim-alert asim-alert-error" role="alert"><c:out value="${activeSettingError}" /></p>
        </c:if>

        <%-- ---------- Governing limits from the active settings ---------- --%>
        <%-- Only the four fields that decide whether a trigger is accepted are
             repeated here. The weightings belong to the settings screen; showing
             them again would just be noise on this page. --%>
        <c:if test="${not empty activeSetting}">
            <section id="resimulationLimitsSection">
                <div class="asim-section-head">
                    <h2 class="asim-section-title">Governing limits</h2>
                    <span class="asim-rule-line"></span>
                    <a class="asim-count-link" href="${pageContext.request.contextPath}/admin/simulation">Change these</a>
                </div>

                <div class="asim-cells" id="resimulationLimitsTable">
                    <div class="asim-cell">
                        <span class="asim-cell-label">Season</span>
                        <span class="asim-cell-value is-gold"><c:out value="${activeSetting.season}" /></span>
                    </div>
                    <div class="asim-cell">
                        <span class="asim-cell-label">Allows resim.</span>
                        <span class="asim-cell-value"><span class="asim-flag ${activeSetting.allowResimulation ? 'is-yes' : 'is-no'}">${activeSetting.allowResimulation ? 'Yes' : 'No'}</span></span>
                    </div>
                    <div class="asim-cell">
                        <span class="asim-cell-label">Max resimulations</span>
                        <span class="asim-cell-value"><c:out value="${activeSetting.maxResimulations}" /></span>
                    </div>
                    <div class="asim-cell">
                        <span class="asim-cell-label">Admin approval</span>
                        <span class="asim-cell-value"><span class="asim-flag ${activeSetting.requireAdminApproval ? 'is-yes' : 'is-no'}">${activeSetting.requireAdminApproval ? 'Yes' : 'No'}</span></span>
                    </div>
                </div>

                <c:if test="${not activeSetting.allowResimulation}">
                    <p class="asim-stub" id="resimulationDisabledNote">
                        The active settings for <c:out value="${activeSetting.season}" /> do not allow resimulation.
                        Triggers will be rejected until &ldquo;Allow resimulation&rdquo; is enabled on the
                        <a href="${pageContext.request.contextPath}/admin/simulation">simulation settings</a> page.
                    </p>
                </c:if>
            </section>
        </c:if>

        <%-- ---------- History + trigger ---------- --%>
        <%-- No static disclaimer here: the backend (ControlledResimulationServiceImpl) is
             fully implemented, and a rejected trigger already surfaces its specific
             reason (e.g. "The round has not reached its lock deadline.") through the
             ${error} alert above — that is the only explanation an admin needs, and
             only appears when something actually goes wrong. --%>
        <%-- Trigger sits on the left as the primary action; the history beside it is
             the reference you check against. Both halves are equal width and are
             stretched to a common height so the split reads down the middle. --%>
        <section id="resimulationSection">
            <div class="asim-grid asim-grid-even asim-grid-split">

                <section class="asim-panel">
                    <h2 class="asim-panel-title">Trigger resimulation</h2>

                    <form method="post" action="${pageContext.request.contextPath}/admin/resimulation" id="resimulationTriggerForm">
                        <%@ include file="/WEB-INF/jspf/csrf-field.jspf" %>
                        <input type="hidden" name="action" value="resimulate">

                        <div class="asim-field">
                            <label class="asim-label" for="resimulateFixtureId">Fixture</label>
                            <select class="asim-input" id="resimulateFixtureId" name="fixtureId" required>
                                <option value="">&mdash; Select fixture &mdash;</option>
                                <c:forEach var="fixture" items="${fixtures}">
                                    <option value="${fixture.fixtureId}" ${fixture.fixtureId eq selectedFixtureId ? 'selected' : ''}>
                                        <c:out value="${fixture.teamAName}" /> vs <c:out value="${fixture.teamBName}" /> &mdash; ${fixture.fixtureDate}
                                    </option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="asim-field">
                            <label class="asim-label" for="resimulationReason">Reason</label>
                            <input class="asim-input" type="text" id="resimulationReason" name="resimulationReason" placeholder="e.g. correction after review" required>
                        </div>

                        <button type="submit" class="btn-gold asim-submit">Trigger resimulation</button>
                    </form>
                </section>

                <section class="asim-panel">
                    <h2 class="asim-panel-title">Resimulation history</h2>

                    <form method="get" action="${pageContext.request.contextPath}/admin/resimulation" id="resimulationHistoryForm" class="asim-inline-form">
                        <div class="asim-field asim-field-grow">
                            <label class="asim-label" for="historyFixtureId">Fixture</label>
                            <select class="asim-input" id="historyFixtureId" name="fixtureId" required>
                                <option value="">&mdash; Select fixture &mdash;</option>
                                <c:forEach var="fixture" items="${fixtures}">
                                    <option value="${fixture.fixtureId}" ${fixture.fixtureId eq selectedFixtureId ? 'selected' : ''}>
                                        <c:out value="${fixture.teamAName}" /> vs <c:out value="${fixture.teamBName}" /> &mdash; ${fixture.fixtureDate}
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                        <button type="submit" class="asim-ghost">View history</button>
                    </form>

                    <c:choose>
                        <c:when test="${empty selectedFixtureId}">
                            <p class="asim-empty asim-empty-sm" id="resimulationsPromptState">Select a fixture to see the runs recorded against it.</p>
                        </c:when>
                        <c:when test="${empty resimulations}">
                            <p class="asim-empty asim-empty-sm" id="resimulationsEmptyState">No resimulations recorded for this fixture yet.</p>
                        </c:when>
                        <c:otherwise>
                            <div class="rtbl" id="resimulationsTable">
                                <div class="rtbl-scroll">
                                    <div class="rtbl-head">
                                        <span class="rtbl-c">Run #</span>
                                        <span>Reason</span>
                                        <span class="rtbl-c">Current</span>
                                        <span class="rtbl-c">Approved</span>
                                        <span>Resimulated at</span>
                                    </div>
                                    <c:forEach var="resimulation" items="${resimulations}">
                                        <div class="rtbl-row">
                                            <span class="rtbl-c rtbl-run"><c:out value="${resimulation.simulationRunNumber}" /></span>
                                            <span class="rtbl-reason" title="${fn:escapeXml(resimulation.resimulationReason)}"><c:out value="${resimulation.resimulationReason}" /></span>
                                            <span class="rtbl-c"><span class="asim-flag ${resimulation.current ? 'is-yes' : 'is-no'}">${resimulation.current ? 'Yes' : 'No'}</span></span>
                                            <span class="rtbl-c"><span class="asim-flag ${resimulation.approved ? 'is-yes' : 'is-no'}">${resimulation.approved ? 'Yes' : 'No'}</span></span>
                                            <span class="rtbl-when">${fn:substring(fn:replace(resimulation.resimulatedAt, 'T', ' '), 0, 16)}</span>
                                        </div>
                                    </c:forEach>
                                </div>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </section>

            </div>
        </section>

    </div>
</main>
</body>
</html>
