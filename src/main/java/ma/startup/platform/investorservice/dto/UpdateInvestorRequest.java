package ma.startup.platform.investorservice.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.startup.platform.investorservice.enums.InvestorType;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateInvestorRequest {

    private String nom;
    private InvestorType type;
    private String secteursInterets;
    private BigDecimal montantMin;
    private BigDecimal montantMax;
    private String description;
    private String localisation;
    private String portfolio;
    private String siteWeb;

    @Email(message = "Email invalide")
    private String email;
}
