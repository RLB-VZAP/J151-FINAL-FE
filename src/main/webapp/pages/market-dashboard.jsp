<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Market Dashboard - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/market-dashboard.css">
</head>
<body class="market-dashboard">
<%@ include file="/WEB-INF/jspf/navigation.jspf" %>

<main id="market">
    <h1>Market Demand Dashboard</h1>
    <p class="intro">Live market intelligence across the whole competition, based on all confirmed transfers,
        current squad selections and fantasy points earned.</p>

    <c:if test="${not empty error}">
        <p class="error-message" role="alert"><c:out value="${error}"/></p>
    </c:if>

    <div class="cards">
        <%-- Trending --%>
        <section class="card">
            <h2>Trending</h2>
            <p class="card-sub">Highest net transfers in</p>
            <c:choose>
                <c:when test="${empty dashboard.trending}"><p class="empty-state">No data yet.</p></c:when>
                <c:otherwise>
                    <table><thead><tr><th>Player</th><th>Net</th><th>In</th><th>Out</th></tr></thead><tbody>
                        <c:forEach var="p" items="${dashboard.trending}">
                            <tr>
                                <td><a href="${pageContext.request.contextPath}/player?submit=player&playerId=${p.playerId}"><c:out value="${p.playerName}"/></a></td>
                                <td class="pos">+${p.netTransfers}</td>
                                <td>${p.transfersIn}</td>
                                <td>${p.transfersOut}</td>
                            </tr>
                        </c:forEach>
                    </tbody></table>
                </c:otherwise>
            </c:choose>
        </section>

        <%-- Most transferred in --%>
        <section class="card">
            <h2>Most transferred in</h2>
            <p class="card-sub">Confirmed transfers in</p>
            <c:choose>
                <c:when test="${empty dashboard.mostTransferredIn}"><p class="empty-state">No data yet.</p></c:when>
                <c:otherwise>
                    <table><thead><tr><th>Player</th><th>In</th></tr></thead><tbody>
                        <c:forEach var="p" items="${dashboard.mostTransferredIn}">
                            <tr>
                                <td><a href="${pageContext.request.contextPath}/player?submit=player&playerId=${p.playerId}"><c:out value="${p.playerName}"/></a></td>
                                <td class="pos">${p.transfersIn}</td>
                            </tr>
                        </c:forEach>
                    </tbody></table>
                </c:otherwise>
            </c:choose>
        </section>

        <%-- Most transferred out --%>
        <section class="card">
            <h2>Most transferred out</h2>
            <p class="card-sub">Confirmed transfers out</p>
            <c:choose>
                <c:when test="${empty dashboard.mostTransferredOut}"><p class="empty-state">No data yet.</p></c:when>
                <c:otherwise>
                    <table><thead><tr><th>Player</th><th>Out</th></tr></thead><tbody>
                        <c:forEach var="p" items="${dashboard.mostTransferredOut}">
                            <tr>
                                <td><a href="${pageContext.request.contextPath}/player?submit=player&playerId=${p.playerId}"><c:out value="${p.playerName}"/></a></td>
                                <td class="neg">${p.transfersOut}</td>
                            </tr>
                        </c:forEach>
                    </tbody></table>
                </c:otherwise>
            </c:choose>
        </section>

        <%-- Hidden gems --%>
        <section class="card">
            <h2>Hidden gems</h2>
            <p class="card-sub">High points per value, low ownership</p>
            <c:choose>
                <c:when test="${empty dashboard.hiddenGems}"><p class="empty-state">No data yet.</p></c:when>
                <c:otherwise>
                    <table><thead><tr><th>Player</th><th>Value</th><th>Pts</th><th>Pts/Val</th></tr></thead><tbody>
                        <c:forEach var="p" items="${dashboard.hiddenGems}">
                            <tr>
                                <td><a href="${pageContext.request.contextPath}/player?submit=player&playerId=${p.playerId}"><c:out value="${p.playerName}"/></a></td>
                                <td>${p.value}</td>
                                <td>${p.recentPoints}</td>
                                <td class="pos">${p.pointsPerValue}</td>
                            </tr>
                        </c:forEach>
                    </tbody></table>
                </c:otherwise>
            </c:choose>
        </section>

        <%-- Overpriced --%>
        <section class="card">
            <h2>Overpriced</h2>
            <p class="card-sub">High value, low points per value</p>
            <c:choose>
                <c:when test="${empty dashboard.overpriced}"><p class="empty-state">No data yet.</p></c:when>
                <c:otherwise>
                    <table><thead><tr><th>Player</th><th>Value</th><th>Pts</th><th>Pts/Val</th></tr></thead><tbody>
                        <c:forEach var="p" items="${dashboard.overpriced}">
                            <tr>
                                <td><a href="${pageContext.request.contextPath}/player?submit=player&playerId=${p.playerId}"><c:out value="${p.playerName}"/></a></td>
                                <td>${p.value}</td>
                                <td>${p.recentPoints}</td>
                                <td class="neg">${p.pointsPerValue}</td>
                            </tr>
                        </c:forEach>
                    </tbody></table>
                </c:otherwise>
            </c:choose>
        </section>

        <%-- Popular captains --%>
        <section class="card">
            <h2>Popular captains</h2>
            <p class="card-sub">Most chosen as captain</p>
            <c:choose>
                <c:when test="${empty dashboard.popularCaptains}"><p class="empty-state">No data yet.</p></c:when>
                <c:otherwise>
                    <table><thead><tr><th>Player</th><th>Captains</th></tr></thead><tbody>
                        <c:forEach var="p" items="${dashboard.popularCaptains}">
                            <tr>
                                <td><a href="${pageContext.request.contextPath}/player?submit=player&playerId=${p.playerId}"><c:out value="${p.playerName}"/></a></td>
                                <td class="pos">${p.captainCount}</td>
                            </tr>
                        </c:forEach>
                    </tbody></table>
                </c:otherwise>
            </c:choose>
        </section>
    </div>
</main>
</body>
</html>
