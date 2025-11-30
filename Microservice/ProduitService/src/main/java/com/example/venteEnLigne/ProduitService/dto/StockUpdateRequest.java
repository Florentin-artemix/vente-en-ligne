package com.example.venteEnLigne.ProduitService.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockUpdateRequest {
    
    @NotBlank(message = "L'ID du produit est obligatoire")
    private String produitId;
    
    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité doit être au moins 1")
    private Integer quantite;
    
    @NotBlank(message = "L'opération est obligatoire (DECREMENT ou INCREMENT)")
    private String operation; // DECREMENT ou INCREMENT
}
