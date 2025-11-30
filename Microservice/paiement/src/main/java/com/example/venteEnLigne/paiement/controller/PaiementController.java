package com.example.venteEnLigne.paiement.controller;

import com.example.venteEnLigne.paiement.dto.*;
import com.example.venteEnLigne.paiement.model.PaiementStatus;
import com.example.venteEnLigne.paiement.service.PaiementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/paiements")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PaiementController {

    private final PaiementService paiementService;

    /**
     * Initier un nouveau paiement
     */
    @PostMapping
    public ResponseEntity<PaiementResponse> createPaiement(@Valid @RequestBody CreatePaiementRequest request) {
        log.info("Requête reçue pour créer un paiement pour la commande: {}", request.getOrderId());
        PaiementResponse paiement = paiementService.createPaiement(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(paiement);
    }

    /**
     * Récupérer un paiement par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<PaiementResponse> getPaiementById(@PathVariable String id) {
        log.info("Requête reçue pour récupérer le paiement: {}", id);
        PaiementResponse paiement = paiementService.getPaiementById(id);
        return ResponseEntity.ok(paiement);
    }

    /**
     * Récupérer les paiements d'une commande
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<PaiementResponse>> getPaiementsByOrderId(@PathVariable String orderId) {
        log.info("Requête reçue pour récupérer les paiements de la commande: {}", orderId);
        List<PaiementResponse> paiements = paiementService.getPaiementsByOrderId(orderId);
        return ResponseEntity.ok(paiements);
    }

    /**
     * Récupérer les paiements d'un utilisateur
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaiementResponse>> getPaiementsByUserId(@PathVariable String userId) {
        log.info("Requête reçue pour récupérer les paiements de l'utilisateur: {}", userId);
        List<PaiementResponse> paiements = paiementService.getPaiementsByUserId(userId);
        return ResponseEntity.ok(paiements);
    }

    /**
     * Récupérer les paiements par statut
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<PaiementResponse>> getPaiementsByStatus(@PathVariable PaiementStatus status) {
        log.info("Requête reçue pour récupérer les paiements avec le statut: {}", status);
        List<PaiementResponse> paiements = paiementService.getPaiementsByStatus(status);
        return ResponseEntity.ok(paiements);
    }

    /**
     * Mettre à jour le statut d'un paiement
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<PaiementResponse> updatePaiementStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdatePaiementStatusRequest request) {
        log.info("Requête reçue pour mettre à jour le statut du paiement: {}", id);
        PaiementResponse paiement = paiementService.updatePaiementStatus(id, request);
        return ResponseEntity.ok(paiement);
    }

    /**
     * Confirmer un paiement (webhook du provider)
     */
    @PostMapping("/{id}/confirm")
    public ResponseEntity<PaiementResponse> confirmPaiement(
            @PathVariable String id,
            @RequestBody Map<String, Object> providerResponse) {
        log.info("Confirmation du paiement: {}", id);
        String responseJson = providerResponse.toString();
        PaiementResponse paiement = paiementService.confirmPaiement(id, responseJson);
        return ResponseEntity.ok(paiement);
    }

    /**
     * Marquer un paiement comme échoué
     */
    @PostMapping("/{id}/fail")
    public ResponseEntity<PaiementResponse> failPaiement(
            @PathVariable String id,
            @RequestBody Map<String, String> request) {
        log.info("Échec du paiement: {}", id);
        String reason = request.getOrDefault("reason", "Raison non spécifiée");
        PaiementResponse paiement = paiementService.failPaiement(id, reason);
        return ResponseEntity.ok(paiement);
    }

    /**
     * Vérifier si une commande a un paiement réussi
     */
    @GetMapping("/order/{orderId}/paid")
    public ResponseEntity<Map<String, Boolean>> checkOrderPaymentStatus(@PathVariable String orderId) {
        log.info("Vérification du statut de paiement pour la commande: {}", orderId);
        boolean isPaid = paiementService.hasSuccessfulPayment(orderId);
        return ResponseEntity.ok(Map.of("paid", isPaid));
    }

    /**
     * Obtenir les statistiques des paiements
     */
    @GetMapping("/stats")
    public ResponseEntity<PaiementStatsDTO> getStats() {
        log.info("Requête reçue pour obtenir les statistiques des paiements");
        PaiementStatsDTO stats = paiementService.getStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Health check
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("PaiementService is running");
    }
}
