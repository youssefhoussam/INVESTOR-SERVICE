package ma.startup.platform.investorservice.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.startup.platform.investorservice.client.AuthServiceClient;
import ma.startup.platform.investorservice.client.StartupServiceClient;
import ma.startup.platform.investorservice.dto.*;
import ma.startup.platform.investorservice.enums.ConnectionRequest;
import ma.startup.platform.investorservice.enums.ConnectionStatus;
import ma.startup.platform.investorservice.model.Investor;
import ma.startup.platform.investorservice.repository.ConnectionRequestRepository;
import ma.startup.platform.investorservice.repository.InvestorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConnectionService {

    private final ConnectionRequestRepository connectionRequestRepository;
    private final InvestorRepository investorRepository;
    private final AuthServiceClient authServiceClient;
    private final StartupServiceClient startupServiceClient;

    /**
     * Startup requests connection with an investor
     */
    @Transactional
    public ConnectionResponse requestConnection(ConnectionRequestDTO request, String authHeader) {
        log.info("Creating connection request to investor: {}", request.getInvestorId());

        // Get current user (must be a startup)
        UserDTO user = authServiceClient.getCurrentUser(authHeader);

        // Verify user has startup role
        if (!"STARTUP".equals(user.getRole())) {
            throw new RuntimeException("Seules les start-ups peuvent demander des connexions");
        }

        // Get startup profile
        // Get startup profile
        StartupDTO startup;
        try {
            startup = startupServiceClient.getStartupByUserId(user.getId(), authHeader);
        } catch (Exception e) {
            throw new RuntimeException("Profil startup non trouvé");
        }

        // Verify investor exists
        Investor investor = investorRepository.findById(request.getInvestorId())
                .orElseThrow(() -> new RuntimeException("Investisseur non trouvé"));

        // Check if connection already exists
        boolean alreadyExists = connectionRequestRepository.existsByStartupIdAndInvestorIdAndStatut(
                startup.getId(),
                request.getInvestorId(),
                ConnectionStatus.PENDING
        );

        if (alreadyExists) {
            throw new RuntimeException("Une demande de connexion est déjà en attente pour cet investisseur");
        }

        // Create connection request
        ConnectionRequest connectionRequest = new ConnectionRequest();
        connectionRequest.setStartupId(startup.getId());
        connectionRequest.setInvestorId(request.getInvestorId());
        connectionRequest.setMessage(request.getMessage());
        connectionRequest.setStatut(ConnectionStatus.PENDING);

        ConnectionRequest saved = connectionRequestRepository.save(connectionRequest);
        log.info("Connection request created with ID: {}", saved.getId());

        // Build response with investor details
        ConnectionResponse response = ConnectionResponse.fromConnectionRequest(saved);
        response.setInvestor(InvestorResponse.fromInvestor(investor));

        return response;
    }

    /**
     * Get all connection requests received by an investor
     */
    public List<ConnectionResponse> getReceivedRequests(String authHeader) {
        log.info("Fetching received connection requests");

        UserDTO user = authServiceClient.getCurrentUser(authHeader);

        // Verify user has investor role
        if (!"INVESTOR".equals(user.getRole())) {
            throw new RuntimeException("Seuls les investisseurs peuvent consulter les demandes reçues");
        }

        // Get investor profile
        Investor investor = investorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Profil investisseur non trouvé"));

        List<ConnectionRequest> requests = connectionRequestRepository
                .findByInvestorIdOrderByCreatedAtDesc(investor.getId());

        return requests.stream()
                .map(ConnectionResponse::fromConnectionRequest)
                .collect(Collectors.toList());
    }

    /**
     * Get all connection requests sent by a startup
     */
    public List<ConnectionResponse> getSentRequests(String authHeader) {
        log.info("Fetching sent connection requests");

        UserDTO user = authServiceClient.getCurrentUser(authHeader);

        // Verify user has startup role
        if (!"STARTUP".equals(user.getRole())) {
            throw new RuntimeException("Seules les start-ups peuvent consulter leurs demandes envoyées");
        }

        // Get startup profile
        // Get startup profile
        StartupDTO startup;
        try {
            startup = startupServiceClient.getStartupByUserId(user.getId(), authHeader);
        } catch (Exception e) {
            throw new RuntimeException("Profil startup non trouvé");
        }

        List<ConnectionRequest> requests = connectionRequestRepository
                .findByStartupIdOrderByCreatedAtDesc(startup.getId());

        return requests.stream()
                .map(req -> {
                    ConnectionResponse response = ConnectionResponse.fromConnectionRequest(req);
                    // Optionally add investor details
                    investorRepository.findById(req.getInvestorId()).ifPresent(inv ->
                            response.setInvestor(InvestorResponse.fromInvestor(inv))
                    );
                    return response;
                })
                .collect(Collectors.toList());
    }

    /**
     * Investor accepts a connection request
     */
    @Transactional
    public ConnectionResponse acceptConnection(UUID requestId, String authHeader) {
        log.info("Accepting connection request: {}", requestId);

        UserDTO user = authServiceClient.getCurrentUser(authHeader);

        // Verify user has investor role
        if (!"INVESTOR".equals(user.getRole())) {
            throw new RuntimeException("Seuls les investisseurs peuvent accepter des demandes");
        }

        // Get investor profile
        Investor investor = investorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Profil investisseur non trouvé"));

        // Get connection request
        ConnectionRequest request = connectionRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Demande de connexion non trouvée"));

        // Verify this request belongs to this investor
        if (!request.getInvestorId().equals(investor.getId())) {
            throw new RuntimeException("Cette demande ne vous appartient pas");
        }

        // Verify status is PENDING
        if (request.getStatut() != ConnectionStatus.PENDING) {
            throw new RuntimeException("Cette demande a déjà été traitée");
        }

        // Accept the request
        request.setStatut(ConnectionStatus.ACCEPTED);
        request.setRespondedAt(LocalDateTime.now());

        ConnectionRequest updated = connectionRequestRepository.save(request);
        log.info("Connection request accepted: {}", requestId);

        return ConnectionResponse.fromConnectionRequest(updated);
    }

    /**
     * Investor rejects a connection request
     */
    @Transactional
    public ConnectionResponse rejectConnection(UUID requestId, String authHeader) {
        log.info("Rejecting connection request: {}", requestId);

        UserDTO user = authServiceClient.getCurrentUser(authHeader);

        // Verify user has investor role
        if (!"INVESTOR".equals(user.getRole())) {
            throw new RuntimeException("Seuls les investisseurs peuvent rejeter des demandes");
        }

        // Get investor profile
        Investor investor = investorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Profil investisseur non trouvé"));

        // Get connection request
        ConnectionRequest request = connectionRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Demande de connexion non trouvée"));

        // Verify this request belongs to this investor
        if (!request.getInvestorId().equals(investor.getId())) {
            throw new RuntimeException("Cette demande ne vous appartient pas");
        }

        // Verify status is PENDING
        if (request.getStatut() != ConnectionStatus.PENDING) {
            throw new RuntimeException("Cette demande a déjà été traitée");
        }

        // Reject the request
        request.setStatut(ConnectionStatus.REJECTED);
        request.setRespondedAt(LocalDateTime.now());

        ConnectionRequest updated = connectionRequestRepository.save(request);
        log.info("Connection request rejected: {}", requestId);

        return ConnectionResponse.fromConnectionRequest(updated);
    }

    /**
     * Get all active (accepted) connections for current user
     */
    public List<ConnectionResponse> getActiveConnections(String authHeader) {
        log.info("Fetching active connections");

        UserDTO user = authServiceClient.getCurrentUser(authHeader);

        List<ConnectionRequest> connections;

        if ("STARTUP".equals(user.getRole())) {
            // Get startup profile
            StartupDTO startup;
            try {
                startup = startupServiceClient.getStartupByUserId(user.getId(), authHeader);
            } catch (Exception e) {
                throw new RuntimeException("Profil startup non trouvé");
            }

            connections = connectionRequestRepository.findActiveConnectionsForStartup(startup.getId());


        } else if ("INVESTOR".equals(user.getRole())) {
            // Get investor profile
            Investor investor = investorRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Profil investisseur non trouvé"));

            connections = connectionRequestRepository.findActiveConnectionsForInvestor(investor.getId());

        } else {
            throw new RuntimeException("Type d'utilisateur non valide");
        }

        return connections.stream()
                .map(ConnectionResponse::fromConnectionRequest)
                .collect(Collectors.toList());
    }
}
