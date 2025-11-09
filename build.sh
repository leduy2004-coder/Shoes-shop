#!/bin/bash

# Exit on error
set -e

echo "Starting build process..."

# Build common-dto first
echo "Building common-dto..."
cd common-dto
mvn clean install -DskipTests
cd ..

# Build all services
services=(
    "api-gateway"
    "shoes-service"
    "auth-service"
    "file-service"
    "config-server"
    "eureka-server"
)

for service in "${services[@]}"
do
    echo "Building $service..."
    cd $service
    mvn clean package -DskipTests
    cd ..
done

echo "All services built successfully!"

# Build Docker images
echo "Building Docker images..."
docker-compose build

echo "Build process completed successfully!" 