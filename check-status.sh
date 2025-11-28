#!/bin/bash

echo "État des conteneurs Docker:"
docker ps -a

echo ""
echo "Pour voir les logs d'un service spécifique, utilisez:"
echo "docker logs <nom-conteneur>"
echo ""
echo "Exemples:"
echo "docker logs eureka-server"
echo "docker logs config-server"
echo "docker logs users-service"
