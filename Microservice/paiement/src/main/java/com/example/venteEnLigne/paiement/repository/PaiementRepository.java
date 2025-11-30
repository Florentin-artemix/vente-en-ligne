package com.example.venteEnLigne.paiement.repository;

import com.example.venteEnLigne.paiement.model.MethodePaiement;
import com.example.venteEnLigne.paiement.model.Paiement;
import com.example.venteEnLigne.paiement.model.PaiementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, String> {

    List<Paiement> findByOrderId(String orderId);

    List<Paiement> findByUserId(String userId);

    List<Paiement> findByStatus(PaiementStatus status);

    List<Paiement> findByMethode(MethodePaiement methode);

    Optional<Paiement> findByTransactionReference(String transactionReference);

    List<Paiement> findByUserIdAndStatus(String userId, PaiementStatus status);

    boolean existsByOrderIdAndStatus(String orderId, PaiementStatus status);
}
