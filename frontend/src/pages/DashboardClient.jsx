import { useState, useEffect, useCallback, useMemo } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { produitService, orderService, cartService, paiementService, userService } from '../services/api';
import PaiementModal from '../components/PaiementModal';
import '../components/Cart.css';
import '../components/Paiement.css';
import './Dashboard.css';

const DashboardClient = () => {
  const { userProfile, logout } = useAuth();
  const navigate = useNavigate();
  
  // États principaux
  const [produits, setProduits] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategorie, setSelectedCategorie] = useState('');
  const [categories, setCategories] = useState([]);
  const [showProduitDetail, setShowProduitDetail] = useState(null);
  const [activeTab, setActiveTab] = useState('products');
  const [orders, setOrders] = useState([]);
  const [paiements, setPaiements] = useState([]);
  const [showCheckout, setShowCheckout] = useState(false);
  const [showCart, setShowCart] = useState(false);
  const [showPaiement, setShowPaiement] = useState(false);
  const [currentOrder, setCurrentOrder] = useState(null);
  const [cart, setCart] = useState({ items: [], totalItems: 0 });
  const [cartLoading, setCartLoading] = useState(false);
  const [sortBy, setSortBy] = useState('default');
  const [priceRange, setPriceRange] = useState({ min: '', max: '' });
  const [notification, setNotification] = useState('');
  
  // États pour le profil
  const [editProfile, setEditProfile] = useState(false);
  const [profileData, setProfileData] = useState({});
  const [profileLoading, setProfileLoading] = useState(false);
  const [photoPreview, setPhotoPreview] = useState(null);
  
  const [adresse, setAdresse] = useState({
    pays: '',
    province: '',
    ville: '',
    commune: '',
    quartier: '',
    avenue: '',
    reference: '',
    telephone: '',
    nomDestinataire: ''
  });

  // Créer une map des produits pour accès rapide
  const produitsMap = useMemo(() => {
    const map = {};
    produits.forEach(p => { map[p.id] = p; });
    return map;
  }, [produits]);

  // Chargement initial optimisé - en parallèle
  useEffect(() => {
    if (userProfile?.uid) {
      // Charger toutes les données en parallèle
      Promise.all([
        loadProduits(),
        loadCart(),
        loadOrders(),
        loadPaiements()
      ]).catch(console.error);
      
      // Initialiser les données du profil
      setProfileData({
        prenom: userProfile.prenom || '',
        nom: userProfile.nom || '',
        email: userProfile.email || '',
        telephone: userProfile.telephone || '',
        photoProfil: userProfile.photoProfil || '',
        adresse: userProfile.adresse || {}
      });
    }
  }, [userProfile]);

  // Charger le panier
  const loadCart = useCallback(async () => {
    if (!userProfile?.uid) return;
    setCartLoading(true);
    try {
      const data = await cartService.getCart(userProfile.uid);
      setCart(data || { items: [], totalItems: 0 });
    } catch (error) {
      console.error('Erreur chargement panier:', error);
      // Fallback localStorage
      const localCart = localStorage.getItem(`cart-${userProfile.uid}`);
      if (localCart) setCart(JSON.parse(localCart));
    } finally {
      setCartLoading(false);
    }
  }, [userProfile?.uid]);

  // Charger les commandes
  const loadOrders = useCallback(async () => {
    if (!userProfile?.uid) return;
    try {
      const data = await orderService.getOrdersByUserId(userProfile.uid);
      setOrders(data || []);
    } catch (error) {
      console.error('Erreur chargement commandes:', error);
    }
  }, [userProfile?.uid]);

  // Charger l'historique des paiements
  const loadPaiements = useCallback(async () => {
    if (!userProfile?.uid) return;
    try {
      const data = await paiementService.getPaiementsByUserId(userProfile.uid);
      setPaiements(data || []);
    } catch (error) {
      console.error('Erreur chargement paiements:', error);
    }
  }, [userProfile?.uid]);

  // Charger les produits
  const loadProduits = useCallback(async () => {
    setLoading(true);
    try {
      const data = await produitService.getProduitsDisponibles();
      setProduits(data || []);
      const cats = [...new Set((data || []).map(p => p.categorie).filter(Boolean))];
      setCategories(cats);
    } catch (error) {
      console.error('Erreur chargement produits:', error);
    } finally {
      setLoading(false);
    }
  }, []);

  // Recherche de produits
  const handleSearch = async () => {
    if (searchQuery.trim()) {
      setLoading(true);
      try {
        const data = await produitService.searchProduits(searchQuery);
        setProduits(data || []);
      } catch (error) {
        console.error('Erreur recherche:', error);
      } finally {
        setLoading(false);
      }
    } else {
      loadProduits();
    }
  };

  // Filtrer par catégorie
  const handleCategorieFilter = async (categorie) => {
    setSelectedCategorie(categorie);
    setLoading(true);
    try {
      if (categorie) {
        const data = await produitService.getProduitsByCategorie(categorie);
        setProduits(data || []);
      } else {
        await loadProduits();
      }
    } catch (error) {
      console.error('Erreur filtrage:', error);
    } finally {
      setLoading(false);
    }
  };

  // Tri et filtrage des produits - mémorisé pour performance
  const displayedProduits = useMemo(() => {
    let filtered = [...produits];
    
    if (priceRange.min) {
      filtered = filtered.filter(p => p.prix >= parseFloat(priceRange.min));
    }
    if (priceRange.max) {
      filtered = filtered.filter(p => p.prix <= parseFloat(priceRange.max));
    }
    
    switch (sortBy) {
      case 'price-asc':
        filtered.sort((a, b) => a.prix - b.prix);
        break;
      case 'price-desc':
        filtered.sort((a, b) => b.prix - a.prix);
        break;
      case 'name-asc':
        filtered.sort((a, b) => (a.titre || '').localeCompare(b.titre || ''));
        break;
      case 'name-desc':
        filtered.sort((a, b) => (b.titre || '').localeCompare(a.titre || ''));
        break;
      case 'newest':
        filtered.sort((a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0));
        break;
      default:
        break;
    }
    
    return filtered;
  }, [produits, priceRange, sortBy]);

  // Afficher notification
  const showNotification = (message) => {
    setNotification(message);
    setTimeout(() => setNotification(''), 3000);
  };

  // Ajouter au panier - Version corrigée
  const addToCart = async (produit, e) => {
    // Empêcher la propagation du clic
    if (e) {
      e.preventDefault();
      e.stopPropagation();
    }
    
    if (!userProfile?.uid) {
      showNotification('❌ Veuillez vous connecter pour ajouter au panier');
      return;
    }
    
    if (cartLoading) return; // Éviter les doubles clics
    
    setCartLoading(true);
    
    try {
      // Essayer d'abord via l'API
      const data = await cartService.addToCart(userProfile.uid, produit.id, 1);
      if (data) {
        setCart(data);
        showNotification(`✅ ${produit.titre} ajouté au panier!`);
        return;
      }
    } catch (error) {
      console.error('Erreur API panier:', error);
    }
    
    // Fallback local si l'API échoue
    try {
      const newItems = [...(cart.items || [])];
      const existingIndex = newItems.findIndex(item => item.produitId === produit.id);
      
      if (existingIndex >= 0) {
        newItems[existingIndex].quantite += 1;
      } else {
        newItems.push({
          produitId: produit.id,
          quantite: 1,
          produitTitre: produit.titre,
          produitImage: produit.imageUrl || produit.image,
          prixUnitaire: produit.prix
        });
      }
      
      const updatedCart = {
        userId: userProfile.uid,
        items: newItems,
        totalItems: newItems.reduce((sum, item) => sum + item.quantite, 0)
      };
      
      setCart(updatedCart);
      localStorage.setItem(`cart-${userProfile.uid}`, JSON.stringify(updatedCart));
      showNotification(`✅ ${produit.titre} ajouté au panier!`);
    } catch (localError) {
      console.error('Erreur locale panier:', localError);
      showNotification('❌ Erreur lors de l\'ajout au panier');
    } finally {
      setCartLoading(false);
    }
  };

  // Mettre à jour la quantité
  const updateCartQuantity = async (produitId, quantite) => {
    if (!userProfile?.uid) return;
    
    if (quantite < 1) {
      removeFromCart(produitId);
      return;
    }
    
    setCartLoading(true);
    try {
      const data = await cartService.updateQuantity(userProfile.uid, produitId, quantite);
      if (data) {
        setCart(data);
      } else {
        throw new Error('API failed');
      }
    } catch (error) {
      console.error('Erreur mise à jour quantité:', error);
      const newItems = cart.items.map(item => 
        item.produitId === produitId ? { ...item, quantite } : item
      );
      const updatedCart = {
        ...cart,
        items: newItems,
        totalItems: newItems.reduce((sum, item) => sum + item.quantite, 0)
      };
      setCart(updatedCart);
      localStorage.setItem(`cart-${userProfile.uid}`, JSON.stringify(updatedCart));
    } finally {
      setCartLoading(false);
    }
  };

  // Supprimer du panier
  const removeFromCart = async (produitId) => {
    if (!userProfile?.uid) return;
    setCartLoading(true);
    try {
      await cartService.removeFromCart(userProfile.uid, produitId);
    } catch (error) {
      console.error('Erreur suppression du panier:', error);
    }
    
    const newItems = cart.items.filter(item => item.produitId !== produitId);
    const updatedCart = {
      ...cart,
      items: newItems,
      totalItems: newItems.reduce((sum, item) => sum + item.quantite, 0)
    };
    setCart(updatedCart);
    localStorage.setItem(`cart-${userProfile.uid}`, JSON.stringify(updatedCart));
    setCartLoading(false);
  };

  // Vider le panier
  const clearCart = async () => {
    if (!userProfile?.uid) return;
    try {
      await cartService.clearCart(userProfile.uid);
    } catch (error) {
      console.error('Erreur vidage du panier:', error);
    }
    setCart({ items: [], totalItems: 0 });
    localStorage.removeItem(`cart-${userProfile.uid}`);
  };

  // Calculer le total du panier
  const getCartTotal = () => {
    return (cart.items || []).reduce((total, item) => {
      const produit = produitsMap[item.produitId];
      const prix = item.prixUnitaire || produit?.prix || 0;
      return total + (prix * item.quantite);
    }, 0);
  };

  // Déconnexion
  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  // Valider la commande
  const handleCheckout = async () => {
    if (!adresse.telephone || !adresse.ville || !adresse.quartier) {
      showNotification('⚠️ Veuillez remplir au moins le téléphone, la ville et le quartier');
      return;
    }
    
    try {
      const orderData = {
        userId: userProfile.uid,
        items: cart.items.map(item => {
          const produit = produitsMap[item.produitId];
          return {
            produitId: item.produitId,
            produitTitre: item.produitTitre || produit?.titre,
            produitImage: item.produitImage || produit?.imageUrl || produit?.image,
            quantite: item.quantite,
            prixUnitaire: item.prixUnitaire || produit?.prix
          };
        }),
        currency: 'USD',
        adresseLivraison: adresse
      };

      const order = await orderService.createOrder(orderData);
      setCurrentOrder(order);
      setShowCheckout(false);
      setShowPaiement(true);
    } catch (error) {
      console.error('Erreur création commande:', error);
      showNotification('❌ Erreur lors de la création de la commande');
    }
  };

  // Succès paiement
  const handlePaiementSuccess = async (paiement) => {
    await clearCart();
    await loadOrders();
    await loadPaiements();
    setActiveTab('orders');
    setShowPaiement(false);
    setCurrentOrder(null);
    showNotification('🎉 Paiement effectué avec succès!');
  };

  // Erreur paiement
  const handlePaiementError = (error) => {
    console.error('Erreur paiement:', error);
    showNotification('❌ Erreur lors du paiement. Veuillez réessayer.');
  };

  // Mise à jour du profil
  const handleUpdateProfile = async () => {
    setProfileLoading(true);
    try {
      await userService.updateUser(userProfile.id, {
        ...profileData,
        photoProfil: photoPreview || profileData.photoProfil
      });
      showNotification('✅ Profil mis à jour avec succès!');
      setEditProfile(false);
    } catch (error) {
      console.error('Erreur mise à jour profil:', error);
      showNotification('❌ Erreur lors de la mise à jour du profil');
    } finally {
      setProfileLoading(false);
    }
  };

  // Gérer le changement de photo
  const handlePhotoChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onloadend = () => {
        setPhotoPreview(reader.result);
        setProfileData({ ...profileData, photoProfil: reader.result });
      };
      reader.readAsDataURL(file);
    }
  };

  // Calculer les stats du profil
  const profileStats = useMemo(() => {
    const totalOrders = orders.length;
    const totalSpent = orders.reduce((sum, o) => sum + (o.montantTotal || 0), 0);
    const completedOrders = orders.filter(o => o.orderStatus === 'LIVRE').length;
    const pendingOrders = orders.filter(o => o.orderStatus === 'EN_ATTENTE' || o.orderStatus === 'EN_COURS').length;
    return { totalOrders, totalSpent, completedOrders, pendingOrders };
  }, [orders]);

  return (
    <div className="dashboard-container client">
      {notification && (
        <div className="notification-toast">
          {notification}
        </div>
      )}

      <header className="dashboard-header">
        <h1>🛒 Espace Client</h1>
        <div className="header-actions">
          <button className="cart-icon-btn" onClick={() => setShowCart(true)}>
            🛒 
            {(cart.totalItems || 0) > 0 && <span className="cart-badge">{cart.totalItems}</span>}
          </button>
          <button onClick={handleLogout} className="logout-btn">
            Déconnexion
          </button>
        </div>
      </header>
      
      <main className="dashboard-content">
        <div className="welcome-card">
          <div className="welcome-avatar" onClick={() => setActiveTab('profile')} style={{ cursor: 'pointer' }}>
            {userProfile?.photoProfil ? (
              <img src={userProfile.photoProfil} alt="Profile" />
            ) : (
              <div className="avatar-placeholder">
                {userProfile?.prenom?.charAt(0)}{userProfile?.nom?.charAt(0)}
              </div>
            )}
          </div>
          <div className="welcome-info">
            <h2>Bonjour {userProfile?.prenom} {userProfile?.nom}!</h2>
            <p className="role-badge client">{userProfile?.role}</p>
            <p className="email">{userProfile?.email}</p>
          </div>
        </div>

        <div className="admin-tabs">
          <button 
            className={activeTab === 'products' ? 'active' : ''} 
            onClick={() => setActiveTab('products')}
          >
            🛍️ Produits
          </button>
          <button 
            className={activeTab === 'orders' ? 'active' : ''} 
            onClick={() => setActiveTab('orders')}
          >
            📦 Commandes ({orders.length})
          </button>
          <button 
            className={activeTab === 'profile' ? 'active' : ''} 
            onClick={() => setActiveTab('profile')}
          >
            ⚙️ Mon Profil
          </button>
        </div>

        {/* Onglet Produits */}
        {activeTab === 'products' && (
          <>
            <div className="search-section">
              <div className="search-bar">
                <input
                  type="text"
                  placeholder="Rechercher un produit..."
                  value={searchQuery}
                  onChange={e => setSearchQuery(e.target.value)}
                  onKeyPress={e => e.key === 'Enter' && handleSearch()}
                />
                <button onClick={handleSearch}>🔍</button>
              </div>
              
              <div className="filters-row">
                <div className="categories-filter">
                  <button 
                    className={!selectedCategorie ? 'active' : ''} 
                    onClick={() => handleCategorieFilter('')}
                  >
                    Tous
                  </button>
                  {categories.map(cat => (
                    <button 
                      key={cat}
                      className={selectedCategorie === cat ? 'active' : ''}
                      onClick={() => handleCategorieFilter(cat)}
                    >
                      {cat}
                    </button>
                  ))}
                </div>
                
                <div className="sort-filter">
                  <label>Trier par:</label>
                  <select value={sortBy} onChange={e => setSortBy(e.target.value)}>
                    <option value="default">Par défaut</option>
                    <option value="price-asc">Prix croissant</option>
                    <option value="price-desc">Prix décroissant</option>
                    <option value="name-asc">Nom A-Z</option>
                    <option value="name-desc">Nom Z-A</option>
                    <option value="newest">Plus récent</option>
                  </select>
                </div>
                
                <div className="price-filter">
                  <input
                    type="number"
                    placeholder="Prix min"
                    value={priceRange.min}
                    onChange={e => setPriceRange({...priceRange, min: e.target.value})}
                  />
                  <span>-</span>
                  <input
                    type="number"
                    placeholder="Prix max"
                    value={priceRange.max}
                    onChange={e => setPriceRange({...priceRange, max: e.target.value})}
                  />
                </div>
              </div>
            </div>

            {(cart.items || []).length > 0 && (
              <div className="panier-summary">
                <h3>🛒 Votre Panier ({cart.totalItems} articles)</h3>
                <div className="panier-items">
                  {cart.items.slice(0, 3).map(item => {
                    const produit = produitsMap[item.produitId];
                    const titre = item.produitTitre || produit?.titre || 'Produit';
                    const prix = item.prixUnitaire || produit?.prix || 0;
                    return (
                      <div key={item.produitId} className="panier-item">
                        <span className="item-name">{titre} x{item.quantite}</span>
                        <span className="item-price">{(prix * item.quantite).toFixed(2)} USD</span>
                        <button className="remove-item-btn" onClick={() => removeFromCart(item.produitId)}>🗑️</button>
                      </div>
                    );
                  })}
                  {cart.items.length > 3 && (
                    <p className="more-items">... et {cart.items.length - 3} autres articles</p>
                  )}
                </div>
                <div className="panier-total">
                  <strong>Total: {getCartTotal().toFixed(2)} USD</strong>
                  <button className="checkout-btn" onClick={() => setShowCart(true)}>Voir le panier</button>
                </div>
              </div>
            )}
            
            <div className="products-section">
              <h3>🛍️ Produits Disponibles ({displayedProduits.length})</h3>
              {loading ? (
                <div className="loading-spinner">
                  <div className="spinner"></div>
                  <p>Chargement des produits...</p>
                </div>
              ) : displayedProduits.length === 0 ? (
                <div className="empty-state">
                  <p>Aucun produit disponible pour le moment.</p>
                </div>
              ) : (
                <div className="products-grid client">
                  {displayedProduits.map(produit => (
                    <div key={produit.id} className="product-card client">
                      <div className="product-image" onClick={() => setShowProduitDetail(produit)}>
                        {(produit.imageUrl || produit.image) ? (
                          <img src={produit.imageUrl || produit.image} alt={produit.titre} />
                        ) : (
                          <div className="no-image">📦</div>
                        )}
                        {produit.status === 'EN_PROMOTION' && <span className="promo-badge">PROMO</span>}
                      </div>
                      <div className="product-info" onClick={() => setShowProduitDetail(produit)}>
                        <h4 className="product-title">{produit.titre}</h4>
                        {produit.marque && <p className="product-marque">{produit.marque}</p>}
                        <p className="product-price">{produit.prix} {produit.currency || 'USD'}</p>
                        <div className="product-tags">
                          {produit.categorie && <span className="category-tag">{produit.categorie}</span>}
                        </div>
                        {produit.stock > 0 && produit.stock <= 5 && (
                          <span className="stock-warning">⚠️ Plus que {produit.stock} en stock</span>
                        )}
                      </div>
                      <button 
                        className="add-to-cart-btn" 
                        onClick={(e) => addToCart(produit, e)}
                        disabled={cartLoading || produit.stock === 0}
                      >
                        {cartLoading ? '⏳ Ajout...' : produit.stock === 0 ? '❌ Rupture' : '🛒 Ajouter au panier'}
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </>
        )}

        {/* Onglet Commandes */}
        {activeTab === 'orders' && (
          <div className="client-orders">
            <h3>📦 Mes Commandes ({orders.length})</h3>
            {orders.length === 0 ? (
              <div className="empty-state">
                <p>Vous n'avez pas encore de commandes.</p>
                <button onClick={() => setActiveTab('products')}>Découvrir les produits</button>
              </div>
            ) : (
              <div className="orders-list">
                {orders.map(order => (
                  <div key={order.id} className="order-card">
                    <div className="order-header">
                      <span className="order-id">Commande #{order.id?.substring(0, 8)}</span>
                      <span className={`status-badge ${order.orderStatus?.toLowerCase()}`}>
                        {order.orderStatus === 'EN_ATTENTE' && '⏳ En attente'}
                        {order.orderStatus === 'EN_COURS' && '📦 En cours'}
                        {order.orderStatus === 'EN_ROUTE' && '🚚 En route'}
                        {order.orderStatus === 'LIVRE' && '✅ Livré'}
                        {order.orderStatus === 'ANNULE' && '❌ Annulé'}
                      </span>
                    </div>
                    <div className="order-details">
                      <div className="order-info-row">
                        <span className="label">Montant:</span>
                        <span className="value">{order.montantTotal} {order.currency}</span>
                      </div>
                      <div className="order-info-row">
                        <span className="label">Date:</span>
                        <span className="value">{order.createdAt ? new Date(order.createdAt).toLocaleDateString('fr-FR', { 
                          day: '2-digit', month: 'long', year: 'numeric', hour: '2-digit', minute: '2-digit'
                        }) : 'N/A'}</span>
                      </div>
                      <div className="order-info-row">
                        <span className="label">Paiement:</span>
                        <span className={`status-badge small ${order.paiementStatus?.toLowerCase()}`}>
                          {order.paiementStatus === 'EN_ATTENTE' && '⏳ En attente'}
                          {order.paiementStatus === 'SUCCES' && '✅ Payé'}
                          {order.paiementStatus === 'ECHOUE' && '❌ Échoué'}
                        </span>
                      </div>
                    </div>
                    {order.items && order.items.length > 0 && (
                      <div className="order-items-preview">
                        {order.items.slice(0, 2).map((item, idx) => (
                          <span key={idx} className="order-item-name">{item.produitTitre}</span>
                        ))}
                        {order.items.length > 2 && <span className="more">+{order.items.length - 2} autres</span>}
                      </div>
                    )}
                    {order.adresseLivraison && (
                      <div className="order-address">
                        <strong>📍 Livraison:</strong>
                        <p>{order.adresseLivraison.avenue}, {order.adresseLivraison.quartier}</p>
                        <p>{order.adresseLivraison.commune}, {order.adresseLivraison.ville}</p>
                        <p>📞 {order.adresseLivraison.telephone}</p>
                      </div>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {/* Onglet Profil */}
        {activeTab === 'profile' && (
          <div className="profile-section">
            <div className="profile-header-section">
              <h3>⚙️ Mon Profil</h3>
              <button 
                className={`edit-profile-btn ${editProfile ? 'cancel' : ''}`}
                onClick={() => setEditProfile(!editProfile)}
              >
                {editProfile ? '❌ Annuler' : '✏️ Modifier'}
              </button>
            </div>

            {/* Statistiques */}
            <div className="profile-stats">
              <div className="stat-card">
                <span className="stat-icon">📦</span>
                <div className="stat-info">
                  <h4>{profileStats.totalOrders}</h4>
                  <p>Commandes</p>
                </div>
              </div>
              <div className="stat-card success">
                <span className="stat-icon">✅</span>
                <div className="stat-info">
                  <h4>{profileStats.completedOrders}</h4>
                  <p>Livrées</p>
                </div>
              </div>
              <div className="stat-card warning">
                <span className="stat-icon">⏳</span>
                <div className="stat-info">
                  <h4>{profileStats.pendingOrders}</h4>
                  <p>En cours</p>
                </div>
              </div>
              <div className="stat-card info">
                <span className="stat-icon">💰</span>
                <div className="stat-info">
                  <h4>{profileStats.totalSpent.toFixed(2)} USD</h4>
                  <p>Total dépensé</p>
                </div>
              </div>
            </div>

            {/* Informations personnelles */}
            <div className="profile-card">
              <h4>👤 Informations Personnelles</h4>
              
              <div className="profile-photo-section">
                <div className="profile-photo">
                  {(photoPreview || profileData.photoProfil) ? (
                    <img src={photoPreview || profileData.photoProfil} alt="Profile" />
                  ) : (
                    <div className="photo-placeholder">
                      {profileData.prenom?.charAt(0)}{profileData.nom?.charAt(0)}
                    </div>
                  )}
                </div>
                {editProfile && (
                  <div className="photo-upload">
                    <input 
                      type="file" 
                      id="photo-input"
                      accept="image/*" 
                      onChange={handlePhotoChange}
                      hidden
                    />
                    <label htmlFor="photo-input" className="upload-btn">
                      📷 Changer la photo
                    </label>
                  </div>
                )}
              </div>

              <div className="profile-form">
                <div className="form-row">
                  <div className="form-group">
                    <label>Prénom</label>
                    <input 
                      type="text" 
                      value={profileData.prenom}
                      onChange={e => setProfileData({...profileData, prenom: e.target.value})}
                      disabled={!editProfile}
                    />
                  </div>
                  <div className="form-group">
                    <label>Nom</label>
                    <input 
                      type="text" 
                      value={profileData.nom}
                      onChange={e => setProfileData({...profileData, nom: e.target.value})}
                      disabled={!editProfile}
                    />
                  </div>
                </div>
                <div className="form-row">
                  <div className="form-group">
                    <label>Email</label>
                    <input 
                      type="email" 
                      value={profileData.email}
                      disabled
                      className="readonly"
                    />
                  </div>
                  <div className="form-group">
                    <label>Téléphone</label>
                    <input 
                      type="tel" 
                      value={profileData.telephone || ''}
                      onChange={e => setProfileData({...profileData, telephone: e.target.value})}
                      disabled={!editProfile}
                      placeholder="+243 XXX XXX XXX"
                    />
                  </div>
                </div>

                {editProfile && (
                  <button 
                    className="save-profile-btn" 
                    onClick={handleUpdateProfile}
                    disabled={profileLoading}
                  >
                    {profileLoading ? '⏳ Enregistrement...' : '💾 Enregistrer les modifications'}
                  </button>
                )}
              </div>
            </div>

            {/* Historique des transactions */}
            <div className="profile-card">
              <h4>💳 Historique des Transactions</h4>
              {paiements.length === 0 ? (
                <div className="empty-state small">
                  <p>Aucune transaction pour le moment.</p>
                </div>
              ) : (
                <div className="transactions-list">
                  {paiements.slice(0, 10).map((paiement, idx) => (
                    <div key={paiement.id || idx} className="transaction-item">
                      <div className="transaction-info">
                        <span className="transaction-method">
                          {paiement.methodePaiement === 'MOBILE_MONEY' && '📱'}
                          {paiement.methodePaiement === 'CARTE_BANCAIRE' && '💳'}
                          {paiement.methodePaiement === 'ESPECES' && '💵'}
                          {paiement.methodePaiement === 'VIREMENT' && '🏦'}
                          {' '}{paiement.methodePaiement?.replace('_', ' ')}
                        </span>
                        <span className="transaction-date">
                          {paiement.createdAt ? new Date(paiement.createdAt).toLocaleDateString('fr-FR') : 'N/A'}
                        </span>
                      </div>
                      <div className="transaction-amount">
                        <span className={`amount ${paiement.status === 'SUCCES' ? 'success' : 'pending'}`}>
                          {paiement.montant} {paiement.currency}
                        </span>
                        <span className={`status-badge small ${paiement.status?.toLowerCase()}`}>
                          {paiement.status === 'SUCCES' && '✅'}
                          {paiement.status === 'EN_ATTENTE' && '⏳'}
                          {paiement.status === 'ECHOUE' && '❌'}
                        </span>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* Adresse par défaut */}
            <div className="profile-card">
              <h4>📍 Adresse de Livraison</h4>
              {userProfile?.adresse ? (
                <div className="address-display">
                  <p>{userProfile.adresse.avenue}, {userProfile.adresse.quartier}</p>
                  <p>{userProfile.adresse.commune}, {userProfile.adresse.ville}</p>
                  <p>{userProfile.adresse.province}, {userProfile.adresse.pays}</p>
                </div>
              ) : (
                <div className="empty-state small">
                  <p>Aucune adresse enregistrée.</p>
                  {editProfile && <p>Vous pouvez l'ajouter lors de votre prochaine commande.</p>}
                </div>
              )}
            </div>
          </div>
        )}
      </main>

      {/* Modal détail produit */}
      {showProduitDetail && (
        <div className="modal-overlay" onClick={() => setShowProduitDetail(null)}>
          <div className="modal-content product-detail-modal" onClick={e => e.stopPropagation()}>
            <button className="close-modal" onClick={() => setShowProduitDetail(null)}>×</button>
            <div className="product-detail-content">
              <div className="product-detail-image">
                {(showProduitDetail.imageUrl || showProduitDetail.image) ? (
                  <img src={showProduitDetail.imageUrl || showProduitDetail.image} alt={showProduitDetail.titre} />
                ) : (
                  <div className="no-image large">📦</div>
                )}
              </div>
              <div className="product-detail-info">
                <h2>{showProduitDetail.titre}</h2>
                
                <div className="product-meta">
                  {showProduitDetail.marque && (
                    <div className="meta-item">
                      <span className="meta-label">Marque:</span>
                      <span className="meta-value">{showProduitDetail.marque}</span>
                    </div>
                  )}
                  {showProduitDetail.categorie && (
                    <div className="meta-item">
                      <span className="meta-label">Catégorie:</span>
                      <span className="meta-value">{showProduitDetail.categorie}</span>
                    </div>
                  )}
                  {showProduitDetail.sousCategorie && (
                    <div className="meta-item">
                      <span className="meta-label">Sous-catégorie:</span>
                      <span className="meta-value">{showProduitDetail.sousCategorie}</span>
                    </div>
                  )}
                </div>
                
                <p className="price-large">{showProduitDetail.prix} {showProduitDetail.currency || 'USD'}</p>
                
                {showProduitDetail.description && (
                  <div className="product-description">
                    <h4>Description</h4>
                    <p>{showProduitDetail.description}</p>
                  </div>
                )}
                
                {showProduitDetail.specifications && Object.keys(showProduitDetail.specifications).length > 0 && (
                  <div className="product-specifications">
                    <h4>Spécifications</h4>
                    <table className="specs-table">
                      <tbody>
                        {Object.entries(showProduitDetail.specifications).map(([key, value]) => (
                          <tr key={key}>
                            <td className="spec-key">{key}</td>
                            <td className="spec-value">{value}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
                
                <div className="stock-info">
                  <span className="label">Stock disponible:</span>
                  <span className={`stock-value ${showProduitDetail.stock <= 5 ? 'low' : ''}`}>
                    {showProduitDetail.stock} unités
                  </span>
                </div>
                
                <button 
                  className="add-to-cart-btn large" 
                  onClick={(e) => {
                    addToCart(showProduitDetail, e);
                    setShowProduitDetail(null);
                  }} 
                  disabled={cartLoading || showProduitDetail.stock === 0}
                >
                  {cartLoading ? '⏳ Ajout...' : showProduitDetail.stock === 0 ? '❌ Rupture de stock' : '🛒 Ajouter au panier'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Cart Sidebar */}
      {showCart && (
        <div className="cart-overlay" onClick={() => setShowCart(false)}>
          <div className="cart-sidebar" onClick={e => e.stopPropagation()}>
            <div className="cart-header">
              <h2>🛒 Mon Panier</h2>
              <button className="close-btn" onClick={() => setShowCart(false)}>×</button>
            </div>

            {cartLoading && <div className="cart-loading">⏳ Chargement...</div>}

            {(cart.items || []).length === 0 ? (
              <div className="cart-empty">
                <span className="empty-icon">🛒</span>
                <p>Votre panier est vide</p>
                <button onClick={() => { setShowCart(false); setActiveTab('products'); }}>
                  Découvrir les produits
                </button>
              </div>
            ) : (
              <>
                <div className="cart-items">
                  {cart.items.map(item => {
                    const produit = produitsMap[item.produitId];
                    const titre = item.produitTitre || produit?.titre || 'Produit';
                    const image = item.produitImage || produit?.imageUrl || produit?.image;
                    const prix = item.prixUnitaire || produit?.prix || 0;
                    return (
                      <div key={item.produitId} className="cart-item">
                        <div className="item-image">
                          {image ? (
                            <img src={image} alt={titre} />
                          ) : (
                            <div className="no-image">📦</div>
                          )}
                        </div>
                        <div className="item-details">
                          <h4>{titre}</h4>
                          <p className="item-price">{prix} USD</p>
                          <div className="quantity-controls">
                            <button 
                              onClick={() => updateCartQuantity(item.produitId, item.quantite - 1)}
                              disabled={cartLoading}
                            >-</button>
                            <span>{item.quantite}</span>
                            <button 
                              onClick={() => updateCartQuantity(item.produitId, item.quantite + 1)}
                              disabled={cartLoading}
                            >+</button>
                          </div>
                        </div>
                        <div className="item-total">
                          <span>{(prix * item.quantite).toFixed(2)} USD</span>
                          <button className="remove-btn" onClick={() => removeFromCart(item.produitId)}>🗑️</button>
                        </div>
                      </div>
                    );
                  })}
                </div>

                <div className="cart-footer">
                  <div className="cart-summary">
                    <span>Total ({cart.totalItems} articles)</span>
                    <span className="total-amount">{getCartTotal().toFixed(2)} USD</span>
                  </div>
                  <button className="clear-cart-btn" onClick={clearCart}>🗑️ Vider le panier</button>
                  <button className="checkout-btn" onClick={() => { setShowCart(false); setShowCheckout(true); }}>
                    Commander 🛒
                  </button>
                </div>
              </>
            )}
          </div>
        </div>
      )}

      {/* Modal Checkout */}
      {showCheckout && (cart.items || []).length > 0 && (
        <div className="modal-overlay" onClick={() => setShowCheckout(false)}>
          <div className="modal-content checkout-modal" onClick={e => e.stopPropagation()}>
            <button className="close-modal" onClick={() => setShowCheckout(false)}>×</button>
            <h2>📦 Finaliser la commande</h2>
            <div className="checkout-content">
              <div className="checkout-summary">
                <h3>Résumé de la commande</h3>
                <div className="checkout-items">
                  {cart.items.map(item => {
                    const produit = produitsMap[item.produitId];
                    const titre = item.produitTitre || produit?.titre || 'Produit';
                    const prix = item.prixUnitaire || produit?.prix || 0;
                    return (
                      <div key={item.produitId} className="checkout-item">
                        <span className="item-name">{titre} x{item.quantite}</span>
                        <span className="item-total">{(prix * item.quantite).toFixed(2)} USD</span>
                      </div>
                    );
                  })}
                </div>
                <div className="checkout-total">
                  <strong>Total à payer:</strong>
                  <strong className="total-value">{getCartTotal().toFixed(2)} USD</strong>
                </div>
              </div>
              
              <div className="checkout-form">
                <h3>📍 Adresse de livraison</h3>
                <div className="form-grid">
                  <div className="form-group full-width">
                    <label>Nom du destinataire</label>
                    <input 
                      placeholder="Nom complet du destinataire" 
                      value={adresse.nomDestinataire} 
                      onChange={e => setAdresse({...adresse, nomDestinataire: e.target.value})} 
                    />
                  </div>
                  <div className="form-group">
                    <label>Téléphone *</label>
                    <input 
                      placeholder="+243 XXX XXX XXX" 
                      value={adresse.telephone} 
                      onChange={e => setAdresse({...adresse, telephone: e.target.value})} 
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label>Pays</label>
                    <input 
                      placeholder="Ex: RDC" 
                      value={adresse.pays} 
                      onChange={e => setAdresse({...adresse, pays: e.target.value})} 
                    />
                  </div>
                  <div className="form-group">
                    <label>Province</label>
                    <input 
                      placeholder="Ex: Kinshasa" 
                      value={adresse.province} 
                      onChange={e => setAdresse({...adresse, province: e.target.value})} 
                    />
                  </div>
                  <div className="form-group">
                    <label>Ville *</label>
                    <input 
                      placeholder="Ex: Kinshasa" 
                      value={adresse.ville} 
                      onChange={e => setAdresse({...adresse, ville: e.target.value})} 
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label>Commune</label>
                    <input 
                      placeholder="Ex: Gombe" 
                      value={adresse.commune} 
                      onChange={e => setAdresse({...adresse, commune: e.target.value})} 
                    />
                  </div>
                  <div className="form-group">
                    <label>Quartier *</label>
                    <input 
                      placeholder="Ex: Centre-ville" 
                      value={adresse.quartier} 
                      onChange={e => setAdresse({...adresse, quartier: e.target.value})} 
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label>Avenue/Rue</label>
                    <input 
                      placeholder="Ex: Avenue du Commerce" 
                      value={adresse.avenue} 
                      onChange={e => setAdresse({...adresse, avenue: e.target.value})} 
                    />
                  </div>
                  <div className="form-group full-width">
                    <label>Point de référence</label>
                    <input 
                      placeholder="Ex: En face de la pharmacie" 
                      value={adresse.reference} 
                      onChange={e => setAdresse({...adresse, reference: e.target.value})} 
                    />
                  </div>
                </div>
                <button className="checkout-btn large" onClick={handleCheckout}>
                  ✅ Confirmer et payer
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Modal Paiement */}
      <PaiementModal
        isOpen={showPaiement}
        onClose={() => setShowPaiement(false)}
        orderId={currentOrder?.id}
        userId={userProfile?.uid}
        montant={currentOrder?.montantTotal || getCartTotal()}
        currency="USD"
        onSuccess={handlePaiementSuccess}
        onError={handlePaiementError}
      />
    </div>
  );
};

export default DashboardClient;
