package za.ac.vzap.trytons.frontend.servlet.scoring;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.scoring.ScoringRuleRestClient;
import za.ac.vzap.trytons.frontend.servlet.shared.AbstractServlet;

import java.io.IOException;

@WebServlet(name = "AdminScoringRuleServlet", urlPatterns = {"/admin/scoring-rules"})
public class AdminScoringRuleServlet extends AbstractServlet {

    private static final String VIEW = "/pages/admin-scoring-rules.jsp";

    @Inject
    private ScoringRuleRestClient scoringRuleRestClient;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO: Require an admin user, load the scoring rules for the current season via ScoringRuleRestClient, and forward to the admin scoring rules view.
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO: Require an admin user, read the submitted scoring rule form data, call ScoringRuleRestClient to save it, and forward back to the admin scoring rules view with a success or error message.
    }
}
