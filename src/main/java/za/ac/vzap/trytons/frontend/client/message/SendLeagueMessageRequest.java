package za.ac.vzap.trytons.frontend.client.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SendLeagueMessageRequest {
    private String body;
}
