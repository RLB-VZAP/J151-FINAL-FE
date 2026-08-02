package za.ac.vzap.trytons.frontend.servlet.shared;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import za.ac.vzap.trytons.frontend.client.auth.LoginResponse;
import za.ac.vzap.trytons.frontend.client.shared.ApiCallStatus;
import za.ac.vzap.trytons.frontend.filter.CsrfFilter;
import za.ac.vzap.trytons.frontend.util.FlashUtil;
import za.ac.vzap.trytons.frontend.util.SessionAuthContext;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AbstractServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(AbstractServlet.class.getName());
    @Inject
    protected SessionAuthContext authContext;

    @Inject
    protected ApiCallStatus apiCallStatus;

    //protected route guard
    protected boolean requireAuthenticated(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if(!authContext.isAuthenticated()) {
            LOG.warning("Unauthenticated access attempt to " + req.getRequestURI());
            clearAuthSession(req);
            resp.sendRedirect(req.getContextPath() + "/login");
            return false;
        }
        return true;
    }


    protected boolean requireAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if(!authContext.isAuthenticated()) {
            LOG.warning("Unauthenticated admin access attempt to " + req.getRequestURI());
            clearAuthSession(req);
            resp.sendRedirect(req.getContextPath() + "/login");
            return false;
        }
        if(!authContext.isAdmin()) {
            LOG.warning("Non-admin access attempt to " + req.getRequestURI());
            flashError(req, "You are not authorised to view that page.");
            redirectTo(resp, req, "/dashboard");
            return false;
        }
        return true;
    }

    protected Optional<UUID> parseUuid(String value) {
        if(value == null || value.isBlank()){
            return Optional.empty();
        }
        try{
            return Optional.of(UUID.fromString(value.trim()));
        }catch(IllegalArgumentException e){
            LOG.log(Level.WARNING, "Invalid UUID format: {0} " , value);
            return Optional.empty();
        }
    }
    protected void establishAuthenticatedSession(HttpServletRequest req, LoginResponse loginResponse){
        HttpSession session = req.getSession(true);
        req.changeSessionId();
        // A fresh session id after login must carry a fresh CSRF token too,
        // otherwise a token minted before authentication (and potentially
        // observable to an attacker on a shared/public machine) would still
        // validate afterwards.
        CsrfFilter.rotateToken(session);
        authContext.signIn(loginResponse);
        session.setAttribute(SessionAuthContext.SESSION_USER_ID, String.valueOf(loginResponse.getUserId()));
        session.setAttribute(SessionAuthContext.SESSION_USERNAME, loginResponse.getUsername());
        session.setAttribute(SessionAuthContext.SESSION_EMAIL, loginResponse.getEmail());
        session.setAttribute(SessionAuthContext.SESSION_ROLE, loginResponse.getRole());
        session.setAttribute(SessionAuthContext.SESSION_AUTHENTICATED, Boolean.TRUE);
    }

    // Keep the signed-in identity in sync after a profile edit. The sidebar and
    // top nav read the HttpSession "username"/"email" attributes (set at login),
    // so updating the profile without this would leave the old name on screen
    // until the next login. Nulls are ignored so a partial update never blanks a
    // field. Session id is left untouched — this is not a privilege change.
    protected void refreshSessionIdentity(HttpServletRequest req, String username, String email){
        HttpSession session = req.getSession(false);
        if(session == null){
            return;
        }
        if(username != null && !username.isBlank()){
            session.setAttribute(SessionAuthContext.SESSION_USERNAME, username);
            authContext.setUsername(username);
        }
        if(email != null && !email.isBlank()){
            session.setAttribute(SessionAuthContext.SESSION_EMAIL, email);
            authContext.setEmail(email);
        }
    }

    //used for logout
    protected void clearAuthSession(HttpServletRequest req){
        authContext.clear();
        HttpSession session = req.getSession(false);
        if(session != null){
            session.removeAttribute(SessionAuthContext.SESSION_USER_ID);
            session.removeAttribute(SessionAuthContext.SESSION_USERNAME);
            session.removeAttribute(SessionAuthContext.SESSION_EMAIL);
            session.removeAttribute(SessionAuthContext.SESSION_ROLE);
            session.removeAttribute(SessionAuthContext.SESSION_AUTHENTICATED);
            session.invalidate();
        }
    }


    protected void forwardWithError(HttpServletRequest req, HttpServletResponse resp, String errorMessage, String jspPath) throws ServletException,IOException {
        req.setAttribute("error", errorMessage);
        req.getRequestDispatcher(jspPath).forward(req, resp);
    }

    //Another helper recommended for success
    protected void forwardWithMessage(HttpServletRequest req, HttpServletResponse resp, String message, String jspPath) throws ServletException,IOException {
        req.setAttribute("message", message);
        req.getRequestDispatcher(jspPath).forward(req, resp);
    }

    // ---- Toast flash ---------------------------------------------------------
    // A one-shot message stashed in the session, rendered once as a toast by
    // /WEB-INF/jspf/toast.jspf on the next page load and then cleared. Pair these
    // with redirectTo() so a successful POST follows the POST-redirect-GET pattern:
    // the toast survives the redirect and a refresh cannot re-submit the form.
    protected static final String FLASH_MESSAGE = FlashUtil.FLASH_MESSAGE;
    protected static final String FLASH_TYPE = FlashUtil.FLASH_TYPE;

    protected void flashSuccess(HttpServletRequest req, String message) {
        FlashUtil.flashSuccess(req, message);
    }

    protected void flashError(HttpServletRequest req, String message) {
        FlashUtil.flashError(req, message);
    }

    protected void flashInfo(HttpServletRequest req, String message) {
        FlashUtil.flashInfo(req, message);
    }

    // Request-scoped toast, for handlers that forward to a view instead of
    // redirecting. Rendered by the same toast.jspf host as the session flash.
    // Prefer flash* + a redirect where the success path already redirects.
    protected static final String TOAST_MESSAGE = "toastMessage";
    protected static final String TOAST_TYPE = "toastType";

    protected void toastSuccess(HttpServletRequest req, String message) {
        setToast(req, "success", message);
    }

    protected void toastError(HttpServletRequest req, String message) {
        setToast(req, "error", message);
    }

    protected void toastInfo(HttpServletRequest req, String message) {
        setToast(req, "info", message);
    }

    private void setToast(HttpServletRequest req, String type, String message) {
        req.setAttribute(TOAST_MESSAGE, message);
        req.setAttribute(TOAST_TYPE, type);
    }

    // Context-relative redirect, e.g. redirectTo(resp, req, "/create-team").
    protected void redirectTo(HttpServletResponse resp, HttpServletRequest req, String contextRelativePath) throws IOException {
        resp.sendRedirect(req.getContextPath() + contextRelativePath);
    }

    protected boolean sessionExpiredRedirect(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if(apiCallStatus.isUnauthorized()) {
            clearAuthSession(req);
            resp.sendRedirect(req.getContextPath() + "/login");
            return true;
        }
        return false;
    }

    protected boolean handleApiFailure(HttpServletRequest req, HttpServletResponse resp, String fallbackMessage) throws IOException {
        if(apiCallStatus.isUnauthorized()) {
            clearAuthSession(req);
            resp.sendRedirect(req.getContextPath() + "/login?expired=1");
            return true;
        }
        req.setAttribute("error", apiCallStatus.getMessage(fallbackMessage));
        return false;
    }

    // ---- Empty-Optional triage -------------------------------------------
    // APIClient never throws: every call returns an Optional that is empty on
    // BOTH a genuine "nothing here" AND a 401/403/500/network failure, with the
    // real reason recorded on the request-scoped apiCallStatus. Left unchecked,
    // a denied or broken call renders identically to a legitimate empty state
    // (see LESSONS.md, "APIClient never throws, so a 403 renders as an empty
    // state"). REDIRECTED / DENIED / FAILED are the three ways an empty
    // Optional can mean "this call did not succeed"; a caller only reaches
    // this method once it already knows the Optional was empty, so there is no
    // fourth "call succeeded but was empty" case to confuse it with.
    protected enum ApiFailure { REDIRECTED, DENIED, FAILED }

    /**
     * Resolves an empty {@code Optional} from an API call into the right request
     * state, distinguishing an authorisation denial and a backend/network failure
     * from a genuinely empty result, in one call. Must be invoked immediately
     * after the API call whose Optional came back empty — {@code apiCallStatus}
     * is request-scoped and reflects only the most recently completed call.
     * <p>
     * Do not call this for a result you already know is a legitimate empty
     * collection (e.g. a 2xx with an empty list) — only when the {@code Optional}
     * itself is empty. On {@code REDIRECTED} the response has already been sent;
     * the caller must stop processing immediately, exactly as with
     * {@link #sessionExpiredRedirect}.
     *
     * <p>Outcomes and the request attributes they set (the JSP contract):
     * <ul>
     *   <li>{@code 401} — session cleared, redirect to {@code /login?expired=1}.
     *       No attributes are set. Returns {@link ApiFailure#REDIRECTED}.</li>
     *   <li>{@code 403} — sets {@code accessDenied} (Boolean.TRUE) and
     *       {@code error} (the backend message, or {@code fallbackMessage}) so the
     *       JSP can render a "you don't have access to this" panel instead of an
     *       empty state. Returns {@link ApiFailure#DENIED}.</li>
     *   <li>anything else (500, 404, network failure, ...) — sets
     *       {@code loadFailed} (Boolean.TRUE) and {@code error} (the backend
     *       message, or {@code fallbackMessage}). Returns {@link ApiFailure#FAILED}.</li>
     * </ul>
     *
     * @param fallbackMessage shown when the backend returned no message of its own
     */
    protected ApiFailure handleEmptyResult(HttpServletRequest req, HttpServletResponse resp, String fallbackMessage) throws IOException {
        return handleEmptyResult(req, resp, fallbackMessage, "error");
    }

    /**
     * Same as {@link #handleEmptyResult(HttpServletRequest, HttpServletResponse, String)},
     * for a page that renders more than one independently-loaded section (e.g. two API
     * calls on one JSP, each with its own message placeholder) and so cannot share a
     * single {@code error} attribute without one call's message clobbering the other's.
     * {@code accessDenied} / {@code loadFailed} remain page-level booleans — either
     * section failing marks the page as having a denial/failure — while
     * {@code errorAttribute} carries that section's own message.
     *
     * @param errorAttribute the request attribute to hold this section's message,
     *                        e.g. {@code "pointsHistoryError"}
     */
    protected ApiFailure handleEmptyResult(HttpServletRequest req, HttpServletResponse resp, String fallbackMessage, String errorAttribute) throws IOException {
        if (apiCallStatus.isUnauthorized()) {
            clearAuthSession(req);
            resp.sendRedirect(req.getContextPath() + "/login?expired=1");
            return ApiFailure.REDIRECTED;
        }
        if (apiCallStatus.isForbidden()) {
            req.setAttribute("accessDenied", Boolean.TRUE);
            req.setAttribute(errorAttribute, apiCallStatus.getMessage(fallbackMessage));
            return ApiFailure.DENIED;
        }
        req.setAttribute("loadFailed", Boolean.TRUE);
        req.setAttribute(errorAttribute, apiCallStatus.getMessage(fallbackMessage));
        return ApiFailure.FAILED;
    }

}
