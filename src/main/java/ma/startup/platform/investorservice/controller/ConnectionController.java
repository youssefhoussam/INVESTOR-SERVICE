package ma.startup.platform.investorservice.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.startup.platform.investorservice.dto.ConnectionRequestDTO;
import ma.startup.platform.investorservice.dto.ConnectionResponse;
import ma.startup.platform.investorservice.service.ConnectionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/connections")
@RequiredArgsConstructor
@Slf4j
public class ConnectionController {

    private final ConnectionService connectionService;

    /**
     * POST /api/connections/request - Request connection with investor
     */
    @PostMapping("/request")
    public ResponseEntity<?> requestConnection(
            @Valid @RequestBody ConnectionRequestDTO request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            log.info("POST /api/connections/request - Requesting connection");
            ConnectionResponse response = connectionService.requestConnection(request, authHeader);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error requesting connection: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    /**
     * GET /api/connections/received - Get received connection requests (investor view)
     */
    @GetMapping("/received")
    public ResponseEntity<?> getReceivedRequests(@RequestHeader("Authorization") String authHeader) {
        try {
            log.info("GET /api/connections/received - Fetching received requests");
            List<ConnectionResponse> requests = connectionService.getReceivedRequests(authHeader);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            log.error("Error fetching received requests: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    /**
     * GET /api/connections/sent - Get sent connection requests (startup view)
     */
    @GetMapping("/sent")
    public ResponseEntity<?> getSentRequests(@RequestHeader("Authorization") String authHeader) {
        try {
            log.info("GET /api/connections/sent - Fetching sent requests");
            List<ConnectionResponse> requests = connectionService.getSentRequests(authHeader);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            log.error("Error fetching sent requests: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    /**
     * PUT /api/connections/{id}/accept - Accept connection request
     */
    @PutMapping("/{id}/accept")
    public ResponseEntity<?> acceptConnection(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            log.info("PUT /api/connections/{}/accept - Accepting connection", id);
            ConnectionResponse response = connectionService.acceptConnection(id, authHeader);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error accepting connection: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    /**
     * PUT /api/connections/{id}/reject - Reject connection request
     */
    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectConnection(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            log.info("PUT /api/connections/{}/reject - Rejecting connection", id);
            ConnectionResponse response = connectionService.rejectConnection(id, authHeader);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error rejecting connection: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    /**
     * GET /api/connections/active - Get all active connections
     */
    @GetMapping("/active")
    public ResponseEntity<?> getActiveConnections(@RequestHeader("Authorization") String authHeader) {
        try {
            log.info("GET /api/connections/active - Fetching active connections");
            List<ConnectionResponse> connections = connectionService.getActiveConnections(authHeader);
            return ResponseEntity.ok(connections);
        } catch (Exception e) {
            log.error("Error fetching active connections: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }
}
