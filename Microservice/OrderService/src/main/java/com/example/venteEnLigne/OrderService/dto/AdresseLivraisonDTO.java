package com.example.venteEnLigne.OrderService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour l'adresse de livraison
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdresseLivraisonDTO {
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
