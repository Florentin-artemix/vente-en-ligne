#!/bin/bash

echo "==================================="
echo "Démarrage des conteneurs Docker"
echo "==================================="

cd /workspaces/vente-en-ligne

echo "Construction et démarrage des conteneurs..."
docker-compose up -d --build

echo ""
echo "Attente du démarrage des conteneurs (60 secondes)..."
sleep 60

echo ""
echo "==================================="
echo "État des conteneurs:"
echo "==================================="
docker-compose ps

echo ""
echo "==================================="
echo "Vérification des logs de chaque service:"
echo "==================================="

services=("postgres" "mongodb" "redis" "eureka-server" "config-server" "users-service" "produit-service" "carte-service" "order-service" "paiement-service" "api-gateway")

for service in "${services[@]}"; do
    echo ""
    echo "--- Logs de $service (dernières 20 lignes) ---"
    docker-compose logs --tail=20 $service
    echo ""
done

echo "==================================="
echo "Services accessibles:"
echo "==================================="
echo "Eureka Dashboard: http://localhost:8761"
echo "Config Server: http://localhost:8888"
echo "API Gateway: http://localhost:8080"
echo "Users Service: http://localhost:8081"
echo "Produit Service: http://localhost:8082"
echo "Carte Service: http://localhost:8083"
echo "Order Service: http://localhost:8084"
echo "Paiement Service: http://localhost:8085"
echo "==================================="
