#!/bin/bash

# Kadali Data Platform Deployment Script
# This script deploys Kadali to Kubernetes

set -e

echo "🚀 Kadali Data Platform Deployment"
echo "=================================="

# Check prerequisites
echo "📋 Checking prerequisites..."

if ! command -v kubectl &> /dev/null; then
    echo "❌ kubectl not found. Please install kubectl"
    exit 1
fi

if ! command -v docker &> /dev/null; then
    echo "❌ docker not found. Please install Docker"
    exit 1
fi

echo "✅ Prerequisites check passed"

# Build Docker image
echo ""
echo "🏗️  Building Kadali API Docker image..."
docker build -t kadali-api:latest .

# Tag for your registry (update this)
REGISTRY=${DOCKER_REGISTRY:-"your-registry"}
IMAGE_TAG=${IMAGE_TAG:-"latest"}
FULL_IMAGE="$REGISTRY/kadali-api:$IMAGE_TAG"

echo "🏷️  Tagging image as: $FULL_IMAGE"
docker tag kadali-api:latest $FULL_IMAGE

# Push to registry
echo "📤 Pushing image to registry..."
docker push $FULL_IMAGE

# Update image in deployment
echo "📝 Updating deployment with new image..."
sed -i.bak "s|your-registry/kadali-api:latest|$FULL_IMAGE|g" k8s/base/kadali-api-deployment.yaml

# Create namespace
echo ""
echo "📦 Creating Kubernetes namespace..."
kubectl apply -f k8s/base/namespace.yaml

# Deploy PostgreSQL
echo "🐘 Deploying PostgreSQL..."
kubectl apply -f k8s/base/postgres-deployment.yaml

# Deploy MinIO
echo "💾 Deploying MinIO..."
kubectl apply -f k8s/base/minio-deployment.yaml

# Wait for databases to be ready
echo "⏳ Waiting for databases to be ready..."
kubectl wait --for=condition=ready pod -l app=postgres -n kadali --timeout=300s
kubectl wait --for=condition=ready pod -l app=minio -n kadali --timeout=300s

# Deploy Kadali API
echo "🚀 Deploying Kadali API..."
kubectl apply -f k8s/base/kadali-api-deployment.yaml

# Deploy HPA
echo "📊 Deploying Horizontal Pod Autoscaler..."
kubectl apply -f k8s/base/hpa.yaml

# Deploy Ingress
echo "🌐 Deploying Ingress..."
kubectl apply -f k8s/base/ingress.yaml

# Wait for API to be ready
echo "⏳ Waiting for Kadali API to be ready..."
kubectl wait --for=condition=ready pod -l app=kadali-api -n kadali --timeout=300s

echo ""
echo "✅ Deployment complete!"
echo ""
echo "📊 Status:"
kubectl get pods -n kadali

echo ""
echo "🔗 Service URLs:"
kubectl get ingress -n kadali

echo ""
echo "📝 Next steps:"
echo "1. Update DNS to point to your ingress IP"
echo "2. Configure SSL certificates (cert-manager)"
echo "3. Update secrets in k8s/base/kadali-api-deployment.yaml"
echo "4. Monitor logs: kubectl logs -f -l app=kadali-api -n kadali"
echo ""
echo "🎉 Kadali is live!"

