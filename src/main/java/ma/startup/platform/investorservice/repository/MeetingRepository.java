package ma.startup.platform.investorservice.repository;

import ma.startup.platform.investorservice.enums.MeetingStatus;
import ma.startup.platform.investorservice.model.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, UUID> {

    List<Meeting> findByInvestorIdOrderByMeetingDateDesc(UUID investorId);

    List<Meeting> findByStartupIdOrderByMeetingDateDesc(UUID startupId);

    @Query("SELECT m FROM Meeting m WHERE m.investorId = :investorId AND m.status = :status")
    List<Meeting> findByInvestorIdAndStatut(@Param("investorId") UUID investorId,
                                            @Param("status") MeetingStatus status);

    @Query("SELECT m FROM Meeting m WHERE m.startupId = :startupId AND m.status = :status")
    List<Meeting> findByStartupIdAndStatut(@Param("startupId") UUID startupId,
                                           @Param("status") MeetingStatus status);

    List<Meeting> findByConnectionId(UUID connectionId);

    @Query("SELECT m FROM Meeting m WHERE m.investorId = :investorId AND m.meetingDate >= :now AND m.status = 'ACCEPTED' ORDER BY m.meetingDate ASC")
    List<Meeting> findUpcomingMeetingsForInvestor(@Param("investorId") UUID investorId,
                                                  @Param("now") LocalDateTime now);

    @Query("SELECT m FROM Meeting m WHERE m.startupId = :startupId AND m.meetingDate >= :now AND m.status = 'ACCEPTED' ORDER BY m.meetingDate ASC")
    List<Meeting> findUpcomingMeetingsForStartup(@Param("startupId") UUID startupId,
                                                 @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(m) > 0 FROM Meeting m WHERE m.connectionId = :connectionId AND m.status = :status")
    boolean existsByConnectionIdAndStatut(@Param("connectionId") UUID connectionId,
                                          @Param("status") MeetingStatus status);
}
