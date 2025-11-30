package com.example.venteEnLigne.OrderService.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO pour une ligne de commande
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    
    private String id;

    @NotBlank(message = "L'ID du produit est obligatoire")
    private String produitId;

    @NotBlank(message = "Le titre du produit est obligatoire")
    private String produitTitre;

    private String produitImage;

    @NotNull(message = "Le prix unitaire est obligatoire")
    @Positive(message = "Le prix unitaire doit être positif")
    private BigDecimal prixUnitaire;

    @Min(value = 1, message = "La quantité doit être au moins 1")
    private int quantite;

    private BigDecimal sousTotal;
}
