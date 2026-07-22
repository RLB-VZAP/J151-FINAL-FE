package za.ac.vzap.trytons.frontend.servlet.transfer;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import za.ac.vzap.trytons.frontend.client.fixture.DeadlineStatusResponse;
import za.ac.vzap.trytons.frontend.client.fixture.LockStatusResponse;
import za.ac.vzap.trytons.frontend.client.catalog.ClubRestClient;
import za.ac.vzap.trytons.frontend.client.catalog.PlayerResponse;
import za.ac.vzap.trytons.frontend.client.catalog.PlayerRestClient;
import za.ac.vzap.trytons.frontend.client.catalog.PositionRestClient;
import za.ac.vzap.trytons.frontend.client.fantasyteam.FantasyTeamPlayerSelectionResponse;
import za.ac.vzap.trytons.frontend.client.fantasyteam.FantasyTeamRestClient;
import za.ac.vzap.trytons.frontend.client.fantasyteam.ViewOwnTeamResponse;
import za.ac.vzap.trytons.frontend.client.transfer.TransferRecommendationResponse;
import za.ac.vzap.trytons.frontend.client.transfer.TransferRequest;
import za.ac.vzap.trytons.frontend.client.transfer.TransferRequestValidator;
import za.ac.vzap.trytons.frontend.client.transfer.TransferResponse;
import za.ac.vzap.trytons.frontend.client.transfer.TransferRestClient;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import za.ac.vzap.trytons.frontend.servlet.shared.AbstractServlet;


@WebServlet(name = "TransferServlet", urlPatterns = {"/transfers", "/transfers/history"})
public class TransferServlet extends AbstractServlet {

    @Inject
    private TransferRestClient transferRestClient;

    @Inject
    private PlayerRestClient playerRestClient;

    @Inject
    private ClubRestClient clubRestClient;

    @Inject
    private PositionRestClient positionRestClient;

    @Inject
    private FantasyTeamRestClient fantasyTeamRestClient;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if(!requireAuthenticated(request, response)) return;
        String destination = switch (request.getServletPath()) {
            case "/transfers/history" -> {
                String teamId = getTeamId(request);

                if (teamId == null || teamId.isBlank()) {
                    request.setAttribute("error", "Team id is required to view transfer history");
                    request.setAttribute("history", List.of());
                } else {
                    Optional<List<TransferResponse>> history = transferRestClient.getTransferHistory(teamId);

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
        if(!requireAuthenticated(request, response)) return;
        String submit = request.getParameter("submit");
        if (submit == null) {
            submit = "";
        }

        switch (submit) {
            case "", "transfer", "executeTransfer" -> {
                TransferRequest transferRequest = buildTransferRequest(request);

                if (!TransferRequestValidator.isValid(transferRequest)) {
                    request.setAttribute("error", "Please select a valid player to remove and a different player to add");
                    loadTransferPage(request);
                    request.getRequestDispatcher("/pages/transfers.jsp").forward(request, response);
                    return;
                }

                Optional<TransferResponse> transferResponse = transferRestClient.executeTransfer(transferRequest);

                if (transferResponse.isPresent()) {
                    String teamId = getTeamId(request);
                    String roundId = getRoundId(request);
                    String redirectUrl = request.getContextPath() + "/transfers?transferred=1"
                            + (teamId != null ? "&teamId=" + teamId : "")
                            + (roundId != null ? "&roundId=" + roundId : "");
                    response.sendRedirect(redirectUrl);
                    return;
                }

                if (handleApiFailure(request, response, "Transfer could not be completed")) return;
                request.setAttribute("transferError", "The transfer may be blocked by lock status, affordability, or squad rules");
                loadTransferPage(request);
                request.getRequestDispatcher("/pages/transfers.jsp").forward(request, response);
            }

            default -> request.getRequestDispatcher("/index.jsp").forward(request, response);
        }
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

        request.setAttribute("clubNamesById", buildClubNameLookup());
        request.setAttribute("positionNamesById", buildPositionNameLookup());

        if (roundId != null && !roundId.isBlank()) {
            Optional<LockStatusResponse> lockStatus = transferRestClient.getLockStatus(roundId);

            if (lockStatus.isPresent()) {
                request.setAttribute("lockStatus", lockStatus.get());
            } else {
                request.setAttribute("lockError", "Unable to load round lock status");
            }

            Optional<DeadlineStatusResponse> deadlineStatus = transferRestClient.getDeadlineStatus(roundId);
            deadlineStatus.ifPresent(status -> request.setAttribute("deadlineStatus", status));
        }

        String teamId = getTeamId(request);

        Optional<TransferRecommendationResponse> recommendations = transferRestClient.getTransferRecommendation(teamId, null);

        if (recommendations.isPresent() && recommendations.get().getRecommendations() != null) {
            request.setAttribute("transferRecommendations", recommendations.get().getRecommendations());
        } else {
            request.setAttribute("transferRecommendations", List.of());
        }

        request.setAttribute("teamId", teamId);
        request.setAttribute("roundId", roundId);

        loadSquad(request, teamId);
    }

    // Loads the user's real locked-in squad for the transfer "remove" side. There is no unconditional
    // "my team" endpoint - viewOwnTeam needs the team id, which we already resolve from the request/
    // session via getTeamId(). If there's no team id yet, the JSP shows an empty state pointing the
    // user at team creation instead of a permanently-empty squad table.
    private void loadSquad(HttpServletRequest request, String teamId) {
        Optional<UUID> parsedTeamId = parseUuid(teamId);
        if (parsedTeamId.isEmpty()) {
            request.setAttribute("squad", List.of());
            return;
        }

        Optional<ViewOwnTeamResponse> team = fantasyTeamRestClient.viewOwnTeam(parsedTeamId.get());
        if (team.isPresent()) {
            ViewOwnTeamResponse ownTeam = team.get();
            List<FantasyTeamPlayerSelectionResponse> squad = ownTeam.getPlayers();
            request.setAttribute("squad", squad != null ? squad : List.of());
            request.setAttribute("remainingBudget", ownTeam.getRemainingBudget());
        } else {
            request.setAttribute("squad", List.of());
            if (!apiCallStatus.isSuccess()) {
                request.setAttribute("squadError", "Unable to load your current squad");
            }
        }
    }

    private Map<UUID, String> buildClubNameLookup() {
        Map<UUID, String> lookup = new HashMap<>();
        clubRestClient.listClubs().ifPresent(clubs -> clubs.forEach(club -> lookup.put(club.getClubId(), club.getClubName())));
        return lookup;
    }

    private Map<UUID, String> buildPositionNameLookup() {
        Map<UUID, String> lookup = new HashMap<>();
        positionRestClient.getAllPositions().ifPresent(positions -> positions.forEach(position -> lookup.put(position.getPositionId(), position.getPositionName())));
        return lookup;
    }

    private TransferRequest buildTransferRequest(HttpServletRequest request) {
        TransferRequest transferRequest = new TransferRequest();

        parseUuid(getTeamId(request)).ifPresent(transferRequest::setTeamId);
        parseUuid(getRoundId(request)).ifPresent(transferRequest::setRoundId);
        parseUuid(request.getParameter("removedPlayerId")).ifPresent(transferRequest::setRemovedPlayerId);
        parseUuid(request.getParameter("addedPlayerId")).ifPresent(transferRequest::setAddedPlayerId);
        transferRequest.setPenaltyConfirmed(parseCheckbox(request.getParameter("penaltyConfirmed")));

        return transferRequest;
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