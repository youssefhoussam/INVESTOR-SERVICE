package ma.startup.platform.investorservice.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.startup.platform.investorservice.dto.MatchingResponse;
import ma.startup.platform.investorservice.dto.StartupMatchResponse;
import ma.startup.platform.investorservice.service.MatchingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
@Slf4j
public class MatchingController {

    private final MatchingService matchingService;

    /**
     * GET /api/matching/for-me - KEY ENDPOINT
     * Get matching investors for current startup (sorted by score)
     */
    @GetMapping("/for-me")
    public ResponseEntity<?> getMatchingInvestorsForMe(@RequestHeader("Authorization") String authHeader) {
        try {
            log.info("GET /api/matching/for-me - Calculating matching investors");
            List<MatchingResponse> matches = matchingService.getMatchingInvestorsForMe(authHeader);
            return ResponseEntity.ok(matches);
        } catch (Exception e) {
            log.error("Error calculating matches: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    /**
     * GET /api/matching/score/{investorId} - Get score for specific investor
     */
    @GetMapping("/score/{investorId}")
    public ResponseEntity<?> getMatchingScore(
            @PathVariable UUID investorId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            log.info("GET /api/matching/score/{} - Getting matching score", investorId);
            MatchingResponse response = matchingService.getMatchingScore(investorId, authHeader);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting matching score: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    /**
     * POST /api/matching/calculate - Force recalculation of matches
     */
    @PostMapping("/calculate")
    public ResponseEntity<?> recalculateMatches(@RequestHeader("Authorization") String authHeader) {
        try {
            log.info("POST /api/matching/calculate - Forcing recalculation");
            matchingService.recalculateMatches(authHeader);
            return ResponseEntity.ok("Matching recalculé avec succès");
        } catch (Exception e) {
            log.error("Error recalculating matches: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }
    /**
     * GET /api/matching/startups - Get matching startups (investor view)
     */
    @GetMapping("/startups")
    public ResponseEntity<?> getMatchingStartupsForMe(@RequestHeader("Authorization") String authHeader) {
        try {
            log.info("GET /api/matching/startups - Calculating matching startups for investor");
            List<StartupMatchResponse> matches = matchingService.getMatchingStartupsForMe(authHeader);
            return ResponseEntity.ok(matches);
        } catch (Exception e) {
            log.error("Error calculating startup matches: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }
}
