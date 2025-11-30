package com.example.venteEnLigne.OrderService.dto;

import com.example.venteEnLigne.OrderService.model.OrderStatus;
import com.example.venteEnLigne.OrderService.model.PaiementStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de réponse pour une commande
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {
    
    private String id;
    private String userId;
    private BigDecimal montantTotal;
    private String currency;
    private PaiementStatus paiementStatus;
    private OrderStatus orderStatus;
    private AdresseLivraisonDTO adresseLivraison;
    private List<OrderItemDTO> items;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
