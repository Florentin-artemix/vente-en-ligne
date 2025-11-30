import { useState, useEffect } from 'react';
import { produitService } from '../services/api';
import { useCart } from '../context/CartContext';
import './Cart.css';

const CartSidebar = ({ isOpen, onClose, onCheckout, produitsData = {} }) => {
  const { cart, loading, updateQuantity, removeFromCart, clearCart, getCartTotal } = useCart();
  const [produits, setProduits] = useState(produitsData);

  // Charger les détails des produits manquants
  useEffect(() => {
    const loadMissingProducts = async () => {
      const missingIds = cart.items
        .filter(item => !produits[item.produitId] && !item.produitTitre)
        .map(item => item.produitId);
      
      for (const id of missingIds) {
        try {
          const produit = await produitService.getProduitById(id);
          setProduits(prev => ({ ...prev, [id]: produit }));
        } catch (err) {
          console.error(`Erreur chargement produit ${id}:`, err);
        }
      }
    };

    if (cart.items.length > 0) {
      loadMissingProducts();
    }
  }, [cart.items]);

  const getItemDetails = (item) => {
    const produit = produits[item.produitId];
    return {
      titre: item.produitTitre || produit?.titre || 'Produit inconnu',
      image: item.produitImage || produit?.imageUrl || produit?.image,
      prix: item.prixUnitaire || produit?.prix || 0,
      currency: produit?.currency || 'USD'
    };
  };

  const handleQuantityChange = (produitId, newQuantity) => {
    if (newQuantity >= 1) {
      updateQuantity(produitId, newQuantity);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="cart-overlay" onClick={onClose}>
      <div className="cart-sidebar" onClick={e => e.stopPropagation()}>
        <div className="cart-header">
          <h2>🛒 Mon Panier</h2>
          <button className="close-btn" onClick={onClose}>×</button>
        </div>

        {loading && <div className="cart-loading">Chargement...</div>}

        {cart.items.length === 0 ? (
          <div className="cart-empty">
            <span className="empty-icon">🛒</span>
            <p>Votre panier est vide</p>
          </div>
        ) : (
          <>
            <div className="cart-items">
              {cart.items.map(item => {
                const details = getItemDetails(item);
                return (
                  <div key={item.produitId} className="cart-item">
                    <div className="item-image">
                      {details.image ? (
                        <img src={details.image} alt={details.titre} />
                      ) : (
                        <div className="no-image">📦</div>
                      )}
                    </div>
                    <div className="item-details">
                      <h4>{details.titre}</h4>
                      <p className="item-price">{details.prix} {details.currency}</p>
                      <div className="quantity-controls">
                        <button 
                          onClick={() => handleQuantityChange(item.produitId, item.quantite - 1)}
                          disabled={item.quantite <= 1}
                        >
                          -
                        </button>
                        <span>{item.quantite}</span>
                        <button onClick={() => handleQuantityChange(item.produitId, item.quantite + 1)}>
                          +
                        </button>
                      </div>
                    </div>
                    <div className="item-total">
                      <span>{(details.prix * item.quantite).toFixed(2)} {details.currency}</span>
                      <button 
                        className="remove-btn" 
                        onClick={() => removeFromCart(item.produitId)}
                        title="Supprimer"
                      >
                        🗑️
                      </button>
                    </div>
                  </div>
                );
              })}
            </div>

            <div className="cart-footer">
              <div className="cart-summary">
                <span>Total ({cart.totalItems} articles)</span>
                <span className="total-amount">{getCartTotal(produits).toFixed(2)} USD</span>
              </div>
              <button className="clear-cart-btn" onClick={clearCart}>
                🗑️ Vider le panier
              </button>
              <button className="checkout-btn" onClick={onCheckout}>
                Commander 🛒
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
};

export default CartSidebar;
