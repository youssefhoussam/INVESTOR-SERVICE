package ma.startup.platform.investorservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchingResponse {

    private UUID matchId;
    private InvestorResponse investor;
    private Integer score;
    private MatchingCriteria criteria;
    private Boolean isViewed;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MatchingCriteria {
        private Boolean secteurMatch;
        private Boolean montantCompatible;
        private Boolean localisationMatch;
        private String details;
    }
}
