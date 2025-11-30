package com.example.venteEnLigne.ProduitService.dto;

import com.example.venteEnLigne.ProduitService.model.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProduitUpdateRequest {
    private String titre;
    private String description;
    private BigDecimal prix;
    private String categorie;
    private String sousCategorie;
    private String marque;
    private String currency;
    private Map<String, String> specifications;
    private ProductStatus status;
    private String image;
    private Integer stock;
}
