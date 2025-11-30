package com.example.venteEnLigne.OrderService.repository;

import com.example.venteEnLigne.OrderService.model.Order;
import com.example.venteEnLigne.OrderService.model.OrderStatus;
import com.example.venteEnLigne.OrderService.model.PaiementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    
    /**
     * Récupérer toutes les commandes d'un utilisateur
     */
    List<Order> findByUserId(String userId);
    
    /**
     * Récupérer les commandes par statut de commande
     */
    List<Order> findByOrderStatus(OrderStatus orderStatus);
    
    /**
     * Récupérer les commandes par statut de paiement
     */
    List<Order> findByPaiementStatus(PaiementStatus paiementStatus);
    
    /**
     * Récupérer les commandes d'un utilisateur par statut
     */
    List<Order> findByUserIdAndOrderStatus(String userId, OrderStatus orderStatus);
    
    /**
     * Compter les commandes d'un utilisateur
     */
    long countByUserId(String userId);
    
    /**
     * Vérifier si une commande existe pour un utilisateur
     */
    boolean existsByIdAndUserId(String id, String userId);
}
