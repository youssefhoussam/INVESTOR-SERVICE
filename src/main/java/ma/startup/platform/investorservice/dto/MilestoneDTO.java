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
public class MilestoneDTO {
    private UUID id;
    private UUID startupId;
    private String titre;
    private String description;
    private String statut;
    private LocalDate dateEcheance;
    private LocalDateTime completedAt;
}
