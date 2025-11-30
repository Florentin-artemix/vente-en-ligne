package com.example.venteEnLigne.paiement.dto;

import com.example.venteEnLigne.paiement.model.MethodePaiement;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaiementRequest {

    @NotBlank(message = "L'ID de la commande est obligatoire")
    private String orderId;

    @NotBlank(message = "L'ID de l'utilisateur est obligatoire")
    private String userId;

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.01", message = "Le montant doit être supérieur à 0")
    private BigDecimal montant;

    @Builder.Default
    private String currency = "USD";

    @NotNull(message = "La méthode de paiement est obligatoire")
    private MethodePaiement methode;

    // Données spécifiques au provider (ex: numéro de téléphone pour mobile money)
    private String phoneNumber;
    private String cardToken;
}
