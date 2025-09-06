#!/bin/bash

# Stop and remove containers, networks, images, and volumes
docker-compose down --rmi all --volumes

# Configure npm registry for better network connectivity
export NPM_CONFIG_REGISTRY="https://registry.npmmirror.com/"

# Build the images
docker-compose build

# Create and start the containers in detached mode
docker-compose up -d