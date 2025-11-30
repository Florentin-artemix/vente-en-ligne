package com.example.venteEnLigne.CarteService.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente le panier d'un utilisateur
 * Stocké dans Redis avec la clé: cart-{userId}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cart implements Serializable {

    private String userId;
    
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();
}
