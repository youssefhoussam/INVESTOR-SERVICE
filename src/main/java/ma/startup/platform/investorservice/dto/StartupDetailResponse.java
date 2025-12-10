package ma.startup.platform.investorservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StartupDetailResponse {

    // Basic Info
    private UUID id;
    private String nom;
    private String secteur;
    private String description;
    private String tags;
    private Integer profileCompletion;
    private String logo;
    private String siteWeb;
    private LocalDate dateCreation;
    private LocalDateTime createdAt;

    // Team Members
    private List<TeamMemberInfo> team;

    // Milestones
    private List<MilestoneInfo> milestones;
    private Integer milestonesCompleted;
    private Integer milestonesPending;

    // Statistics
    private Integer pitchsGenerated;
    private Integer matchingScore; // Score with current investor

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TeamMemberInfo {
        private UUID id;
        private String nom;
        private String role;
        private String linkedIn;
        private String photo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MilestoneInfo {
        private UUID id;
        private String titre;
        private String description;
        private String statut;
        private LocalDate dateEcheance;
        private LocalDateTime completedAt;
    }
}