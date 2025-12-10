package ma.startup.platform.investorservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.startup.platform.investorservice.client.AuthServiceClient;
import ma.startup.platform.investorservice.client.StartupServiceClient;
import ma.startup.platform.investorservice.dto.MeetingResponse;
import ma.startup.platform.investorservice.dto.ScheduleMeetingRequest;
import ma.startup.platform.investorservice.dto.StartupDTO;
import ma.startup.platform.investorservice.dto.UserDTO;
import ma.startup.platform.investorservice.enums.ConnectionRequest;
import ma.startup.platform.investorservice.enums.ConnectionStatus;
import ma.startup.platform.investorservice.enums.MeetingStatus;
import ma.startup.platform.investorservice.model.Investor;
import ma.startup.platform.investorservice.model.Meeting;
import ma.startup.platform.investorservice.repository.ConnectionRequestRepository;
import ma.startup.platform.investorservice.repository.InvestorRepository;
import ma.startup.platform.investorservice.repository.MeetingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final ConnectionRequestRepository connectionRequestRepository;
    private final InvestorRepository investorRepository;
    private final AuthServiceClient authServiceClient;
    private final StartupServiceClient startupServiceClient;

    /**
     * Investor schedules a meeting with a startup
     */
    @Transactional
    public MeetingResponse scheduleMeeting(ScheduleMeetingRequest request, String authHeader) {
        log.info("Scheduling meeting for connection: {}", request.getConnectionId());

        // Get current user (must be investor)
        UserDTO user = authServiceClient.getCurrentUser(authHeader);
        if (!"INVESTOR".equals(user.getRole())) {
            throw new RuntimeException("Seuls les investisseurs peuvent proposer des réunions");
        }

        // Get investor profile
        Investor investor = investorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Profil investisseur non trouvé"));

        // Verify connection exists and is accepted
        ConnectionRequest connection = connectionRequestRepository.findById(request.getConnectionId())
                .orElseThrow(() -> new RuntimeException("Connexion non trouvée"));

        if (connection.getStatut() != ConnectionStatus.ACCEPTED) {
            throw new RuntimeException("La connexion doit être acceptée avant de proposer une réunion");
        }

        // Verify this investor is part of the connection
        if (!connection.getInvestorId().equals(investor.getId())) {
            throw new RuntimeException("Cette connexion ne vous appartient pas");
        }

        // Check if there's already a pending meeting for this connection
        boolean hasPendingMeeting = meetingRepository.existsByConnectionIdAndStatut(
                request.getConnectionId(), MeetingStatus.PENDING);

        if (hasPendingMeeting) {
            throw new RuntimeException("Une réunion est déjà en attente pour cette connexion");
        }

        // Create meeting
        Meeting meeting = new Meeting();
        meeting.setConnectionId(request.getConnectionId());
        meeting.setInvestorId(investor.getId());
        meeting.setStartupId(connection.getStartupId());
        meeting.setMeetingDate(request.getMeetingDate());
        meeting.setMeetingPlace(request.getMeetingPlace());
        meeting.setMessage(request.getMessage());
        meeting.setStatus(MeetingStatus.PENDING);

        Meeting saved = meetingRepository.save(meeting);
        log.info("Meeting scheduled with ID: {}", saved.getId());

        // Build response with investor details
        MeetingResponse response = MeetingResponse.fromMeeting(saved);
        response.setInvestor(new MeetingResponse.InvestorInfo(
                investor.getId(),
                investor.getNom(),
                investor.getType().name(),
                investor.getEmail()
        ));

        return response;
    }

    /**
     * Get meetings received by startup (pending approval)
     */
    public List<MeetingResponse> getReceivedMeetings(String authHeader) {
        log.info("Fetching received meeting requests");

        UserDTO user = authServiceClient.getCurrentUser(authHeader);

        if (!"STARTUP".equals(user.getRole())) {
            throw new RuntimeException("Seules les start-ups peuvent consulter les réunions reçues");
        }

        // Get startup profile
        StartupDTO startup;
        try {
            startup = startupServiceClient.getMyStartup(authHeader);
        } catch (Exception e) {
            throw new RuntimeException("Profil startup non trouvé");
        }

        List<Meeting> meetings = meetingRepository.findByStartupIdOrderByMeetingDateDesc(startup.getId());

        return meetings.stream()
                .map(meeting -> {
                    MeetingResponse response = MeetingResponse.fromMeeting(meeting);
                    // Add investor details
                    investorRepository.findById(meeting.getInvestorId()).ifPresent(inv ->
                            response.setInvestor(new MeetingResponse.InvestorInfo(
                                    inv.getId(),
                                    inv.getNom(),
                                    inv.getType().name(),
                                    inv.getEmail()
                            ))
                    );
                    return response;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get meetings sent by investor
     */
    public List<MeetingResponse> getSentMeetings(String authHeader) {
        log.info("Fetching sent meeting requests");

        UserDTO user = authServiceClient.getCurrentUser(authHeader);

        if (!"INVESTOR".equals(user.getRole())) {
            throw new RuntimeException("Seuls les investisseurs peuvent consulter leurs réunions envoyées");
        }

        // Get investor profile
        Investor investor = investorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Profil investisseur non trouvé"));

        List<Meeting> meetings = meetingRepository.findByInvestorIdOrderByMeetingDateDesc(investor.getId());

        return meetings.stream()
                .map(meeting -> {
                    MeetingResponse response = MeetingResponse.fromMeeting(meeting);
                    // Add startup details
                    try {
                        StartupDTO startup = startupServiceClient.getStartupById(meeting.getStartupId(), authHeader);
                        response.setStartup(new MeetingResponse.StartupInfo(
                                startup.getId(),
                                startup.getNom(),
                                startup.getSecteur()
                        ));
                    } catch (Exception e) {
                        log.warn("Could not fetch startup details: {}", e.getMessage());
                    }
                    return response;
                })
                .collect(Collectors.toList());
    }

    /**
     * Startup accepts a meeting
     */
    @Transactional
    public MeetingResponse acceptMeeting(UUID meetingId, String authHeader) {
        log.info("Accepting meeting: {}", meetingId);

        UserDTO user = authServiceClient.getCurrentUser(authHeader);

        if (!"STARTUP".equals(user.getRole())) {
            throw new RuntimeException("Seules les start-ups peuvent accepter des réunions");
        }

        // Get startup profile
        StartupDTO startup;
        try {
            startup = startupServiceClient.getMyStartup(authHeader);
        } catch (Exception e) {
            throw new RuntimeException("Profil startup non trouvé");
        }

        // Get meeting
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new RuntimeException("Réunion non trouvée"));

        // Verify this meeting belongs to this startup
        if (!meeting.getStartupId().equals(startup.getId())) {
            throw new RuntimeException("Cette réunion ne vous appartient pas");
        }

        // Verify status is PENDING
        if (meeting.getStatus() != MeetingStatus.PENDING) {
            throw new RuntimeException("Cette réunion a déjà été traitée");
        }

        // Accept the meeting
        meeting.setStatus(MeetingStatus.ACCEPTED);
        meeting.setRespondedAt(LocalDateTime.now());

        Meeting updated = meetingRepository.save(meeting);
        log.info("Meeting accepted: {}", meetingId);

        return MeetingResponse.fromMeeting(updated);
    }

    /**
     * Startup rejects a meeting
     */
    @Transactional
    public MeetingResponse rejectMeeting(UUID meetingId, String authHeader) {
        log.info("Rejecting meeting: {}", meetingId);

        UserDTO user = authServiceClient.getCurrentUser(authHeader);

        if (!"STARTUP".equals(user.getRole())) {
            throw new RuntimeException("Seules les start-ups peuvent rejeter des réunions");
        }

        // Get startup profile
        StartupDTO startup;
        try {
            startup = startupServiceClient.getMyStartup(authHeader);
        } catch (Exception e) {
            throw new RuntimeException("Profil startup non trouvé");
        }

        // Get meeting
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new RuntimeException("Réunion non trouvée"));

        // Verify this meeting belongs to this startup
        if (!meeting.getStartupId().equals(startup.getId())) {
            throw new RuntimeException("Cette réunion ne vous appartient pas");
        }

        // Verify status is PENDING
        if (meeting.getStatus() != MeetingStatus.PENDING) {
            throw new RuntimeException("Cette réunion a déjà été traitée");
        }

        // Reject the meeting
        meeting.setStatus(MeetingStatus.REJECTED);
        meeting.setRespondedAt(LocalDateTime.now());

        Meeting updated = meetingRepository.save(meeting);
        log.info("Meeting rejected: {}", meetingId);

        return MeetingResponse.fromMeeting(updated);
    }

    /**
     * Reschedule a meeting (propose new time)
     */
    @Transactional
    public MeetingResponse rescheduleMeeting(UUID meetingId, ScheduleMeetingRequest request, String authHeader) {
        log.info("Rescheduling meeting: {}", meetingId);

        UserDTO user = authServiceClient.getCurrentUser(authHeader);

        // Get meeting
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new RuntimeException("Réunion non trouvée"));

        // Verify user is part of this meeting (either investor or startup)
        boolean isInvestor = false;
        boolean isStartup = false;

        if ("INVESTOR".equals(user.getRole())) {
            Investor investor = investorRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Profil investisseur non trouvé"));
            isInvestor = meeting.getInvestorId().equals(investor.getId());
        } else if ("STARTUP".equals(user.getRole())) {
            StartupDTO startup = startupServiceClient.getMyStartup(authHeader);
            isStartup = meeting.getStartupId().equals(startup.getId());
        }

        if (!isInvestor && !isStartup) {
            throw new RuntimeException("Vous n'êtes pas autorisé à modifier cette réunion");
        }

        // Update meeting details
        meeting.setMeetingDate(request.getMeetingDate());
        meeting.setMeetingPlace(request.getMeetingPlace());
        if (request.getMessage() != null) {
            meeting.setMessage(request.getMessage());
        }
        meeting.setStatus(MeetingStatus.PENDING); // Reset to pending for approval
        meeting.setRespondedAt(null);

        Meeting updated = meetingRepository.save(meeting);
        log.info("Meeting rescheduled: {}", meetingId);

        return MeetingResponse.fromMeeting(updated);
    }

    /**
     * Get upcoming meetings for current user
     */
    public List<MeetingResponse> getUpcomingMeetings(String authHeader) {
        log.info("Fetching upcoming meetings");

        UserDTO user = authServiceClient.getCurrentUser(authHeader);
        LocalDateTime now = LocalDateTime.now();

        List<Meeting> meetings;

        if ("INVESTOR".equals(user.getRole())) {
            Investor investor = investorRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Profil investisseur non trouvé"));
            meetings = meetingRepository.findUpcomingMeetingsForInvestor(investor.getId(), now);
        } else if ("STARTUP".equals(user.getRole())) {
            StartupDTO startup = startupServiceClient.getMyStartup(authHeader);
            meetings = meetingRepository.findUpcomingMeetingsForStartup(startup.getId(), now);
        } else {
            throw new RuntimeException("Type d'utilisateur non valide");
        }

        return meetings.stream()
                .map(MeetingResponse::fromMeeting)
                .collect(Collectors.toList());
    }

    /**
     * Cancel a meeting
     */
    @Transactional
    public MeetingResponse cancelMeeting(UUID meetingId, String authHeader) {
        log.info("Cancelling meeting: {}", meetingId);

        UserDTO user = authServiceClient.getCurrentUser(authHeader);

        // Get meeting
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new RuntimeException("Réunion non trouvée"));

        // Verify user is part of this meeting
        boolean canCancel = false;

        if ("INVESTOR".equals(user.getRole())) {
            Investor investor = investorRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Profil investisseur non trouvé"));
            canCancel = meeting.getInvestorId().equals(investor.getId());
        } else if ("STARTUP".equals(user.getRole())) {
            StartupDTO startup = startupServiceClient.getMyStartup(authHeader);
            canCancel = meeting.getStartupId().equals(startup.getId());
        }

        if (!canCancel) {
            throw new RuntimeException("Vous n'êtes pas autorisé à annuler cette réunion");
        }

        // Can only cancel PENDING or ACCEPTED meetings
        if (meeting.getStatus() != MeetingStatus.PENDING && meeting.getStatus() != MeetingStatus.ACCEPTED) {
            throw new RuntimeException("Cette réunion ne peut pas être annulée");
        }

        // Cancel the meeting
        meeting.setStatus(MeetingStatus.CANCELLED);
        meeting.setRespondedAt(LocalDateTime.now());

        Meeting updated = meetingRepository.save(meeting);
        log.info("Meeting cancelled: {}", meetingId);

        return MeetingResponse.fromMeeting(updated);
    }
}
