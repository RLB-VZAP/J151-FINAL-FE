package za.ac.vzap.trytons.frontend.servlet.fantasyteam;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.ac.vzap.trytons.frontend.client.catalog.ClubRestClient;
import za.ac.vzap.trytons.frontend.client.fantasyteam.FantasyTeamRestClient;
import za.ac.vzap.trytons.frontend.client.catalog.PlayerRestClient;
import za.ac.vzap.trytons.frontend.client.fantasyteam.ViewOwnTeamResponse;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if(!requireAuthenticated(request, response)) return;
        String destination = switch (request.getServletPath()){
            case "/fantasy-team/own" ->{
                Optional<String> teamName = Optional.ofNullable(request.getParameter("teamName"));
                if(teamName.isEmpty()){
                    request.setAttribute("error","Team name is required to view your team");
                    yield VIEW_OWN_TEAM_JSP;
                }
                Optional<ViewOwnTeamResponse> team = fantasyTeamRestClient.viewOwnTeam(teamName.get());
                if(team.isPresent()){
                    request.setAttribute("team",team.get());
                }else{
                    request.setAttribute("error","Unable to load your team");
                }
                yield VIEW_OWN_TEAM_JSP;
            }
            case "/fantasy-team/update" ->{
                Optional<UUID> teamId = parseUuid(request.getParameter("teamId"));
                if(teamId.isEmpty()){
                    request.setAttribute("error","Team ID is required to edit a team");
                }
                yield VIEW_OPPONENT_TEAM_JSP;//this is wrong just need to finish to push.
            }
            default -> throw new IllegalStateException("Unexpected value: " + request.getServletPath());
        };

    }

}
