package za.ac.vzap.trytons.frontend.filter;

import jakarta.inject.Inject;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import za.ac.vzap.trytons.frontend.client.fantasyteam.FantasyTeamRestClient;
import za.ac.vzap.trytons.frontend.client.notification.NotificationRestClient;
import za.ac.vzap.trytons.frontend.util.SessionAuthContext;

import java.io.IOException;

/**
 * Makes the sidebar's unread-notification badge, and whether the signed-in
 * user already owns a fantasy team, available on every page, not just the
 * pages that would otherwise compute them.
 *
 * The sidebar shows the badge from a "sidebarUnreadCount" request attribute, but
 * only NotificationServlet set it, so the badge appeared only while viewing
 * notifications. This filter sets the same attribute for every authenticated
 * page request, so the count is visible throughout the app. Static assets and
 * unauthenticated requests are skipped, so the count is fetched once per page
 * navigation rather than per resource. A servlet that computes its own count
 * (the notifications page) still overrides this value, since the filter runs
 * first.
 *
 * The nav also needs to know, on every page, whether to link "Create Team" or
 * "Edit Team". That would mean a getMyTeam() backend call on every single
 * request, so instead the result is cached as HttpSession attributes
 * "hasFantasyTeam" / "fantasyTeamId" and only looked up once — the first
 * request of the session that needs it. FantasyTeamServlet.handleCreateTeam
 * refreshes these two attributes directly the moment a team is created, so the
 * nav flips immediately without waiting for the cache to expire. Admins never
 * own a team, so they skip the lookup (and the cache) entirely.
 */
@WebFilter("/*")
public class SidebarBadgeFilter implements Filter {

    public static final String SESSION_HAS_TEAM = "hasFantasyTeam";
    public static final String SESSION_TEAM_ID = "fantasyTeamId";

    @Inject
    private SessionAuthContext authContext;

    @Inject
    private NotificationRestClient notificationRestClient;

    @Inject
    private FantasyTeamRestClient fantasyTeamRestClient;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest && shouldPopulate(httpRequest)) {
            try {
                notificationRestClient.getUnreadCount()
                        .ifPresent(count -> httpRequest.setAttribute("sidebarUnreadCount", count));
            } catch (RuntimeException e) {
                // The badge is non-essential; never let its lookup break the page.
            }
            populateTeamStatus(httpRequest);
        }
        chain.doFilter(request, response);
    }

    private void populateTeamStatus(HttpServletRequest request) {
        if (authContext.isAdmin()) {
            return;
        }
        HttpSession session = request.getSession(true);
        if (session.getAttribute(SESSION_HAS_TEAM) != null) {
            return; // already cached for this session
        }
        try {
            fantasyTeamRestClient.getMyTeam().ifPresentOrElse(
                    team -> {
                        session.setAttribute(SESSION_HAS_TEAM, Boolean.TRUE);
                        session.setAttribute(SESSION_TEAM_ID, team.getTeamId());
                    },
                    () -> session.setAttribute(SESSION_HAS_TEAM, Boolean.FALSE));
        } catch (RuntimeException e) {
            // Non-essential; the nav just falls back to "Create Team" for this request.
        }
    }

    private boolean shouldPopulate(HttpServletRequest request) {
        if (authContext == null || !authContext.isAuthenticated()) {
            return false;
        }
        // Only rendered pages need the badge; skip the static asset requests.
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return !path.startsWith("/assets/");
    }
}
