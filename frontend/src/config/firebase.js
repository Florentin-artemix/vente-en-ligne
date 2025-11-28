// Configuration Firebase pour le frontend
import { initializeApp } from 'firebase/app';
import { getAuth } from 'firebase/auth';

const firebaseConfig = {
  apiKey: "AIzaSyADD9IcDy934q7SN81WZLAi0M0_k8FJ02w",
  authDomain: "vente-en-ligne-13622.firebaseapp.com",
  projectId: "vente-en-ligne-13622",
  storageBucket: "vente-en-ligne-13622.firebasestorage.app",
  messagingSenderId: "25617024382",
  appId: "1:25617024382:web:c0a47a67031798a5615f2f"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
export default app;

