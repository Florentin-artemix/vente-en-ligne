package com.example.venteEnLigne.CarteService.controller;

import com.example.venteEnLigne.CarteService.dto.AddToCartRequest;
import com.example.venteEnLigne.CarteService.dto.CartDTO;
import com.example.venteEnLigne.CarteService.dto.UpdateQuantityRequest;
import com.example.venteEnLigne.CarteService.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carte")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;

    /**
     * Récupérer le panier d'un utilisateur
     */
    @GetMapping("/{userId}")
    public ResponseEntity<CartDTO> getCart(@PathVariable String userId) {
        log.info("Requête reçue pour récupérer le panier de l'utilisateur: {}", userId);
        CartDTO cart = cartService.getCart(userId);
        return ResponseEntity.ok(cart);
    }

    /**
     * Ajouter un produit au panier
     */
    @PostMapping("/{userId}/items")
    public ResponseEntity<CartDTO> addToCart(
            @PathVariable String userId,
            @Valid @RequestBody AddToCartRequest request) {
        log.info("Requête reçue pour ajouter au panier de l'utilisateur: {}", userId);
        CartDTO cart = cartService.addToCart(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(cart);
    }

    /**
     * Mettre à jour la quantité d'un produit dans le panier
     */
    @PatchMapping("/{userId}/items/{produitId}")
    public ResponseEntity<CartDTO> updateItemQuantity(
            @PathVariable String userId,
            @PathVariable String produitId,
            @Valid @RequestBody UpdateQuantityRequest request) {
        log.info("Requête reçue pour mettre à jour la quantité du produit {} pour l'utilisateur {}", produitId, userId);
        CartDTO cart = cartService.updateItemQuantity(userId, produitId, request.getQuantite());
        return ResponseEntity.ok(cart);
    }

    /**
     * Supprimer un produit du panier
     */
    @DeleteMapping("/{userId}/items/{produitId}")
    public ResponseEntity<CartDTO> removeFromCart(
            @PathVariable String userId,
            @PathVariable String produitId) {
        log.info("Requête reçue pour supprimer le produit {} du panier de l'utilisateur {}", produitId, userId);
        CartDTO cart = cartService.removeFromCart(userId, produitId);
        return ResponseEntity.ok(cart);
    }

    /**
     * Vider le panier
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> clearCart(@PathVariable String userId) {
        log.info("Requête reçue pour vider le panier de l'utilisateur: {}", userId);
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Health check
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("CartService is running");
    }
}
