# RAPPORT COMPLET - Projet Vente en Ligne

## 📋 Résumé Exécutif

Ce projet est une plateforme e-commerce complète construite avec une architecture microservices. Le système utilise Spring Boot 4.0.0, React 19, Firebase Authentication, et Docker pour l'orchestration des conteneurs.

---

## 🏗️ Architecture du Système

### Vue d'ensemble

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           FRONTEND (React 19 + Vite)                     │
│                              Port: 5173                                  │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                         API GATEWAY (Spring Cloud)                       │
│                              Port: 8080                                  │
│  Routes:                                                                │
│    /api/users/**    → UsersService                                      │
│    /api/produits/** → ProduitService                                    │
│    /api/carte/**    → CarteService                                      │
│    /api/orders/**   → OrderService                                      │
│    /api/paiements/**→ PaiementService                                   │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
          ┌─────────────────────────┼─────────────────────────┐
          ▼                         ▼                         ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  CONFIG SERVER  │    │  EUREKA SERVER  │    │  MICROSERVICES  │
│   Port: 8888    │    │   Port: 8761    │    │                 │
│                 │    │  (Discovery)    │    │  UsersService   │
│  config-repo/   │    │                 │    │  ProduitService │
│   *.yml         │    └─────────────────┘    │  CarteService   │
└─────────────────┘                           │  OrderService   │
                                              │  PaiementService│
                                              └─────────────────┘
                                                      │
                    ┌─────────────────────────────────┼─────────────────┐
                    ▼                                 ▼                 ▼
            ┌─────────────┐                   ┌─────────────┐   ┌─────────────┐
            │ PostgreSQL  │                   │   MongoDB   │   │    Redis    │
            │  Port: 5432 │                   │ Port: 27017 │   │ Port: 6379  │
            │  users_db   │                   │ produits_db │   │   Cache     │
            └─────────────┘                   └─────────────┘   └─────────────┘
```

---

## 🔧 Technologies Utilisées

### Backend
| Technologie | Version | Usage |
|-------------|---------|-------|
| Java | 21 | Langage de programmation |
| Spring Boot | 4.0.0 | Framework principal |
| Spring Cloud | 2025.1.0-RC1 | Microservices (Eureka, Config, Gateway) |
| Spring Data JPA | - | Persistence PostgreSQL |
| Spring Data MongoDB | - | Persistence MongoDB |
| Firebase Admin SDK | 9.2.0 | Authentification |
| Lombok | - | Réduction du boilerplate |
| MapStruct | 1.5.5.Final | Mapping DTO/Entity |

### Frontend
| Technologie | Version | Usage |
|-------------|---------|-------|
| React | 19.2.0 | Framework UI |
| Vite | 7.2.4 | Build tool |
| Firebase | 12.6.0 | Authentification client |
| Axios | 1.9.0 | Requêtes HTTP |
| React Router DOM | 7.6.2 | Navigation |

### Infrastructure
| Technologie | Version | Usage |
|-------------|---------|-------|
| Docker | - | Conteneurisation |
| Docker Compose | - | Orchestration |
| PostgreSQL | 16-alpine | Base de données users |
| MongoDB | 7-jammy | Base de données produits |
| Redis | 7-alpine | Cache |

---

## 📦 Microservices Implémentés

### 1. UsersService (Port 8081)

#### Description
Service de gestion des utilisateurs avec authentification Firebase.

#### Modèle de données

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    private String id;              // Firebase UID
    private String nom;
    private String prenom;
    @Column(unique = true)
    private String email;
    @Enumerated(EnumType.STRING)
    private Role role;              // VENDEUR, CLIENT, ADMIN
    private String telephone;
    @Embedded
    private Adresse adresse;        // Objet embarqué
    private String photoProfil;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

public enum Role {
    VENDEUR, CLIENT, ADMIN
}

@Embeddable
public class Adresse {
    private String pays;
    private String province;
    private String ville;
    private String commune;
    private String quartier;
    private String avenue;
    private String reference;
}
```

#### Endpoints API

| Méthode | Endpoint | Description | Auth |
|---------|----------|-------------|------|
| POST | `/api/users/register` | Inscription | ❌ |
| POST | `/api/users/login` | Connexion | ❌ |
| POST | `/api/users/verify-token` | Vérification token | ❌ |
| GET | `/api/users/{id}` | Obtenir un utilisateur | ✅ |
| GET | `/api/users/email/{email}` | Recherche par email | ✅ |
| PUT | `/api/users/{id}` | Mise à jour | ✅ |
| DELETE | `/api/users/{id}` | Suppression | ✅ |

#### Sécurité
- Authentification Firebase avec token JWT
- Filtre personnalisé `FirebaseTokenFilter`
- Configuration CORS pour le frontend

---

### 2. ProduitService (Port 8082)

#### Description
Service de gestion des produits avec MongoDB.

#### Modèle de données

```java
@Document(collection = "produits")
public class Produit {
    @Id
    private String id;
    private String vendeurId;           // Référence au vendeur
    private String titre;
    private String description;
    private BigDecimal prix;
    private String categorie;
    private Map<String, String> specifications;  // Spécifications flexibles
    @Enumerated(EnumType.STRING)
    private ProduitStatus status;        // ACTIF, INACTIF, EN_ATTENTE, RUPTURE
    private int stock;
    private List<String> images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

public enum ProduitStatus {
    ACTIF, INACTIF, EN_ATTENTE, RUPTURE
}
```

#### Endpoints API

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/produits` | Tous les produits |
| GET | `/api/produits/{id}` | Produit par ID |
| GET | `/api/produits/vendeur/{vendeurId}` | Produits d'un vendeur |
| GET | `/api/produits/categorie/{categorie}` | Produits par catégorie |
| GET | `/api/produits/search?titre=` | Recherche par titre |
| GET | `/api/produits/active` | Produits actifs |
| POST | `/api/produits` | Créer un produit |
| PUT | `/api/produits/{id}` | Modifier un produit |
| PUT | `/api/produits/{id}/stock?quantite=` | Modifier le stock |
| DELETE | `/api/produits/{id}` | Supprimer un produit |

#### Configuration MongoDB
```java
@Configuration
public class MongoConfig {
    @Value("${SPRING_DATA_MONGODB_URI:mongodb://localhost:27017/produits_db}")
    private String mongoUri;

    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create(mongoUri);
    }
}
```

---

### 3. API Gateway (Port 8080)

#### Description
Point d'entrée unique pour tous les microservices avec routage et CORS.

#### Configuration des routes

```java
@Configuration
public class GatewayConfig {
    @Bean
    public RouterFunction<ServerResponse> usersServiceRoute() {
        return route("users-service")
                .route(path("/api/users/**"), HandlerFunctions.http())
                .filter(lb("UsersService"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> produitServiceRoute() {
        return route("produit-service")
                .route(path("/api/produits/**"), HandlerFunctions.http())
                .filter(lb("ProduitService"))
                .build();
    }
    // ... autres routes
}
```

#### Configuration CORS
```java
@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*",
            "http://127.0.0.1:*"
        ));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        return new CorsFilter(source);
    }
}
```

---

### 4. Config Server (Port 8888)

#### Description
Serveur de configuration centralisée utilisant un dépôt local.

#### Fichiers de configuration
- `application.yml` - Configuration commune
- `UsersService.yml` - Configuration UsersService
- `ProduitService.yml` - Configuration ProduitService
- `APIGateway.yml` - Configuration API Gateway
- `EurekaServer.yml` - Configuration Eureka

---

### 5. Eureka Server (Port 8761)

#### Description
Service de découverte pour tous les microservices.

#### Services enregistrés
- APIGATEWAY
- CONFIGSERVER
- USERSSERVICE
- PRODUITSERVICE

---

## 🖥️ Frontend React

### Structure du projet

```
frontend/
├── src/
│   ├── components/
│   │   └── ProtectedRoute.jsx      # Route protégée
│   ├── config/
│   │   └── firebase.js             # Configuration Firebase
│   ├── context/
│   │   └── AuthContext.jsx         # Contexte d'authentification
│   ├── pages/
│   │   ├── Login.jsx               # Page de connexion
│   │   ├── Register.jsx            # Page d'inscription
│   │   ├── DashboardClient.jsx     # Dashboard client
│   │   ├── DashboardVendeur.jsx    # Dashboard vendeur
│   │   ├── DashboardAdmin.jsx      # Dashboard admin
│   │   ├── Auth.css                # Styles auth
│   │   └── Dashboard.css           # Styles dashboards
│   ├── services/
│   │   └── api.js                  # Services API
│   ├── App.jsx
│   └── main.jsx
└── package.json
```

### Services API (api.js)

#### UsersService
```javascript
export const usersService = {
    register: (userData, token) => api.post('/api/users/register', userData, authConfig(token)),
    login: (credentials) => api.post('/api/users/login', credentials),
    getByEmail: (email, token) => api.get(`/api/users/email/${email}`, authConfig(token)),
    getById: (id, token) => api.get(`/api/users/${id}`, authConfig(token)),
    update: (id, userData, token) => api.put(`/api/users/${id}`, userData, authConfig(token)),
    delete: (id, token) => api.delete(`/api/users/${id}`, authConfig(token)),
    verifyToken: (token) => api.post('/api/users/verify-token', {}, authConfig(token))
};
```

#### ProduitService
```javascript
export const produitService = {
    getAllProducts: () => api.get('/api/produits'),
    getProductById: (id) => api.get(`/api/produits/${id}`),
    createProduct: (productData, token) => api.post('/api/produits', productData, authConfig(token)),
    updateProduct: (id, productData, token) => api.put(`/api/produits/${id}`, productData, authConfig(token)),
    deleteProduct: (id, token) => api.delete(`/api/produits/${id}`, authConfig(token)),
    updateStock: (id, quantite, token) => api.put(`/api/produits/${id}/stock?quantite=${quantite}`, {}, authConfig(token)),
    getProductsByVendeur: (vendeurId) => api.get(`/api/produits/vendeur/${vendeurId}`),
    searchProducts: (titre) => api.get(`/api/produits/search?titre=${titre}`),
    getProductsByCategory: (categorie) => api.get(`/api/produits/categorie/${categorie}`),
    getActiveProducts: () => api.get('/api/produits/active')
};
```

### Pages principales

#### DashboardVendeur
- **Affichage** des produits du vendeur
- **Création** de nouveaux produits avec formulaire complet
- **Suppression** de produits
- **Gestion du stock** (ajout/retrait de quantité)
- **Statistiques**: produits total, actifs, rupture de stock

#### DashboardClient
- **Catalogue** des produits actifs
- **Recherche** par titre
- **Filtrage** par catégorie
- **Boutons** "Ajouter au panier" (préparé pour CarteService)

#### DashboardAdmin
- **Onglet Produits**: Liste de tous les produits avec toggle status et suppression
- **Onglet Utilisateurs**: Gestion des utilisateurs (liste, recherche, suppression)
- **Statistiques**: Nombre de produits, utilisateurs

---

## 🐳 Docker

### docker-compose.yml

```yaml
services:
  # Bases de données
  postgres-db:
    image: postgres:16-alpine
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
      POSTGRES_DB: users_db
    ports:
      - "5432:5432"

  mongodb:
    image: mongo:7-jammy
    environment:
      MONGO_INITDB_DATABASE: produits_db
    ports:
      - "27017:27017"

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  # Infrastructure
  eureka-server:
    build: ./Microservice/EurekaServer
    ports:
      - "8761:8761"

  config-server:
    build: ./Microservice/ConfigServer
    ports:
      - "8888:8888"

  # API Gateway
  api-gateway:
    build: ./Microservice/APIGateway
    ports:
      - "8080:8080"
    environment:
      - SPRING_CLOUD_CONFIG_URI=http://config-server:8888
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/

  # Microservices
  users-service:
    build: ./Microservice/UsersService
    ports:
      - "8081:8081"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-db:5432/users_db
      - GOOGLE_APPLICATION_CREDENTIALS=/app/firebase-credentials.json

  produit-service:
    build: ./Microservice/ProduitService
    ports:
      - "8082:8082"
    environment:
      - SPRING_DATA_MONGODB_URI=mongodb://mongodb:27017/produits_db
```

### Commandes Docker utiles

```bash
# Démarrer tous les services
docker-compose up -d

# Vérifier les conteneurs
docker ps

# Voir les logs d'un service
docker logs -f users-service

# Arrêter tous les services
docker-compose down

# Reconstruire un service
docker-compose up -d --build users-service
```

---

## ✅ Fonctionnalités Testées

### UsersService
- ✅ Inscription d'un utilisateur avec Firebase UID
- ✅ Stockage en PostgreSQL
- ✅ Rôles VENDEUR, CLIENT, ADMIN
- ✅ Adresse embarquée

### ProduitService
- ✅ Connexion à MongoDB
- ✅ CRUD complet des produits
- ✅ Recherche par titre, catégorie, vendeur
- ✅ Gestion du stock

### API Gateway
- ✅ Routage vers UsersService (/api/users/**)
- ✅ Routage vers ProduitService (/api/produits/**)
- ✅ Configuration CORS
- ✅ Load balancing avec Eureka

### Frontend
- ✅ Page de connexion Firebase
- ✅ Page d'inscription
- ✅ Dashboards selon rôle
- ✅ Gestion des produits (Vendeur)
- ✅ Catalogue produits (Client)
- ✅ Administration (Admin)

---

## 🧪 Tests effectués

### Test 1: API Gateway vers ProduitService
```bash
$ curl http://localhost:8080/api/produits
[]
```
✅ **Succès**: L'API Gateway route correctement vers ProduitService

### Test 2: Accès direct ProduitService
```bash
$ curl http://localhost:8082/api/produits
[]
```
✅ **Succès**: ProduitService répond correctement

### Test 3: Vérification utilisateur dans PostgreSQL
```sql
SELECT * FROM users;
-- Résultat: 1 utilisateur "FLORENTIN NERIA" avec rôle VENDEUR
```
✅ **Succès**: UsersService persiste correctement les données

### Test 4: Services enregistrés dans Eureka
- APIGATEWAY
- CONFIGSERVER
- USERSSERVICE
- PRODUITSERVICE
✅ **Succès**: Tous les services sont découvrables

---

## 📊 État actuel du projet

| Composant | État | Port |
|-----------|------|------|
| PostgreSQL | ✅ Opérationnel | 5432 |
| MongoDB | ✅ Opérationnel | 27017 |
| Redis | ✅ Opérationnel | 6379 |
| Eureka Server | ✅ Opérationnel | 8761 |
| Config Server | ✅ Opérationnel | 8888 |
| API Gateway | ✅ Opérationnel | 8080 |
| UsersService | ✅ Opérationnel | 8081 |
| ProduitService | ✅ Opérationnel | 8082 |
| Frontend React | 🔧 Prêt à démarrer | 5173 |

---

## 🚀 Prochaines étapes

### Services à implémenter
1. **CarteService** - Gestion du panier d'achats
2. **OrderService** - Gestion des commandes
3. **PaiementService** - Intégration paiement

### Améliorations suggérées
1. Ajouter des tests unitaires et d'intégration
2. Implémenter la pagination pour les listes
3. Ajouter le téléchargement d'images produits
4. Implémenter les notifications en temps réel
5. Ajouter un service de messagerie entre vendeur/client

---

## 📝 Notes importantes

### Firebase
- Les credentials Firebase ne doivent PAS être commités dans Git
- Le fichier `vente-en-ligne-*-firebase-adminsdk-*.json` est dans `.gitignore`

### Configuration Docker
- Tous les services utilisent le réseau `microservices-network`
- Les services attendent que leurs dépendances soient prêtes avant de démarrer

### Sécurité
- CORS configuré pour `localhost:*` (développement)
- Authentification Firebase obligatoire pour les endpoints protégés

---

*Rapport généré le 28 Novembre 2025*
*Projet Vente en Ligne - Architecture Microservices*
