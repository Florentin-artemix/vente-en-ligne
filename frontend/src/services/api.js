import axios from 'axios';

const API_BASE_URL = 'http://localhost:8081/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Intercepteur pour ajouter le token d'authentification
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('authToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Service utilisateur
export const userService = {
  // Inscription d'un nouvel utilisateur
  register: async (userData) => {
    const response = await api.post('/users/register', userData);
    return response.data;
  },

  // Vérifier le token et récupérer les infos utilisateur
  verifyToken: async (token) => {
    const response = await api.post('/users/verify-token', {}, {
      headers: { Authorization: `Bearer ${token}` }
    });
    return response.data;
  },

  // Récupérer un utilisateur par ID
  getUserById: async (id) => {
    const response = await api.get(`/users/${id}`);
    return response.data;
  },

  // Récupérer un utilisateur par email
  getUserByEmail: async (email) => {
    const response = await api.get(`/users/email/${email}`);
    return response.data;
  },

  // Mettre à jour un utilisateur
  updateUser: async (id, userData) => {
    const response = await api.put(`/users/${id}`, userData);
    return response.data;
  },

  // Supprimer un utilisateur
  deleteUser: async (id) => {
    const response = await api.delete(`/users/${id}`);
    return response.data;
  }
};

export default api;
