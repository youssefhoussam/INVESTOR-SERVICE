package ma.startup.platform.investorservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.startup.platform.investorservice.enums.InvestorType;
import ma.startup.platform.investorservice.model.Investor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvestorResponse {

    private UUID id;
    private UUID userId;
    private String nom;
    private InvestorType type;
    private String secteursInterets;
    private BigDecimal montantMin;
    private BigDecimal montantMax;
    private String description;
    private String localisation;
    private String portfolio;
    private String siteWeb;
    private String email;
    private LocalDateTime createdAt;

    public static InvestorResponse fromInvestor(Investor investor) {
        return new InvestorResponse(
                investor.getId(),
                investor.getUserId(),
                investor.getNom(),
                investor.getType(),
                investor.getSecteursInterets(),
                investor.getMontantMin(),
                investor.getMontantMax(),
                investor.getDescription(),
                investor.getLocalisation(),
                investor.getPortfolio(),
                investor.getSiteWeb(),
                investor.getEmail(),
                investor.getCreatedAt()
        );
    }
}
