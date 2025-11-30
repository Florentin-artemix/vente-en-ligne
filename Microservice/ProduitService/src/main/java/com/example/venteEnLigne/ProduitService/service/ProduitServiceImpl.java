package com.example.venteEnLigne.ProduitService.service;

import com.example.venteEnLigne.ProduitService.dto.*;
import com.example.venteEnLigne.ProduitService.exception.InsufficientStockException;
import com.example.venteEnLigne.ProduitService.exception.ProduitNotFoundException;
import com.example.venteEnLigne.ProduitService.model.Produit;
import com.example.venteEnLigne.ProduitService.model.ProductStatus;
import com.example.venteEnLigne.ProduitService.repository.ProduitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProduitServiceImpl implements ProduitService {

    private final ProduitRepository produitRepository;

    @Override
    @Transactional
    public ProduitResponse createProduit(ProduitCreateRequest request) {
        log.info("Création d'un nouveau produit: {}", request.getTitre());

        Produit produit = Produit.builder()
                .vendeurId(request.getVendeurId())
                .titre(request.getTitre())
                .description(request.getDescription())
                .prix(request.getPrix())
                .categorie(request.getCategorie())
                .sousCategorie(request.getSousCategorie())
                .marque(request.getMarque())
                .currency(request.getCurrency())
                .specifications(request.getSpecifications())
                .status(request.getStatus())
                .image(request.getImage())
                .stock(request.getStock())
                .build();

        Produit savedProduit = produitRepository.save(produit);
        log.info("Produit créé avec l'ID: {}", savedProduit.getId());

        return mapToResponse(savedProduit);
    }

    @Override
    public ProduitResponse getProduitById(String id) {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new ProduitNotFoundException("Produit non trouvé avec l'ID: " + id));
        return mapToResponse(produit);
    }

    @Override
    @Transactional
    public ProduitResponse updateProduit(String id, ProduitUpdateRequest request) {
        log.info("Mise à jour du produit: {}", id);

        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new ProduitNotFoundException("Produit non trouvé avec l'ID: " + id));

        if (request.getTitre() != null) produit.setTitre(request.getTitre());
        if (request.getDescription() != null) produit.setDescription(request.getDescription());
        if (request.getPrix() != null) produit.setPrix(request.getPrix());
        if (request.getCategorie() != null) produit.setCategorie(request.getCategorie());
        if (request.getSousCategorie() != null) produit.setSousCategorie(request.getSousCategorie());
        if (request.getMarque() != null) produit.setMarque(request.getMarque());
        if (request.getCurrency() != null) produit.setCurrency(request.getCurrency());
        if (request.getSpecifications() != null) produit.setSpecifications(request.getSpecifications());
        if (request.getStatus() != null) produit.setStatus(request.getStatus());
        if (request.getImage() != null) produit.setImage(request.getImage());
        if (request.getStock() != null) produit.setStock(request.getStock());

        Produit updatedProduit = produitRepository.save(produit);
        log.info("Produit mis à jour: {}", id);

        return mapToResponse(updatedProduit);
    }

    @Override
    @Transactional
    public void deleteProduit(String id) {
        log.info("Suppression du produit: {}", id);

        if (!produitRepository.existsById(id)) {
            throw new ProduitNotFoundException("Produit non trouvé avec l'ID: " + id);
        }

        produitRepository.deleteById(id);
        log.info("Produit supprimé: {}", id);
    }

    @Override
    public List<ProduitResponse> getAllProduits() {
        return produitRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ProduitResponse> getAllProduits(Pageable pageable) {
        return produitRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Override
    public List<ProduitResponse> getProduitsByVendeur(String vendeurId) {
        return produitRepository.findByVendeurId(vendeurId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ProduitResponse> getProduitsByVendeur(String vendeurId, Pageable pageable) {
        return produitRepository.findByVendeurId(vendeurId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public List<ProduitResponse> getProduitsByCategorie(String categorie) {
        return produitRepository.findByCategorie(categorie).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ProduitResponse> searchProduits(String keyword, Pageable pageable) {
        return produitRepository.searchProducts(keyword, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public List<ProduitResponse> getProduitsByStatus(ProductStatus status) {
        return produitRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProduitResponse updateStock(StockUpdateRequest request) {
        if ("DECREMENT".equalsIgnoreCase(request.getOperation())) {
            return decrementStock(request.getProduitId(), request.getQuantite());
        } else if ("INCREMENT".equalsIgnoreCase(request.getOperation())) {
            return incrementStock(request.getProduitId(), request.getQuantite());
        } else {
            throw new IllegalArgumentException("Opération invalide: " + request.getOperation());
        }
    }

    @Override
    @Transactional
    public ProduitResponse decrementStock(String produitId, Integer quantite) {
        log.info("Décrémentation du stock pour le produit {} de {} unités", produitId, quantite);

        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new ProduitNotFoundException("Produit non trouvé avec l'ID: " + produitId));

        if (produit.getStock() < quantite) {
            throw new InsufficientStockException(
                    String.format("Stock insuffisant pour le produit %s. Stock actuel: %d, Demandé: %d",
                            produit.getTitre(), produit.getStock(), quantite));
        }

        produit.setStock(produit.getStock() - quantite);
        Produit updatedProduit = produitRepository.save(produit);

        log.info("Stock décrémenté. Nouveau stock pour {}: {}", produitId, updatedProduit.getStock());
        return mapToResponse(updatedProduit);
    }

    @Override
    @Transactional
    public ProduitResponse incrementStock(String produitId, Integer quantite) {
        log.info("Incrémentation du stock pour le produit {} de {} unités", produitId, quantite);

        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new ProduitNotFoundException("Produit non trouvé avec l'ID: " + produitId));

        produit.setStock(produit.getStock() + quantite);
        Produit updatedProduit = produitRepository.save(produit);

        log.info("Stock incrémenté. Nouveau stock pour {}: {}", produitId, updatedProduit.getStock());
        return mapToResponse(updatedProduit);
    }

    @Override
    public long countProduitsByVendeur(String vendeurId) {
        return produitRepository.countByVendeurId(vendeurId);
    }

    @Override
    public List<ProduitResponse> getProduitsEnRupture() {
        return produitRepository.findByStockLessThanEqual(0).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ProduitResponse mapToResponse(Produit produit) {
        return ProduitResponse.builder()
                .id(produit.getId())
                .vendeurId(produit.getVendeurId())
                .titre(produit.getTitre())
                .description(produit.getDescription())
                .prix(produit.getPrix())
                .categorie(produit.getCategorie())
                .sousCategorie(produit.getSousCategorie())
                .marque(produit.getMarque())
                .currency(produit.getCurrency())
                .specifications(produit.getSpecifications())
                .status(produit.getStatus())
                .image(produit.getImage())
                .stock(produit.getStock())
                .createdAt(produit.getCreatedAt())
                .updatedAt(produit.getUpdatedAt())
                .build();
    }
}
