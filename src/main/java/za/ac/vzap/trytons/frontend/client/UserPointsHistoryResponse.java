package za.ac.vzap.trytons.frontend.client;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class UserPointsHistoryResponse {

    private int totals;
    private List<WeeklyPerformanceResponse> rounds;
    private Integer ranking;

}