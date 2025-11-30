package com.example.venteEnLigne.CarteService.service;

import com.example.venteEnLigne.CarteService.dto.AddToCartRequest;
import com.example.venteEnLigne.CarteService.dto.CartDTO;
import com.example.venteEnLigne.CarteService.dto.CartItemDTO;
import com.example.venteEnLigne.CarteService.model.Cart;
import com.example.venteEnLigne.CarteService.model.CartItem;
import com.example.venteEnLigne.CarteService.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;

    /**
     * Récupérer le panier d'un utilisateur
     */
    public CartDTO getCart(String userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElse(Cart.builder()
                        .userId(userId)
                        .items(new ArrayList<>())
                        .build());
        
        return mapToDTO(cart);
    }

    /**
     * Ajouter un produit au panier
     */
    public CartDTO addToCart(String userId, AddToCartRequest request) {
        log.info("Ajout du produit {} au panier de l'utilisateur {}", request.getProduitId(), userId);

        Cart cart = cartRepository.findByUserId(userId)
                .orElse(Cart.builder()
                        .userId(userId)
                        .items(new ArrayList<>())
                        .build());

        // Chercher si le produit existe déjà dans le panier
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduitId().equals(request.getProduitId()))
                .findFirst();

        if (existingItem.isPresent()) {
            // Mettre à jour la quantité
            CartItem item = existingItem.get();
            item.setQuantite(item.getQuantite() + request.getQuantite());
            item.setUpdatedAt(LocalDateTime.now());
            log.info("Quantité mise à jour pour le produit {}: {}", request.getProduitId(), item.getQuantite());
        } else {
            // Ajouter un nouveau produit
            CartItem newItem = CartItem.builder()
                    .produitId(request.getProduitId())
                    .quantite(request.getQuantite())
                    .updatedAt(LocalDateTime.now())
                    .build();
            cart.getItems().add(newItem);
            log.info("Nouveau produit ajouté au panier: {}", request.getProduitId());
        }

        Cart savedCart = cartRepository.save(cart);
        return mapToDTO(savedCart);
    }

    /**
     * Mettre à jour la quantité d'un produit dans le panier
     */
    public CartDTO updateItemQuantity(String userId, String produitId, int quantite) {
        log.info("Mise à jour de la quantité du produit {} pour l'utilisateur {}: {}", produitId, userId, quantite);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Panier non trouvé pour l'utilisateur: " + userId));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProduitId().equals(produitId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Produit non trouvé dans le panier: " + produitId));

        item.setQuantite(quantite);
        item.setUpdatedAt(LocalDateTime.now());

        Cart savedCart = cartRepository.save(cart);
        return mapToDTO(savedCart);
    }

    /**
     * Supprimer un produit du panier
     */
    public CartDTO removeFromCart(String userId, String produitId) {
        log.info("Suppression du produit {} du panier de l'utilisateur {}", produitId, userId);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Panier non trouvé pour l'utilisateur: " + userId));

        cart.getItems().removeIf(item -> item.getProduitId().equals(produitId));

        Cart savedCart = cartRepository.save(cart);
        return mapToDTO(savedCart);
    }

    /**
     * Vider le panier
     */
    public void clearCart(String userId) {
        log.info("Vidage du panier de l'utilisateur {}", userId);
        cartRepository.deleteByUserId(userId);
    }

    /**
     * Vérifier si le panier existe
     */
    public boolean cartExists(String userId) {
        return cartRepository.existsByUserId(userId);
    }

    /**
     * Mapper Cart vers CartDTO
     */
    private CartDTO mapToDTO(Cart cart) {
        List<CartItemDTO> itemDTOs = cart.getItems().stream()
                .map(item -> CartItemDTO.builder()
                        .produitId(item.getProduitId())
                        .quantite(item.getQuantite())
                        .updatedAt(item.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());

        int totalItems = cart.getItems().stream()
                .mapToInt(CartItem::getQuantite)
                .sum();

        return CartDTO.builder()
                .userId(cart.getUserId())
                .items(itemDTOs)
                .totalItems(totalItems)
                .build();
    }
}
