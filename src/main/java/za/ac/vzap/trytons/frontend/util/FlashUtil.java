package za.ac.vzap.trytons.frontend.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

// Shared home for the session-flash constants and the set-flash logic, so
// AbstractServlet (for POST-redirect-GET handlers) and CsrfFilter (which is
// not an AbstractServlet subclass) both flash through the same one
// definition instead of duplicating the attribute-key literals.
public final class FlashUtil {

    public static final String FLASH_MESSAGE = "flash.message";
    public static final String FLASH_TYPE = "flash.type";

    private static final String FLASH_KIND_SUCCESS = "success";
    private static final String FLASH_KIND_ERROR = "error";
    private static final String FLASH_KIND_INFO = "info";

    private FlashUtil(){
    }

    public static void flashSuccess(HttpServletRequest req, String message) {
        setFlash(req, FLASH_KIND_SUCCESS, message);
    }

    public static void flashError(HttpServletRequest req, String message) {
        setFlash(req, FLASH_KIND_ERROR, message);
    }

    public static void flashInfo(HttpServletRequest req, String message) {
        setFlash(req, FLASH_KIND_INFO, message);
    }

    public static void setFlash(HttpServletRequest req, String type, String message) {
        HttpSession session = req.getSession(true);
        session.setAttribute(FLASH_MESSAGE, message);
        session.setAttribute(FLASH_TYPE, type);
    }
}
