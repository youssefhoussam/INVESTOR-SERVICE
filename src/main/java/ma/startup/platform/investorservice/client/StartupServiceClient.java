package ma.startup.platform.investorservice.client;

import ma.startup.platform.investorservice.dto.FounderMemberDTO;
import ma.startup.platform.investorservice.dto.MilestoneDTO;
import ma.startup.platform.investorservice.dto.StartupDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "startup-service", url = "${startup.service.url}")
public interface StartupServiceClient {

    @GetMapping("/api/startups/{id}")
    StartupDTO getStartupById(@PathVariable("id") UUID id, @RequestHeader("Authorization") String token);

    @GetMapping("/api/startups/me")
    StartupDTO getMyStartup(@RequestHeader("Authorization") String token);

    @GetMapping("/api/startups")
    List<StartupDTO> getAllStartups(@RequestHeader("Authorization") String token);

    @GetMapping("/api/startups/search")
    List<StartupDTO> searchBySecteur(@RequestParam("secteur") String secteur, @RequestHeader("Authorization") String token);

    // NEW: Get team members
    @GetMapping("/api/team/startup/{startupId}")
    List<FounderMemberDTO> getTeamByStartupId(@PathVariable("startupId") UUID startupId, @RequestHeader("Authorization") String token);

    // NEW: Get milestones
    @GetMapping("/api/milestones/startup/{startupId}")
    List<MilestoneDTO> getMilestonesByStartupId(@PathVariable("startupId") UUID startupId, @RequestHeader("Authorization") String token);

    @GetMapping("/api/startups/user/{userId}")
    StartupDTO getStartupByUserId(
            @PathVariable("userId") UUID userId,
            @RequestHeader("Authorization") String token
    );


}