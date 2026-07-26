package za.ac.vzap.trytons.frontend.servlet.admin;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.pricing.PricingRestClient;
import za.ac.vzap.trytons.frontend.client.pricing.PricingRunSummaryResponse;
import za.ac.vzap.trytons.frontend.client.pricing.PricingSettingsResponse;
import za.ac.vzap.trytons.frontend.servlet.shared.AbstractServlet;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

@WebServlet(name = "AdminPricingServlet", urlPatterns = {"/admin/pricing"})
public class AdminPricingServlet extends AbstractServlet {

    private static final String VIEW = "/pages/admin-pricing.jsp";

    @Inject
    private PricingRestClient pricingRestClient;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!requireAdmin(request, response)) {
            return;
        }
        loadSettings(request, response);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!requireAdmin(request, response)) {
            return;
        }

        String action = request.getParameter("action");
        if (action == null) {
            action = "";
        }

        switch (action) {
            case "preview" -> preview(request, response);
            case "run" -> run(request, response);
            case "saveSettings" -> saveSettings(request, response);
            default -> {
                request.setAttribute("error", "Unknown pricing action requested");
                loadSettings(request, response);
                request.getRequestDispatcher(VIEW).forward(request, response);
            }
        }
    }

    private void preview(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Optional<PricingRunSummaryResponse> summary = pricingRestClient.preview();
        if (summary.isEmpty() && sessionExpiredRedirect(request, response)) {
            return;
        }
        summary.ifPresent(s -> request.setAttribute("summary", s));
        if (summary.isEmpty()) {
            request.setAttribute("error", apiCallStatus.getMessage("Unable to preview price changes"));
        } else {
            request.setAttribute("info", "Preview only — no prices have been changed yet.");
        }
        loadSettings(request, response);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private void run(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Optional<PricingRunSummaryResponse> summary = pricingRestClient.run();
        if (summary.isEmpty() && sessionExpiredRedirect(request, response)) {
            return;
        }
        summary.ifPresent(s -> request.setAttribute("summary", s));
        if (summary.isEmpty()) {
            request.setAttribute("error", apiCallStatus.getMessage("Unable to run the pricing update"));
        } else {
            request.setAttribute("success", summary.get().getPlayersRepriced() + " player price(s) updated.");
        }
        loadSettings(request, response);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private void saveSettings(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PricingSettingsResponse settings = new PricingSettingsResponse();
        settings.setWeightForm(parseDecimal(request.getParameter("weightForm")));
        settings.setWeightPopularity(parseDecimal(request.getParameter("weightPopularity")));
        settings.setWeightPoints(parseDecimal(request.getParameter("weightPoints")));
        settings.setWeightInjury(parseDecimal(request.getParameter("weightInjury")));
        settings.setWeightDemand(parseDecimal(request.getParameter("weightDemand")));
        settings.setWeightAvailability(parseDecimal(request.getParameter("weightAvailability")));
        settings.setMaxDeltaPct(parseDecimal(request.getParameter("maxDeltaPct")));
        settings.setMinValue(parseDecimal(request.getParameter("minValue")));
        settings.setMaxValue(parseDecimal(request.getParameter("maxValue")));

        Optional<PricingSettingsResponse> updated = pricingRestClient.updateSettings(settings);
        if (updated.isEmpty() && sessionExpiredRedirect(request, response)) {
            return;
        }
        if (updated.isEmpty()) {
            request.setAttribute("error", apiCallStatus.getMessage("Unable to save pricing settings"));
            request.setAttribute("settings", settings);
            request.getRequestDispatcher(VIEW).forward(request, response);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/admin/pricing?saved=1");
    }

    private void loadSettings(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (request.getAttribute("settings") != null) {
            return;
        }
        Optional<PricingSettingsResponse> settings = pricingRestClient.getSettings();
        if (settings.isEmpty()) {
            if (sessionExpiredRedirect(request, response)) {
                return;
            }
            if (request.getAttribute("error") == null) {
                request.setAttribute("error", "Unable to load pricing settings right now");
            }
        } else {
            request.setAttribute("settings", settings.get());
        }
        if ("1".equals(request.getParameter("saved"))) {
            request.setAttribute("success", "Pricing settings saved.");
        }
    }

    private BigDecimal parseDecimal(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public String getServletInfo() {
        return "Admin Pricing Servlet: configure weights, preview, and apply dynamic player pricing";
    }
}
