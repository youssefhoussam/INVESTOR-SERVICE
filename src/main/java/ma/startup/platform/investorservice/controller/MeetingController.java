package ma.startup.platform.investorservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.startup.platform.investorservice.dto.MeetingResponse;
import ma.startup.platform.investorservice.dto.ScheduleMeetingRequest;
import ma.startup.platform.investorservice.service.MeetingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
@Slf4j
public class MeetingController {

    private final MeetingService meetingService;

    /**
     * POST /api/meetings/schedule - Investor schedules a meeting
     */
    @PostMapping("/schedule")
    public ResponseEntity<?> scheduleMeeting(
            @Valid @RequestBody ScheduleMeetingRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            log.info("POST /api/meetings/schedule - Scheduling meeting");
            MeetingResponse response = meetingService.scheduleMeeting(request, authHeader);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error scheduling meeting: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    /**
     * GET /api/meetings/received - Get received meeting requests (startup view)
     */
    @GetMapping("/received")
    public ResponseEntity<?> getReceivedMeetings(@RequestHeader("Authorization") String authHeader) {
        try {
            log.info("GET /api/meetings/received - Fetching received meetings");
            List<MeetingResponse> meetings = meetingService.getReceivedMeetings(authHeader);
            return ResponseEntity.ok(meetings);
        } catch (Exception e) {
            log.error("Error fetching received meetings: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    /**
     * GET /api/meetings/sent - Get sent meeting requests (investor view)
     */
    @GetMapping("/sent")
    public ResponseEntity<?> getSentMeetings(@RequestHeader("Authorization") String authHeader) {
        try {
            log.info("GET /api/meetings/sent - Fetching sent meetings");
            List<MeetingResponse> meetings = meetingService.getSentMeetings(authHeader);
            return ResponseEntity.ok(meetings);
        } catch (Exception e) {
            log.error("Error fetching sent meetings: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    /**
     * PUT /api/meetings/{id}/accept - Accept meeting (startup)
     */
    @PutMapping("/{id}/accept")
    public ResponseEntity<?> acceptMeeting(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            log.info("PUT /api/meetings/{}/accept - Accepting meeting", id);
            MeetingResponse response = meetingService.acceptMeeting(id, authHeader);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error accepting meeting: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    /**
     * PUT /api/meetings/{id}/reject - Reject meeting (startup)
     */
    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectMeeting(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            log.info("PUT /api/meetings/{}/reject - Rejecting meeting", id);
            MeetingResponse response = meetingService.rejectMeeting(id, authHeader);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error rejecting meeting: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    /**
     * PUT /api/meetings/{id}/reschedule - Reschedule meeting
     */
    @PutMapping("/{id}/reschedule")
    public ResponseEntity<?> rescheduleMeeting(
            @PathVariable UUID id,
            @Valid @RequestBody ScheduleMeetingRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            log.info("PUT /api/meetings/{}/reschedule - Rescheduling meeting", id);
            MeetingResponse response = meetingService.rescheduleMeeting(id, request, authHeader);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error rescheduling meeting: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    /**
     * GET /api/meetings/upcoming - Get upcoming meetings
     */
    @GetMapping("/upcoming")
    public ResponseEntity<?> getUpcomingMeetings(@RequestHeader("Authorization") String authHeader) {
        try {
            log.info("GET /api/meetings/upcoming - Fetching upcoming meetings");
            List<MeetingResponse> meetings = meetingService.getUpcomingMeetings(authHeader);
            return ResponseEntity.ok(meetings);
        } catch (Exception e) {
            log.error("Error fetching upcoming meetings: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    /**
     * DELETE /api/meetings/{id}/cancel - Cancel meeting
     */
    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<?> cancelMeeting(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            log.info("DELETE /api/meetings/{}/cancel - Cancelling meeting", id);
            MeetingResponse response = meetingService.cancelMeeting(id, authHeader);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error cancelling meeting: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }
}
