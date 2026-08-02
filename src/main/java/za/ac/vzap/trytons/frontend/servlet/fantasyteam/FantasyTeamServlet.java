package za.ac.vzap.trytons.frontend.servlet.fantasyteam;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import za.ac.vzap.trytons.frontend.client.catalog.ClubRestClient;
import za.ac.vzap.trytons.frontend.client.catalog.PlayerResponse;
import za.ac.vzap.trytons.frontend.client.catalog.PositionRestClient;
import za.ac.vzap.trytons.frontend.client.catalog.PositionResponse;
import za.ac.vzap.trytons.frontend.client.fantasyteam.*;
import za.ac.vzap.trytons.frontend.client.catalog.PlayerRestClient;
import za.ac.vzap.trytons.frontend.filter.SidebarBadgeFilter;
import java.io.IOException;
import java.util.*;

import za.ac.vzap.trytons.frontend.servlet.shared.AbstractServlet;

@WebServlet(name = "FantasyTeamServlet", urlPatterns = {"/create-team", "/fantasy-team/own", "/fantasy-team/opponent", "/fantasy-team/update"})
public class FantasyTeamServlet extends AbstractServlet{
    private static final String CREATE_TEAM_JSP = "/pages/create-team.jsp";
    private static final String VIEW_OWN_TEAM_JSP = "/pages/view-own-team.jsp";
    private static final String VIEW_OPPONENT_TEAM_JSP = "/pages/view-opponent-team.jsp";

    @Inject
    private FantasyTeamRestClient fantasyTeamRestClient;
    @Inject
    private PlayerRestClient playerRestClient;
    @Inject
    private ClubRestClient clubRestClient;
    @Inject
    private PositionRestClient positionRestClient;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if(!requireAuthenticated(request, response)) return;
        String destination = switch (request.getServletPath()){
            case "/fantasy-team/own" ->{
                // The "My Team" nav link carries no teamId, so fall back to the signed-in
                // user's own team. uk_fantasyTeam_owner makes that unambiguous. Same pattern
                // as transfers and fixtures; an explicit ?teamId= still wins.
                // getMyTeam() answers 404 for a genuine "no team yet", which must still
                // fall through to the friendly empty state below rather than the failure path.
                Optional<FantasyTeamResponse> myTeam = fantasyTeamRestClient.getMyTeam();
                if (myTeam.isEmpty() && !apiCallStatus.isNotFound()) {
                    if (handleEmptyResult(request, response, "Unable to load your team") == ApiFailure.REDIRECTED) {
                        yield null;
                    }
                    yield VIEW_OWN_TEAM_JSP;
                }
                Optional<UUID> teamId = parseUuid(request.getParameter("teamId"))
                        .or(() -> myTeam.map(FantasyTeamResponse::getTeamId));
                if(teamId.isEmpty()){
                    request.setAttribute("error","You don't have a team yet. Create one to see it here.");
                    yield VIEW_OWN_TEAM_JSP;
                }
                Optional<ViewOwnTeamResponse> team = fantasyTeamRestClient.viewOwnTeam(teamId.get());
                if(team.isPresent()){
                    request.setAttribute("team",team.get());
                }else{
                    if (handleEmptyResult(request, response, "Unable to load your team") == ApiFailure.REDIRECTED) {
                        yield null;
                    }
                }
                yield VIEW_OWN_TEAM_JSP;
            }
            case "/fantasy-team/opponent" ->{
                Optional<UUID> teamId = parseUuid(request.getParameter("teamId"));
                if(teamId.isEmpty()){
                    request.setAttribute("error","Team ID is required to view an opponent's team");
                    yield VIEW_OPPONENT_TEAM_JSP;
                }
                Optional<ViewOpponentTeamResponse> team = fantasyTeamRestClient.viewOpponentTeam(UUID.fromString(request.getParameter("teamId")));
                if(team.isPresent()){
                    request.setAttribute("team",team.get());
                }else{
                    if (handleEmptyResult(request, response, "Unable to load that team") == ApiFailure.REDIRECTED) {
                        yield null;
                    }
                }
                yield VIEW_OPPONENT_TEAM_JSP;
            }
            case "/fantasy-team/update" ->{
                Optional<UUID> teamId = parseUuid(request.getParameter("teamId"));
                if(teamId.isEmpty()){
                    request.setAttribute("error","Team ID is required to update your team");
                }else {
                    // A stray overwrite here used to clobber the loaded ViewOwnTeamResponse
                    // with the raw teamId, so the JSP could never tell it was in edit mode
                    // and always rendered a blank create form. That silently discarded the
                    // existing squad/team name, so every "edit" actually submitted as a
                    // brand new create — which then failed on the one-team-per-user
                    // constraint with a generic, unhelpful error.
                    Optional<ViewOwnTeamResponse> team = fantasyTeamRestClient.viewOwnTeam(teamId.get());
                    if (team.isPresent()) {
                        request.setAttribute("teamId", team.get().getTeamId());
                        request.setAttribute("teamName", team.get().getTeamName());
                        List<UUID> ownedPlayerIds = new ArrayList<>();
                        if(team.get().getPlayers() != null){
                            for(FantasyTeamPlayerSelectionResponse player : team.get().getPlayers()){
                                ownedPlayerIds.add(player.getPlayerId());
                            }
                        }
                        request.setAttribute("selectedPlayerIds", ownedPlayerIds);
                    } else {
                        if (handleEmptyResult(request, response, "Unable to load your team") == ApiFailure.REDIRECTED) {
                            yield null;
                        }
                    }
                }
                loadPlayerOptions(request);
                yield CREATE_TEAM_JSP;
            }

            default -> {
                if(authContext.isAdmin()){
                    request.setAttribute("adminCannotCreate", true);
                    yield CREATE_TEAM_JSP;
                }
                // One team per user (uk_fantasyTeam_owner): an existing owner is sent
                // straight to the edit view rather than shown a create-form dead end.
                // getMyTeam() answers 404 for "no team yet", which is the normal case
                // here and must fall through to the create form, not the failure path.
                Optional<FantasyTeamResponse> myTeam = fantasyTeamRestClient.getMyTeam();
                if (myTeam.isEmpty() && !apiCallStatus.isNotFound()) {
                    if (handleEmptyResult(request, response, "Unable to load your team") == ApiFailure.REDIRECTED) {
                        yield null;
                    }
                }
                Optional<UUID> existingTeamId = myTeam.map(FantasyTeamResponse::getTeamId);
                if (existingTeamId.isPresent()) {
                    redirectTo(response, request, "/fantasy-team/update?teamId=" + existingTeamId.get());
                    yield null;
                }
                loadPlayerOptions(request);
                yield CREATE_TEAM_JSP;
            }
        };
        if(destination == null){
            // Already redirected (either to the edit view or, on session expiry, to
            // /login) inside the switch above — nothing left to forward.
            return;
        }
        request.getRequestDispatcher(destination).forward(request, response);

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if(!requireAuthenticated(request, response)) return;
        String submit = request.getParameter("submit");
        if(submit == null){
            submit = "";
        }
        // On success each handler stashes a toast flash and we follow
        // POST-redirect-GET, so the message survives the redirect and a refresh
        // cannot re-submit the team. On failure we fall through and re-render the
        // form with its inline validation/error attributes intact.
        if(("".equals(submit) || "create-team".equals(submit)) && authContext.isAdmin()){
            request.setAttribute("adminCannotCreate", true);
            request.getRequestDispatcher(CREATE_TEAM_JSP).forward(request, response);
            return;
        }
        boolean succeeded = switch(submit){
            case"","create-team" -> handleCreateTeam(request);
            case "update-team" -> handleUpdateTeam(request);
            default -> { request.setAttribute("error","Invalid submit"); yield false; }
        };
        if(succeeded){
            redirectTo(response, request, "update-team".equals(submit) ? "/fantasy-team/own" : "/create-team");
            return;
        }
        request.getRequestDispatcher(CREATE_TEAM_JSP).forward(request, response);

    }

    // Returns true when the team was created (caller then flashes + redirects),
    // false when validation or the API call failed (caller re-renders the form).
    private boolean handleCreateTeam(HttpServletRequest request){
        List<String> validationErrors = new ArrayList<>();
        String teamName = request.getParameter("teamName");
        if(teamName == null || teamName.isBlank()){
            validationErrors.add("Team name is required");
        }
        String[] playerIdParams = request.getParameterValues("playerIds");
        List<UUID> selectedPlayerIds = new ArrayList<>();
        if(playerIdParams != null){
            for(String value : playerIdParams){
                parseUuid(value).ifPresent(selectedPlayerIds::add);
            }
        }
        if(selectedPlayerIds.isEmpty()){
            validationErrors.add("You must select at least one player");
        }
        // Re-offered on any failure below so a rejected submission does not
        // dump the user back to an empty, unchecked player pool.
        request.setAttribute("teamName", teamName);
        request.setAttribute("selectedPlayerIds", selectedPlayerIds);
        if(!validationErrors.isEmpty()){
            request.setAttribute("validationErrors",validationErrors);
            loadPlayerOptions(request);
            return false;
        }

        FantasyTeamRequest fantasyTeamRequest = buildFantasyTeamRequest(teamName,selectedPlayerIds);
        Optional<FantasyTeamResponse> fantasyTeamResponse = fantasyTeamRestClient.createTeam(fantasyTeamRequest);
        if(fantasyTeamResponse.isPresent()){
            // The sidebar/dashboard nav read this session cache (see
            // SidebarBadgeFilter) rather than calling the backend on every page, so
            // it has to be refreshed here or the nav would keep offering "Create
            // Team" until the cache happened to be repopulated some other way.
            HttpSession session = request.getSession(true);
            session.setAttribute(SidebarBadgeFilter.SESSION_HAS_TEAM, Boolean.TRUE);
            session.setAttribute(SidebarBadgeFilter.SESSION_TEAM_ID, fantasyTeamResponse.get().getTeamId());
            flashSuccess(request, "Team created");
            return true;
        }
        request.setAttribute("error", apiCallStatus.getMessage("Team could not be created. Check your squad rules and budget and try again"));
        loadPlayerOptions(request);
        return false;
    }

    private boolean handleUpdateTeam(HttpServletRequest request){
        List<String> validationErrors = new ArrayList<>();
        Optional<UUID> teamId = parseUuid(request.getParameter("teamId"));
        if(teamId.isEmpty()){
            validationErrors.add("Team ID is required to update a team.");
        }
        String teamName = request.getParameter("teamName");
        if(teamName == null || teamName.isBlank()){
            validationErrors.add("Team name is required");
        }
        String[] playerIdParams = request.getParameterValues("playerIds");
        List<UUID> selectedPlayerIds = new ArrayList<>();
        if(playerIdParams != null){
            for(String value : playerIdParams){
                parseUuid(value).ifPresent(selectedPlayerIds::add);
            }
        }
        if(selectedPlayerIds.isEmpty()){
            validationErrors.add("You must select at least one player");
        }
        // Re-offered on any failure below so a rejected submission stays in edit
        // mode with the attempted picks intact, instead of reverting to a blank
        // create form.
        teamId.ifPresent(id -> request.setAttribute("teamId", id));
        request.setAttribute("teamName", teamName);
        request.setAttribute("selectedPlayerIds", selectedPlayerIds);
        if(!validationErrors.isEmpty()){
            request.setAttribute("validationErrors",validationErrors);
            loadPlayerOptions(request);
            return false;
        }
        FantasyTeamRequest fantasyTeamRequest = buildFantasyTeamRequest(teamName, selectedPlayerIds);
        Optional<FantasyTeamResponse> fantasyTeamResponse = fantasyTeamRestClient.updateTeam(teamId.get(),fantasyTeamRequest);
        if(fantasyTeamResponse.isPresent()){
            flashSuccess(request, "Team updated");
            return true;
        }
        request.setAttribute("error", apiCallStatus.getMessage("Team could not be updated. Check your squad rules and budget and try again"));
        loadPlayerOptions(request);
        return false;
    }

    private FantasyTeamRequest buildFantasyTeamRequest(String teamName,List<UUID> selectedPlayerIds){
        List<FantasyTeamPlayerSelectionRequest> selections = new ArrayList<>();
        for(UUID playerId : selectedPlayerIds){
            selections.add(new FantasyTeamPlayerSelectionRequest (playerId, "STARTING", false,false));
        }
        return new FantasyTeamRequest(teamName, selections);
    }

    private void loadPlayerOptions(HttpServletRequest request){
        // The same catalogue the Players screen shows — one list, so a player seen
        // there is a player who can be picked here. Injured and suspended players
        // still appear, marked and with their checkbox disabled, rather than being
        // silently missing; the squad validator would reject them on submit.
        Optional<List<PlayerResponse>> players = playerRestClient.listPlayers(null, null, null);
        if(players.isPresent()){
            request.setAttribute("players",players.get());
        }else{
            request.setAttribute("error","Unable to load available players");
            request.setAttribute("players",List.of());
        }
        request.setAttribute("clubNamesById", buildClubNameLookup());
        request.setAttribute("positionNamesById",buildPositionNameLookUp());
        // The squad-requirements helper reads the real per-position rules
        // (minRequired/maxAllowed/category) straight from the backend, so it can
        // never drift from what the server actually validates on submit.
        request.setAttribute("positions", loadPositionRules());
    }

    /**
     * Positions with their squad rules, forwards first then backs, each group
     * ordered by name. Empty list if the catalogue cannot be loaded.
     */
    private List<PositionResponse> loadPositionRules(){
        List<PositionResponse> positions = positionRestClient.getAllPositions().orElse(List.of());
        List<PositionResponse> ordered = new ArrayList<>(positions);
        ordered.sort(Comparator
                .comparing((PositionResponse p) -> !"FORWARD".equalsIgnoreCase(p.getPositionCategory()))
                .thenComparing(PositionResponse::getPositionName, Comparator.nullsLast(String::compareToIgnoreCase)));
        return ordered;
    }

    private Map<UUID,String> buildClubNameLookup(){
        Map<UUID,String> lookup = new HashMap<>();
        clubRestClient.listClubs().ifPresent(clubs -> clubs.forEach(club -> lookup.put(club.getClubId(), club.getClubName())));
        return lookup;
    }

    private Map<UUID,String> buildPositionNameLookUp(){
        Map<UUID,String> lookup = new HashMap<>();
        positionRestClient.getAllPositions().ifPresent(positions -> positions.forEach(position -> lookup.put(position.getPositionId(), position.getPositionName())));
        return lookup;
    }

}
