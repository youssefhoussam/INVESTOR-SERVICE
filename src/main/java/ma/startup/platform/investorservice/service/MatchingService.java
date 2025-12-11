package ma.startup.platform.investorservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchingService {

    private final MatchingResultRepository matchingResultRepository;
    private final InvestorRepository investorRepository;
    private final StartupServiceClient startupServiceClient;
    private final AuthServiceClient authServiceClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Get matching investors for current startup user
     * This is THE KEY ENDPOINT: GET /api/matching/for-me
     */
    @Transactional
    public List<MatchingResponse> getMatchingInvestorsForMe(String authHeader) {
        log.info("Calculating matching investors for current startup");

        // 1. Get current user
        UserDTO user = authServiceClient.getCurrentUser(authHeader);

        // 2. Get startup profile
        // 2. Get startup profile
        StartupDTO startup;
        try {
            // Use getMyStartup instead of getStartupById with userId
            startup = startupServiceClient.getMyStartup(authHeader);
        } catch (Exception e) {
            log.error("Error fetching startup: {}", e.getMessage());
            throw new RuntimeException("Profil startup non trouvé pour cet utilisateur");
        }

        // 3. Get all investors
        List<Investor> allInvestors = investorRepository.findAll();

        if (allInvestors.isEmpty()) {
            log.warn("No investors found in database");
            return Collections.emptyList();
        }

        // 4. Calculate matching score for each investor
        List<MatchingResponse> matches = new ArrayList<>();

        for (Investor investor : allInvestors) {
            int score = calculateMatchingScore(startup, investor);

            // Create or update matching result
            MatchingResult matchingResult = matchingResultRepository
                    .findByStartupIdAndInvestorId(startup.getId(), investor.getId())
                    .orElse(new MatchingResult());

            matchingResult.setStartupId(startup.getId());
            matchingResult.setInvestorId(investor.getId());
            matchingResult.setScore(score);
            matchingResult.setCriteria(buildCriteriaJson(startup, investor, score));

            matchingResultRepository.save(matchingResult);

            // Build response
            MatchingResponse response = new MatchingResponse();
            response.setMatchId(matchingResult.getId());
            response.setInvestor(InvestorResponse.fromInvestor(investor));
            response.setScore(score);
            response.setCriteria(parseCriteria(startup, investor, score));
            response.setIsViewed(matchingResult.getIsViewed());

            matches.add(response);
        }

        // 5. Sort by score (highest first) and return top 20
        return matches.stream()
                .sorted((a, b) -> b.getScore().compareTo(a.getScore()))
                .limit(20)
                .collect(Collectors.toList());
    }

    /**
     * Calculate matching score between startup and investor
     * Algorithm from documentation:
     * - Sector match: 70 points
     * - Investment amount: 20 points
     * - Location: 10 points
     */
    private int calculateMatchingScore(StartupDTO startup, Investor investor) {
        int score = 0;

        // 1. Sector matching (70 points)
        if (startup.getSecteur() != null && investor.getSecteursInterets() != null) {
            String secteur = startup.getSecteur().toLowerCase();
            String secteursInterets = investor.getSecteursInterets().toLowerCase();

            if (secteursInterets.contains(secteur)) {
                score += 70;
                log.debug("Sector match found: {} in {}", secteur, secteursInterets);
            }
        }

        // 2. Investment amount matching (20 points)
        // Note: Startup entity doesn't have montant_recherche field in documentation
        // So we give partial points by default
        score += 10; // Default partial compatibility

        // 3. Location matching (10 points)
        if (startup.getLocalisation() != null && investor.getLocalisation() != null) {
            if (startup.getLocalisation().equalsIgnoreCase(investor.getLocalisation())) {
                score += 10;
                log.debug("Location match: {}", startup.getLocalisation());
            }
        }

        return score;
    }

    /**
     * Build criteria JSON string for database storage
     */
    private String buildCriteriaJson(StartupDTO startup, Investor investor, int score) {
        Map<String, Object> criteria = new HashMap<>();

        boolean secteurMatch = startup.getSecteur() != null &&
                investor.getSecteursInterets() != null &&
                investor.getSecteursInterets().toLowerCase().contains(startup.getSecteur().toLowerCase());

        boolean localisationMatch = startup.getLocalisation() != null &&
                investor.getLocalisation() != null &&
                startup.getLocalisation().equalsIgnoreCase(investor.getLocalisation());

        criteria.put("secteurMatch", secteurMatch);
        criteria.put("montantCompatible", true); // Default
        criteria.put("localisationMatch", localisationMatch);
        criteria.put("totalScore", score);

        try {
            return objectMapper.writeValueAsString(criteria);
        } catch (JsonProcessingException e) {
            log.error("Error serializing criteria to JSON", e);
            return "{}";
        }
    }

    /**
     * Parse criteria for response DTO
     */
    private MatchingResponse.MatchingCriteria parseCriteria(StartupDTO startup, Investor investor, int score) {
        boolean secteurMatch = startup.getSecteur() != null &&
                investor.getSecteursInterets() != null &&
                investor.getSecteursInterets().toLowerCase().contains(startup.getSecteur().toLowerCase());

        boolean localisationMatch = startup.getLocalisation() != null &&
                investor.getLocalisation() != null &&
                startup.getLocalisation().equalsIgnoreCase(investor.getLocalisation());

        String details = String.format("Score: %d/100 - Secteur: %s, Localisation: %s",
                score, secteurMatch ? "✓" : "✗", localisationMatch ? "✓" : "✗");

        return new MatchingResponse.MatchingCriteria(secteurMatch, true, localisationMatch, details);
    }

    /**
     * Get matching score for specific investor
     */
    public MatchingResponse getMatchingScore(UUID investorId, String authHeader) {
        UserDTO user = authServiceClient.getCurrentUser(authHeader);

        // FIXED: Use getMyStartup
        StartupDTO startup;
        try {
            startup = startupServiceClient.getMyStartup(authHeader);
        } catch (Exception e) {
            log.error("Error fetching startup: {}", e.getMessage());
            throw new RuntimeException("Profil startup non trouvé");
        }

        Investor investor = investorRepository.findById(investorId)
                .orElseThrow(() -> new RuntimeException("Investisseur non trouvé"));

        int score = calculateMatchingScore(startup, investor);

        MatchingResponse response = new MatchingResponse();
        response.setInvestor(InvestorResponse.fromInvestor(investor));
        response.setScore(score);
        response.setCriteria(parseCriteria(startup, investor, score));

        return response;
    }

    /**
     * Force recalculation of all matches for current startup
     */
    @Transactional
    public void recalculateMatches(String authHeader) {
        log.info("Force recalculating matches");
        getMatchingInvestorsForMe(authHeader);
    }
    /**
     * Get matching startups for current investor (investor's perspective)
     */
    @Transactional
    public List<StartupMatchResponse> getMatchingStartupsForMe(String authHeader) {
        log.info("Calculating matching startups for current investor");

        // 1. Get current user
        UserDTO user = authServiceClient.getCurrentUser(authHeader);

        // Verify user is an investor
        if (!"INVESTOR".equals(user.getRole())) {
            throw new RuntimeException("Seuls les investisseurs peuvent consulter les start-ups matchées");
        }

        // 2. Get investor profile
        Investor investor = investorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Profil investisseur non trouvé"));

        // 3. Get all startups
        List<StartupDTO> allStartups;
        try {
            allStartups = startupServiceClient.getAllStartups(authHeader);
        } catch (Exception e) {
            log.error("Error fetching startups: {}", e.getMessage());
            return Collections.emptyList();
        }

        if (allStartups.isEmpty()) {
            log.warn("No startups found in database");
            return Collections.emptyList();
        }

        // 4. Calculate matching score for each startup
        List<StartupMatchResponse> matches = new ArrayList<>();

        for (StartupDTO startup : allStartups) {
            int score = calculateMatchingScore(startup, investor);

            // Only return startups with score >= 50 (decent match)
            if (score >= 50) {
                // Create or update matching result
                MatchingResult matchingResult = matchingResultRepository
                        .findByStartupIdAndInvestorId(startup.getId(), investor.getId())
                        .orElse(new MatchingResult());

                matchingResult.setStartupId(startup.getId());
                matchingResult.setInvestorId(investor.getId());
                matchingResult.setScore(score);
                matchingResult.setCriteria(buildCriteriaJson(startup, investor, score));

                matchingResultRepository.save(matchingResult);

                // Build startup info
                StartupMatchResponse.StartupInfo startupInfo = new StartupMatchResponse.StartupInfo(
                        startup.getId(),
                        startup.getNom(),
                        startup.getSecteur(),
                        startup.getDescription(),
                        startup.getLocalisation(),
                        startup.getProfileCompletion(),
                        startup.getLogo(),
                        startup.getSiteWeb()
                );

                // Build criteria
                StartupMatchResponse.MatchingCriteria criteria = buildMatchingCriteria(startup, investor, score);

                // Build response
                StartupMatchResponse response = new StartupMatchResponse(
                        matchingResult.getId(),
                        startupInfo,
                        score,
                        criteria,
                        matchingResult.getIsViewed()
                );

                matches.add(response);
            }
        }

        // 5. Sort by score (highest first) and return top 20
        return matches.stream()
                .sorted((a, b) -> b.getScore().compareTo(a.getScore()))
                .limit(20)
                .collect(Collectors.toList());
    }

    /**
     * Helper method to build matching criteria
     */
    private StartupMatchResponse.MatchingCriteria buildMatchingCriteria(StartupDTO startup, Investor investor, int score) {
        boolean secteurMatch = startup.getSecteur() != null &&
                investor.getSecteursInterets() != null &&
                investor.getSecteursInterets().toLowerCase().contains(startup.getSecteur().toLowerCase());

        boolean localisationMatch = startup.getLocalisation() != null &&
                investor.getLocalisation() != null &&
                startup.getLocalisation().equalsIgnoreCase(investor.getLocalisation());

        String details = String.format("Score: %d/100 - Secteur: %s, Localisation: %s",
                score, secteurMatch ? "✓" : "✗", localisationMatch ? "✓" : "✗");

        return new StartupMatchResponse.MatchingCriteria(secteurMatch, true, localisationMatch, details);
    }}