package za.ac.vzap.trytons.frontend.servlet;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.ClubRequest;
import za.ac.vzap.trytons.frontend.client.ClubResponse;
import za.ac.vzap.trytons.frontend.client.ClubRestClient;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@WebServlet(name ="ClubServlet" , urlPatterns = {"/clubs","/club","/club/create","/club/update"})
public class ClubServlet extends HttpServlet {
    @Inject
    private ClubRestClient clubRestClient;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String submit = request.getParameter("submit");
        if (submit == null) {
            submit = "";
        }
        String destination = switch (submit){
            case "clubs" -> {
                String search = request.getParameter("search");
                Optional<List<ClubResponse>> clubs = clubRestClient.listClubs();
                if (clubs.isPresent()) {
                    request.setAttribute("clubs", clubs.get());
                } else {
                    request.setAttribute("error", "Unable to load clubs");
                    request.setAttribute("clubs", List.of());
                }
                request.setAttribute("searchTerm", search);
                yield "/pages/clubs.jsp";
            }
            case "club"  -> {
                Optional<UUID> clubId = parseUuid(request.getParameter("clubId"));
                if (clubId.isEmpty()) {
                    request.setAttribute("error", "Invalid or missing club id");
                    yield "/pages/clubs.jsp";
                }
                Optional<ClubResponse> club = clubRestClient.getClubById(clubId.get());
                if (club.isPresent()) {
                    request.setAttribute("club", club.get());
                    yield "/pages/club.jsp";
                }
                request.setAttribute("error", "Club not found");
                yield "/pages/clubs.jsp";
            }
            default ->  "/index.jsp";
        };
        request.getRequestDispatcher(destination).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String submit = request.getParameter("submit");
        if (submit == null) {
            submit = "";
        }
        String destination = switch (submit){
            case "club/create" -> {
                ClubRequest clubRequest = buildClubRequest(request);
                Optional<ClubResponse> created = clubRestClient.createClub(clubRequest);
                if (created.isPresent()) {
                    yield reloadClubs(request);
                }
                request.setAttribute("error", "Unable to create club");
                yield "/pages/club.jsp";
            }
            case "club/update" -> {
                Optional<UUID> clubId = parseUuid(request.getParameter("clubId"));
                if (clubId.isEmpty()) {
                    request.setAttribute("error", "Invalid or missing club id");
                    yield "/pages/club.jsp";
                }
                ClubRequest clubRequest = buildClubRequest(request);
                Optional<ClubResponse> updated = clubRestClient.updateClub(clubId.get(), clubRequest);
                if (updated.isPresent()) {
                    yield reloadClubs(request);
                }
                request.setAttribute("error", "Unable to update club");
                yield "/pages/club.jsp";
            }
            default ->  "/index.jsp";
        };
        request.getRequestDispatcher(destination).forward(request, response);
    }

    private ClubRequest buildClubRequest(HttpServletRequest request) {
        String clubName = request.getParameter("clubName");
        String location = request.getParameter("location");
        String homeVenue = request.getParameter("homeVenue");
        boolean isActive = parseCheckbox(request.getParameter("isActive"));
        return new ClubRequest(clubName, location, homeVenue, isActive);
    }

    private String reloadClubs(HttpServletRequest request) {
        Optional<List<ClubResponse>> clubs = clubRestClient.listClubs();
        request.setAttribute("clubs", clubs.orElse(List.of()));
        return "/pages/clubs.jsp";
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

    private boolean parseCheckbox(String value) {
        return "on".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value);
    }
}
