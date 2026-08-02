<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Tournament - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/catalog.css">
    <%-- Rounds & fixtures reuses the fx-* fixture-card classes from the standalone
         Fixtures page rather than restating them. --%>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/fixtures.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/tournament.css">
</head>
<body class="catalog-page tournament-page">

<%-- Reached from the league pages, so the sidebar keeps Leagues highlighted. --%>
<c:set var="activeNav" value="leagues" scope="request" />
<%@ include file="/WEB-INF/jspf/sidebar.jspf" %>

<c:set var="leagueName" value="${not empty tournament.leagueName ? tournament.leagueName : league.leagueName}" />

<main class="catalog-main">
    <div class="catalog-content">

        <header class="catalog-header">
            <div>
                <p class="catalog-eyebrow">Fantasy TryTons Tournament</p>
                <h1 class="brand-font">${empty leagueName ? 'Tournament' : fn:escapeXml(leagueName)}</h1>
            </div>
            <div class="tn-actions">
                <c:if test="${not empty leagueId}">
                    <a class="btn-outline" href="${pageContext.request.contextPath}/league?leagueId=${fn:escapeXml(leagueId)}">Back to league</a>
                </c:if>
                <a class="btn-outline" href="${pageContext.request.contextPath}/leagues">All leagues</a>
            </div>
        </header>

        <c:if test="${not empty error}">
            <p class="catalog-error" role="alert"><c:out value="${error}" /></p>
        </c:if>

        <c:choose>
            <%-- ==================== Not started yet ==================== --%>
            <c:when test="${notStarted}">
                <section class="tn-panel tn-notstarted">
                    <h2 class="brand-font">This league has not started yet</h2>
                    <p>
                        A tournament is drawn the moment a league starts: every manager is
                        seeded into a pool, the pool matchdays are generated, and the top
                        finishers carry through into a knockout bracket.
                    </p>
                    <p class="tn-muted">
                        A league needs at least two managers before it can be started. Until
                        then, invite more managers from the league&rsquo;s members page.
                    </p>

                    <div class="tn-notstarted-actions">
                        <c:if test="${not empty leagueId}">
                            <a class="btn-outline" href="${pageContext.request.contextPath}/league/members?leagueId=${fn:escapeXml(leagueId)}">View members</a>
                        </c:if>
                        <%-- Only the league's own manager may start it; the backend
                             enforces the same rule. --%>
                        <c:if test="${isLeagueManager and not empty leagueId}">
                            <form action="${pageContext.request.contextPath}/league/start" method="post">
                                <%@ include file="/WEB-INF/jspf/csrf-field.jspf" %>
                                <input type="hidden" name="leagueId" value="${fn:escapeXml(leagueId)}">
                                <button type="submit" class="btn-gold">Start league</button>
                            </form>
                        </c:if>
                    </div>
                </section>
            </c:when>

            <%-- ==================== Tournament ==================== --%>
            <c:when test="${tournament != null}">
                <c:set var="status" value="${fn:toUpperCase(tournament.status)}" />
                <c:set var="statusLabel">
                    <c:choose>
                        <c:when test="${status == 'POOL_STAGE'}">Pool stage</c:when>
                        <c:when test="${status == 'KNOCKOUT_STAGE'}">Knockout stage</c:when>
                        <c:when test="${status == 'COMPLETED'}">Completed</c:when>
                        <c:when test="${status == 'CANCELLED'}">Cancelled</c:when>
                        <c:otherwise>${fn:escapeXml(tournament.status)}</c:otherwise>
                    </c:choose>
                </c:set>

                <%-- Pools are uniform, so the first pool's size describes them all. --%>
                <c:set var="poolSize" value="${empty tournament.pools ? 0 : tournament.pools[0].poolSize}" />

                <section class="tn-summary">
                    <div class="tn-summary-head">
                        <div>
                            <p class="tn-season">Season <c:out value="${tournament.season}" /></p>
                            <p class="tn-shape">
                                ${tournament.poolCount} pool<c:if test="${tournament.poolCount != 1}">s</c:if><c:if test="${poolSize > 0}"> of ${poolSize}</c:if>
                                &middot; ${tournament.bracketSize}-team knockout
                            </p>
                        </div>
                        <span class="tn-pill tn-pill-${fn:toLowerCase(status)}">${statusLabel}</span>
                    </div>

                    <div class="tn-stats">
                        <div class="tn-stat">
                            <span class="tn-stat-label">Managers</span>
                            <span class="tn-stat-value">${tournament.managerCount}</span>
                        </div>
                        <div class="tn-stat">
                            <span class="tn-stat-label">Pools</span>
                            <span class="tn-stat-value">${tournament.poolCount}</span>
                        </div>
                        <div class="tn-stat">
                            <span class="tn-stat-label">Pool matchdays</span>
                            <span class="tn-stat-value">${tournament.poolMatchdays}</span>
                        </div>
                        <div class="tn-stat">
                            <span class="tn-stat-label">Knockout</span>
                            <span class="tn-stat-value">${tournament.bracketSize}</span>
                        </div>
                    </div>
                </section>

                <%-- ---------- Podium (completed tournaments only) ---------- --%>
                <c:if test="${status == 'COMPLETED'}">
                    <section class="tn-podium-wrap">
                        <h2 class="tn-section-title brand-font">Final standings</h2>
                        <div class="tn-podium">
                            <div class="tn-podium-col tn-podium-2">
                                <span class="tn-medal tn-medal-2">2</span>
                                <span class="tn-podium-team">${empty tournament.runnerUpTeamName ? '&ndash;' : fn:escapeXml(tournament.runnerUpTeamName)}</span>
                                <span class="tn-podium-role">Runner-up</span>
                                <span class="tn-podium-stand"></span>
                            </div>
                            <div class="tn-podium-col tn-podium-1">
                                <svg class="tn-crown" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M3 7l4.5 4L12 4l4.5 7L21 7l-2 12H5z"/></svg>
                                <span class="tn-medal tn-medal-1">1</span>
                                <span class="tn-podium-team">${empty tournament.championTeamName ? '&ndash;' : fn:escapeXml(tournament.championTeamName)}</span>
                                <span class="tn-podium-role">Champion</span>
                                <span class="tn-podium-stand"></span>
                            </div>
                            <div class="tn-podium-col tn-podium-3">
                                <span class="tn-medal tn-medal-3">3</span>
                                <span class="tn-podium-team">${empty tournament.thirdPlaceTeamName ? '&ndash;' : fn:escapeXml(tournament.thirdPlaceTeamName)}</span>
                                <span class="tn-podium-role">Third place</span>
                                <span class="tn-podium-stand"></span>
                            </div>
                        </div>
                    </section>
                </c:if>

                <%-- ---------- Pool tables ---------- --%>
                <c:if test="${not empty tournament.pools}">
                    <section class="tn-section">
                        <h2 class="tn-section-title brand-font">Pools</h2>
                        <p class="tn-section-note">
                            PF and PA are fantasy points scored and conceded. BP combines
                            attacking and losing bonus points. Highlighted rows have qualified
                            for the knockout stage.
                        </p>

                        <div class="tn-pool-grid">
                            <c:forEach var="pool" items="${tournament.pools}">
                                <article class="tn-pool">
                                    <div class="tn-pool-head">
                                        <h3 class="tn-pool-name brand-font">Pool <c:out value="${pool.poolName}" /></h3>
                                        <span class="tn-pool-size">${pool.poolSize} managers</span>
                                    </div>

                                    <div class="tn-scroll">
                                        <div class="ctable tn-table">
                                            <div class="crow chead tn-standing-row">
                                                <span>Pos</span>
                                                <span>Manager</span>
                                                <span class="tn-num">P</span>
                                                <span class="tn-num">W</span>
                                                <span class="tn-num">D</span>
                                                <span class="tn-num">L</span>
                                                <span class="tn-num">PF</span>
                                                <span class="tn-num">PA</span>
                                                <span class="tn-num">PD</span>
                                                <span class="tn-num">BP</span>
                                                <span class="tn-num">Pts</span>
                                            </div>
                                            <div class="cbody">
                                                <c:forEach var="row" items="${pool.standings}">
                                                    <div class="crow tn-standing-row ${row.qualified ? 'is-qualified' : ''}">
                                                        <span class="tn-pos">${empty row.position ? '&ndash;' : row.position}</span>
                                                        <span class="tn-manager">
                                                            <span class="tn-team-name" title="${fn:escapeXml(row.teamName)}">${fn:escapeXml(row.teamName)}</span>
                                                            <span class="tn-owner">@${fn:escapeXml(row.ownerUsername)}</span>
                                                        </span>
                                                        <span class="tn-num">${row.played}</span>
                                                        <span class="tn-num">${row.won}</span>
                                                        <span class="tn-num">${row.drawn}</span>
                                                        <span class="tn-num">${row.lost}</span>
                                                        <span class="tn-num">${row.pointsFor}</span>
                                                        <span class="tn-num">${row.pointsAgainst}</span>
                                                        <span class="tn-num">${row.pointsDifference > 0 ? '+' : ''}${row.pointsDifference}</span>
                                                        <span class="tn-num">${row.attackBonus + row.losingBonus}</span>
                                                        <span class="tn-num tn-pts">${row.tournamentPoints}</span>
                                                    </div>
                                                </c:forEach>
                                            </div>
                                        </div>
                                    </div>
                                </article>
                            </c:forEach>
                        </div>
                    </section>
                </c:if>

                <%-- ---------- Knockout bracket ---------- --%>
                <c:if test="${not empty bracketGroups}">
                    <section class="tn-section">
                        <h2 class="tn-section-title brand-font">Knockout bracket</h2>
                        <div class="tn-scroll">
                            <div class="tn-bracket">
                                <c:forEach var="stage" items="${bracketGroups}">
                                    <div class="tn-round">
                                        <h3 class="tn-round-title">${fn:escapeXml(stage.key)}</h3>
                                        <div class="tn-round-list">
                                            <c:forEach var="tie" items="${stage.value}">
                                                <c:set var="played" value="${tie.teamAScore != null and tie.teamBScore != null}" />
                                                <article class="tn-tie">
                                                    <div class="tn-tie-side ${played and tie.teamAScore > tie.teamBScore ? 'is-winner' : ''}">
                                                        <span class="tn-tie-team">${empty tie.teamAName ? 'TBC' : fn:escapeXml(tie.teamAName)}</span>
                                                        <span class="tn-tie-score">${played ? tie.teamAScore : ''}</span>
                                                    </div>
                                                    <div class="tn-tie-side ${played and tie.teamBScore > tie.teamAScore ? 'is-winner' : ''}">
                                                        <span class="tn-tie-team">${empty tie.teamBName ? 'TBC' : fn:escapeXml(tie.teamBName)}</span>
                                                        <span class="tn-tie-score">${played ? tie.teamBScore : ''}</span>
                                                    </div>
                                                    <%-- Without both scores there is nothing to show but
                                                         where the tie has got to. --%>
                                                    <c:if test="${not played}">
                                                        <span class="tn-tie-status">${fn:escapeXml(empty tie.status ? 'UPCOMING' : tie.status)}</span>
                                                    </c:if>
                                                </article>
                                            </c:forEach>
                                        </div>
                                    </div>
                                </c:forEach>
                            </div>
                        </div>
                    </section>
                </c:if>

            </c:when>

            <c:otherwise>
                <p class="catalog-empty">No tournament to show.</p>
            </c:otherwise>
        </c:choose>

        <%-- ---------- Friendly-league note ---------- --%>
        <%@ include file="/WEB-INF/jspf/friendly-league-notice.jspf" %>

        <%-- ---------- Rounds & fixtures ----------
             League-scoped rather than tournament-scoped, so this renders even for a
             FORMING league with no tournament yet. Covers pool, knockout and any
             non-tournament fixture, grouped by fantasy round; a knockout tie shows
             up here as well as in the bracket above. --%>
        <c:if test="${not empty roundGroups}">
            <section class="tn-section" id="rounds-fixtures">
                <h2 class="tn-section-title brand-font">Rounds &amp; fixtures</h2>

                <c:forEach var="group" items="${roundGroups}">
                    <details class="tn-round-block" ${group.current ? 'open' : ''}>
                        <summary class="fx-group-head tn-round-summary">
                            <h3 class="fx-group-title">
                                <c:choose>
                                    <c:when test="${not empty group.roundNumber}">Round ${group.roundNumber}</c:when>
                                    <c:otherwise>Fixtures</c:otherwise>
                                </c:choose>
                                &middot; ${fn:escapeXml(group.stageLabel)}
                            </h3>
                            <span class="fx-group-rule"></span>
                            <span class="tn-round-count">${group.count} fixture<c:if test="${group.count != 1}">s</c:if></span>
                        </summary>

                        <div class="fx-list tn-round-list-fixtures">
                            <c:forEach var="fixture" items="${group.fixtures}">
                                <c:set var="fxStatus" value="${empty fixture.fixtureStatus ? 'UPCOMING' : fn:toUpperCase(fixture.fixtureStatus)}" />
                                <c:set var="fxHasScore" value="${fixture.teamAScore != null and fixture.teamBScore != null}" />

                                <article class="fx-card fx-card-${fn:toLowerCase(fxStatus)}">
                                    <span class="fx-side fx-side-home">
                                        <p class="fx-team-name ${not empty myTeamId and myTeamId == fixture.teamAId ? 'is-mine' : ''}">${fn:escapeXml(fixture.teamAName)}</p>
                                    </span>

                                    <div class="fx-centre">
                                        <c:choose>
                                            <c:when test="${fxHasScore}">
                                                <span class="fx-score">
                                                    <span class="${fixture.teamAScore > fixture.teamBScore ? 'is-winner' : (fixture.teamAScore < fixture.teamBScore ? 'is-loser' : '')}">${fixture.teamAScore}</span>&ndash;<span class="${fixture.teamBScore > fixture.teamAScore ? 'is-winner' : (fixture.teamBScore < fixture.teamAScore ? 'is-loser' : '')}">${fixture.teamBScore}</span>
                                                </span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="fx-vs">VS</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>

                                    <span class="fx-side fx-side-away">
                                        <p class="fx-team-name ${not empty myTeamId and myTeamId == fixture.teamBId ? 'is-mine' : ''}">${fn:escapeXml(fixture.teamBName)}</p>
                                    </span>

                                    <div class="fx-meta">
                                        <span class="fx-pill fx-pill-${fn:toLowerCase(fxStatus)}">${fn:escapeXml(fxStatus)}</span>
                                        <span class="fx-meta-when">
                                            ${fixtureDateById[fixture.fixtureId]}
                                            <c:if test="${not empty fixtureTimeById[fixture.fixtureId]}">&middot; ${fixtureTimeById[fixture.fixtureId]}</c:if>
                                        </span>
                                        <a class="fx-view" href="${pageContext.request.contextPath}/fixture?submit=fixture&amp;fixtureId=${fixture.fixtureId}">View &rarr;</a>
                                    </div>
                                </article>
                            </c:forEach>
                        </div>
                    </details>
                </c:forEach>
            </section>
        </c:if>

    </div>
</main>
</body>
</html>
