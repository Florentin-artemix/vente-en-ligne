#!/bin/bash

# Script pour compiler OrderService

echo "🔨 Compilation d'OrderService..."

cd /workspaces/vente-en-ligne/Microservice/OrderService

# Donner les permissions d'exécution à mvnw
chmod +x mvnw

# Compiler le projet
./mvnw clean package -DskipTests

echo "✅ Compilation terminée !"
