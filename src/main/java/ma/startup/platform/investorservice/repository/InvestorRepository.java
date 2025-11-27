package ma.startup.platform.investorservice.repository;

import ma.startup.platform.investorservice.enums.InvestorType;
import ma.startup.platform.investorservice.model.Investor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvestorRepository extends JpaRepository<Investor, UUID> {

    Optional<Investor> findByUserId(UUID userId);

    List<Investor> findByType(InvestorType type);

    Page<Investor> findAll(Pageable pageable);

    @Query("SELECT i FROM Investor i WHERE i.secteursInterets LIKE %:secteur%")
    List<Investor> findBySecteur(@Param("secteur") String secteur);

    @Query("SELECT i FROM Investor i WHERE i.localisation = :localisation")
    List<Investor> findByLocalisation(@Param("localisation") String localisation);
}
