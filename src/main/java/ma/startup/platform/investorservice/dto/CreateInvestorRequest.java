package ma.startup.platform.investorservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.startup.platform.investorservice.enums.InvestorType;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateInvestorRequest {

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotNull(message = "Le type d'investisseur est obligatoire")
    private InvestorType type;

    private String secteursInterets; // JSON array: ["FinTech","EdTech"]

    private BigDecimal montantMin;

    private BigDecimal montantMax;

    private String description;

    private String localisation;

    private String portfolio;

    private String siteWeb;

    @Email(message = "Email invalide")
    private String email;
}