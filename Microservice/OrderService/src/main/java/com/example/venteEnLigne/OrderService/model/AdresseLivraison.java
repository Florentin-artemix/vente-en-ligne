package com.example.venteEnLigne.OrderService.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Adresse de livraison embarquée dans la commande
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class AdresseLivraison {
    
    private String pays;
    private String province;
    private String ville;
    private String commune;
    private String quartier;
    private String avenue;
    private String reference;
    private String telephone;
    private String nomDestinataire;
}
