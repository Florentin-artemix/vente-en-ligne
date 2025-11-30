import { useState } from 'react';
import { paiementService, METHODES_PAIEMENT } from '../services/api';
import './Paiement.css';

const PaiementModal = ({ 
  isOpen, 
  onClose, 
  orderId, 
  userId, 
  montant, 
  currency = 'USD',
  onSuccess,
  onError 
}) => {
  const [selectedMethode, setSelectedMethode] = useState(null);
  const [phoneNumber, setPhoneNumber] = useState('');
  const [loading, setLoading] = useState(false);
  const [step, setStep] = useState('select'); // 'select', 'details', 'processing', 'result'
  const [result, setResult] = useState(null);

  const handleMethodeSelect = (methode) => {
    setSelectedMethode(methode);
    if (methode === 'CASH_ON_DELIVERY') {
      // Paiement à la livraison - pas de détails supplémentaires
      setStep('details');
    } else if (['MPESA', 'ORANGE_MONEY', 'AIRTEL_MONEY', 'AFRI_MONEY'].includes(methode)) {
      // Mobile money - besoin du numéro de téléphone
      setStep('details');
    } else if (methode === 'CARTE_BANCAIRE') {
      // Carte bancaire - pour l'instant juste simuler
      setStep('details');
    }
  };

  const handleSubmit = async () => {
    setLoading(true);
    setStep('processing');
    
    try {
      const paiementData = {
        orderId,
        userId,
        montant,
        currency,
        methode: selectedMethode,
        phoneNumber: phoneNumber || null
      };

      const paiement = await paiementService.createPaiement(paiementData);
      
      // Simuler un délai de traitement
      await new Promise(resolve => setTimeout(resolve, 2000));
      
      // Simuler une réponse du provider
      const providerResponse = {
        message: 'Paiement traité avec succès',
        transactionId: paiement.transactionReference,
        timestamp: new Date().toISOString()
      };

      // Confirmer le paiement (dans un vrai cas, cela viendrait d'un webhook)
      const confirmedPaiement = await paiementService.confirmPaiement(paiement.id, providerResponse);
      
      setResult({
        success: true,
        paiement: confirmedPaiement,
        message: 'Paiement effectué avec succès!'
      });
      setStep('result');
      
      if (onSuccess) {
        onSuccess(confirmedPaiement);
      }
    } catch (error) {
      console.error('Erreur paiement:', error);
      setResult({
        success: false,
        message: error.response?.data?.message || 'Erreur lors du paiement'
      });
      setStep('result');
      
      if (onError) {
        onError(error);
      }
    } finally {
      setLoading(false);
    }
  };

  const resetModal = () => {
    setSelectedMethode(null);
    setPhoneNumber('');
    setStep('select');
    setResult(null);
  };

  const handleClose = () => {
    resetModal();
    onClose();
  };

  if (!isOpen) return null;

  return (
    <div className="paiement-overlay" onClick={handleClose}>
      <div className="paiement-modal" onClick={e => e.stopPropagation()}>
        <button className="close-btn" onClick={handleClose}>×</button>
        
        {step === 'select' && (
          <>
            <div className="paiement-header">
              <h2>💳 Choisir le mode de paiement</h2>
              <div className="paiement-amount">
                <span>Montant à payer:</span>
                <strong>{montant?.toFixed(2)} {currency}</strong>
              </div>
            </div>
            
            <div className="methodes-grid">
              {Object.entries(METHODES_PAIEMENT).map(([key, value]) => (
                <button
                  key={key}
                  className={`methode-btn ${selectedMethode === key ? 'selected' : ''}`}
                  onClick={() => handleMethodeSelect(key)}
                  style={{ '--accent-color': value.color }}
                >
                  <span className="methode-icon">{value.icon}</span>
                  <span className="methode-label">{value.label}</span>
                </button>
              ))}
            </div>
          </>
        )}

        {step === 'details' && (
          <>
            <div className="paiement-header">
              <button className="back-btn" onClick={() => setStep('select')}>← Retour</button>
              <h2>{METHODES_PAIEMENT[selectedMethode]?.icon} {METHODES_PAIEMENT[selectedMethode]?.label}</h2>
            </div>
            
            <div className="paiement-details">
              <div className="amount-summary">
                <span>Montant:</span>
                <strong>{montant?.toFixed(2)} {currency}</strong>
              </div>
              
              {['MPESA', 'ORANGE_MONEY', 'AIRTEL_MONEY', 'AFRI_MONEY'].includes(selectedMethode) && (
                <div className="form-group">
                  <label>Numéro de téléphone</label>
                  <input
                    type="tel"
                    placeholder="+243 XXX XXX XXX"
                    value={phoneNumber}
                    onChange={(e) => setPhoneNumber(e.target.value)}
                  />
                  <small>Vous recevrez une demande de paiement sur ce numéro</small>
                </div>
              )}
              
              {selectedMethode === 'CARTE_BANCAIRE' && (
                <div className="card-form">
                  <div className="form-group">
                    <label>Numéro de carte</label>
                    <input type="text" placeholder="1234 5678 9012 3456" maxLength={19} />
                  </div>
                  <div className="form-row">
                    <div className="form-group">
                      <label>Date d'expiration</label>
                      <input type="text" placeholder="MM/AA" maxLength={5} />
                    </div>
                    <div className="form-group">
                      <label>CVV</label>
                      <input type="text" placeholder="123" maxLength={3} />
                    </div>
                  </div>
                </div>
              )}
              
              {selectedMethode === 'CASH_ON_DELIVERY' && (
                <div className="cod-info">
                  <span className="info-icon">ℹ️</span>
                  <p>Vous paierez en espèces à la livraison de votre commande.</p>
                </div>
              )}
              
              <button 
                className="submit-btn" 
                onClick={handleSubmit}
                disabled={['MPESA', 'ORANGE_MONEY', 'AIRTEL_MONEY', 'AFRI_MONEY'].includes(selectedMethode) && !phoneNumber}
              >
                Confirmer le paiement
              </button>
            </div>
          </>
        )}

        {step === 'processing' && (
          <div className="paiement-processing">
            <div className="spinner"></div>
            <h3>Traitement en cours...</h3>
            <p>Veuillez ne pas fermer cette fenêtre</p>
          </div>
        )}

        {step === 'result' && (
          <div className={`paiement-result ${result?.success ? 'success' : 'error'}`}>
            <div className="result-icon">
              {result?.success ? '✅' : '❌'}
            </div>
            <h3>{result?.success ? 'Paiement réussi!' : 'Échec du paiement'}</h3>
            <p>{result?.message}</p>
            {result?.success && result?.paiement && (
              <div className="transaction-info">
                <p>Référence: <strong>{result.paiement.transactionReference}</strong></p>
              </div>
            )}
            <button className="close-result-btn" onClick={handleClose}>
              {result?.success ? 'Continuer' : 'Réessayer'}
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default PaiementModal;
