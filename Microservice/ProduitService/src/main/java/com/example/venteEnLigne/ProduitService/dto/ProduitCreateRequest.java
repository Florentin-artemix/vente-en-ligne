package com.example.venteEnLigne.ProduitService.dto;

import com.example.venteEnLigne.ProduitService.model.ProductStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class ProduitCreateRequest {

    @NotBlank(message = "L'ID du vendeur est obligatoire")
    private String vendeurId;

    @NotBlank(message = "Le titre est obligatoire")
    private String titre;

    private String description;

    @NotNull(message = "Le prix est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le prix doit être positif")
    private BigDecimal prix;

    @NotBlank(message = "La catégorie est obligatoire")
    private String categorie;

    private String sousCategorie;

    private String marque;

    @Builder.Default
    private String currency = "USD";

    private Map<String, String> specifications;

    @Builder.Default
    private ProductStatus status = ProductStatus.DISPONIBLE;

    private String image;
    
    // Setter pour accepter imageUrl comme alias de image
    public void setImageUrl(String imageUrl) {
        this.image = imageUrl;
    }

    @Min(value = 0, message = "Le stock ne peut pas être négatif")
    @Builder.Default
    private Integer stock = 0;
}
