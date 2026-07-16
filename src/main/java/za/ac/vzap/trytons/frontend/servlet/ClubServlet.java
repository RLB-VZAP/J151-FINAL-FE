package za.ac.vzap.trytons.frontend.servlet;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.ClubResponse;
import za.ac.vzap.trytons.frontend.client.ClubRestClient;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@WebServlet(name = "ClubServlet", urlPatterns = {"/clubs", "/club"})
public class ClubServlet extends HttpServlet {

    @Inject
    private ClubRestClient clubRestClient;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("submit");
        if (action == null || action.isBlank()) {
            action = "/club".equals(request.getServletPath()) ? "club" : "clubs";
        }

        if ("club".equals(action)) {
            showClub(request, response);
        } else {
            showClubs(request, response);
        }
    }

    private void showClubs(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String search = request.getParameter("search");
        Optional<List<ClubResponse>> loadedClubs = clubRestClient.listClubs();
        List<ClubResponse> clubs = loadedClubs.orElse(List.of());

        if (search != null && !search.isBlank()) {
            String term = search.trim().toLowerCase(Locale.ROOT);
            clubs = clubs.stream()
                    .filter(club -> containsIgnoreCase(club.getClubName(), term)
                            || containsIgnoreCase(club.getLocation(), term)
                            || containsIgnoreCase(club.getHomeVenue(), term))
                    .toList();
        }

        if (loadedClubs.isEmpty()) {
            request.setAttribute("error", "Unable to load clubs. Confirm that the backend and database are running.");
        }

        request.setAttribute("clubs", clubs);
        request.setAttribute("searchTerm", search);
        request.getRequestDispatcher("/pages/clubs.jsp").forward(request, response);
    }

    private void showClub(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Optional<UUID> clubId = parseUuid(request.getParameter("clubId"));
        if (clubId.isEmpty()) {
            request.setAttribute("error", "Invalid or missing club ID.");
            showClubs(request, response);
            return;
        }

        Optional<ClubResponse> club = clubRestClient.getClubById(clubId.get());
        if (club.isEmpty()) {
            request.setAttribute("error", "Club was not found.");
            showClubs(request, response);
            return;
        }

        request.setAttribute("club", club.get());
        request.getRequestDispatcher("/pages/club.jsp").forward(request, response);
    }

    private boolean containsIgnoreCase(String value, String lowerCaseTerm) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(lowerCaseTerm);
    }

    // TODO [W4-FE-FIXES-09]: duplicated parseUuid — extract shared util/ helper (see W4-CR-FE-12)
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
