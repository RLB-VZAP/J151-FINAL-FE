package za.ac.vzap.trytons.frontend.servlet.catalog;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.catalog.ClubRequest;
import za.ac.vzap.trytons.frontend.client.catalog.ClubResponse;
import za.ac.vzap.trytons.frontend.client.catalog.ClubRestClient;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import za.ac.vzap.trytons.frontend.servlet.shared.AbstractServlet;

@WebServlet(name ="ClubServlet" , urlPatterns = {"/clubs","/club","/club/create","/club/update"})
public class ClubServlet extends AbstractServlet {

    @Inject
    private ClubRestClient clubRestClient;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String servletPath = request.getServletPath();
        String submit = request.getParameter("submit");
        if (submit == null) {
            submit = "";
        }
        boolean clubsList = "clubs".equals(submit) || (submit.isEmpty() && "/clubs".equals(servletPath));
        boolean clubDetail = "club".equals(submit) || (submit.isEmpty() && "/club".equals(servletPath));

        String destination;
        if (clubsList) {
            destination = renderClubsList(request);
        } else if (clubDetail) {
            destination = renderClubDetail(request);
        } else {
            destination = "/index.jsp";
        }
        request.getRequestDispatcher(destination).forward(request, response);
    }

    private String renderClubsList(HttpServletRequest request) {
        String search = request.getParameter("search");
        Optional<List<ClubResponse>> clubs = clubRestClient.listClubs();
        if (clubs.isPresent()) {
            request.setAttribute("clubs", clubs.get());
        } else {
            request.setAttribute("error", "Unable to load clubs");
            request.setAttribute("clubs", List.of());
        }
        request.setAttribute("searchTerm", search);
        return "/pages/clubs.jsp";
    }

    private String renderClubDetail(HttpServletRequest request) {
        Optional<UUID> clubId = parseUuid(request.getParameter("clubId"));
        if (clubId.isEmpty()) {
            request.setAttribute("error", "Invalid or missing club id");
            return renderClubsList(request);
        }
        Optional<ClubResponse> club = clubRestClient.getClubById(clubId.get());
        if (club.isPresent()) {
            request.setAttribute("club", club.get());
            return "/pages/club.jsp";
        }
        request.setAttribute("error", "Club not found");
        return renderClubsList(request);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if(!requireAdmin(request, response)) return;
        String servletPath = request.getServletPath();
        String submit = request.getParameter("submit");
        if (submit == null) {
            submit = "";
        }
        String destination;
        if ("club/create".equals(submit) || "/club/create".equals(servletPath)) {
            destination = handleCreateClub(request);
        } else if ("club/update".equals(submit) || "/club/update".equals(servletPath)) {
            destination = handleUpdateClub(request);
        } else {
            destination = "/index.jsp";
        }
        request.getRequestDispatcher(destination).forward(request, response);
    }

    private String handleCreateClub(HttpServletRequest request) {
        ClubRequest clubRequest = buildClubRequest(request);
        Optional<ClubResponse> created = clubRestClient.createClub(clubRequest);
        if (created.isPresent()) {
            return reloadClubs(request);
        }
        request.setAttribute("error", "Unable to create club");
        return "/pages/club.jsp";
    }

    private String handleUpdateClub(HttpServletRequest request) {
        Optional<UUID> clubId = parseUuid(request.getParameter("clubId"));
        if (clubId.isEmpty()) {
            request.setAttribute("error", "Invalid or missing club id");
            return "/pages/club.jsp";
        }
        ClubRequest clubRequest = buildClubRequest(request);
        Optional<ClubResponse> updated = clubRestClient.updateClub(clubId.get(), clubRequest);
        if (updated.isPresent()) {
            return reloadClubs(request);
        }
        request.setAttribute("error", "Unable to update club");
        return "/pages/club.jsp";
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


    private boolean parseCheckbox(String value) {
        return "on".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value);
    }
}
