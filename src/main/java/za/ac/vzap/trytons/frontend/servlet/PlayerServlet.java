package za.ac.vzap.trytons.frontend.servlet;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.ClubResponse;
import za.ac.vzap.trytons.frontend.client.PlayerRequest;
import za.ac.vzap.trytons.frontend.client.PlayerResponse;
import za.ac.vzap.trytons.frontend.client.PlayerRestClient;
import za.ac.vzap.trytons.frontend.client.PositionResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@WebServlet(name ="PlayerServlet", urlPatterns = {"/players", "/player", "/player/create" , "/player/update"} )
public class PlayerServlet extends HttpServlet {
    @Inject
    private PlayerRestClient playerRestClient;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String submit = request.getParameter("submit");
        if (submit == null) {
            submit = "";
        }
        String destination = switch (submit){
            case "players" -> {
                String search = request.getParameter("search");
                UUID clubId = parseUuid(request.getParameter("clubId")).orElse(null);
                UUID positionId = parseUuid(request.getParameter("positionId")).orElse(null);
                Optional<List<PlayerResponse>> players = playerRestClient.listPlayers(search, clubId, positionId);
                if (players.isPresent()) {
                    request.setAttribute("players", players.get());
                } else {
                    request.setAttribute("error", "Unable to load players");
                    request.setAttribute("players", List.of());
                }
                yield "players.jsp";
            }
            case "player" -> {
                Optional<UUID> playerId = parseUuid(request.getParameter("playerId"));
                if (playerId.isEmpty()) {
                    request.setAttribute("error", "Invalid or missing player id");
                    yield "players.jsp";
                }
                Optional<PlayerResponse> player = playerRestClient.getPlayer(playerId.get());
                if (player.isPresent()) {
                    request.setAttribute("player", player.get());
                    yield "player.jsp";
                }
                request.setAttribute("error", "Player not found");
                yield "players.jsp";
            }
            default -> "index.jsp";
        };
        request.getRequestDispatcher(destination).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String submit = request.getParameter("submit");
        if (submit == null){
            submit = "";
        }
        String destination = switch (submit){
            case "player/create" -> {
                PlayerRequest playerRequest = buildPlayerRequest(request);
                Optional<PlayerResponse> created = playerRestClient.createPlayer(playerRequest);
                request.setAttribute("error", "Unable to create player");
                yield "player.jsp";
            }
            case "player/update" -> {
                Optional<UUID> playerId = parseUuid(request.getParameter("playerId"));
                if (playerId.isEmpty()) {
                    request.setAttribute("error", "Invalid or missing player id");
                    yield "player.jsp";
                }
                PlayerRequest playerRequest = buildPlayerRequest(request);
                Optional<PlayerResponse> updated = playerRestClient.updatePlayer(playerId.get(), playerRequest);

                request.setAttribute("error", "Unable to update player");
                yield "player.jsp";
            }
            default -> "index.jsp";
        };
        request.getRequestDispatcher(destination).forward(request, response);
    }

    private PlayerRequest buildPlayerRequest(HttpServletRequest request) {
        PlayerRequest playerRequest = new PlayerRequest();
        playerRequest.setPlayerName(request.getParameter("playerName"));
        playerRequest.setValue(parseDecimal(request.getParameter("value")));
        playerRequest.setAttackingAbility(parseInt(request.getParameter("attackingAbility")));
        playerRequest.setDefensiveAbility(parseInt(request.getParameter("defensiveAbility")));
        playerRequest.setKickingAbility(parseInt(request.getParameter("kickingAbility")));
        playerRequest.setDiscipline(parseInt(request.getParameter("discipline")));
        playerRequest.setConsistency(parseInt(request.getParameter("consistency")));
        playerRequest.setFitness(parseInt(request.getParameter("fitness")));
        playerRequest.setCurrentForm(parseInt(request.getParameter("currentForm")));
        playerRequest.setTotalFantasyPoints(parseInt(request.getParameter("totalFantasyPoints")));
        playerRequest.setActive(parseCheckbox(request.getParameter("isActive")));

        parseUuid(request.getParameter("clubId")).ifPresent(clubId -> {
            ClubResponse club = new ClubResponse();
            club.setClubId(clubId);
            playerRequest.setClub(club);
        });
        parseUuid(request.getParameter("positionId")).ifPresent(positionId -> {
            PositionResponse position = new PositionResponse();
            position.setPositionId(positionId);
            playerRequest.setPosition(position);
        });
        return playerRequest;
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

    private int parseInt(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private BigDecimal parseDecimal(String value) {
        if (value == null || value.isBlank()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private boolean parseCheckbox(String value) {
        return "on".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value);
    }
}
