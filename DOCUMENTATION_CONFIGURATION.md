# Documentation de Configuration du Système de Microservices - Vente en Ligne

## Vue d'ensemble

Ce document décrit toutes les modifications apportées au système de microservices pour la vente en ligne. Les modifications incluent la mise en place d'un serveur de configuration centralisé, l'intégration d'Eureka pour la découverte de services, la configuration de l'API Gateway, et l'ajout de Firebase Admin SDK au service utilisateurs.

---

## 1. Analyse des Fichiers POM.xml

### Services Identifiés

Le système comprend 8 microservices :

1. **EurekaServer** (Port: 8761) - Serveur de découverte de services
2. **ConfigServer** (Port: 8888) - Serveur de configuration centralisé
3. **APIGateway** (Port: 8080) - Passerelle d'API
4. **UsersService** (Port: 8081) - Gestion des utilisateurs
5. **ProduitService** (Port: 8082) - Gestion des produits
6. **CarteService** (Port: 8083) - Gestion du panier
7. **OrderService** (Port: 8084) - Gestion des commandes
8. **paiement** (Port: 8085) - Gestion des paiements

### Technologies Utilisées

- **Spring Boot**: 4.0.0
- **Spring Cloud**: 2025.1.0-RC1
- **Java**: 21
- **Bases de données**:
  - PostgreSQL (UsersService, OrderService, paiement)
  - MongoDB (ProduitService)
  - Redis (CarteService)

---

## 2. Référentiel de Configuration Local

### Structure Créée

Un nouveau répertoire `/workspaces/vente-en-ligne/config-repo` a été créé pour héberger toutes les configurations centralisées.

### Fichiers de Configuration

#### 2.1. application.yml (Configuration Commune)

Configuration partagée par tous les services :
- Enregistrement auprès d'Eureka
- Exposition des endpoints de monitoring Actuator
- Activation du Config Server

```yaml
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
    register-with-eureka: true
    fetch-registry: true
```

#### 2.2. Fichiers Spécifiques par Service

**ConfigServer.yml**
- Port: 8888
- Configuration du référentiel natif pointant vers `/workspaces/vente-en-ligne/config-repo`

**EurekaServer.yml**
- Port: 8761
- Ne s'enregistre pas lui-même
- Désactivation de la préservation automatique

**APIGateway.yml**
- Port: 8080
- Configuration des routes pour tous les services
- Routes configurées :
  - `/api/users/**` → UsersService
  - `/api/produits/**` → ProduitService
  - `/api/carte/**` → CarteService
  - `/api/orders/**` → OrderService
  - `/api/paiements/**` → paiement

**UsersService.yml**
- Port: 8081
- Configuration PostgreSQL
- Base de données: users_db
- Liquibase activé

**ProduitService.yml**
- Port: 8082
- Configuration MongoDB
- Base de données: produits_db

**CarteService.yml**
- Port: 8083
- Configuration Redis
- Host: localhost, Port: 6379

**OrderService.yml**
- Port: 8084
- Configuration PostgreSQL
- Base de données: orders_db
- Liquibase activé

**paiement.yml**
- Port: 8085
- Configuration PostgreSQL
- Base de données: paiement_db
- Liquibase activé

---

## 3. Configuration du Config Server

### Modifications Apportées

1. **Classe Principale** : `ConfigServerApplication.java`
   - Ajout de l'annotation `@EnableConfigServer`

2. **application.properties**
   - Port: 8888
   - Profil actif: native
   - Chemin du référentiel de configuration local

```properties
spring.application.name=ConfigServer
server.port=8888
spring.profiles.active=native
spring.cloud.config.server.native.search-locations=file:///workspaces/vente-en-ligne/config-repo
```

---

## 4. Configuration d'Eureka Server

### Modifications Apportées

1. **Classe Principale** : `EurekaServerApplication.java`
   - Ajout de l'annotation `@EnableEurekaServer`

2. **application.properties**
   - Port: 8761
   - Configuration pour ne pas s'auto-enregistrer
   - Désactivation de la préservation automatique

```properties
spring.application.name=EurekaServer
server.port=8761
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
```

---

## 5. Configuration de Tous les Services

### Fichiers bootstrap.properties Créés

Chaque service (sauf EurekaServer et ConfigServer) a reçu un fichier `bootstrap.properties` :

```properties
spring.application.name=[NOM_DU_SERVICE]
spring.cloud.config.uri=http://localhost:8888
spring.cloud.config.fail-fast=true
spring.config.import=optional:configserver:http://localhost:8888
```

### Annotations Ajoutées aux Classes Principales

Tous les services applicatifs ont reçu les annotations suivantes :
- `@EnableDiscoveryClient` : Pour s'enregistrer auprès d'Eureka
- `@EnableFeignClients` : Pour activer les clients Feign (communication inter-services)

**Services modifiés** :
- CarteServiceApplication
- OrderServiceApplication
- PaiementApplication
- ProduitServiceApplication
- UsersServiceApplication
- ApiGatewayApplication (seulement @EnableDiscoveryClient)

---

## 6. Configuration de l'API Gateway

### Routes Configurées

L'API Gateway route les requêtes vers les services appropriés :

| Chemin | Service Cible | Action |
|--------|---------------|--------|
| `/api/users/**` | UsersService | Suppression du préfixe `/api` |
| `/api/produits/**` | ProduitService | Suppression du préfixe `/api` |
| `/api/carte/**` | CarteService | Suppression du préfixe `/api` |
| `/api/orders/**` | OrderService | Suppression du préfixe `/api` |
| `/api/paiements/**` | paiement | Suppression du préfixe `/api` |

### Découverte Dynamique

- Discovery locator activé
- Conversion automatique des noms de services en minuscules

---

## 7. Ajout de Firebase Admin SDK au UsersService

### Modifications du POM.xml

Ajout de la dépendance Firebase Admin SDK :

```xml
<dependency>
    <groupId>com.google.firebase</groupId>
    <artifactId>firebase-admin</artifactId>
    <version>9.2.0</version>
</dependency>
```

### Classe de Configuration Firebase

Création de `FirebaseConfig.java` dans le package `config` :

**Fonctionnalités** :
- Initialisation de Firebase au démarrage de l'application
- Deux options de configuration :
  1. **Credentials par défaut** : Utilise les credentials configurés dans l'environnement
  2. **Fichier de service account** : Configuration via un fichier JSON (commenté par défaut)

**Gestion des erreurs** :
- En cas d'échec d'initialisation, un message d'erreur est affiché
- L'application continue de fonctionner même si Firebase n'est pas configuré

**Pour utiliser Firebase** :
1. Obtenir un fichier de service account depuis la console Firebase
2. Décommenter la section "Option 2" dans `FirebaseConfig.java`
3. Spécifier le chemin vers le fichier JSON

---

## 8. Ordre de Démarrage des Services

Pour démarrer le système correctement, suivez cet ordre :

1. **EurekaServer** (Port 8761)
   ```bash
   cd Microservice/EurekaServer
   ./mvnw spring-boot:run
   ```

2. **ConfigServer** (Port 8888)
   ```bash
   cd Microservice/ConfigServer
   ./mvnw spring-boot:run
   ```

3. **Services Applicatifs** (dans n'importe quel ordre)
   - UsersService (Port 8081)
   - ProduitService (Port 8082)
   - CarteService (Port 8083)
   - OrderService (Port 8084)
   - paiement (Port 8085)

4. **APIGateway** (Port 8080) - En dernier
   ```bash
   cd Microservice/APIGateway
   ./mvnw spring-boot:run
   ```

---

## 9. Prérequis d'Infrastructure

### Bases de Données PostgreSQL

Créer les bases de données suivantes :

```sql
CREATE DATABASE users_db;
CREATE DATABASE orders_db;
CREATE DATABASE paiement_db;
```

**Configuration par défaut** :
- Host: localhost
- Port: 5432
- Username: postgres
- Password: postgres

### MongoDB

- Host: localhost
- Port: 27017
- Base de données: produits_db (créée automatiquement)

### Redis

- Host: localhost
- Port: 6379

---

## 10. Points d'Accès et Monitoring

### Eureka Dashboard
- URL: http://localhost:8761
- Permet de visualiser tous les services enregistrés

### Config Server
- URL: http://localhost:8888
- Accès aux configurations : `http://localhost:8888/{service-name}/default`

### API Gateway
- URL: http://localhost:8080
- Point d'entrée unique pour toutes les requêtes

### Endpoints Actuator

Chaque service expose des endpoints de monitoring :
- `/actuator/health` - État de santé
- `/actuator/info` - Informations sur le service
- `/actuator/*` - Autres métriques

---

## 11. Architecture Finale

```
┌─────────────────┐
│   Clients       │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  API Gateway    │ (Port 8080)
│  (Load Balancer)│
└────────┬────────┘
         │
    ┌────┴────────────────────────┐
    │                             │
    ▼                             ▼
┌──────────┐              ┌──────────────┐
│ Eureka   │◄────────────►│ Config Server│
│ Server   │              │              │
│(Port 8761)│              │ (Port 8888)  │
└────┬─────┘              └──────────────┘
     │                           ▲
     │                           │
     └───────────┬───────────────┘
                 │
    ┌────────────┼────────────┬──────────┬──────────┐
    ▼            ▼            ▼          ▼          ▼
┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐
│ Users   │ │ Produit │ │ Carte   │ │ Order   │ │Paiement │
│ Service │ │ Service │ │ Service │ │ Service │ │ Service │
│(8081)   │ │(8082)   │ │(8083)   │ │(8084)   │ │(8085)   │
└────┬────┘ └────┬────┘ └────┬────┘ └────┬────┘ └────┬────┘
     │           │           │           │           │
     ▼           ▼           ▼           ▼           ▼
┌──────────┐┌──────────┐┌──────────┐┌──────────┐┌──────────┐
│PostgreSQL││ MongoDB  ││  Redis   ││PostgreSQL││PostgreSQL│
└──────────┘└──────────┘└──────────┘└──────────┘└──────────┘
```

---

## 12. Résumé des Modifications

### Fichiers Créés

1. **Répertoire de configuration** : `/workspaces/vente-en-ligne/config-repo/`
   - application.yml
   - ConfigServer.yml
   - EurekaServer.yml
   - APIGateway.yml
   - UsersService.yml
   - ProduitService.yml
   - CarteService.yml
   - OrderService.yml
   - paiement.yml

2. **Fichiers bootstrap.properties** pour tous les services

3. **Configuration Firebase** : `FirebaseConfig.java` dans UsersService

### Fichiers Modifiés

1. **Classes Application** (ajout d'annotations)
   - ConfigServerApplication.java
   - EurekaServerApplication.java
   - CarteServiceApplication.java
   - OrderServiceApplication.java
   - PaiementApplication.java
   - ProduitServiceApplication.java
   - UsersServiceApplication.java
   - ApiGatewayApplication.java

2. **Fichiers application.properties**
   - ConfigServer/application.properties
   - EurekaServer/application.properties

3. **POM.xml**
   - UsersService/pom.xml (ajout de Firebase Admin SDK)

---

## 13. Avantages de Cette Architecture

1. **Configuration Centralisée** : Toutes les configurations sont gérées dans un seul endroit
2. **Découverte de Services** : Les services se trouvent automatiquement via Eureka
3. **Scalabilité** : Possibilité d'ajouter plusieurs instances de chaque service
4. **Routage Intelligent** : API Gateway gère le load balancing automatiquement
5. **Monitoring** : Actuator permet de surveiller l'état de chaque service
6. **Authentification Firebase** : UsersService peut gérer l'authentification avec Firebase

---

## 14. Prochaines Étapes Recommandées

1. **Sécurité** :
   - Configurer Spring Security sur l'API Gateway
   - Implémenter JWT pour l'authentification
   - Ajouter HTTPS

2. **Résilience** :
   - Ajouter Circuit Breaker (Resilience4j)
   - Configurer les timeouts et retry policies

3. **Observabilité** :
   - Intégrer Spring Cloud Sleuth pour le tracing distribué
   - Ajouter Zipkin ou Jaeger pour la visualisation des traces
   - Configurer un système de logs centralisé (ELK Stack)

4. **Base de Données** :
   - Configurer les migrations Liquibase
   - Optimiser les index et les requêtes

5. **Tests** :
   - Ajouter des tests d'intégration
   - Tester les scénarios de failure

6. **Déploiement** :
   - Créer des fichiers Docker pour chaque service
   - Préparer les fichiers docker-compose ou Kubernetes

---

## 15. Dépannage

### Problème : Service ne se connecte pas à Eureka
**Solution** : Vérifier que EurekaServer est démarré et accessible sur le port 8761

### Problème : Configuration non chargée
**Solution** : Vérifier que ConfigServer est démarré et que le chemin du référentiel est correct

### Problème : Erreur Firebase
**Solution** : Firebase est optionnel. Pour l'activer, configurer les credentials dans FirebaseConfig.java

### Problème : Gateway ne route pas correctement
**Solution** : Vérifier que les services sont enregistrés dans Eureka Dashboard

---

## 16. Contact et Support

Pour toute question ou problème, veuillez consulter :
- Documentation Spring Cloud : https://spring.io/projects/spring-cloud
- Documentation Eureka : https://cloud.spring.io/spring-cloud-netflix/
- Documentation Firebase Admin : https://firebase.google.com/docs/admin/setup

---

**Date de création** : 27 novembre 2025
**Version** : 1.0
**Auteur** : Configuration automatisée via GitHub Copilot
