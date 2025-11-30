package com.example.venteEnLigne.CarteService.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddToCartRequest {

    @NotBlank(message = "L'ID du produit est obligatoire")
    private String produitId;

    @Min(value = 1, message = "La quantité doit être au moins 1")
    private int quantite;
}
