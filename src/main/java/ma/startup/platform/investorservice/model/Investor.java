package ma.startup.platform.investorservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.startup.platform.investorservice.enums.InvestorType;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "investors", schema = "investor_schema")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Investor {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvestorType type;

    @Column(name = "secteurs_interets", columnDefinition = "TEXT")
    private String secteursInterets; // JSON array as String: ["FinTech","EdTech"]

    @Column(name = "montant_min", precision = 15, scale = 2)
    private BigDecimal montantMin;

    @Column(name = "montant_max", precision = 15, scale = 2)
    private BigDecimal montantMax;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String localisation;

    @Column(columnDefinition = "TEXT")
    private String portfolio;

    @Column(name = "site_web")
    private String siteWeb;

    private String email;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
