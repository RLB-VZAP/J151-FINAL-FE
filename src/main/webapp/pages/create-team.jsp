<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.UUID" %>
<%@ page import="za.ac.vzap.trytons.frontend.client.catalog.PlayerResponse" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>TryTons - Create Team</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/create-team.css">
    </head>

    <body>
        <% request.setAttribute("activeNav", "create-team"); %>
        <%@ include file="/WEB-INF/jspf/navigation.jspf" %>
        <main>
            <h1>Create Team</h1>
            <p>Pick your players from the pool, keep an eye on your budget and give your team a name.</p>

            <c:if test="${not empty error}">
            <p class="error-message" role="alert">
                <c:out value="${error}" />
            </p>
            </c:if>

            <c:if test="${not empty validationErrors}">
            <div class="error-message" role="alert">
                <ul>
                    <c:forEach var="validationError" items="${validationErrors}">
                    <li><c:out value="${validationError}" /></li>
                    </c:forEach>
                </ul>
            </div>
            </c:if>

            <c:if test="${not empty message}">
            <p class="success-message" role="status">
                <c:out value="${message}" />
            </p>
            </c:if>

            <%
                List<PlayerResponse> players = (List<PlayerResponse>) request.getAttribute("players");
                Object budget = request.getAttribute("budget");
                if (budget == null) {
                    budget = "50000000";
                }
                Object squadSize = request.getAttribute("squadSize");
                if (squadSize == null) {
                    squadSize = "15";
                }
                request.setAttribute("budget", budget);
                request.setAttribute("squadSize", squadSize);
            %>
            <form method="post" action="${pageContext.request.contextPath}/create-team" id="createTeamForm" data-budget="${fn:escapeXml(budget)}">

                <section>
                    <h2>Player Pool</h2>
                    <div>
                        <input
                            type="search"
                            id="playerSearch"
                            placeholder="Search players..."
                            aria-label="Search players">
                    </div>

                    <% if (players == null) { %>
                    <p>The players could not be loaded right now. Please try again later.</p>
                    <% } else if (players.isEmpty()) { %>
                    <p>No players are available for selection yet.</p>
                    <% } else { %>
                    <table>
                        <thead>
                            <tr>
                                <th></th>
                                <th>Player</th>
                                <th>Club</th>
                                <th>Position</th>
                                <th>Value</th>
                                <th>Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            <%
                                Map<UUID, String> clubNamesById = (Map<UUID, String>) request.getAttribute("clubNamesById");
                                Map<UUID, String> positionNamesById = (Map<UUID, String>) request.getAttribute("positionNamesById");
                                for (PlayerResponse p : players) {
                                    if (p == null || p.getPlayerId() == null) {
                                        continue;
                                    }
                                    String clubName = (clubNamesById == null) ? "-" : clubNamesById.getOrDefault(p.getClubId(), "-");
                                    String positionName = (positionNamesById == null) ? "-" : positionNamesById.getOrDefault(p.getPositionId(), "-");
                                    pageContext.setAttribute("player", p);
                                    pageContext.setAttribute("clubName", clubName);
                                    pageContext.setAttribute("positionName", positionName);
                            %>
                            <tr>
                                <td>
                                    <input
                                        type="checkbox"
                                        name="playerIds"
                                        value="<%= p.getPlayerId() %>"
                                        data-player-name="${fn:escapeXml(player.playerName)}"
                                        data-value="<%= p.getValue() %>"
                                        <%= p.isActive() ? "" : "disabled" %>>
                                </td>
                                <td><c:out value="${player.playerName}" /></td>
                                <td><c:out value="${clubName}" /></td>
                                <td><c:out value="${positionName}" /></td>
                                <td>R <%= p.getValue() %></td>
                                <td><%= p.isActive() ? "Available" : "Unavailable" %></td>
                            </tr>
                            <%
                                }
                            %>
                        </tbody>
                    </table>
                    <% } %>
                </section>

                <section>
                    <h2>Your Squad</h2>
                    <p>Budget: <span id="budgetTotal">R ${fn:escapeXml(budget)}</span></p>
                    <p>Selected value: <span id="budgetUsed">R 0</span></p>
                    <p>Remaining: <span id="budgetRemaining">R ${fn:escapeXml(budget)}</span></p>
                    <p>Players selected: <span><span id="selectedCount">0</span> / ${fn:escapeXml(squadSize)}</span></p>
                    <p class="error-message" role="alert" id="overBudgetWarning" hidden>
                        You are over budget. You can still submit, but the server will reject an over-budget squad.
                    </p>
                    <ul id="selectedList"></ul>
                    <div>
                        <label for="teamName">Team name</label>
                        <input
                            type="text"
                            id="teamName"
                            name="teamName"
                            required>
                    </div>
                    <br>
                    <button type="submit" name="submit" value="create-team">Create team</button>
                    <p>The budget numbers are a preview only. Final totals and squad rules are checked on the server.</p>
                </section>
            </form>
            <p>
                Just browsing?
                <a href="${pageContext.request.contextPath}/players?submit=players">
                    Check out the full player list here my brodie, get cooking. You're missing all the action!
                </a>.
            </p>
        </main>
        <script src="${pageContext.request.contextPath}/assets/js/create-team.js"></script>
    </body>
</html>
