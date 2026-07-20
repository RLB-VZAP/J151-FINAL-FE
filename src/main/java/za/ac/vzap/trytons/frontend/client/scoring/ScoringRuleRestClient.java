package za.ac.vzap.trytons.frontend.client.scoring;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import za.ac.vzap.trytons.frontend.client.shared.APIClient;

import java.util.List;
import java.util.Optional;

@Dependent
public class ScoringRuleRestClient {

    private static final String SCORING_RULES_PATH = "/scoring-rules";

    @Inject
    private APIClient apiClient;

    public Optional<List<ScoringRuleResponse>> getScoringRules(String season) {
        // TODO: Call GET /scoring-rules?season={season} via the APIClient and return the list of scoring rules for that season.
    }

    public Optional<ScoringRuleResponse> saveScoringRule(ScoringRuleRequest request) {
        // TODO: Call POST /scoring-rules via the APIClient with the given request body and return the saved scoring rule.
    }
}
