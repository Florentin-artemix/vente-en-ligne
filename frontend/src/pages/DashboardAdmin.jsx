import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { produitService, userService, orderService, paiementService, METHODES_PAIEMENT, PAIEMENT_STATUS } from '../services/api';
import './Dashboard.css';

const DashboardAdmin = () => {
  const { userProfile, logout } = useAuth();
  const navigate = useNavigate();

  const [activeTab, setActiveTab] = useState('overview');
  const [produits, setProduits] = useState([]);
  const [stats, setStats] = useState({
    totalProduits: 0,
    produitsDisponibles: 0,
    produitsRupture: 0
  });
  const [orders, setOrders] = useState([]);
  const [orderStats, setOrderStats] = useState(null);
  const [paiements, setPaiements] = useState([]);
  const [paiementStats, setPaiementStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState({ type: '', text: '' });

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    try {
      const produitsData = await produitService.getAllProduits();
      setProduits(produitsData);
      
      // Calculer les stats
      setStats({
        totalProduits: produitsData.length,
        produitsDisponibles: produitsData.filter(p => p.status === 'DISPONIBLE').length,
        produitsRupture: produitsData.filter(p => p.status === 'RUPTURE_STOCK').length
      });

      // Charger les commandes
      try {
        const ordersData = await orderService.getAllOrders();
        setOrders(ordersData);
        
        const statsData = await orderService.getOrderStats();
        setOrderStats(statsData);
      } catch (error) {
        console.error('Erreur chargement commandes:', error);
      }

      // Charger les paiements
      try {
        const paiementsData = await paiementService.getPaiementsByStatus('EN_ATTENTE');
        setPaiements(paiementsData);
        
        const pStatsData = await paiementService.getStats();
        setPaiementStats(pStatsData);
      } catch (error) {
        console.error('Erreur chargement paiements:', error);
      }
    } catch (error) {
      console.error('Erreur chargement données:', error);
      setMessage({ type: 'error', text: 'Erreur lors du chargement des données' });
    } finally {
      setLoading(false);
    }
  };

  const handleUpdateProduitStatus = async (id, newStatus) => {
    try {
      await produitService.updateStatus(id, newStatus);
      setMessage({ type: 'success', text: 'Statut mis à jour!' });
      loadData();
    } catch (error) {
      console.error('Erreur mise à jour statut:', error);
      setMessage({ type: 'error', text: 'Erreur lors de la mise à jour' });
    }
  };

  const handleDeleteProduit = async (id) => {
    if (window.confirm('Voulez-vous vraiment supprimer ce produit?')) {
      try {
        await produitService.deleteProduit(id);
        setMessage({ type: 'success', text: 'Produit supprimé!' });
        loadData();
      } catch (error) {
        console.error('Erreur suppression:', error);
        setMessage({ type: 'error', text: 'Erreur lors de la suppression' });
      }
    }
  };

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  const handleUpdateOrderStatus = async (orderId, statusData) => {
    try {
      await orderService.updateOrderStatus(orderId, statusData);
      setMessage({ type: 'success', text: 'Statut de commande mis à jour!' });
      loadData();
    } catch (error) {
      console.error('Erreur mise à jour commande:', error);
      setMessage({ type: 'error', text: 'Erreur lors de la mise à jour' });
    }
  };

  const handleUpdatePaiementStatus = async (paiementId, newStatus) => {
    try {
      if (newStatus === 'SUCCES') {
        await paiementService.confirmPaiement(paiementId, { transactionReference: `ADMIN-${Date.now()}` });
      } else if (newStatus === 'ECHOUE') {
        await paiementService.failPaiement(paiementId, { errorMessage: 'Rejeté par admin' });
      }
      setMessage({ type: 'success', text: 'Statut de paiement mis à jour!' });
      loadData();
    } catch (error) {
      console.error('Erreur mise à jour paiement:', error);
      setMessage({ type: 'error', text: 'Erreur lors de la mise à jour du paiement' });
    }
  };

  const loadAllPaiements = async () => {
    try {
      // Charger tous les paiements par statut
      const [enAttente, succes, echoue] = await Promise.all([
        paiementService.getPaiementsByStatus('EN_ATTENTE').catch(() => []),
        paiementService.getPaiementsByStatus('SUCCES').catch(() => []),
        paiementService.getPaiementsByStatus('ECHOUE').catch(() => [])
      ]);
      setPaiements([...enAttente, ...succes, ...echoue]);
    } catch (error) {
      console.error('Erreur chargement paiements:', error);
    }
  };

  const renderPaiements = () => (
    <div className="admin-paiements">
      <h3>💳 Gestion des Paiements ({paiements.length})</h3>
      
      {/* Stats de paiement */}
      {paiementStats && (
        <div className="paiement-stats-grid">
          <div className="stat-mini">
            <span className="stat-label">Total</span>
            <span className="stat-value">{paiementStats.totalPaiements || 0}</span>
          </div>
          <div className="stat-mini success">
            <span className="stat-label">Réussis</span>
            <span className="stat-value">{paiementStats.paiementsSucces || 0}</span>
          </div>
          <div className="stat-mini warning">
            <span className="stat-label">En attente</span>
            <span className="stat-value">{paiementStats.paiementsEnAttente || 0}</span>
          </div>
          <div className="stat-mini danger">
            <span className="stat-label">Échoués</span>
            <span className="stat-value">{paiementStats.paiementsEchoue || 0}</span>
          </div>
          <div className="stat-mini info">
            <span className="stat-label">Montant Total</span>
            <span className="stat-value">{paiementStats.montantTotal?.toFixed(2) || 0} $</span>
          </div>
        </div>
      )}

      {loading ? (
        <div className="loading">Chargement...</div>
      ) : (
        <div className="admin-table">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Commande</th>
                <th>Client</th>
                <th>Montant</th>
                <th>Méthode</th>
                <th>Statut</th>
                <th>Date</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {paiements.map(paiement => (
                <tr key={paiement.id}>
                  <td>{paiement.id?.substring(0, 8)}...</td>
                  <td>{paiement.orderId?.substring(0, 8)}...</td>
                  <td>{paiement.userId?.substring(0, 8)}...</td>
                  <td>{paiement.montant} {paiement.currency}</td>
                  <td>
                    <span className="method-badge">
                      {METHODES_PAIEMENT[paiement.methode]?.icon} {METHODES_PAIEMENT[paiement.methode]?.label || paiement.methode}
                    </span>
                  </td>
                  <td>
                    <span className={`status-badge ${paiement.status?.toLowerCase()}`}>
                      {PAIEMENT_STATUS[paiement.status] || paiement.status}
                    </span>
                  </td>
                  <td>{new Date(paiement.createdAt).toLocaleDateString()}</td>
                  <td>
                    {paiement.status === 'EN_ATTENTE' && (
                      <>
                        <button 
                          className="action-btn success" 
                          onClick={() => handleUpdatePaiementStatus(paiement.id, 'SUCCES')}
                          title="Confirmer"
                        >
                          ✅
                        </button>
                        <button 
                          className="action-btn danger" 
                          onClick={() => handleUpdatePaiementStatus(paiement.id, 'ECHOUE')}
                          title="Rejeter"
                        >
                          ❌
                        </button>
                      </>
                    )}
                    <button 
                      className="action-btn view" 
                      onClick={() => alert(`Ref: ${paiement.transactionReference || 'N/A'}\nRéponse: ${JSON.stringify(paiement.providerResponse || {})}`)}
                      title="Voir détails"
                    >
                      👁️
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );

  const renderOrders = () => (
    <div className="admin-orders">
      <h3>🛒 Gestion des Commandes ({orders.length})</h3>
      {loading ? (
        <div className="loading">Chargement...</div>
      ) : (
        <div className="admin-table">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Client</th>
                <th>Montant</th>
                <th>Statut Paiement</th>
                <th>Statut Commande</th>
                <th>Date</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {orders.map(order => (
                <tr key={order.id}>
                  <td>{order.id.substring(0, 8)}...</td>
                  <td>{order.userId.substring(0, 8)}...</td>
                  <td>{order.montantTotal} {order.currency}</td>
                  <td>
                    <span className={`status-badge ${order.paiementStatus?.toLowerCase()}`}>
                      {order.paiementStatus}
                    </span>
                  </td>
                  <td>
                    <select 
                      value={order.orderStatus}
                      onChange={(e) => handleUpdateOrderStatus(order.id, { orderStatus: e.target.value })}
                      className={`status-select ${order.orderStatus?.toLowerCase()}`}
                    >
                      <option value="EN_ATTENTE">En Attente</option>
                      <option value="EN_COURS">En Cours</option>
                      <option value="EN_ROUTE">En Route</option>
                      <option value="LIVRE">Livré</option>
                      <option value="ANNULE">Annulé</option>
                    </select>
                  </td>
                  <td>{new Date(order.createdAt).toLocaleDateString()}</td>
                  <td>
                    <button 
                      className="action-btn view" 
                      onClick={() => alert(`Détails de la commande: ${order.id}`)}
                      title="Voir détails"
                    >
                      👁️
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );

  const renderOverview = () => (
    <div className="admin-overview">
      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-icon">📦</div>
          <div className="stat-info">
            <h3>{stats.totalProduits}</h3>
            <p>Total Produits</p>
          </div>
        </div>
        <div className="stat-card success">
          <div className="stat-icon">✅</div>
          <div className="stat-info">
            <h3>{stats.produitsDisponibles}</h3>
            <p>Disponibles</p>
          </div>
        </div>
        <div className="stat-card warning">
          <div className="stat-icon">⚠️</div>
          <div className="stat-info">
            <h3>{stats.produitsRupture}</h3>
            <p>En Rupture</p>
          </div>
        </div>
        <div className="stat-card info">
          <div className="stat-icon">🛒</div>
          <div className="stat-info">
            <h3>{orderStats?.totalOrders || 0}</h3>
            <p>Total Commandes</p>
          </div>
        </div>
        <div className="stat-card success">
          <div className="stat-icon">💰</div>
          <div className="stat-info">
            <h3>{orderStats?.revenus || 0} $</h3>
            <p>Revenus</p>
          </div>
        </div>
        <div className="stat-card info">
          <div className="stat-icon">💳</div>
          <div className="stat-info">
            <h3>{paiementStats?.paiementsSucces || 0}</h3>
            <p>Paiements Réussis</p>
          </div>
        </div>
        <div className="stat-card warning">
          <div className="stat-icon">⏳</div>
          <div className="stat-info">
            <h3>{paiementStats?.paiementsEnAttente || 0}</h3>
            <p>Paiements En Attente</p>
          </div>
        </div>
      </div>
      
      <div className="recent-section">
        <h3>📦 Derniers Produits Ajoutés</h3>
        <div className="recent-list">
          {produits.slice(0, 5).map(produit => (
            <div key={produit.id} className="recent-item">
              <div className="item-info">
                <span className="item-title">{produit.titre}</span>
                <span className="item-price">{produit.prix} {produit.currency}</span>
              </div>
              <span className={`status-badge ${produit.status?.toLowerCase()}`}>
                {produit.status}
              </span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );

  const renderProduits = () => (
    <div className="admin-produits">
      <h3>📦 Gestion des Produits ({produits.length})</h3>
      {loading ? (
        <div className="loading">Chargement...</div>
      ) : (
        <div className="admin-table">
          <table>
            <thead>
              <tr>
                <th>Produit</th>
                <th>Prix</th>
                <th>Stock</th>
                <th>Catégorie</th>
                <th>Statut</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {produits.map(produit => (
                <tr key={produit.id}>
                  <td>
                    <div className="product-cell">
                      {produit.imageUrl && <img src={produit.imageUrl} alt="" />}
                      <span>{produit.titre}</span>
                    </div>
                  </td>
                  <td>{produit.prix} {produit.currency}</td>
                  <td>{produit.stock}</td>
                  <td>{produit.categorie || '-'}</td>
                  <td>
                    <select 
                      value={produit.status}
                      onChange={(e) => handleUpdateProduitStatus(produit.id, e.target.value)}
                      className={`status-select ${produit.status?.toLowerCase()}`}
                    >
                      <option value="DISPONIBLE">Disponible</option>
                      <option value="RUPTURE_STOCK">Rupture</option>
                      <option value="EN_PROMOTION">Promotion</option>
                      <option value="DESACTIVE">Désactivé</option>
                    </select>
                  </td>
                  <td>
                    <button 
                      className="action-btn delete" 
                      onClick={() => handleDeleteProduit(produit.id)}
                      title="Supprimer"
                    >
                      🗑️
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );

  return (
    <div className="dashboard-container admin">
      <header className="dashboard-header">
        <h1>⚙️ Administration</h1>
        <button onClick={handleLogout} className="logout-btn">
          Déconnexion
        </button>
      </header>
      
      <main className="dashboard-content">
        <div className="welcome-card">
          <div className="welcome-avatar">
            {userProfile?.photoProfil ? (
              <img src={userProfile.photoProfil} alt="Profile" />
            ) : (
              <div className="avatar-placeholder admin">
                {userProfile?.prenom?.charAt(0)}{userProfile?.nom?.charAt(0)}
              </div>
            )}
          </div>
          <div className="welcome-info">
            <h2>Bonjour {userProfile?.prenom} {userProfile?.nom}!</h2>
            <p className="role-badge admin">Je suis {userProfile?.role}</p>
            <p className="email">{userProfile?.email}</p>
          </div>
        </div>

        {message.text && (
          <div className={`message ${message.type}`}>
            {message.text}
            <button onClick={() => setMessage({ type: '', text: '' })}>×</button>
          </div>
        )}

        {/* Navigation tabs */}
        <div className="admin-tabs">
          <button 
            className={activeTab === 'overview' ? 'active' : ''} 
            onClick={() => setActiveTab('overview')}
          >
            📊 Vue d'ensemble
          </button>
          <button 
            className={activeTab === 'produits' ? 'active' : ''} 
            onClick={() => setActiveTab('produits')}
          >
            📦 Produits
          </button>
          <button 
            className={activeTab === 'users' ? 'active' : ''} 
            onClick={() => setActiveTab('users')}
          >
            👥 Utilisateurs
          </button>
          <button 
            className={activeTab === 'orders' ? 'active' : ''} 
            onClick={() => setActiveTab('orders')}
          >
            🛒 Commandes
          </button>
          <button 
            className={activeTab === 'paiements' ? 'active' : ''} 
            onClick={() => { setActiveTab('paiements'); loadAllPaiements(); }}
          >
            💳 Paiements
          </button>
        </div>

        {/* Tab content */}
        <div className="tab-content">
          {activeTab === 'overview' && renderOverview()}
          {activeTab === 'produits' && renderProduits()}
          {activeTab === 'orders' && renderOrders()}
          {activeTab === 'paiements' && renderPaiements()}
          {activeTab === 'users' && (
            <div className="admin-users">
              <h3>👥 Gestion des Utilisateurs</h3>
              <p className="coming-soon">Fonctionnalité à venir...</p>
            </div>
          )}
        </div>
      </main>
    </div>
  );
};

export default DashboardAdmin;
