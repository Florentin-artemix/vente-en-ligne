import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import './Dashboard.css';

const DashboardClient = () => {
  const { userProfile, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <div className="dashboard-container">
      <header className="dashboard-header">
        <h1>🛒 Espace Client</h1>
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
              <div className="avatar-placeholder">
                {userProfile?.prenom?.charAt(0)}{userProfile?.nom?.charAt(0)}
              </div>
            )}
          </div>
          <div className="welcome-info">
            <h2>Bonjour {userProfile?.prenom} {userProfile?.nom}!</h2>
            <p className="role-badge client">Je suis {userProfile?.role}</p>
            <p className="email">{userProfile?.email}</p>
          </div>
        </div>
        
        <div className="dashboard-grid">
          <div className="dashboard-card">
            <h3>🛍️ Mes Commandes</h3>
            <p>Suivez vos commandes en cours</p>
          </div>
          <div className="dashboard-card">
            <h3>🛒 Mon Panier</h3>
            <p>Articles dans votre panier</p>
          </div>
          <div className="dashboard-card">
            <h3>❤️ Favoris</h3>
            <p>Produits que vous aimez</p>
          </div>
          <div className="dashboard-card">
            <h3>👤 Mon Profil</h3>
            <p>Gérez vos informations</p>
          </div>
        </div>
      </main>
    </div>
  );
};

export default DashboardClient;
