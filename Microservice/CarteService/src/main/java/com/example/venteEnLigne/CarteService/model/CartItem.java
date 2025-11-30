package com.example.venteEnLigne.CarteService.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Représente un item dans le panier
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem implements Serializable {

    private String produitId;
    private int quantite;
    private LocalDateTime updatedAt;
}
