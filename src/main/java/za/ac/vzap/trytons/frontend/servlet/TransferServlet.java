package za.ac.vzap.trytons.frontend.servlet;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import za.ac.vzap.trytons.frontend.client.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@WebServlet(name = "TransferServlet", urlPatterns = {"/transfers", "/transfers/history"})
public class TransferServlet extends HttpServlet {

    @Inject
    private TransferRestClient transferRestClient;

    @Inject
    private PlayerRestClient playerRestClient;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String destination = switch (request.getServletPath()) {
            case "/transfers/history" -> {
                String teamId = getTeamId(request);

                if (teamId == null || teamId.isBlank()) {
                    request.setAttribute("error", "Team id is required to view transfer history");
                    request.setAttribute("history", List.of());
                } else {
                    Optional<List<TransferHistoryResponse>> history = transferRestClient.getTransferHistory(teamId);

                    if (history.isPresent()) {
                        request.setAttribute("history", history.get());
                    } else {
                        request.setAttribute("error", "Unable to load transfer history");
                        request.setAttribute("history", List.of());
                    }
                }

                yield "/pages/transfer-history.jsp";
            }

            case "/transfers" -> {
                loadTransferPage(request);
                yield "/pages/transfers.jsp";
            }

            default -> "/index.jsp";
        };

        request.getRequestDispatcher(destination).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String submit = request.getParameter("submit");
        if (submit == null) {
            submit = "";
        }

        String destination = switch (submit) {
            case "", "transfer", "executeTransfer" -> {
                TransferRequest transferRequest = buildTransferRequest(request);

                if (!isValidTransferRequest(transferRequest)) {
                    request.setAttribute("error", "Please select a valid player to remove and a different player to add");
                    loadTransferPage(request);
                    yield "/pages/transfers.jsp";
                }

                Optional<TransferResponse> transferResponse = transferRestClient.executeTransfer(transferRequest);

                if (transferResponse.isPresent()) {
                    request.setAttribute("success", "Transfer completed successfully");
                    request.setAttribute("transfer", transferResponse.get());
                } else {
                    request.setAttribute("error", "Transfer could not be completed");
                    request.setAttribute("transferError", "The transfer may be blocked by lock status, affordability, or squad rules");
                }

                loadTransferPage(request);
                yield "/pages/transfers.jsp";
            }

            default -> "/index.jsp";
        };

        request.getRequestDispatcher(destination).forward(request, response);
    }

    private void loadTransferPage(HttpServletRequest request) {
        String roundId = getRoundId(request);

        Optional<List<PlayerResponse>> players = playerRestClient.listPlayers(null, null, null);

        if (players.isPresent()) {
            request.setAttribute("players", players.get());
            request.setAttribute("candidatePlayers", players.get());
        } else {
            request.setAttribute("error", "Unable to load available players");
            request.setAttribute("players", List.of());
            request.setAttribute("candidatePlayers", List.of());
        }

        if (roundId != null && !roundId.isBlank()) {
            Optional<LockStatusResponse> lockStatus = transferRestClient.getLockStatus(roundId);

            if (lockStatus.isPresent()) {
                request.setAttribute("lockStatus", lockStatus.get());
            } else {
                request.setAttribute("lockError", "Unable to load round lock status");
            }
        }

        String teamId = getTeamId(request);

        Optional<TransferRecommendationResponse> recommendations = transferRestClient.getTransferRecommendation(teamId, roundId);

        if (recommendations.isPresent() && recommendations.get().getRecommendation() != null) {
            request.setAttribute("transferRecommendations", recommendations.get().getRecommendation());
        } else {
            request.setAttribute("transferRecommendations", List.of());
        }

        request.setAttribute("teamId", teamId);
        request.setAttribute("roundId", roundId);

        request.setAttribute("squad", List.of());
        request.setAttribute("currentSquad", List.of());
    }

    private TransferRequest buildTransferRequest(HttpServletRequest request) {
        TransferRequest transferRequest = new TransferRequest();

        transferRequest.setTeamId(getTeamId(request));
        transferRequest.setRoundId(getRoundId(request));
        transferRequest.setRemovedPlayerId(request.getParameter("removedPlayerId"));
        transferRequest.setAddedPlayerId(request.getParameter("addedPlayerId"));
        transferRequest.setPenaltyConfirmed(parseCheckbox(request.getParameter("penaltyConfirmed")));

        return transferRequest;
    }

    private boolean isValidTransferRequest(TransferRequest transferRequest) {
        return transferRequest.getTeamId() != null
                && transferRequest.getRoundId() != null
                && transferRequest.getRemovedPlayerId() != null
                && transferRequest.getAddedPlayerId() != null
                && !transferRequest.getTeamId().isBlank()
                && !transferRequest.getRoundId().isBlank()
                && !transferRequest.getRemovedPlayerId().isBlank()
                && !transferRequest.getAddedPlayerId().isBlank()
                && !transferRequest.getRemovedPlayerId().equals(transferRequest.getAddedPlayerId());
    }

    private String getTeamId(HttpServletRequest request) {
        String teamId = request.getParameter("teamId");

        if (teamId != null && !teamId.isBlank()) {
            return teamId;
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        Object sessionTeamId = session.getAttribute("teamId");
        return sessionTeamId == null ? null : sessionTeamId.toString();
    }

    private String getRoundId(HttpServletRequest request) {
        String roundId = request.getParameter("roundId");

        if (roundId != null && !roundId.isBlank()) {
            return roundId;
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        Object sessionRoundId = session.getAttribute("roundId");
        return sessionRoundId == null ? null : sessionRoundId.toString();
    }

    private boolean parseCheckbox(String value) {
        return "on".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value);
    }

    @Override
    public String getServletInfo() {
        return "Transfer Servlet, handles transfer page requests, transfer submissions, and transfer history";
    }
}