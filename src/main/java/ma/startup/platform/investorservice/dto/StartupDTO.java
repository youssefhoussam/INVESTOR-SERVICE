package ma.startup.platform.investorservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StartupDTO {
    private UUID id;
    private UUID userId;
    private String nom;
    private String secteur;
    private String description;
    private String tags;
    private Integer profileCompletion;
    private String logo;
    private String siteWeb;
    private LocalDate dateCreation;
    private String localisation; // May not exist in actual Startup entity
    private LocalDateTime createdAt;
}
