import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import './Dashboard.css';

const DashboardAdmin = () => {
  const { userProfile, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

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
        
        <div className="dashboard-grid">
          <div className="dashboard-card">
            <h3>👥 Utilisateurs</h3>
            <p>Gérer les comptes</p>
          </div>
          <div className="dashboard-card">
            <h3>🏪 Vendeurs</h3>
            <p>Valider les vendeurs</p>
          </div>
          <div className="dashboard-card">
            <h3>📦 Produits</h3>
            <p>Modérer le catalogue</p>
          </div>
          <div className="dashboard-card">
            <h3>📊 Statistiques</h3>
            <p>Vue d'ensemble</p>
          </div>
          <div className="dashboard-card">
            <h3>💳 Paiements</h3>
            <p>Transactions</p>
          </div>
          <div className="dashboard-card">
            <h3>🔧 Paramètres</h3>
            <p>Configuration</p>
          </div>
        </div>
      </main>
    </div>
  );
};

export default DashboardAdmin;
