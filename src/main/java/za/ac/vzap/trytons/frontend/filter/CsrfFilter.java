package za.ac.vzap.trytons.frontend.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import za.ac.vzap.trytons.frontend.util.FlashUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Synchronizer-token CSRF protection for every request. A token is minted
 * lazily into the session (so pre-auth login/register forms get one) and
 * every state-changing request must echo it back via the "csrfToken" form
 * field. AbstractServlet.establishAuthenticatedSession rotates the token
 * right after login's session-id change; logout invalidates the session
 * outright, which drops the token attribute along with everything else.
 */
@WebFilter(filterName = "CsrfFilter", urlPatterns = "/*")
public class CsrfFilter implements Filter {

    private static final Logger LOG = Logger.getLogger(CsrfFilter.class.getName());

    public static final String CSRF_TOKEN_ATTRIBUTE = "csrf.token";
    public static final String CSRF_PARAMETER_NAME = "csrfToken";

    private static final int TOKEN_BYTE_LENGTH = 32;
    private static final Set<String> PROTECTED_METHODS = Set.of("POST");

    private static final String CSRF_REJECTION_MESSAGE =
            "Your session expired or the form was out of date. Please try again.";
    private static final String DEFAULT_REJECTION_REDIRECT = "/dashboard";

    private static final String ASSET_PATH_PREFIX = "/assets/";

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        if (isAssetRequest(request)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        issueTokenIfAbsent(request);

        if (!requiresValidation(request)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        if (!validateToken(request)) {
            rejectRequest(request, response);
            return;
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    // Static assets never carry a token and must not mint a session for every
    // anonymous visitor fetching CSS, JS or images.
    private boolean isAssetRequest(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return path.startsWith(ASSET_PATH_PREFIX);
    }

    private boolean requiresValidation(HttpServletRequest request) {
        return PROTECTED_METHODS.contains(request.getMethod().toUpperCase());
    }

    // Generates the token on first contact (GET of a login/register page
    // included) so every rendered form, authenticated or not, has one to
    // submit. Never overwrites an existing token here — rotation on login
    // is handled explicitly by rotateToken().
    private void issueTokenIfAbsent(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        if (session.getAttribute(CSRF_TOKEN_ATTRIBUTE) != null) {
            return;
        }
        session.setAttribute(CSRF_TOKEN_ATTRIBUTE, generateToken());
    }

    private static String generateToken() {
        byte[] tokenBytes = new byte[TOKEN_BYTE_LENGTH];
        SECURE_RANDOM.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    private boolean validateToken(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        String sessionToken = (String) session.getAttribute(CSRF_TOKEN_ATTRIBUTE);
        String submittedToken = request.getParameter(CSRF_PARAMETER_NAME);
        if (sessionToken == null || submittedToken == null) {
            return false;
        }
        return MessageDigest.isEqual(
                sessionToken.getBytes(StandardCharsets.UTF_8),
                submittedToken.getBytes(StandardCharsets.UTF_8));
    }

    // No sendError(403) here: glassfish-web.xml has no error page configured
    // for it, so the container would show a bare, unstyled 403. Reusing the
    // app's flash + redirect (POST-redirect-GET) convention keeps the user on
    // a normal, styled page instead.
    private void rejectRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        LOG.log(Level.WARNING, "Rejected request with missing/invalid CSRF token: {0}", request.getRequestURI());
        FlashUtil.flashError(request, CSRF_REJECTION_MESSAGE);
        response.sendRedirect(request.getContextPath() + DEFAULT_REJECTION_REDIRECT);
    }

    // Called by AbstractServlet.establishAuthenticatedSession immediately
    // after req.changeSessionId(), so a token minted before login cannot
    // still be valid for the authenticated session that follows it.
    public static void rotateToken(HttpSession session) {
        session.setAttribute(CSRF_TOKEN_ATTRIBUTE, generateToken());
    }
}
