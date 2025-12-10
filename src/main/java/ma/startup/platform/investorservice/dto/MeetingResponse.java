package ma.startup.platform.investorservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.startup.platform.investorservice.enums.MeetingStatus;
import ma.startup.platform.investorservice.model.Meeting;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeetingResponse {

    private UUID id;
    private UUID connectionId;
    private UUID investorId;
    private UUID startupId;
    private LocalDateTime meetingDate;
    private String meetingPlace;
    private String message;
    private MeetingStatus statut;
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;

    // Optional: Include investor/startup details
    private InvestorInfo investor;
    private StartupInfo startup;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvestorInfo {
        private UUID id;
        private String nom;
        private String type;
        private String email;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StartupInfo {
        private UUID id;
        private String nom;
        private String secteur;
    }

    public static MeetingResponse fromMeeting(Meeting meeting) {
        return new MeetingResponse(
                meeting.getId(),
                meeting.getConnectionId(),
                meeting.getInvestorId(),
                meeting.getStartupId(),
                meeting.getMeetingDate(),
                meeting.getMeetingPlace(),
                meeting.getMessage(),
                meeting.getStatus(),
                meeting.getCreatedAt(),
                meeting.getRespondedAt(),
                null,
                null
        );
    }
}
