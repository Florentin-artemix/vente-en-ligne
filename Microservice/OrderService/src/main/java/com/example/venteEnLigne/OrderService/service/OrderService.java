package com.example.venteEnLigne.OrderService.service;

import com.example.venteEnLigne.OrderService.dto.*;
import com.example.venteEnLigne.OrderService.model.*;
import com.example.venteEnLigne.OrderService.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;

    /**
     * Créer une nouvelle commande
     */
    public OrderResponseDTO createOrder(CreateOrderDTO createOrderDTO) {
        log.info("Création d'une nouvelle commande pour l'utilisateur: {}", createOrderDTO.getUserId());

        // Créer l'adresse de livraison
        AdresseLivraison adresse = AdresseLivraison.builder()
            .pays(createOrderDTO.getAdresseLivraison().getPays())
            .province(createOrderDTO.getAdresseLivraison().getProvince())
            .ville(createOrderDTO.getAdresseLivraison().getVille())
            .commune(createOrderDTO.getAdresseLivraison().getCommune())
            .quartier(createOrderDTO.getAdresseLivraison().getQuartier())
            .avenue(createOrderDTO.getAdresseLivraison().getAvenue())
            .reference(createOrderDTO.getAdresseLivraison().getReference())
            .telephone(createOrderDTO.getAdresseLivraison().getTelephone())
            .nomDestinataire(createOrderDTO.getAdresseLivraison().getNomDestinataire())
            .build();

        // Créer la commande
        Order order = Order.builder()
            .userId(createOrderDTO.getUserId())
            .currency(createOrderDTO.getCurrency() != null ? createOrderDTO.getCurrency() : "USD")
            .adresseLivraison(adresse)
            .notes(createOrderDTO.getNotes())
            .montantTotal(BigDecimal.ZERO)
            .build();

        // Ajouter les items
        if (createOrderDTO.getItems() != null) {
            for (OrderItemDTO itemDTO : createOrderDTO.getItems()) {
                OrderItem item = OrderItem.builder()
                    .produitId(itemDTO.getProduitId())
                    .produitTitre(itemDTO.getProduitTitre())
                    .produitImage(itemDTO.getProduitImage())
                    .quantite(itemDTO.getQuantite())
                    .prixUnitaire(itemDTO.getPrixUnitaire())
                    .build();
                item.calculateSousTotal();
                order.addItem(item);
            }
        }

        order.recalculerMontantTotal();
        Order savedOrder = orderRepository.save(order);
        
        log.info("Commande créée avec succès: {}", savedOrder.getId());
        return convertToDTO(savedOrder);
    }

    /**
     * Récupérer toutes les commandes
     */
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getAllOrders() {
        return orderRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Récupérer une commande par ID
     */
    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderById(String id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Commande non trouvée avec l'ID: " + id));
        return convertToDTO(order);
    }

    /**
     * Récupérer les commandes d'un utilisateur
     */
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getOrdersByUserId(String userId) {
        return orderRepository.findByUserId(userId).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Récupérer les commandes par statut de commande
     */
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByOrderStatus(status).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Récupérer les commandes par statut de paiement
     */
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getOrdersByPaiementStatus(PaiementStatus status) {
        return orderRepository.findByPaiementStatus(status).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Mettre à jour le statut d'une commande
     */
    public OrderResponseDTO updateOrderStatus(String id, UpdateOrderStatusDTO updateDTO) {
        log.info("Mise à jour du statut de la commande: {}", id);
        
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Commande non trouvée avec l'ID: " + id));

        if (updateDTO.getOrderStatus() != null) {
            order.setOrderStatus(updateDTO.getOrderStatus());
        }

        if (updateDTO.getPaiementStatus() != null) {
            order.setPaiementStatus(updateDTO.getPaiementStatus());
        }

        Order updatedOrder = orderRepository.save(order);
        log.info("Statut de la commande mis à jour: {}", id);
        
        return convertToDTO(updatedOrder);
    }

    /**
     * Annuler une commande
     */
    public OrderResponseDTO cancelOrder(String id) {
        log.info("Annulation de la commande: {}", id);
        
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Commande non trouvée avec l'ID: " + id));

        if (order.getOrderStatus() == OrderStatus.LIVRE) {
            throw new IllegalStateException("Impossible d'annuler une commande déjà livrée");
        }

        order.setOrderStatus(OrderStatus.ANNULE);
        Order cancelledOrder = orderRepository.save(order);
        
        log.info("Commande annulée: {}", id);
        return convertToDTO(cancelledOrder);
    }

    /**
     * Supprimer une commande
     */
    public void deleteOrder(String id) {
        log.info("Suppression de la commande: {}", id);
        
        if (!orderRepository.existsById(id)) {
            throw new EntityNotFoundException("Commande non trouvée avec l'ID: " + id);
        }

        orderRepository.deleteById(id);
        log.info("Commande supprimée: {}", id);
    }

    /**
     * Obtenir les statistiques des commandes
     */
    @Transactional(readOnly = true)
    public OrderStatsDTO getOrderStats() {
        List<Order> allOrders = orderRepository.findAll();
        
        long totalOrders = allOrders.size();
        long enAttente = allOrders.stream().filter(o -> o.getOrderStatus() == OrderStatus.EN_ATTENTE).count();
        long enCours = allOrders.stream().filter(o -> o.getOrderStatus() == OrderStatus.EN_COURS).count();
        long enRoute = allOrders.stream().filter(o -> o.getOrderStatus() == OrderStatus.EN_ROUTE).count();
        long livre = allOrders.stream().filter(o -> o.getOrderStatus() == OrderStatus.LIVRE).count();
        long annule = allOrders.stream().filter(o -> o.getOrderStatus() == OrderStatus.ANNULE).count();
        
        BigDecimal revenus = allOrders.stream()
            .filter(o -> o.getPaiementStatus() == PaiementStatus.PAYE)
            .map(Order::getMontantTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return OrderStatsDTO.builder()
            .totalOrders(totalOrders)
            .enAttente(enAttente)
            .enCours(enCours)
            .enRoute(enRoute)
            .livre(livre)
            .annule(annule)
            .revenus(revenus)
            .build();
    }

    /**
     * Convertir une entité Order en DTO
     */
    private OrderResponseDTO convertToDTO(Order order) {
        AdresseLivraisonDTO adresseDTO = AdresseLivraisonDTO.builder()
            .pays(order.getAdresseLivraison().getPays())
            .province(order.getAdresseLivraison().getProvince())
            .ville(order.getAdresseLivraison().getVille())
            .commune(order.getAdresseLivraison().getCommune())
            .quartier(order.getAdresseLivraison().getQuartier())
            .avenue(order.getAdresseLivraison().getAvenue())
            .reference(order.getAdresseLivraison().getReference())
            .telephone(order.getAdresseLivraison().getTelephone())
            .nomDestinataire(order.getAdresseLivraison().getNomDestinataire())
            .build();

        List<OrderItemDTO> itemsDTO = order.getItems().stream()
            .map(item -> OrderItemDTO.builder()
                .produitId(item.getProduitId())
                .produitTitre(item.getProduitTitre())
                .produitImage(item.getProduitImage())
                .quantite(item.getQuantite())
                .prixUnitaire(item.getPrixUnitaire())
                .sousTotal(item.getSousTotal())
                .build())
            .collect(Collectors.toList());

        return OrderResponseDTO.builder()
            .id(order.getId())
            .userId(order.getUserId())
            .montantTotal(order.getMontantTotal())
            .currency(order.getCurrency())
            .paiementStatus(order.getPaiementStatus())
            .orderStatus(order.getOrderStatus())
            .adresseLivraison(adresseDTO)
            .items(itemsDTO)
            .notes(order.getNotes())
            .createdAt(order.getCreatedAt())
            .updatedAt(order.getUpdatedAt())
            .build();
    }
}
