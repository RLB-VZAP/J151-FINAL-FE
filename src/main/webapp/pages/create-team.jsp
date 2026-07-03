<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.LinkedHashSet" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Set" %>
<%@ page import="za.ac.vzap.trytons.frontend.client.PlayerResponse" %>

<%--
  W2-T05B - Fantasy Team Creation page.
  Served by FantasyTeamServlet (W2-T05A) at GET/POST /create-team.

  Request attributes consumed (all optional; page degrades safely):
    players           List<PlayerResponse>  selectable player pool
    budget            BigDecimal            team budget cap (preview only)
    squadSize         Integer               target squad size (preview only)
    error             String                top-level backend error
    validationErrors  List<String>          SquadValidationService messages (W2-T04A)
    message           String                success/info message
    teamName          String                sticky team name after failed submit
    selectedPlayerIds List<String>          sticky selection after failed submit

  Form contract posted back to /create-team:
    teamName, playerIds (repeated), submit=create-team
--%>

<%!
    private static String esc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static String fmt(BigDecimal v) {
        if (v == null) {
            return "0";
        }
        return new java.text.DecimalFormat("#,##0.##").format(v);
    }
%>

<%
    request.setAttribute("activeNav", "create-team");

    String errorMessage = (String) request.getAttribute("error");
    String successMessage = (String) request.getAttribute("message");
    @SuppressWarnings("unchecked")
    List<String> validationErrors = (List<String>) request.getAttribute("validationErrors");

    @SuppressWarnings("unchecked")
    List<PlayerResponse> players = (List<PlayerResponse>) request.getAttribute("players");

    BigDecimal budget = (BigDecimal) request.getAttribute("budget");
    if (budget == null) {
        budget = new BigDecimal("50000000");
    }
    Integer squadSize = (Integer) request.getAttribute("squadSize");
    if (squadSize == null) {
        squadSize = 15;
    }

    String teamNameValue = (String) request.getAttribute("teamName");
    if (teamNameValue == null) {
        teamNameValue = request.getParameter("teamName") != null ? request.getParameter("teamName") : "";
    }

    @SuppressWarnings("unchecked")
    List<String> stickyIds = (List<String>) request.getAttribute("selectedPlayerIds");
    Set<String> selectedIds = stickyIds == null ? new HashSet<>() : new HashSet<>(stickyIds);

    boolean loggedIn = session.getAttribute("userId") != null;


    BigDecimal selectedTotal = BigDecimal.ZERO;
    int selectedCount = 0;
    Set<String> positionCategories = new LinkedHashSet<>();
    Set<String> clubNames = new LinkedHashSet<>();
    if (players != null) {
        for (PlayerResponse p : players) {
            if (p == null) {
                continue;
            }
            if (p.getPosition() != null && p.getPosition().getPositionCategory() != null) {
                positionCategories.add(p.getPosition().getPositionCategory());
            }
            if (p.getClub() != null && p.getClub().getClubName() != null) {
                clubNames.add(p.getClub().getClubName());
            }
            if (p.getPlayerId() != null && selectedIds.contains(p.getPlayerId().toString())) {
                selectedCount++;
                if (p.getValue() != null) {
                    selectedTotal = selectedTotal.add(p.getValue());
                }
            }
        }
    }
    BigDecimal remaining = budget.subtract(selectedTotal);
%>

<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>TryTons - Create Team</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/create-team.css">
</head>
<body>

<%@ include file="/WEB-INF/jspf/nav.jspf" %>

<main class="ct-page">
<div class="ct-page__head">
<h1>Create your team</h1>
<p>Pick your squad from the player pool, keep an eye on your budget, name your team and submit.</p>
</div>

<% if (errorMessage != null && !errorMessage.isBlank()) { %>
<p class="error-message" role="alert"><%= esc(errorMessage) %></p>
<% } %>

<% if (validationErrors != null && !validationErrors.isEmpty()) { %>
<div class="error-message" role="alert">
<strong>Your squad could not be saved:</strong>
<ul>
<% for (String validationError : validationErrors) { %>
<li><%= esc(validationError) %></li>
<% } %>
</ul>
</div>
<% } %>

<% if (successMessage != null && !successMessage.isBlank()) { %>
<p class="success-message" role="status"><%= esc(successMessage) %></p>
<% } %>

<% if (!loggedIn) { %>
<div class="ct-card ct-locked">
<h2>You need to log in first</h2>
<p>Creating a fantasy team requires an account, so we know whose squad this is.</p>
<a class="btn-primary" href="${pageContext.request.contextPath}/pages/login.jsp">Log in to continue</a>
</div>
<% } else { %>

<form method="post"
      action="${pageContext.request.contextPath}/create-team"
      id="createTeamForm"
      class="ct-layout"
      data-budget="<%= budget.toPlainString() %>"
      data-squad-size="<%= squadSize %>">

<%-- ============================ Player pool ============================ --%>
<section class="ct-card ct-pool" aria-labelledby="poolHeading">
<div class="ct-pool__head">
<h2 id="poolHeading">Player pool</h2>
<span class="ct-pool__count" id="poolCount"></span>
</div>

<div class="ct-toolbar">
<input type="search"
       id="playerSearch"
       class="ct-toolbar__search"
       placeholder="Search players or clubs&hellip;"
       aria-label="Search players by name or club"
       autocomplete="off">
<select id="positionFilter" class="ct-toolbar__select" aria-label="Filter by position category">
<option value="">All positions</option>
<% for (String category : positionCategories) { %>
<option value="<%= esc(category) %>"><%= esc(category) %></option>
<% } %>
</select>
<select id="clubFilter" class="ct-toolbar__select" aria-label="Filter by club">
<option value="">All clubs</option>
<% for (String clubName : clubNames) { %>
<option value="<%= esc(clubName) %>"><%= esc(clubName) %></option>
<% } %>
</select>
</div>

<% if (players == null) { %>
<p class="ct-empty" role="alert">
The player pool could not be loaded right now. Please refresh the page or try again later.
</p>
<% } else if (players.isEmpty()) { %>
<p class="ct-empty">No players are available for selection yet.</p>
<% } else { %>
<div class="ct-table-wrap">
<table class="ct-table">
<thead>
<tr>
<th scope="col" class="ct-table__pick"><span class="visually-hidden">Pick</span></th>
<th scope="col">Player</th>
<th scope="col">Position</th>
<th scope="col" class="ct-table__num">Form</th>
<th scope="col" class="ct-table__num">Points</th>
<th scope="col" class="ct-table__num">Value</th>
<th scope="col">Status</th>
</tr>
</thead>
<tbody id="playerRows">
<%
    for (PlayerResponse p : players) {
        if (p == null || p.getPlayerId() == null) {
            continue;
        }
        String pid = p.getPlayerId().toString();
        String playerName = p.getPlayerName() != null ? p.getPlayerName() : "Unknown player";
        String clubName = p.getClub() != null && p.getClub().getClubName() != null ? p.getClub().getClubName() : "-";
        String positionName = p.getPosition() != null && p.getPosition().getPositionName() != null ? p.getPosition().getPositionName() : "-";
        String category = p.getPosition() != null && p.getPosition().getPositionCategory() != null ? p.getPosition().getPositionCategory() : "";
        BigDecimal value = p.getValue() != null ? p.getValue() : BigDecimal.ZERO;
        boolean available = p.isActive();
        boolean checked = selectedIds.contains(pid);
        String checkboxId = "pick-" + pid;
%>
<tr class="ct-row <%= available ? "" : "is-unavailable" %>"
    data-name="<%= esc(playerName.toLowerCase()) %>"
    data-club="<%= esc(clubName) %>"
    data-category="<%= esc(category) %>"
    data-value="<%= value.toPlainString() %>">
<td class="ct-table__pick">
<input type="checkbox"
       class="ct-pick"
       id="<%= checkboxId %>"
       name="playerIds"
       value="<%= pid %>"
       data-player-name="<%= esc(playerName) %>"
       <%= checked ? "checked" : "" %>
       <%= available ? "" : "disabled" %>
       aria-label="Select <%= esc(playerName) %>">
</td>
<td>
<label class="ct-player" for="<%= checkboxId %>">
<span class="ct-player__name"><%= esc(playerName) %></span>
<span class="ct-player__club"><%= esc(clubName) %></span>
</label>
</td>
<td>
<span class="ct-position"><%= esc(positionName) %></span>
<% if (!category.isBlank()) { %>
<span class="ct-position__category"><%= esc(category) %></span>
<% } %>
</td>
<td class="ct-table__num"><%= p.getCurrentForm() %></td>
<td class="ct-table__num"><%= p.getTotalFantasyPoints() %></td>
<td class="ct-table__num ct-table__value">R&nbsp;<%= fmt(value) %></td>
<td>
<span class="ct-badge <%= available ? "ct-badge--ok" : "ct-badge--off" %>">
<%= available ? "Available" : "Unavailable" %>
</span>
</td>
</tr>
<% } %>
</tbody>
</table>
</div>
<p class="ct-empty ct-empty--filtered" id="noMatches" hidden>No players match your search or filters.</p>
<% } %>
</section>

<%-- ==================== Budget preview & submit ==================== --%>
<aside class="ct-card ct-summary" aria-labelledby="summaryHeading">
<h2 id="summaryHeading">Your squad</h2>

<dl class="ct-budget">
<div class="ct-budget__row">
<dt>Budget</dt>
<dd id="budgetTotal">R&nbsp;<%= fmt(budget) %></dd>
</div>
<div class="ct-budget__row">
<dt>Selected value</dt>
<dd id="budgetUsed">R&nbsp;<%= fmt(selectedTotal) %></dd>
</div>
<div class="ct-budget__row ct-budget__row--remaining">
<dt>Remaining</dt>
<dd id="budgetRemaining">R&nbsp;<%= fmt(remaining) %></dd>
</div>
</dl>

<div class="ct-meter" role="presentation">
<div class="ct-meter__fill" id="budgetMeter"></div>
</div>
<p class="ct-over-warning" id="overBudgetWarning" role="alert" hidden>
You are over budget. You can still submit, but the server will reject an over-budget squad.
</p>

<p class="ct-squad-count">
<span id="selectedCount"><%= selectedCount %></span>&nbsp;/&nbsp;<%= squadSize %> players selected
</p>

<ul class="ct-selected" id="selectedList" aria-label="Selected players">
<%
    if (players != null) {
        for (PlayerResponse p : players) {
            if (p == null || p.getPlayerId() == null || !selectedIds.contains(p.getPlayerId().toString())) {
                continue;
            }
%>
<li data-player-id="<%= p.getPlayerId() %>"><%= esc(p.getPlayerName() != null ? p.getPlayerName() : "Unknown player") %></li>
<%
        }
    }
%>
</ul>
<p class="ct-selected-empty" id="selectedEmpty" <%= selectedCount > 0 ? "hidden" : "" %>>
No players selected yet.
</p>

<div class="ct-name-field">
<label for="teamName">Team name</label>
<input type="text"
       id="teamName"
       name="teamName"
       required
       maxlength="60"
       value="<%= esc(teamNameValue) %>"
       placeholder="e.g. Breakdown Bandits"
       autocomplete="off">
</div>

<button type="submit" name="submit" value="create-team" class="btn-primary">Create team</button>

<p class="ct-disclaimer">
Budget and squad numbers above are a preview only &mdash; final totals and squad
rules are validated on the server when you submit.
</p>
<noscript>
<p class="ct-disclaimer">JavaScript is disabled, so the preview will not update live. Your selection is still submitted and validated normally.</p>
</noscript>
</aside>
</form>
<% } %>
</main>

<script src="${pageContext.request.contextPath}/assets/js/create-team.js"></script>
</body>
</html>
