package ma.startup.platform.investorservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.startup.platform.investorservice.enums.MeetingStatus;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "meetings", schema = "investor_schema")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "connection_id", nullable = false)
    private UUID connectionId;

    @Column(name = "investor_id", nullable = false)
    private UUID investorId;

    @Column(name = "startup_id", nullable = false)
    private UUID startupId;

    @Column(name = "meeting_date", nullable = false)
    private LocalDateTime meetingDate;

    @Column(name = "meeting_place", length = 500)
    private String meetingPlace;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MeetingStatus status = MeetingStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;
}