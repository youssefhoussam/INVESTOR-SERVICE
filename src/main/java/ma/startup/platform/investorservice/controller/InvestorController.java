package ma.startup.platform.investorservice.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.startup.platform.investorservice.dto.CreateInvestorRequest;
import ma.startup.platform.investorservice.dto.InvestorResponse;
import ma.startup.platform.investorservice.dto.StartupDetailResponse;
import ma.startup.platform.investorservice.dto.UpdateInvestorRequest;
import ma.startup.platform.investorservice.service.InvestorService;
import ma.startup.platform.investorservice.service.StartupDetailService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/investors")
@RequiredArgsConstructor
@Slf4j
public class InvestorController {

    private final InvestorService investorService;
    private final StartupDetailService startupDetailService;

    /**
     * POST /api/investors - Create investor profile
     */
    @PostMapping
    public ResponseEntity<?> createInvestor(
            @Valid @RequestBody CreateInvestorRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            log.info("POST /api/investors - Creating investor profile");
            InvestorResponse response = investorService.createInvestor(request, authHeader);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating investor: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    /**
     * GET /api/investors/me - Get my investor profile
     */
    @GetMapping("/me")
    public ResponseEntity<?> getMyInvestor(@RequestHeader("Authorization") String authHeader) {
        try {
            log.info("GET /api/investors/me - Fetching my investor profile");
            InvestorResponse response = investorService.getMyInvestor(authHeader);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching investor profile: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    /**
     * PUT /api/investors/me - Update my investor profile
     */
    @PutMapping("/me")
    public ResponseEntity<?> updateMyInvestor(
            @Valid @RequestBody UpdateInvestorRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            log.info("PUT /api/investors/me - Updating investor profile");
            InvestorResponse response = investorService.updateMyInvestor(request, authHeader);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating investor: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    /**
     * GET /api/investors - List all investors (paginated)
     */
    @GetMapping
    public ResponseEntity<?> getAllInvestors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            log.info("GET /api/investors - Fetching all investors (page: {}, size: {})", page, size);
            Pageable pageable = PageRequest.of(page, size);
            Page<InvestorResponse> response = investorService.getAllInvestors(pageable);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching investors: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    /**
     * GET /api/investors/{id} - Get investor by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getInvestorById(@PathVariable UUID id) {
        try {
            log.info("GET /api/investors/{} - Fetching investor details", id);
            InvestorResponse response = investorService.getInvestorById(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching investor {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    /**
     * GET /api/investors/search?secteur={secteur} - Search by sector
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchBySecteur(@RequestParam String secteur) {
        try {
            log.info("GET /api/investors/search?secteur={}", secteur);
            List<InvestorResponse> response = investorService.searchBySecteur(secteur);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error searching investors: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }
    @GetMapping("/startups/{startupId}/details")
    public ResponseEntity<?> getStartupDetails(
            @PathVariable UUID startupId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            log.info("GET /api/investors/startups/{}/details", startupId);
            StartupDetailResponse response = startupDetailService.getStartupDetails(startupId, authHeader);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching startup details: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }
}
