package ma.startup.platform.investorservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FounderMemberDTO {
    private UUID id;
    private UUID startupId;
    private String nom;
    private String role;
    private String linkedIn;
    private String photo;
}
