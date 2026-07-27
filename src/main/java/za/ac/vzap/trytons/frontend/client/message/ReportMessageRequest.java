package za.ac.vzap.trytons.frontend.client.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Shared body shape for both /messages/direct/{id}/report and
// /leagues/{leagueId}/messages/{id}/report — the backend accepts the same
// { "reason": "..." } payload for either scope.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportMessageRequest {
    private String reason;
}
