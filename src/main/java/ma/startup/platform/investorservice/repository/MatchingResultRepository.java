package ma.startup.platform.investorservice.repository;

import ma.startup.platform.investorservice.model.MatchingResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MatchingResultRepository extends JpaRepository<MatchingResult, UUID> {

    List<MatchingResult> findByStartupIdOrderByScoreDesc(UUID startupId);

    List<MatchingResult> findByInvestorIdOrderByScoreDesc(UUID investorId);

    Optional<MatchingResult> findByStartupIdAndInvestorId(UUID startupId, UUID investorId);

    @Query("SELECT mr FROM MatchingResult mr WHERE mr.startupId = :startupId AND mr.score >= :minScore ORDER BY mr.score DESC")
    List<MatchingResult> findTopMatchesForStartup(@Param("startupId") UUID startupId, @Param("minScore") Integer minScore);

    void deleteByStartupId(UUID startupId);

    void deleteByInvestorId(UUID investorId);
}
