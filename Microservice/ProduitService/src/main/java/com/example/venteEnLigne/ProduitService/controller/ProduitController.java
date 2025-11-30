package com.example.venteEnLigne.ProduitService.controller;

import com.example.venteEnLigne.ProduitService.dto.*;
import com.example.venteEnLigne.ProduitService.model.ProductStatus;
import com.example.venteEnLigne.ProduitService.service.ProduitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/produits")
@RequiredArgsConstructor
@Slf4j
public class ProduitController {

    private final ProduitService produitService;

    @PostMapping
    public ResponseEntity<ProduitResponse> createProduit(@Valid @RequestBody ProduitCreateRequest request) {
        log.info("Requête de création de produit: {}", request.getTitre());
        ProduitResponse response = produitService.createProduit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProduitResponse> getProduitById(@PathVariable String id) {
        log.info("Requête de récupération du produit: {}", id);
        ProduitResponse response = produitService.getProduitById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ProduitResponse>> getAllProduits() {
        log.info("Requête de récupération de tous les produits");
        List<ProduitResponse> produits = produitService.getAllProduits();
        return ResponseEntity.ok(produits);
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<ProduitResponse>> getAllProduitsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("asc") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProduitResponse> produits = produitService.getAllProduits(pageable);
        return ResponseEntity.ok(produits);
    }

    @GetMapping("/vendeur/{vendeurId}")
    public ResponseEntity<List<ProduitResponse>> getProduitsByVendeur(@PathVariable String vendeurId) {
        log.info("Requête de récupération des produits du vendeur: {}", vendeurId);
        List<ProduitResponse> produits = produitService.getProduitsByVendeur(vendeurId);
        return ResponseEntity.ok(produits);
    }

    @GetMapping("/vendeur/{vendeurId}/paginated")
    public ResponseEntity<Page<ProduitResponse>> getProduitsByVendeurPaginated(
            @PathVariable String vendeurId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ProduitResponse> produits = produitService.getProduitsByVendeur(vendeurId, pageable);
        return ResponseEntity.ok(produits);
    }

    @GetMapping("/categorie/{categorie}")
    public ResponseEntity<List<ProduitResponse>> getProduitsByCategorie(@PathVariable String categorie) {
        log.info("Requête de récupération des produits par catégorie: {}", categorie);
        List<ProduitResponse> produits = produitService.getProduitsByCategorie(categorie);
        return ResponseEntity.ok(produits);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProduitResponse>> searchProduits(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Recherche de produits avec le mot-clé: {}", keyword);
        Pageable pageable = PageRequest.of(page, size);
        Page<ProduitResponse> produits = produitService.searchProduits(keyword, pageable);
        return ResponseEntity.ok(produits);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ProduitResponse>> getProduitsByStatus(@PathVariable ProductStatus status) {
        log.info("Requête de récupération des produits par statut: {}", status);
        List<ProduitResponse> produits = produitService.getProduitsByStatus(status);
        return ResponseEntity.ok(produits);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProduitResponse> updateProduit(
            @PathVariable String id,
            @Valid @RequestBody ProduitUpdateRequest request) {
        log.info("Requête de mise à jour du produit: {}", id);
        ProduitResponse response = produitService.updateProduit(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduit(@PathVariable String id) {
        log.info("Requête de suppression du produit: {}", id);
        produitService.deleteProduit(id);
        return ResponseEntity.noContent().build();
    }

    // Endpoints de gestion du stock

    @PostMapping("/stock/update")
    public ResponseEntity<ProduitResponse> updateStock(@Valid @RequestBody StockUpdateRequest request) {
        log.info("Requête de mise à jour du stock: {} - {} unités", 
                request.getProduitId(), request.getOperation());
        ProduitResponse response = produitService.updateStock(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/stock/decrement")
    public ResponseEntity<ProduitResponse> decrementStock(
            @PathVariable String id,
            @RequestParam Integer quantite) {
        log.info("Décrémentation du stock pour le produit {}: {} unités", id, quantite);
        ProduitResponse response = produitService.decrementStock(id, quantite);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/stock/increment")
    public ResponseEntity<ProduitResponse> incrementStock(
            @PathVariable String id,
            @RequestParam Integer quantite) {
        log.info("Incrémentation du stock pour le produit {}: {} unités", id, quantite);
        ProduitResponse response = produitService.incrementStock(id, quantite);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/rupture")
    public ResponseEntity<List<ProduitResponse>> getProduitsEnRupture() {
        log.info("Requête de récupération des produits en rupture de stock");
        List<ProduitResponse> produits = produitService.getProduitsEnRupture();
        return ResponseEntity.ok(produits);
    }

    @GetMapping("/vendeur/{vendeurId}/count")
    public ResponseEntity<Map<String, Long>> countProduitsByVendeur(@PathVariable String vendeurId) {
        long count = produitService.countProduitsByVendeur(vendeurId);
        return ResponseEntity.ok(Map.of("count", count));
    }
}
