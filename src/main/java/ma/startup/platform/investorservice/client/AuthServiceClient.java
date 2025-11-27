package ma.startup.platform.investorservice.client;

import ma.startup.platform.investorservice.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.UUID;

@FeignClient(name = "auth-service", url = "${auth.service.url}")
public interface AuthServiceClient {

    @GetMapping("/api/users/{id}")
    UserDTO getUser(@PathVariable("id") UUID id, @RequestHeader("Authorization") String token);

    @GetMapping("/api/users/me")
    UserDTO getCurrentUser(@RequestHeader("Authorization") String token);
}
