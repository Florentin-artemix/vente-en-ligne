package com.example.venteEnLigne.OrderService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatsDTO {
    private Long totalOrders;
    private Long enAttente;
    private Long enCours;
    private Long enRoute;
    private Long livre;
    private Long annule;
    private BigDecimal revenus;
}
