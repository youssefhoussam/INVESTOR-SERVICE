package ma.startup.platform.investorservice.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StartupMatchResponse {

    private UUID matchId;
    private StartupInfo startup;
    private Integer score;
    private MatchingCriteria criteria;
    private Boolean isViewed;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StartupInfo {
        private UUID id;
        private String nom;
        private String secteur;
        private String description;
        private String localisation;
        private Integer profileCompletion;
        private String logo;
        private String siteWeb;
    }

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
