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
      console.log('Profil récupéré depuis le backend:', profile);
      
      // Mapper id vers uid pour cohérence avec Firebase
      const normalizedProfile = { ...profile, uid: profile.id || profile.uid };
      console.log('Profil normalisé:', normalizedProfile);
      setUserProfile(normalizedProfile);
      
      return { user: userCredential.user, profile: normalizedProfile };
    } catch (error) {
      console.error('Erreur login:', error);
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

  // Rafraîchir le profil utilisateur
  const refreshUserProfile = async () => {
    if (!currentUser) return null;
    try {
      const token = await currentUser.getIdToken(true); // Force refresh token
      localStorage.setItem('authToken', token);
      const profile = await userService.verifyToken(token);
      const normalizedProfile = { ...profile, uid: profile.id || profile.uid };
      setUserProfile(normalizedProfile);
      return normalizedProfile;
    } catch (error) {
      console.error('Erreur lors du rafraîchissement du profil:', error);
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
          // Mapper id vers uid pour cohérence avec Firebase
          const normalizedProfile = { ...profile, uid: profile.id || profile.uid };
          setUserProfile(normalizedProfile);
        } catch (error) {
          console.error('Erreur lors de la récupération du profil:', error);
          // En cas d'erreur, utiliser au moins les données Firebase de base
          setUserProfile({
            uid: user.uid,
            email: user.email,
            prenom: user.displayName?.split(' ')[0] || '',
            nom: user.displayName?.split(' ').slice(1).join(' ') || '',
            photoProfil: user.photoURL || ''
          });
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
    refreshUserProfile,
    loading
  };

  return (
    <AuthContext.Provider value={value}>
      {!loading && children}
    </AuthContext.Provider>
  );
};
