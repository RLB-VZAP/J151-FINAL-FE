package za.ac.vzap.trytons.frontend.servlet.history;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.history.UserHistoryRestClient;
import za.ac.vzap.trytons.frontend.client.history.UserPointsHistoryResponse;
import za.ac.vzap.trytons.frontend.client.history.WeeklyPerformanceResponse;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import za.ac.vzap.trytons.frontend.servlet.shared.AbstractServlet;

//Frontend MVC controller for the user history page: load the authenticated user's
// points history and weekly performance through UserHistoryRestClient, populate request attributes,
// and forward to the history page.

@WebServlet(name = "UserHistoryServlet", urlPatterns = {"/history"})
public class UserHistoryServlet extends AbstractServlet {

    @Inject
    private UserHistoryRestClient userHistoryRestClient;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if(!requireAuthenticated(request, response)) return;
        String destination = "/pages/history.jsp";

        Optional<UserPointsHistoryResponse> pointsHistory = userHistoryRestClient.getUserPointsHistory();
        if (pointsHistory.isPresent()) {
            request.setAttribute("pointsHistory", pointsHistory.get());
        } else {
            request.setAttribute("pointsHistoryError", "Unable to load user points history");
        }

        Optional<List<WeeklyPerformanceResponse>> weeklyPerformance = userHistoryRestClient.getWeeklyPerformance();
        if (weeklyPerformance.isPresent()){
            request.setAttribute("weeklyPerformance", weeklyPerformance.get());
        } else {
            request.setAttribute("weeklyPerformanceError", "Unable to load user weekly performance");
        }

        request.getRequestDispatcher(destination).forward(request, response);
    }
}