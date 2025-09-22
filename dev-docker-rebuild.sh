#!/bin/bash

# Load environment variables from .env file
if [ -f .env ]; then
    echo "Environment variables loaded from .env file"
    # Use a more reliable way to load .env file
    set -a
    source .env
    set +a
else
    echo "Warning: .env file not found, using default values"
fi

# Debug: Show loaded environment variables
echo "LOG_PATH_HOST: ${LOG_PATH_HOST:-not set}"
echo "DATABASE_STORE_HOST: ${DATABASE_STORE_HOST:-not set}"
echo "STRM_PATH_HOST: ${STRM_PATH_HOST:-not set}"

# Stop and remove containers, networks, images, and volumes
docker-compose down --rmi all --volumes

# Configure npm registry for better network connectivity
export NPM_CONFIG_REGISTRY="https://registry.npmmirror.com/"

# Build the images
docker-compose build

# Create and start the containers in detached mode
docker-compose up -d