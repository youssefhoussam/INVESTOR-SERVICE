package ma.startup.platform.investorservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.startup.platform.investorservice.client.AuthServiceClient;
import ma.startup.platform.investorservice.client.StartupServiceClient;
import ma.startup.platform.investorservice.dto.*;
import ma.startup.platform.investorservice.model.Investor;
import ma.startup.platform.investorservice.model.MatchingResult;
import ma.startup.platform.investorservice.repository.InvestorRepository;
import ma.startup.platform.investorservice.repository.MatchingResultRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StartupDetailService {

    private final StartupServiceClient startupServiceClient;
    private final InvestorRepository investorRepository;
    private final MatchingResultRepository matchingResultRepository;
    private final AuthServiceClient authServiceClient;

    /**
     * Get detailed startup information for investor
     */
    public StartupDetailResponse getStartupDetails(UUID startupId, String authHeader) {
        log.info("Fetching detailed info for startup: {}", startupId);

        // Verify user is an investor
        UserDTO user = authServiceClient.getCurrentUser(authHeader);
        if (!"INVESTOR".equals(user.getRole())) {
            throw new RuntimeException("Seuls les investisseurs peuvent consulter les détails des start-ups");
        }

        // Get investor profile
        Investor investor = investorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Profil investisseur non trouvé"));

        // Fetch startup basic info
        StartupDTO startup;
        try {
            startup = startupServiceClient.getStartupById(startupId, authHeader);
        } catch (Exception e) {
            log.error("Error fetching startup: {}", e.getMessage());
            throw new RuntimeException("Start-up non trouvée");
        }

        // Fetch team members
        List<FounderMemberDTO> teamMembers;
        try {
            teamMembers = startupServiceClient.getTeamByStartupId(startupId, authHeader);
        } catch (Exception e) {
            log.warn("Could not fetch team members: {}", e.getMessage());
            teamMembers = List.of();
        }

        // Fetch milestones
        List<MilestoneDTO> milestones;
        try {
            milestones = startupServiceClient.getMilestonesByStartupId(startupId, authHeader);
        } catch (Exception e) {
            log.warn("Could not fetch milestones: {}", e.getMessage());
            milestones = List.of();
        }



        // Get matching score with this investor
        Integer matchingScore = null;
        try {
            MatchingResult matchingResult = matchingResultRepository
                    .findByStartupIdAndInvestorId(startupId, investor.getId())
                    .orElse(null);
            if (matchingResult != null) {
                matchingScore = matchingResult.getScore();
            }
        } catch (Exception e) {
            log.warn("Could not fetch matching score: {}", e.getMessage());
        }

        // Count milestones by status
        long milestonesCompleted = milestones.stream()
                .filter(m -> "COMPLETED".equals(m.getStatut()))
                .count();
        long milestonesPending = milestones.stream()
                .filter(m -> !"COMPLETED".equals(m.getStatut()))
                .count();

        // Build team info list
        List<StartupDetailResponse.TeamMemberInfo> teamInfo = teamMembers.stream()
                .map(member -> StartupDetailResponse.TeamMemberInfo.builder()
                        .id(member.getId())
                        .nom(member.getNom())
                        .role(member.getRole())
                        .linkedIn(member.getLinkedIn())
                        .photo(member.getPhoto())
                        .build())
                .collect(Collectors.toList());

        // Build milestone info list
        List<StartupDetailResponse.MilestoneInfo> milestoneInfo = milestones.stream()
                .map(milestone -> StartupDetailResponse.MilestoneInfo.builder()
                        .id(milestone.getId())
                        .titre(milestone.getTitre())
                        .description(milestone.getDescription())
                        .statut(milestone.getStatut())
                        .dateEcheance(milestone.getDateEcheance())
                        .completedAt(milestone.getCompletedAt())
                        .build())
                .collect(Collectors.toList());

        // Build complete response
        return StartupDetailResponse.builder()
                .id(startup.getId())
                .nom(startup.getNom())
                .secteur(startup.getSecteur())
                .description(startup.getDescription())
                .tags(startup.getTags())
                .profileCompletion(startup.getProfileCompletion())
                .logo(startup.getLogo())
                .siteWeb(startup.getSiteWeb())
                .dateCreation(startup.getDateCreation())
                .createdAt(startup.getCreatedAt())
                .team(teamInfo)
                .milestones(milestoneInfo)
                .milestonesCompleted((int) milestonesCompleted)
                .milestonesPending((int) milestonesPending)
                .matchingScore(matchingScore)
                .build();
    }
}
