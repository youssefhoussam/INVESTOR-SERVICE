package ma.startup.platform.investorservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.startup.platform.investorservice.client.AuthServiceClient;
import ma.startup.platform.investorservice.dto.CreateInvestorRequest;
import ma.startup.platform.investorservice.dto.InvestorResponse;
import ma.startup.platform.investorservice.dto.UpdateInvestorRequest;
import ma.startup.platform.investorservice.dto.UserDTO;
import ma.startup.platform.investorservice.model.Investor;
import ma.startup.platform.investorservice.repository.InvestorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvestorService {

    private final InvestorRepository investorRepository;
    private final AuthServiceClient authServiceClient;

    @Transactional
    public InvestorResponse createInvestor(CreateInvestorRequest request, String authHeader) {
        log.info("Creating investor profile");

        // Get current user from auth service
        UserDTO user = authServiceClient.getCurrentUser(authHeader);

        // Check if user already has an investor profile
        if (investorRepository.findByUserId(user.getId()).isPresent()) {
            throw new RuntimeException("Un profil investisseur existe déjà pour cet utilisateur");
        }

        // Check if user has INVESTOR role
        if (!"INVESTOR".equals(user.getRole())) {
            throw new RuntimeException("Seuls les utilisateurs avec le rôle INVESTOR peuvent créer un profil investisseur");
        }

        Investor investor = new Investor();
        investor.setUserId(user.getId());
        investor.setNom(request.getNom());
        investor.setType(request.getType());
        investor.setSecteursInterets(request.getSecteursInterets());
        investor.setMontantMin(request.getMontantMin());
        investor.setMontantMax(request.getMontantMax());
        investor.setDescription(request.getDescription());
        investor.setLocalisation(request.getLocalisation());
        investor.setPortfolio(request.getPortfolio());
        investor.setSiteWeb(request.getSiteWeb());
        investor.setEmail(request.getEmail());

        Investor saved = investorRepository.save(investor);
        log.info("Investor profile created with ID: {}", saved.getId());

        return InvestorResponse.fromInvestor(saved);
    }

    public InvestorResponse getMyInvestor(String authHeader) {
        UserDTO user = authServiceClient.getCurrentUser(authHeader);

        Investor investor = investorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Profil investisseur non trouvé"));

        return InvestorResponse.fromInvestor(investor);
    }

    @Transactional
    public InvestorResponse updateMyInvestor(UpdateInvestorRequest request, String authHeader) {
        log.info("Updating investor profile");

        UserDTO user = authServiceClient.getCurrentUser(authHeader);

        Investor investor = investorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Profil investisseur non trouvé"));

        if (request.getNom() != null) investor.setNom(request.getNom());
        if (request.getType() != null) investor.setType(request.getType());
        if (request.getSecteursInterets() != null) investor.setSecteursInterets(request.getSecteursInterets());
        if (request.getMontantMin() != null) investor.setMontantMin(request.getMontantMin());
        if (request.getMontantMax() != null) investor.setMontantMax(request.getMontantMax());
        if (request.getDescription() != null) investor.setDescription(request.getDescription());
        if (request.getLocalisation() != null) investor.setLocalisation(request.getLocalisation());
        if (request.getPortfolio() != null) investor.setPortfolio(request.getPortfolio());
        if (request.getSiteWeb() != null) investor.setSiteWeb(request.getSiteWeb());
        if (request.getEmail() != null) investor.setEmail(request.getEmail());

        Investor updated = investorRepository.save(investor);
        log.info("Investor profile updated: {}", updated.getId());

        return InvestorResponse.fromInvestor(updated);
    }

    public Page<InvestorResponse> getAllInvestors(Pageable pageable) {
        return investorRepository.findAll(pageable)
                .map(InvestorResponse::fromInvestor);
    }

    public InvestorResponse getInvestorById(UUID id) {
        Investor investor = investorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Investisseur non trouvé"));

        return InvestorResponse.fromInvestor(investor);
    }

    public List<InvestorResponse> searchBySecteur(String secteur) {
        return investorRepository.findBySecteur(secteur).stream()
                .map(InvestorResponse::fromInvestor)
                .collect(Collectors.toList());
    }
}
