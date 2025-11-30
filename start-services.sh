#!/bin/bash

# Script pour démarrer tous les services

echo "🚀 Démarrage de tous les services..."

# Naviguer vers le répertoire principal
cd /workspaces/vente-en-ligne

# Arrêter les services existants
echo "🛑 Arrêt des services existants..."
docker-compose down

# Construire et démarrer tous les services
echo "🔨 Construction et démarrage des services..."
docker-compose up --build -d

# Attendre que les services démarrent
echo "⏳ Attente du démarrage des services..."
sleep 30

# Vérifier l'état des services
echo "✅ Vérification de l'état des services..."
docker-compose ps

echo ""
echo "✨ Services démarrés !"
echo ""
echo "📊 Services disponibles :"
echo "  - Eureka Server: http://localhost:8761"
echo "  - Config Server: http://localhost:8888"
echo "  - API Gateway: http://localhost:8080"
echo "  - Users Service: http://localhost:8081"
echo "  - Produit Service: http://localhost:8082"
echo "  - Carte Service: http://localhost:8083"
echo "  - Order Service: http://localhost:8084"
echo "  - Paiement Service: http://localhost:8085"
echo ""
echo "🗄️ Bases de données :"
echo "  - PostgreSQL: localhost:5432"
echo "  - MongoDB: localhost:27017"
echo "  - Redis: localhost:6379"
echo ""
echo "📝 Pour voir les logs : docker-compose logs -f [service-name]"
echo "🛑 Pour arrêter : docker-compose down"
