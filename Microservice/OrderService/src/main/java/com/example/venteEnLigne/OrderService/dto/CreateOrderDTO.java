package com.example.venteEnLigne.OrderService.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO pour créer une nouvelle commande
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderDTO {

    @NotBlank(message = "L'ID de l'utilisateur est obligatoire")
    private String userId;

    private String currency;

    @NotNull(message = "L'adresse de livraison est obligatoire")
    @Valid
    private AdresseLivraisonDTO adresseLivraison;

    @NotEmpty(message = "La commande doit contenir au moins un article")
    @Valid
    private List<OrderItemDTO> items;

    private String notes;
}
