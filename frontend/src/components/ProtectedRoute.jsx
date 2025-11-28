import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const ProtectedRoute = ({ children, allowedRoles }) => {
  const { currentUser, userProfile, loading } = useAuth();

  if (loading) {
    return (
      <div style={{ 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center', 
        height: '100vh' 
      }}>
        <p>Chargement...</p>
      </div>
    );
  }

  if (!currentUser) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && userProfile && !allowedRoles.includes(userProfile.role)) {
    // Rediriger vers le dashboard approprié selon le rôle
    switch (userProfile.role) {
      case 'ADMIN':
        return <Navigate to="/dashboard/admin" replace />;
      case 'VENDEUR':
        return <Navigate to="/dashboard/vendeur" replace />;
      default:
        return <Navigate to="/dashboard/client" replace />;
    }
  }

  return children;
};

export default ProtectedRoute;
