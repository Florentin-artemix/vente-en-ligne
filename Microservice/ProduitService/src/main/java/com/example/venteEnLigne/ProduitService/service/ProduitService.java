package com.example.venteEnLigne.ProduitService.service;

import com.example.venteEnLigne.ProduitService.dto.*;
import com.example.venteEnLigne.ProduitService.model.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProduitService {
    
    // CRUD de base
    ProduitResponse createProduit(ProduitCreateRequest request);
    ProduitResponse getProduitById(String id);
    ProduitResponse updateProduit(String id, ProduitUpdateRequest request);
    void deleteProduit(String id);
    
    // Listes et recherches
    List<ProduitResponse> getAllProduits();
    Page<ProduitResponse> getAllProduits(Pageable pageable);
    List<ProduitResponse> getProduitsByVendeur(String vendeurId);
    Page<ProduitResponse> getProduitsByVendeur(String vendeurId, Pageable pageable);
    List<ProduitResponse> getProduitsByCategorie(String categorie);
    Page<ProduitResponse> searchProduits(String keyword, Pageable pageable);
    List<ProduitResponse> getProduitsByStatus(ProductStatus status);
    
    // Gestion du stock (transactionnel)
    ProduitResponse updateStock(StockUpdateRequest request);
    ProduitResponse decrementStock(String produitId, Integer quantite);
    ProduitResponse incrementStock(String produitId, Integer quantite);
    
    // Statistiques
    long countProduitsByVendeur(String vendeurId);
    List<ProduitResponse> getProduitsEnRupture();
}
