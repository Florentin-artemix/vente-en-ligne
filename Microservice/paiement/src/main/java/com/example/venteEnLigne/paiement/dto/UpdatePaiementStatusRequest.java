package com.example.venteEnLigne.paiement.dto;

import com.example.venteEnLigne.paiement.model.PaiementStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePaiementStatusRequest {

    @NotNull(message = "Le statut est obligatoire")
    private PaiementStatus status;

    private String transactionReference;
    private String providerResponse;
}
