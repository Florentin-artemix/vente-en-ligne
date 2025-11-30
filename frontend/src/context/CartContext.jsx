import { useState, useEffect, createContext, useContext } from 'react';
import { cartService } from '../services/api';

const CartContext = createContext();

export const useCart = () => {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error('useCart must be used within a CartProvider');
  }
  return context;
};

export const CartProvider = ({ children, userId }) => {
  const [cart, setCart] = useState({ items: [], totalItems: 0 });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Charger le panier au montage
  useEffect(() => {
    if (userId) {
      loadCart();
    }
  }, [userId]);

  const loadCart = async () => {
    if (!userId) return;
    setLoading(true);
    try {
      const data = await cartService.getCart(userId);
      setCart(data);
      setError(null);
    } catch (err) {
      console.error('Erreur chargement panier:', err);
      setError('Erreur lors du chargement du panier');
      // Fallback en local si le backend n'est pas disponible
      const localCart = localStorage.getItem(`cart-${userId}`);
      if (localCart) {
        setCart(JSON.parse(localCart));
      }
    } finally {
      setLoading(false);
    }
  };

  const addToCart = async (produit, quantite = 1) => {
    if (!userId) return;
    setLoading(true);
    try {
      const data = await cartService.addToCart(userId, produit.id, quantite);
      setCart(data);
      setError(null);
    } catch (err) {
      console.error('Erreur ajout au panier:', err);
      // Fallback local
      const newItems = [...cart.items];
      const existingIndex = newItems.findIndex(item => item.produitId === produit.id);
      
      if (existingIndex >= 0) {
        newItems[existingIndex].quantite += quantite;
        newItems[existingIndex].updatedAt = new Date().toISOString();
      } else {
        newItems.push({
          produitId: produit.id,
          quantite,
          updatedAt: new Date().toISOString(),
          // Données supplémentaires pour l'affichage local
          produitTitre: produit.titre,
          produitImage: produit.imageUrl || produit.image,
          prixUnitaire: produit.prix
        });
      }
      
      const updatedCart = {
        userId,
        items: newItems,
        totalItems: newItems.reduce((sum, item) => sum + item.quantite, 0)
      };
      setCart(updatedCart);
      localStorage.setItem(`cart-${userId}`, JSON.stringify(updatedCart));
    } finally {
      setLoading(false);
    }
  };

  const updateQuantity = async (produitId, quantite) => {
    if (!userId || quantite < 1) return;
    setLoading(true);
    try {
      const data = await cartService.updateQuantity(userId, produitId, quantite);
      setCart(data);
      setError(null);
    } catch (err) {
      console.error('Erreur mise à jour quantité:', err);
      // Fallback local
      const newItems = cart.items.map(item => 
        item.produitId === produitId 
          ? { ...item, quantite, updatedAt: new Date().toISOString() }
          : item
      );
      const updatedCart = {
        ...cart,
        items: newItems,
        totalItems: newItems.reduce((sum, item) => sum + item.quantite, 0)
      };
      setCart(updatedCart);
      localStorage.setItem(`cart-${userId}`, JSON.stringify(updatedCart));
    } finally {
      setLoading(false);
    }
  };

  const removeFromCart = async (produitId) => {
    if (!userId) return;
    setLoading(true);
    try {
      const data = await cartService.removeFromCart(userId, produitId);
      setCart(data);
      setError(null);
    } catch (err) {
      console.error('Erreur suppression du panier:', err);
      // Fallback local
      const newItems = cart.items.filter(item => item.produitId !== produitId);
      const updatedCart = {
        ...cart,
        items: newItems,
        totalItems: newItems.reduce((sum, item) => sum + item.quantite, 0)
      };
      setCart(updatedCart);
      localStorage.setItem(`cart-${userId}`, JSON.stringify(updatedCart));
    } finally {
      setLoading(false);
    }
  };

  const clearCart = async () => {
    if (!userId) return;
    setLoading(true);
    try {
      await cartService.clearCart(userId);
      setCart({ items: [], totalItems: 0 });
      localStorage.removeItem(`cart-${userId}`);
      setError(null);
    } catch (err) {
      console.error('Erreur vidage du panier:', err);
      setCart({ items: [], totalItems: 0 });
      localStorage.removeItem(`cart-${userId}`);
    } finally {
      setLoading(false);
    }
  };

  const getCartTotal = (produitsMap = {}) => {
    return cart.items.reduce((total, item) => {
      const prix = item.prixUnitaire || produitsMap[item.produitId]?.prix || 0;
      return total + (prix * item.quantite);
    }, 0);
  };

  return (
    <CartContext.Provider value={{
      cart,
      loading,
      error,
      addToCart,
      updateQuantity,
      removeFromCart,
      clearCart,
      getCartTotal,
      loadCart
    }}>
      {children}
    </CartContext.Provider>
  );
};

export default CartContext;
