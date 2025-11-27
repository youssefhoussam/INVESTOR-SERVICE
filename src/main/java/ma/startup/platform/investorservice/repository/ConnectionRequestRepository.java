package ma.startup.platform.investorservice.repository;

import ma.startup.platform.investorservice.enums.ConnectionRequest;
import ma.startup.platform.investorservice.enums.ConnectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConnectionRequestRepository extends JpaRepository<ConnectionRequest, UUID> {

    List<ConnectionRequest> findByStartupIdOrderByCreatedAtDesc(UUID startupId);

    List<ConnectionRequest> findByInvestorIdOrderByCreatedAtDesc(UUID investorId);

    List<ConnectionRequest> findByStartupIdAndStatut(UUID startupId, ConnectionStatus statut);

    List<ConnectionRequest> findByInvestorIdAndStatut(UUID investorId, ConnectionStatus statut);

    Optional<ConnectionRequest> findByStartupIdAndInvestorIdAndStatut(UUID startupId, UUID investorId, ConnectionStatus statut);

    @Query("SELECT cr FROM ConnectionRequest cr WHERE cr.startupId = :startupId AND cr.statut = 'ACCEPTED'")
    List<ConnectionRequest> findActiveConnectionsForStartup(@Param("startupId") UUID startupId);

    @Query("SELECT cr FROM ConnectionRequest cr WHERE cr.investorId = :investorId AND cr.statut = 'ACCEPTED'")
    List<ConnectionRequest> findActiveConnectionsForInvestor(@Param("investorId") UUID investorId);

    boolean existsByStartupIdAndInvestorIdAndStatut(UUID startupId, UUID investorId, ConnectionStatus statut);
}
