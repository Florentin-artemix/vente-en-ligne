package com.example.venteEnLigne.OrderService.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité principale représentant une commande
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;  // Firebase UID

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montantTotal;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaiementStatus paiementStatus = PaiementStatus.EN_ATTENTE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrderStatus orderStatus = OrderStatus.EN_ATTENTE;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "pays", column = @Column(name = "livraison_pays")),
        @AttributeOverride(name = "province", column = @Column(name = "livraison_province")),
        @AttributeOverride(name = "ville", column = @Column(name = "livraison_ville")),
        @AttributeOverride(name = "commune", column = @Column(name = "livraison_commune")),
        @AttributeOverride(name = "quartier", column = @Column(name = "livraison_quartier")),
        @AttributeOverride(name = "avenue", column = @Column(name = "livraison_avenue")),
        @AttributeOverride(name = "reference", column = @Column(name = "livraison_reference")),
        @AttributeOverride(name = "telephone", column = @Column(name = "livraison_telephone")),
        @AttributeOverride(name = "nomDestinataire", column = @Column(name = "livraison_nom_destinataire"))
    })
    private AdresseLivraison adresseLivraison;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Méthodes utilitaires
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
        recalculerMontantTotal();
    }

    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
        recalculerMontantTotal();
    }

    public void recalculerMontantTotal() {
        this.montantTotal = items.stream()
            .map(OrderItem::getSousTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
