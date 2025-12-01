import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Auth.css';

const Register = () => {
  const navigate = useNavigate();
  const { register } = useAuth();
  
  const [formData, setFormData] = useState({
    nom: '',
    prenom: '',
    email: '',
    password: '',
    confirmPassword: '',
    role: 'CLIENT',
    telephone: '',
    adresse: {
      pays: '',
      province: '',
      ville: '',
      commune: '',
      quartier: '',
      avenue: '',
      reference: ''
    }
  });
  
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [showAddress, setShowAddress] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    
    if (name.startsWith('adresse.')) {
      const addressField = name.split('.')[1];
      setFormData(prev => ({
        ...prev,
        adresse: {
          ...prev.adresse,
          [addressField]: value
        }
      }));
    } else {
      setFormData(prev => ({
        ...prev,
        [name]: value
      }));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    // Validation
    if (formData.password !== formData.confirmPassword) {
      return setError('Les mots de passe ne correspondent pas');
    }

    if (formData.password.length < 6) {
      return setError('Le mot de passe doit contenir au moins 6 caractères');
    }

    setLoading(true);

    try {
      const { confirmPassword, ...userData } = formData;
      await register(userData);
      
      // Redirection selon le rôle
      switch (formData.role) {
        case 'ADMIN':
          navigate('/dashboard/admin');
          break;
        case 'VENDEUR':
          navigate('/dashboard/vendeur');
          break;
        default:
          navigate('/dashboard/client');
      }
    } catch (err) {
      console.error('Erreur inscription:', err);
      
      // Messages d'erreur personnalisés pour Firebase
      if (err.code === 'auth/email-already-in-use') {
        setError('Cet email est déjà utilisé. Essayez de vous connecter.');
      } else if (err.code === 'auth/invalid-email') {
        setError('Adresse email invalide');
      } else if (err.code === 'auth/weak-password') {
        setError('Le mot de passe doit contenir au moins 6 caractères');
      } else if (err.code === 'auth/operation-not-allowed') {
        setError('L\'inscription par email/mot de passe n\'est pas activée');
      } else {
        setError(err.response?.data?.message || err.message || 'Erreur lors de l\'inscription');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <h1 className="auth-title">Créer un compte</h1>
        <p className="auth-subtitle">Rejoignez notre plateforme de vente en ligne</p>
        
        {error && <div className="error-message">{error}</div>}
        
        <form onSubmit={handleSubmit} className="auth-form">
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="prenom">Prénom *</label>
              <input
                type="text"
                id="prenom"
                name="prenom"
                value={formData.prenom}
                onChange={handleChange}
                required
                placeholder="Votre prénom"
              />
            </div>
            
            <div className="form-group">
              <label htmlFor="nom">Nom *</label>
              <input
                type="text"
                id="nom"
                name="nom"
                value={formData.nom}
                onChange={handleChange}
                required
                placeholder="Votre nom"
              />
            </div>
          </div>
          
          <div className="form-group">
            <label htmlFor="email">Email *</label>
            <input
              type="email"
              id="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              required
              placeholder="votre@email.com"
            />
          </div>
          
          <div className="form-group">
            <label htmlFor="telephone">Téléphone</label>
            <input
              type="tel"
              id="telephone"
              name="telephone"
              value={formData.telephone}
              onChange={handleChange}
              placeholder="+243 XXX XXX XXX"
            />
          </div>
          
          <div className="form-group">
            <label htmlFor="role">Rôle *</label>
            <select
              id="role"
              name="role"
              value={formData.role}
              onChange={handleChange}
              required
            >
              <option value="CLIENT">Client - Je veux acheter</option>
              <option value="VENDEUR">Vendeur - Je veux vendre</option>
              <option value="ADMIN">Administrateur</option>
            </select>
          </div>
          
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="password">Mot de passe *</label>
              <input
                type="password"
                id="password"
                name="password"
                value={formData.password}
                onChange={handleChange}
                required
                placeholder="Minimum 6 caractères"
              />
            </div>
            
            <div className="form-group">
              <label htmlFor="confirmPassword">Confirmer *</label>
              <input
                type="password"
                id="confirmPassword"
                name="confirmPassword"
                value={formData.confirmPassword}
                onChange={handleChange}
                required
                placeholder="Confirmer le mot de passe"
              />
            </div>
          </div>
          
          <button 
            type="button" 
            className="toggle-address-btn"
            onClick={() => setShowAddress(!showAddress)}
          >
            {showAddress ? '▼ Masquer l\'adresse' : '▶ Ajouter une adresse (optionnel)'}
          </button>
          
          {showAddress && (
            <div className="address-section">
              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="pays">Pays</label>
                  <input
                    type="text"
                    id="pays"
                    name="adresse.pays"
                    value={formData.adresse.pays}
                    onChange={handleChange}
                    placeholder="RDC"
                  />
                </div>
                
                <div className="form-group">
                  <label htmlFor="province">Province</label>
                  <input
                    type="text"
                    id="province"
                    name="adresse.province"
                    value={formData.adresse.province}
                    onChange={handleChange}
                    placeholder="Kinshasa"
                  />
                </div>
              </div>
              
              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="ville">Ville</label>
                  <input
                    type="text"
                    id="ville"
                    name="adresse.ville"
                    value={formData.adresse.ville}
                    onChange={handleChange}
                    placeholder="Kinshasa"
                  />
                </div>
                
                <div className="form-group">
                  <label htmlFor="commune">Commune</label>
                  <input
                    type="text"
                    id="commune"
                    name="adresse.commune"
                    value={formData.adresse.commune}
                    onChange={handleChange}
                    placeholder="Gombe"
                  />
                </div>
              </div>
              
              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="quartier">Quartier</label>
                  <input
                    type="text"
                    id="quartier"
                    name="adresse.quartier"
                    value={formData.adresse.quartier}
                    onChange={handleChange}
                    placeholder="Votre quartier"
                  />
                </div>
                
                <div className="form-group">
                  <label htmlFor="avenue">Avenue</label>
                  <input
                    type="text"
                    id="avenue"
                    name="adresse.avenue"
                    value={formData.adresse.avenue}
                    onChange={handleChange}
                    placeholder="Votre avenue"
                  />
                </div>
              </div>
              
              <div className="form-group">
                <label htmlFor="reference">Référence</label>
                <input
                  type="text"
                  id="reference"
                  name="adresse.reference"
                  value={formData.adresse.reference}
                  onChange={handleChange}
                  placeholder="Point de repère"
                />
              </div>
            </div>
          )}
          
          <button 
            type="submit" 
            className="auth-btn"
            disabled={loading}
          >
            {loading ? 'Inscription en cours...' : 'S\'inscrire'}
          </button>
        </form>
        
        <p className="auth-footer">
          Déjà un compte ? <Link to="/login">Se connecter</Link>
        </p>
      </div>
    </div>
  );
};

export default Register;
