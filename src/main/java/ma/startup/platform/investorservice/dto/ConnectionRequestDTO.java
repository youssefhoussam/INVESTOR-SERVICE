package ma.startup.platform.investorservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionRequestDTO {

    @NotNull(message = "L'ID de l'investisseur est obligatoire")
    private UUID investorId;

    private String message;
}
