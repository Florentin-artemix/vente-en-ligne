#!/bin/bash

echo "Arrêt de tous les conteneurs..."
cd /workspaces/vente-en-ligne
docker-compose down

echo "Conteneurs arrêtés avec succès!"
