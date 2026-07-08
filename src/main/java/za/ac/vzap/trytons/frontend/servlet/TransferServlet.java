package za.ac.vzap.trytons.frontend.servlet;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.TransferRecommendationResponse;
import za.ac.vzap.trytons.frontend.client.TransferRestClient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TransferServlet extends HttpServlet {

    @Inject
    private TransferRestClient transferRestClient;

    void populateTransferRecommendations(HttpServletRequest request, UUID teamId) {
        Optional<TransferRecommendationResponse> recommendations = transferRestClient.getRecommendation(teamId);

    }
}