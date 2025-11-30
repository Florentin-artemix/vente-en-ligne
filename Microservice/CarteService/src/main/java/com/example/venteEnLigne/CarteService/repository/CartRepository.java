package com.example.venteEnLigne.CarteService.repository;

import com.example.venteEnLigne.CarteService.model.Cart;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
@Slf4j
public class CartRepository {

    private static final String CART_KEY_PREFIX = "cart-";
    private static final long CART_EXPIRATION_DAYS = 30; // Le panier expire après 30 jours

    private final RedisTemplate<String, Cart> redisTemplate;

    /**
     * Génère la clé Redis pour un utilisateur
     */
    private String getCartKey(String userId) {
        return CART_KEY_PREFIX + userId;
    }

    /**
     * Sauvegarde le panier dans Redis
     */
    public Cart save(Cart cart) {
        String key = getCartKey(cart.getUserId());
        redisTemplate.opsForValue().set(key, cart, CART_EXPIRATION_DAYS, TimeUnit.DAYS);
        log.info("Panier sauvegardé pour l'utilisateur: {}", cart.getUserId());
        return cart;
    }

    /**
     * Récupère le panier d'un utilisateur
     */
    public Optional<Cart> findByUserId(String userId) {
        String key = getCartKey(userId);
        Cart cart = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(cart);
    }

    /**
     * Vérifie si un panier existe pour un utilisateur
     */
    public boolean existsByUserId(String userId) {
        String key = getCartKey(userId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * Supprime le panier d'un utilisateur
     */
    public void deleteByUserId(String userId) {
        String key = getCartKey(userId);
        redisTemplate.delete(key);
        log.info("Panier supprimé pour l'utilisateur: {}", userId);
    }

    /**
     * Prolonge la durée de vie du panier
     */
    public void refreshExpiration(String userId) {
        String key = getCartKey(userId);
        redisTemplate.expire(key, CART_EXPIRATION_DAYS, TimeUnit.DAYS);
    }
}
