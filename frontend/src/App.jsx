import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Login from './pages/Login';
import Register from './pages/Register';
import DashboardClient from './pages/DashboardClient';
import DashboardVendeur from './pages/DashboardVendeur';
import DashboardAdmin from './pages/DashboardAdmin';
import './App.css';

function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          {/* Routes publiques */}
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          
          {/* Routes protégées par rôle */}
          <Route 
            path="/dashboard/client" 
            element={
              <ProtectedRoute allowedRoles={['CLIENT']}>
                <DashboardClient />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/dashboard/vendeur" 
            element={
              <ProtectedRoute allowedRoles={['VENDEUR']}>
                <DashboardVendeur />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/dashboard/admin" 
            element={
              <ProtectedRoute allowedRoles={['ADMIN']}>
                <DashboardAdmin />
              </ProtectedRoute>
            } 
          />
          
          {/* Redirection par défaut */}
          <Route path="/" element={<Navigate to="/login" replace />} />
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;

