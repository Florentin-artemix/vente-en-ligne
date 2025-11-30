package com.example.venteEnLigne.OrderService.controller;

import com.example.venteEnLigne.OrderService.dto.*;
import com.example.venteEnLigne.OrderService.model.OrderStatus;
import com.example.venteEnLigne.OrderService.model.PaiementStatus;
import com.example.venteEnLigne.OrderService.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;

    /**
     * Créer une nouvelle commande
     */
    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@Valid @RequestBody CreateOrderDTO createOrderDTO) {
        log.info("Requête reçue pour créer une commande pour l'utilisateur: {}", createOrderDTO.getUserId());
        OrderResponseDTO order = orderService.createOrder(createOrderDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    /**
     * Récupérer toutes les commandes
     */
    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {
        log.info("Requête reçue pour récupérer toutes les commandes");
        List<OrderResponseDTO> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    /**
     * Récupérer une commande par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable String id) {
        log.info("Requête reçue pour récupérer la commande: {}", id);
        OrderResponseDTO order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    /**
     * Récupérer les commandes d'un utilisateur
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByUserId(@PathVariable String userId) {
        log.info("Requête reçue pour récupérer les commandes de l'utilisateur: {}", userId);
        List<OrderResponseDTO> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    /**
     * Récupérer les commandes par statut de commande
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByStatus(@PathVariable OrderStatus status) {
        log.info("Requête reçue pour récupérer les commandes avec le statut: {}", status);
        List<OrderResponseDTO> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }

    /**
     * Récupérer les commandes par statut de paiement
     */
    @GetMapping("/paiement-status/{status}")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByPaiementStatus(@PathVariable PaiementStatus status) {
        log.info("Requête reçue pour récupérer les commandes avec le statut de paiement: {}", status);
        List<OrderResponseDTO> orders = orderService.getOrdersByPaiementStatus(status);
        return ResponseEntity.ok(orders);
    }

    /**
     * Mettre à jour le statut d'une commande
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateOrderStatusDTO updateDTO) {
        log.info("Requête reçue pour mettre à jour le statut de la commande: {}", id);
        OrderResponseDTO order = orderService.updateOrderStatus(id, updateDTO);
        return ResponseEntity.ok(order);
    }

    /**
     * Annuler une commande
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderResponseDTO> cancelOrder(@PathVariable String id) {
        log.info("Requête reçue pour annuler la commande: {}", id);
        OrderResponseDTO order = orderService.cancelOrder(id);
        return ResponseEntity.ok(order);
    }

    /**
     * Supprimer une commande
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable String id) {
        log.info("Requête reçue pour supprimer la commande: {}", id);
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtenir les statistiques des commandes
     */
    @GetMapping("/stats")
    public ResponseEntity<OrderStatsDTO> getOrderStats() {
        log.info("Requête reçue pour obtenir les statistiques des commandes");
        OrderStatsDTO stats = orderService.getOrderStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Health check
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("OrderService is running");
    }
}
