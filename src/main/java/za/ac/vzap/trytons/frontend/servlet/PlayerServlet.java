package za.ac.vzap.trytons.frontend.servlet;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.ClubRestClient;
import za.ac.vzap.trytons.frontend.client.PlayerResponse;
import za.ac.vzap.trytons.frontend.client.PlayerRestClient;
import za.ac.vzap.trytons.frontend.client.PositionRestClient;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@WebServlet(name = "PlayerServlet", urlPatterns = {"/players", "/player"})
public class PlayerServlet extends HttpServlet {

    @Inject
    private PlayerRestClient playerRestClient;

    @Inject
    private ClubRestClient clubRestClient;

    @Inject
    private PositionRestClient positionRestClient;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("submit");
        if (action == null || action.isBlank()) {
            action = "/player".equals(request.getServletPath()) ? "player" : "players";
        }

        switch (action) {
            case "player" -> showPlayer(request, response);
            default -> showPlayers(request, response);
        }
    }

    private void showPlayers(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String search = request.getParameter("search");
        UUID clubId = parseUuid(request.getParameter("clubId")).orElse(null);
        UUID positionId = parseUuid(request.getParameter("positionId")).orElse(null);

        Optional<List<PlayerResponse>> players = playerRestClient.listPlayers(search, clubId, positionId);
        request.setAttribute("players", players.orElse(List.of()));
        if (players.isEmpty()) {
            request.setAttribute("error", "Unable to load players. Confirm that the backend and database are running.");
        }

        request.setAttribute("clubs", clubRestClient.listClubs().orElse(List.of()));
        request.setAttribute("positions", positionRestClient.listPositions().orElse(List.of()));
        request.setAttribute("searchTerm", search);
        request.setAttribute("selectedClubId", clubId);
        request.setAttribute("selectedPositionId", positionId);

        request.getRequestDispatcher("/pages/players.jsp").forward(request, response);
    }

    private void showPlayer(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Optional<UUID> playerId = parseUuid(request.getParameter("playerId"));
        if (playerId.isEmpty()) {
            request.setAttribute("error", "Invalid or missing player ID.");
            showPlayers(request, response);
            return;
        }

        Optional<PlayerResponse> player = playerRestClient.getPlayer(playerId.get());
        if (player.isEmpty()) {
            request.setAttribute("error", "Player was not found.");
            showPlayers(request, response);
            return;
        }

        request.setAttribute("player", player.get());
        request.getRequestDispatcher("/pages/player.jsp").forward(request, response);
    }

    private Optional<UUID> parseUuid(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(value.trim()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

}
