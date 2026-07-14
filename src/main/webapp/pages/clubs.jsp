<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Clubs - Fantasy TryTons</title>
</head>
<body>
<%@ include file="/WEB-INF/jspf/navigation.jspf" %>

<h1>Clubs</h1>

<c:if test="${not empty error}">
    <p class="error-message" role="alert">${error}</p>
</c:if>

<form action="${pageContext.request.contextPath}/clubs" method="get" id="clubSearchForm">
    <input type="hidden" name="submit" value="clubs" />
    <input type="text" name="search" placeholder="Search club name"
           value="${searchTerm}" id="clubSearchInput" />
    <button type="submit">Search</button>
</form>

<c:choose>
    <c:when test="${empty clubs}">
        <p id="clubsEmptyState">No clubs found matching your search.</p>
    </c:when>
    <c:otherwise>
        <table id="clubsTable">
            <thead>
            <tr>
                <th>Club Name</th>
                <th>Location</th>
                <th>Home Venue</th>
                <th>Status</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="club" items="${clubs}">
                <tr>
                    <td><a href="${pageContext.request.contextPath}/club?submit=club&amp;clubId=${club.clubId}">${club.clubName}</a></td>
                    <td>${club.location}</td>
                    <td>${club.homeVenue}</td>
                    <td>
                        <c:choose>
                            <c:when test="${club.active}">Active</c:when>
                            <c:otherwise>Inactive</c:otherwise>
                        </c:choose>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </c:otherwise>
</c:choose>

<script src="${pageContext.request.contextPath}/assets/js/search-filter.js"></script>
</body>
</html>