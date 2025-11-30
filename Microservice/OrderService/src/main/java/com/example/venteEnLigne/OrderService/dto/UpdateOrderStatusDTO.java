package com.example.venteEnLigne.OrderService.dto;

import com.example.venteEnLigne.OrderService.model.OrderStatus;
import com.example.venteEnLigne.OrderService.model.PaiementStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour mettre à jour le statut d'une commande
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderStatusDTO {
    private OrderStatus orderStatus;
    private PaiementStatus paiementStatus;
}
