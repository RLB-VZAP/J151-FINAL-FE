/* ============================================================================
   Sidebar — collapsible Administration section.

   This script is included with a plain (non-deferred) <script src> tag placed
   immediately after the section's markup in sidebar.jspf, not at the end of
   the document and not with `defer`. That placement is what prevents a flash
   of the wrong open/closed state: the browser does not paint anything until
   HTML parsing yields, and a blocking, in-flow <script> like this one runs
   *during* parsing, before that first paint happens. So the stored preference
   is applied to the DOM before the section is ever visible, rather than after
   it briefly flashes open (or closed) the wrong way.

   Default state (nothing stored yet) is expanded, which matches the plain
   markup with no extra classes, so first-time visitors see no visual change
   from this script at all.
   ========================================================================== */
(function () {
    "use strict";

    var STORAGE_KEY = "sidebarAdminCollapsed";

    var group = document.getElementById("sidebarAdminGroup");
    var toggle = document.getElementById("sidebarAdminToggle");
    var panel = document.getElementById("sidebarAdminLinks");
    if (!group || !toggle || !panel) {
        return;
    }

    function readStoredPreference() {
        try {
            return window.localStorage.getItem(STORAGE_KEY);
        } catch (e) {
            // Storage unavailable (private browsing, disabled cookies, etc.):
            // fall back to the default expanded state.
            return null;
        }
    }

    function writeStoredPreference(collapsed) {
        try {
            window.localStorage.setItem(STORAGE_KEY, String(collapsed));
        } catch (e) {
            // Nothing to persist to; the toggle still works for this load.
        }
    }

    function applyState(collapsed) {
        group.classList.toggle("is-collapsed", collapsed);
        toggle.setAttribute("aria-expanded", String(!collapsed));
        panel.toggleAttribute("inert", collapsed);
    }

    // The section the user is currently on must never be hidden from them,
    // regardless of what was stored on a previous visit.
    var mustStayOpen = group.getAttribute("data-force-open") === "true";
    var storedCollapsed = readStoredPreference() === "true";
    applyState(!mustStayOpen && storedCollapsed);

    toggle.addEventListener("click", function () {
        var collapsedAfterClick = !group.classList.contains("is-collapsed");
        applyState(collapsedAfterClick);
        writeStoredPreference(collapsedAfterClick);
    });
})();
