package za.ac.vzap.trytons.frontend.servlet.market;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.market.MarketDashboardRestClient;
import za.ac.vzap.trytons.frontend.client.market.MarketDashboardResponse;
import za.ac.vzap.trytons.frontend.servlet.shared.AbstractServlet;

import java.io.IOException;
import java.util.Optional;

@WebServlet(name = "MarketDashboardServlet", urlPatterns = {"/market-dashboard"})
public class MarketDashboardServlet extends AbstractServlet {

    private static final String VIEW = "/pages/market-dashboard.jsp";

    @Inject
    private MarketDashboardRestClient marketDashboardRestClient;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!requireAuthenticated(request, response)) {
            return;
        }

        Optional<MarketDashboardResponse> dashboard = marketDashboardRestClient.getDashboard();
        if (dashboard.isEmpty()) {
            if (sessionExpiredRedirect(request, response)) {
                return;
            }
            request.setAttribute("error", apiCallStatus.getMessage("Unable to load the market dashboard right now"));
        } else {
            request.setAttribute("dashboard", dashboard.get());
        }

        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Market Dashboard Servlet: trending, transfers, hidden gems, overpriced, captains";
    }
}
