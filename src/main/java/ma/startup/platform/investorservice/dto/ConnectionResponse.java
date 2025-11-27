package ma.startup.platform.investorservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.startup.platform.investorservice.enums.ConnectionRequest;
import ma.startup.platform.investorservice.enums.ConnectionStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionResponse {

    private UUID id;
    private UUID startupId;
    private UUID investorId;
    private String message;
    private ConnectionStatus statut;
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;

    // Optional: include investor details
    private InvestorResponse investor;

    public static ConnectionResponse fromConnectionRequest(ConnectionRequest request) {
        return new ConnectionResponse(
                request.getId(),
                request.getStartupId(),
                request.getInvestorId(),
                request.getMessage(),
                request.getStatut(),
                request.getCreatedAt(),
                request.getRespondedAt(),
                null // investor details can be added separately
        );
    }
}
