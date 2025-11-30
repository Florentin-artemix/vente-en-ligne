package com.example.venteEnLigne.paiement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaiementStatsDTO {

    private long totalPaiements;
    private long paiementsEnAttente;
    private long paiementsReussis;
    private long paiementsEchoues;
    private BigDecimal montantTotal;
    private Map<String, Long> paiementsParMethode;
}
