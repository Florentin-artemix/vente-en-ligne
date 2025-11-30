package com.example.venteEnLigne.ProduitService.repository;

import com.example.venteEnLigne.ProduitService.model.Produit;
import com.example.venteEnLigne.ProduitService.model.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProduitRepository extends MongoRepository<Produit, String> {

    // Trouver les produits par vendeur
    List<Produit> findByVendeurId(String vendeurId);
    Page<Produit> findByVendeurId(String vendeurId, Pageable pageable);

    // Trouver les produits par catégorie
    List<Produit> findByCategorie(String categorie);
    Page<Produit> findByCategorie(String categorie, Pageable pageable);

    // Trouver les produits par sous-catégorie
    List<Produit> findBySousCategorie(String sousCategorie);

    // Trouver les produits par statut
    List<Produit> findByStatus(ProductStatus status);
    Page<Produit> findByStatus(ProductStatus status, Pageable pageable);

    // Trouver les produits actifs d'un vendeur
    List<Produit> findByVendeurIdAndStatus(String vendeurId, ProductStatus status);

    // Trouver les produits par marque
    List<Produit> findByMarque(String marque);

    // Recherche par titre (contient, insensible à la casse)
    @Query("{'titre': {$regex: ?0, $options: 'i'}}")
    List<Produit> searchByTitre(String titre);

    // Recherche full-text
    @Query("{'$or': [{'titre': {$regex: ?0, $options: 'i'}}, {'description': {$regex: ?0, $options: 'i'}}]}")
    Page<Produit> searchProducts(String keyword, Pageable pageable);

    // Trouver les produits par gamme de prix
    List<Produit> findByPrixBetween(BigDecimal minPrix, BigDecimal maxPrix);

    // Trouver les produits avec stock > 0
    List<Produit> findByStockGreaterThan(Integer stock);

    // Trouver les produits en rupture de stock
    List<Produit> findByStockLessThanEqual(Integer stock);

    // Compter les produits par vendeur
    long countByVendeurId(String vendeurId);

    // Compter les produits par catégorie
    long countByCategorie(String categorie);
}
