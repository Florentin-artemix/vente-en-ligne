package com.example.venteEnLigne.paiement.service;

import com.example.venteEnLigne.paiement.dto.*;
import com.example.venteEnLigne.paiement.model.MethodePaiement;
import com.example.venteEnLigne.paiement.model.Paiement;
import com.example.venteEnLigne.paiement.model.PaiementStatus;
import com.example.venteEnLigne.paiement.repository.PaiementRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaiementService {

    private final PaiementRepository paiementRepository;
    private final ObjectMapper objectMapper;

    /**
     * Initier un nouveau paiement
     */
    public PaiementResponse createPaiement(CreatePaiementRequest request) {
        log.info("Création d'un paiement pour la commande: {}", request.getOrderId());

        // Générer une référence de transaction unique
        String transactionRef = generateTransactionReference(request.getMethode());

        Paiement paiement = Paiement.builder()
                .orderId(request.getOrderId())
                .userId(request.getUserId())
                .montant(request.getMontant())
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .methode(request.getMethode())
                .status(PaiementStatus.EN_ATTENTE)
                .transactionReference(transactionRef)
                .build();

        Paiement savedPaiement = paiementRepository.save(paiement);
        log.info("Paiement créé avec l'ID: {} et référence: {}", savedPaiement.getId(), transactionRef);

        return mapToResponse(savedPaiement);
    }

    /**
     * Récupérer un paiement par ID
     */
    @Transactional(readOnly = true)
    public PaiementResponse getPaiementById(String id) {
        Paiement paiement = paiementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Paiement non trouvé avec l'ID: " + id));
        return mapToResponse(paiement);
    }

    /**
     * Récupérer les paiements d'une commande
     */
    @Transactional(readOnly = true)
    public List<PaiementResponse> getPaiementsByOrderId(String orderId) {
        return paiementRepository.findByOrderId(orderId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les paiements d'un utilisateur
     */
    @Transactional(readOnly = true)
    public List<PaiementResponse> getPaiementsByUserId(String userId) {
        return paiementRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les paiements par statut
     */
    @Transactional(readOnly = true)
    public List<PaiementResponse> getPaiementsByStatus(PaiementStatus status) {
        return paiementRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Mettre à jour le statut d'un paiement (callback du provider)
     */
    public PaiementResponse updatePaiementStatus(String id, UpdatePaiementStatusRequest request) {
        log.info("Mise à jour du statut du paiement: {} vers {}", id, request.getStatus());

        Paiement paiement = paiementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Paiement non trouvé avec l'ID: " + id));

        paiement.setStatus(request.getStatus());

        if (request.getTransactionReference() != null) {
            paiement.setTransactionReference(request.getTransactionReference());
        }

        if (request.getProviderResponse() != null) {
            paiement.setProviderResponse(request.getProviderResponse());
        }

        Paiement updatedPaiement = paiementRepository.save(paiement);
        log.info("Statut du paiement {} mis à jour: {}", id, request.getStatus());

        return mapToResponse(updatedPaiement);
    }

    /**
     * Confirmer un paiement (succès)
     */
    public PaiementResponse confirmPaiement(String id, String providerResponse) {
        log.info("Confirmation du paiement: {}", id);

        Paiement paiement = paiementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Paiement non trouvé avec l'ID: " + id));

        paiement.setStatus(PaiementStatus.SUCCES);
        paiement.setProviderResponse(providerResponse);

        Paiement updatedPaiement = paiementRepository.save(paiement);
        log.info("Paiement {} confirmé avec succès", id);

        return mapToResponse(updatedPaiement);
    }

    /**
     * Échouer un paiement
     */
    public PaiementResponse failPaiement(String id, String reason) {
        log.info("Échec du paiement: {} - Raison: {}", id, reason);

        Paiement paiement = paiementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Paiement non trouvé avec l'ID: " + id));

        paiement.setStatus(PaiementStatus.ECHOUE);

        try {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Paiement échoué");
            response.put("reason", reason);
            paiement.setProviderResponse(objectMapper.writeValueAsString(response));
        } catch (JsonProcessingException e) {
            paiement.setProviderResponse("{\"message\": \"Paiement échoué\", \"reason\": \"" + reason + "\"}");
        }

        Paiement updatedPaiement = paiementRepository.save(paiement);
        log.info("Paiement {} marqué comme échoué", id);

        return mapToResponse(updatedPaiement);
    }

    /**
     * Vérifier si une commande a un paiement réussi
     */
    @Transactional(readOnly = true)
    public boolean hasSuccessfulPayment(String orderId) {
        return paiementRepository.existsByOrderIdAndStatus(orderId, PaiementStatus.SUCCES);
    }

    /**
     * Obtenir les statistiques des paiements
     */
    @Transactional(readOnly = true)
    public PaiementStatsDTO getStats() {
        List<Paiement> allPaiements = paiementRepository.findAll();

        long totalPaiements = allPaiements.size();
        long enAttente = allPaiements.stream().filter(p -> p.getStatus() == PaiementStatus.EN_ATTENTE).count();
        long reussis = allPaiements.stream().filter(p -> p.getStatus() == PaiementStatus.SUCCES).count();
        long echoues = allPaiements.stream().filter(p -> p.getStatus() == PaiementStatus.ECHOUE).count();

        BigDecimal montantTotal = allPaiements.stream()
                .filter(p -> p.getStatus() == PaiementStatus.SUCCES)
                .map(Paiement::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Long> parMethode = allPaiements.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getMethode().name(),
                        Collectors.counting()
                ));

        return PaiementStatsDTO.builder()
                .totalPaiements(totalPaiements)
                .paiementsEnAttente(enAttente)
                .paiementsReussis(reussis)
                .paiementsEchoues(echoues)
                .montantTotal(montantTotal)
                .paiementsParMethode(parMethode)
                .build();
    }

    /**
     * Générer une référence de transaction unique
     */
    private String generateTransactionReference(MethodePaiement methode) {
        String prefix = switch (methode) {
            case MPESA -> "MP";
            case ORANGE_MONEY -> "OM";
            case AIRTEL_MONEY -> "AM";
            case AFRI_MONEY -> "AF";
            case CARTE_BANCAIRE -> "CB";
            case CASH_ON_DELIVERY -> "COD";
        };
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Mapper Paiement vers PaiementResponse
     */
    private PaiementResponse mapToResponse(Paiement paiement) {
        return PaiementResponse.builder()
                .id(paiement.getId())
                .orderId(paiement.getOrderId())
                .userId(paiement.getUserId())
                .montant(paiement.getMontant())
                .currency(paiement.getCurrency())
                .methode(paiement.getMethode())
                .status(paiement.getStatus())
                .transactionReference(paiement.getTransactionReference())
                .providerResponse(paiement.getProviderResponse())
                .createdAt(paiement.getCreatedAt())
                .updatedAt(paiement.getUpdatedAt())
                .build();
    }
}
