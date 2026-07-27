<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Resimulation - Fantasy TryTons</title>
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

        <header class="catalog-header">
            <div>
                <p class="catalog-eyebrow">Administration</p>
                <h1 class="brand-font">Resimulation</h1>
                <p class="asim-intro">
                    Trigger an admin-controlled resimulation for a fixture and review the full history of past runs.
                </p>
            </div>
        </header>

        <c:if test="${not empty success}">
            <p class="asim-alert asim-alert-success" role="status"><c:out value="${success}" /></p>
        </c:if>
        <c:if test="${not empty error}">
            <p class="asim-alert asim-alert-error" role="alert"><c:out value="${error}" /></p>
        </c:if>

        <%-- ---------- Trigger resimulation ---------- --%>
        <section class="asim-panel" id="resimulationTriggerSection">
            <h2 class="asim-panel-title">Trigger resimulation</h2>

            <%-- No static disclaimer here: the backend (ControlledResimulationServiceImpl) is
                 fully implemented, and a rejected trigger already surfaces its specific
                 reason (e.g. "The round has not reached its lock deadline.") through the
                 ${error} alert above — that is the only explanation an admin needs, and
                 only appears when something actually goes wrong. --%>
            <form method="post" action="${pageContext.request.contextPath}/admin/resimulation" id="resimulationTriggerForm">
                <%@ include file="/WEB-INF/jspf/csrf-field.jspf" %>
                <input type="hidden" name="action" value="resimulate">

                <div class="asim-row-2">
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
                </div>

                <button type="submit" class="btn-gold asim-submit">Trigger resimulation</button>
            </form>
        </section>

        <%-- ---------- Resimulation history ---------- --%>
        <section class="asim-panel" id="resimulationHistorySection">
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

            <c:if test="${not empty selectedFixtureId}">
                <c:choose>
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
                                        <span class="rtbl-when"><c:out value="${fn:replace(resimulation.resimulatedAt, 'T', ' ')}" /></span>
                                    </div>
                                </c:forEach>
                            </div>
                        </div>
                    </c:otherwise>
                </c:choose>
            </c:if>
        </section>

    </div>
</main>
</body>
</html>
