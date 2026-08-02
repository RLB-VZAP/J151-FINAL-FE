<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Create League - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/catalog.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/create-league.css">
</head>
<body class="catalog-page cl-page">

<c:set var="activeNav" value="leagues" scope="request" />
<%@ include file="/WEB-INF/jspf/sidebar.jspf" %>

<main class="catalog-main">
    <div class="catalog-content">

        <a class="cl-back" href="${pageContext.request.contextPath}/leagues">&larr; Back to leagues</a>

        <header class="catalog-header">
            <div>
                <p class="catalog-eyebrow">Leagues</p>
                <h1 class="brand-font">Create League</h1>
            </div>
        </header>

        <c:if test="${not empty error}">
            <p class="cl-alert" role="alert"><c:out value="${error}" /></p>
        </c:if>

        <c:choose>
            <%-- Creating a league enrols the creator as its first member and manager, and a
                 membership needs a team (leagueMembership.teamId is NOT NULL). The backend
                 rejects the attempt outright, so the form is withheld rather than letting it
                 be filled in and fail on submit. --%>
            <c:when test="${not canCreateLeague}">
                <div class="cl-notice">
                    <span class="cl-notice-icon" aria-hidden="true">
                        <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
                    </span>
                        <h2>You need a team first</h2>
                    <p>Creating a league signs you up as its first member and manager &mdash; and that needs a fantasy team.</p>
                    <a class="btn-gold cl-notice-cta" href="${pageContext.request.contextPath}/create-team">Create your team &rarr;</a>
                </div>
            </c:when>

            <c:otherwise>
                <%-- A successful create redirects straight to /league?leagueId=... (PRG), so
                     this page only ever renders the form (with an error on failure). --%>
                <form class="cl-card" method="post" action="${pageContext.request.contextPath}/league/create" id="createLeagueForm">
                    <%@ include file="/WEB-INF/jspf/csrf-field.jspf" %>
                    <input type="hidden" name="submit" value="league/create" />

                    <div class="cl-field">
                        <label class="cl-label" for="leagueName">League name</label>
                        <input class="cl-input" type="text" id="leagueName" name="leagueName"
                               value="${fn:escapeXml(param.leagueName)}" placeholder="e.g. Highveld Heroes"
                               maxlength="100" required>
                    </div>

                    <div class="cl-field">
                        <label class="cl-label" for="description">Description</label>
                        <textarea class="cl-textarea" id="description" name="description" rows="3"
                                  placeholder="What's this league about?" required><c:out value="${param.description}" /></textarea>
                    </div>

                    <div class="cl-row">
                        <div class="cl-field">
                            <span class="cl-label">Visibility</span>
                            <%-- There is no choice to make. Public leagues are part of the
                                 competition proper (master leaderboard, tournament seeding,
                                 pricing, market demand) and are run by administrators, who
                                 hold no fantasy team and so cannot be a founding member. A
                                 registered user's own league is a friendly: always private,
                                 joined with its league code. The backend rejects the other
                                 combination outright, so the form posts a fixed value rather
                                 than offering one that would be refused. ${isAdmin} comes
                                 from sidebar.jspf, included above. --%>
                            <c:choose>
                                <c:when test="${isAdmin}">
                                    <input type="hidden" name="leagueType" value="PUBLIC">
                                    <p class="cl-static">Public</p>
                                    <p class="cl-help">Administrator-created leagues are public: anyone can find and join them, and their results count towards the master leaderboard. You will not be a member of it yourself.</p>
                                </c:when>
                                <c:otherwise>
                                    <input type="hidden" name="leagueType" value="PRIVATE">
                                    <p class="cl-static">Private</p>
                                    <p class="cl-help">Your league is private. Once it is created you will get a league code &mdash; share it with friends and they join with it. Public leagues are run by administrators.</p>
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <div class="cl-field">
                            <label class="cl-label" for="maxMembers">Max members</label>
                            <input class="cl-input" type="number" id="maxMembers" name="maxMembers" min="1" max="100"
                                   value="${empty param.maxMembers ? 20 : fn:escapeXml(param.maxMembers)}" required>
                            <p class="cl-help">Up to how many teams can join${isAdmin ? '.' : ', including yours.'}</p>
                        </div>
                    </div>

                    <button type="submit" name="submit" value="league/create" class="btn-gold cl-submit">Create league</button>
                </form>
            </c:otherwise>
        </c:choose>

    </div>
</main>

</body>
</html>
