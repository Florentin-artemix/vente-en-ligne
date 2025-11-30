package com.example.venteEnLigne.paiement.dto;

import com.example.venteEnLigne.paiement.model.MethodePaiement;
import com.example.venteEnLigne.paiement.model.PaiementStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaiementResponse {

    private String id;
    private String orderId;
    private String userId;
    private BigDecimal montant;
    private String currency;
    private MethodePaiement methode;
    private PaiementStatus status;
    private String transactionReference;
    private String providerResponse;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
