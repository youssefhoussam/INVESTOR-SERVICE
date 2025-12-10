package ma.startup.platform.investorservice.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleMeetingRequest {

    @NotNull(message = "L'ID de la connexion est obligatoire")
    private UUID connectionId;

    @NotNull(message = "La date de réunion est obligatoire")
    @Future(message = "La date de réunion doit être dans le futur")
    private LocalDateTime meetingDate;

    @NotNull(message = "Le lieu de réunion est obligatoire")
    private String meetingPlace;

    private String message;
}
