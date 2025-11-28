import { createContext, useContext, useState, useEffect } from 'react';
import { auth } from '../config/firebase';
import { 
  signInWithEmailAndPassword, 
  signOut, 
  onAuthStateChanged 
} from 'firebase/auth';
import { userService } from '../services/api';

const AuthContext = createContext();

export const useAuth = () => {
  return useContext(AuthContext);
};

export const AuthProvider = ({ children }) => {
  const [currentUser, setCurrentUser] = useState(null);
  const [userProfile, setUserProfile] = useState(null);
  const [loading, setLoading] = useState(true);

  // Inscription
  const register = async (userData) => {
    try {
      const response = await userService.register(userData);
      localStorage.setItem('authToken', response.token);
      setUserProfile(response.user);
      return response;
    } catch (error) {
      throw error;
    }
  };

  // Connexion
  const login = async (email, password) => {
    try {
      const userCredential = await signInWithEmailAndPassword(auth, email, password);
      const token = await userCredential.user.getIdToken();
      localStorage.setItem('authToken', token);
      
      // Récupérer le profil utilisateur depuis notre backend
      const profile = await userService.verifyToken(token);
      setUserProfile(profile);
      
      return { user: userCredential.user, profile };
    } catch (error) {
      throw error;
    }
  };

  // Déconnexion
  const logout = async () => {
    try {
      await signOut(auth);
      localStorage.removeItem('authToken');
      setCurrentUser(null);
      setUserProfile(null);
    } catch (error) {
      throw error;
    }
  };

  // Observer les changements d'authentification
  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, async (user) => {
      setCurrentUser(user);
      
      if (user) {
        try {
          const token = await user.getIdToken();
          localStorage.setItem('authToken', token);
          const profile = await userService.verifyToken(token);
          setUserProfile(profile);
        } catch (error) {
          console.error('Erreur lors de la récupération du profil:', error);
        }
      } else {
        setUserProfile(null);
      }
      
      setLoading(false);
    });

    return unsubscribe;
  }, []);

  const value = {
    currentUser,
    userProfile,
    register,
    login,
    logout,
    loading
  };

  return (
    <AuthContext.Provider value={value}>
      {!loading && children}
    </AuthContext.Provider>
  );
};
