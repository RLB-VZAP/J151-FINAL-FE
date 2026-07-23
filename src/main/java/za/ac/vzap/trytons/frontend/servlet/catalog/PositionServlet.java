package za.ac.vzap.trytons.frontend.servlet.catalog;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.catalog.*;
import za.ac.vzap.trytons.frontend.servlet.shared.AbstractServlet;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@WebServlet(name = "PositionServlet", urlPatterns = {"/admin/positions"})
public class PositionServlet extends AbstractServlet {

    private static final String VIEW = "/pages/admin-positions.jsp";

    @Inject
    private PositionRestClient positionRestClient;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if(!requireAdmin(request, response)) return;

        String submit = request.getParameter("submit");
        if (submit == null) {
            submit = "";
        }
        if (submit.isEmpty()) {
            submit = "positions";
        }
        String destination = switch (submit){
            case "positions" -> {
                String search = request.getParameter("search");
                Optional<List<PositionResponse>> positions = positionRestClient.getAllPositions();
                if (positions.isPresent()) {
                    request.setAttribute("positions", positions.get());
                } else {
                    request.setAttribute("error", "Unable to load positions");
                    request.setAttribute("positions", List.of());
                }
                request.setAttribute("searchTerm", search);
                yield VIEW;
            }
            case "position"  -> {
                Optional<UUID> positionId = parseUuid(request.getParameter("positionId"));
                if (positionId.isEmpty()) {
                    request.setAttribute("error", "Invalid or missing position id");
                    yield reloadPositions(request);
                }
                Optional<PositionResponse> position = positionRestClient.getPositionById(positionId.get());
                if (position.isPresent()) {
                    request.setAttribute("position", position.get());
                    yield reloadPositions(request);
                }
                request.setAttribute("error", "Position not found");
                yield reloadPositions(request);
            }
            default ->  "/index.jsp";
        };
        request.getRequestDispatcher(destination).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if(!requireAdmin(request, response)) return;

        String submit = request.getParameter("submit");
        if (submit == null){
            submit = "";
        }
        String destination = switch (submit){
            case "position/create" -> {
                // A brand-new position has no squad rule of its own; the backend's
                // hardcoded validation governs which positions count towards a squad.
                Optional<PositionRequest> positionRequest = buildPositionRequest(request, 0, 0);
                if (positionRequest.isEmpty()) {
                    request.setAttribute("error", "Invalid position fields");
                    yield reloadPositions(request);
                }
                Optional<PositionResponse> created = positionRestClient.createPosition(positionRequest.get());
                if(created.isPresent()){
                    request.setAttribute("position", created.get());
                    toastSuccess(request, "Position created");
                    yield reloadPositions(request);
                }
                request.setAttribute("error", "Unable to create position");
                yield reloadPositions(request);
            }
            case "position/update" -> {
                Optional<UUID> positionId = parseUuid(request.getParameter("positionId"));
                if (positionId.isEmpty()) {
                    request.setAttribute("error", "Invalid or missing position id");
                    yield reloadPositions(request);
                }
                // Carry the stored limits over unchanged: the update endpoint writes the
                // whole row, and these are backend-owned squad rules, not admin fields.
                Optional<PositionResponse> existing = positionRestClient.getPositionById(positionId.get());
                if (existing.isEmpty()) {
                    request.setAttribute("error", "Position not found");
                    yield reloadPositions(request);
                }
                Optional<PositionRequest> positionRequest = buildPositionRequest(
                        request, existing.get().getMinRequired(), existing.get().getMaxAllowed());
                if (positionRequest.isEmpty()) {
                    request.setAttribute("error", "Invalid position fields");
                    yield reloadPositions(request);
                }
                Optional<PositionResponse> updated = positionRestClient.updatePosition(positionId.get(), positionRequest.get());
                if(updated.isPresent()){
                    request.setAttribute("position", updated.get());
                    toastSuccess(request, "Position updated");
                    yield reloadPositions(request);
                }

                request.setAttribute("error", "Unable to update position");
                yield reloadPositions(request);
            }
            default -> "/index.jsp";
        };
        request.getRequestDispatcher(destination).forward(request, response);


        }
    /**
     * Builds a position request from the editable fields only.
     *
     * minRequired/maxAllowed are squad-composition rules, not position metadata:
     * they are enforced by the backend's hardcoded squad validation, so the admin
     * page no longer offers them for editing. They still have to be sent, because
     * the update endpoint writes the whole row, so the caller supplies the values
     * to carry over — the existing ones when editing, so an edit never rewrites
     * the stored limits.
     */
    private Optional<PositionRequest> buildPositionRequest(HttpServletRequest request,
                                                           int minRequired,
                                                           int maxAllowed) {

        String positionName = request.getParameter("positionName");
        String positionCategory = request.getParameter("positionCategory");

        if (positionName == null || positionName.isBlank()
                || positionCategory == null || positionCategory.isBlank()) {
            return Optional.empty();
        }

        PositionRequest positionRequest = new PositionRequest();
        positionRequest.setPositionName(positionName.trim());
        positionRequest.setPositionCategory(positionCategory.trim());
        positionRequest.setMinRequired(minRequired);
        positionRequest.setMaxAllowed(maxAllowed);

        return Optional.of(positionRequest);
    }

    private String reloadPositions(HttpServletRequest request) {
        Optional<List<PositionResponse>> positions = positionRestClient.getAllPositions();
        request.setAttribute("positions", positions.orElse(List.of()));
        return VIEW;
    }

    private Optional<Integer> parseInt(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Integer.parseInt(value.trim()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }




}
