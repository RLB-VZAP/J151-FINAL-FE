<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Position Management - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/catalog.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-positions.css">
</head>
<body class="catalog-page apos-page">

<c:set var="activeNav" value="admin-positions" scope="request" />
<%@ include file="/WEB-INF/jspf/sidebar.jspf" %>

<main class="catalog-main" id="adminPositions">
    <div class="catalog-content">

        <header class="catalog-header">
            <div>
                <p class="catalog-eyebrow">Administration</p>
                <h1 class="brand-font">Position Management</h1>
            </div>
        </header>

        <c:if test="${not empty error}">
            <p class="apos-alert apos-alert-error" role="alert"><c:out value="${error}" /></p>
        </c:if>
        <c:if test="${not empty message}">
            <p class="apos-alert apos-alert-success" role="status"><c:out value="${message}" /></p>
        </c:if>

        <div class="apos-grid">

            <%-- ---------- Existing positions ---------- --%>
            <section id="positionListSection">
                <div class="apos-section-head">
                    <h2 class="apos-section-title">Existing positions</h2>
                    <span class="apos-rule"></span>
                    <span class="apos-count">${fn:length(positions)} position${fn:length(positions) == 1 ? '' : 's'}</span>
                </div>

                <c:choose>
                    <c:when test="${empty positions}">
                        <p class="apos-empty" id="positionsEmptyState">No positions have been created yet.</p>
                    </c:when>
                    <c:otherwise>
                        <div class="ptbl" id="positionsTable">
                            <div class="ptbl-head">
                                <span>Name</span>
                                <span>Category</span>
                                <span aria-hidden="true"></span>
                            </div>
                            <c:forEach var="pos" items="${positions}">
                                <div class="ptbl-row">
                                    <span class="ptbl-name"><c:out value="${pos.positionName}" /></span>
                                    <span><span class="ptbl-chip"><c:out value="${pos.positionCategory}" /></span></span>
                                    <span class="ptbl-action">
                                        <a class="ptbl-edit" href="${pageContext.request.contextPath}/admin/positions?submit=position&amp;positionId=${pos.positionId}">Edit</a>
                                    </span>
                                </div>
                            </c:forEach>
                        </div>
                    </c:otherwise>
                </c:choose>
            </section>

            <%-- ---------- Create / edit form ---------- --%>
            <section class="apos-panel" id="positionFormSection">
                <h2 class="apos-panel-title">
                    <c:choose>
                        <c:when test="${not empty position}">Edit position</c:when>
                        <c:otherwise>Create position</c:otherwise>
                    </c:choose>
                </h2>

                <form method="post" action="${pageContext.request.contextPath}/admin/positions" id="positionForm">
                    <c:choose>
                        <c:when test="${not empty position}">
                            <input type="hidden" name="submit" value="position/update" />
                            <input type="hidden" name="positionId" value="${position.positionId}" />
                        </c:when>
                        <c:otherwise>
                            <input type="hidden" name="submit" value="position/create" />
                        </c:otherwise>
                    </c:choose>

                    <div class="apos-field">
                        <label class="apos-label" for="positionName">Name</label>
                        <input class="apos-input" type="text" id="positionName" name="positionName"
                               value="${not empty position ? fn:escapeXml(position.positionName) : ''}"
                               placeholder="e.g. Loose Forward" required>
                    </div>

                    <div class="apos-field">
                        <label class="apos-label" for="positionCategory">Category</label>
                        <input class="apos-input" type="text" id="positionCategory" name="positionCategory"
                               value="${not empty position ? fn:escapeXml(position.positionCategory) : ''}"
                               placeholder="e.g. FORWARD" required>
                    </div>

                    <%-- Squad limits (min required / max allowed) are deliberately absent:
                         they are enforced by the backend's hardcoded squad validation and
                         are not editable here. --%>
                    <p class="apos-note">
                        Squad limits for each position are fixed in the system and cannot be changed here.
                    </p>

                    <button type="submit" class="btn-gold apos-submit">
                        <c:choose>
                            <c:when test="${not empty position}">Update position</c:when>
                            <c:otherwise>Create position</c:otherwise>
                        </c:choose>
                    </button>

                    <c:if test="${not empty position}">
                        <a class="apos-cancel" href="${pageContext.request.contextPath}/admin/positions?submit=positions">Cancel edit</a>
                    </c:if>
                </form>
            </section>

        </div>

    </div>
</main>

</body>
</html>
