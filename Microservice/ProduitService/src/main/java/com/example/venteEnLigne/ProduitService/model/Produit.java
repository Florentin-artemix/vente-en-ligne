package com.example.venteEnLigne.ProduitService.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "produits")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Produit {

    @Id
    private String id; // ObjectId MongoDB

    @Indexed
    private String vendeurId; // Firebase UID du vendeur

    private String titre;

    private String description;

    private BigDecimal prix;

    private String categorie;

    private String sousCategorie;

    private String marque;

    @Builder.Default
    private String currency = "USD"; // CDF ou USD

    private Map<String, String> specifications; // Spécifications JSON {key: value}

    @Builder.Default
    private ProductStatus status = ProductStatus.DISPONIBLE;

    private String image; // URL de l'image

    @Builder.Default
    private Integer stock = 0; // Quantité en stock (transactionnel)

    @Version
    private Long version; // Pour le contrôle de concurrence optimiste

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
