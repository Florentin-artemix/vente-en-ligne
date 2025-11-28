import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import './Dashboard.css';

const DashboardVendeur = () => {
  const { userProfile, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <div className="dashboard-container vendeur">
      <header className="dashboard-header">
        <h1>🏪 Espace Vendeur</h1>
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
              <div className="avatar-placeholder vendeur">
                {userProfile?.prenom?.charAt(0)}{userProfile?.nom?.charAt(0)}
              </div>
            )}
          </div>
          <div className="welcome-info">
            <h2>Bonjour {userProfile?.prenom} {userProfile?.nom}!</h2>
            <p className="role-badge vendeur">Je suis {userProfile?.role}</p>
            <p className="email">{userProfile?.email}</p>
          </div>
        </div>
        
        <div className="dashboard-grid">
          <div className="dashboard-card">
            <h3>📦 Mes Produits</h3>
            <p>Gérez votre catalogue</p>
          </div>
          <div className="dashboard-card">
            <h3>📊 Commandes Reçues</h3>
            <p>Commandes à traiter</p>
          </div>
          <div className="dashboard-card">
            <h3>💰 Revenus</h3>
            <p>Statistiques de vente</p>
          </div>
          <div className="dashboard-card">
            <h3>➕ Ajouter Produit</h3>
            <p>Nouveau produit</p>
          </div>
        </div>
      </main>
    </div>
  );
};

export default DashboardVendeur;
