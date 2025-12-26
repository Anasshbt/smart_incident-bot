#!/bin/bash

# =============================================================
# Smart Incident Bot - Monitoring Stack Deployment Script
# =============================================================
# This script deploys Prometheus and Grafana to Kubernetes
# for monitoring the Smart Incident Bot application.
# =============================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MONITORING_DIR="${SCRIPT_DIR}"

echo "=============================================="
echo "   Smart Incident Bot - Monitoring Deployment"
echo "=============================================="
echo ""

# Check if kubectl is available
if ! command -v kubectl &> /dev/null; then
    echo "Error: kubectl is not installed or not in PATH"
    exit 1
fi

# Check cluster connection
echo "Checking Kubernetes cluster connection..."
if ! kubectl cluster-info &> /dev/null; then
    echo "Error: Cannot connect to Kubernetes cluster"
    exit 1
fi
echo "✓ Connected to cluster"
echo ""

# Step 1: Create monitoring namespace
echo "Step 1: Creating monitoring namespace..."
kubectl apply -f "${MONITORING_DIR}/namespace.yaml"
echo "✓ Namespace created"
echo ""

# Step 2: Deploy Prometheus
echo "Step 2: Deploying Prometheus..."
kubectl apply -f "${MONITORING_DIR}/prometheus-rbac.yaml"
kubectl apply -f "${MONITORING_DIR}/prometheus-configmap.yaml"
kubectl apply -f "${MONITORING_DIR}/prometheus-rules.yaml"
kubectl apply -f "${MONITORING_DIR}/prometheus-deployment.yaml"
echo "✓ Prometheus deployed"
echo ""

# Step 3: Deploy Grafana
echo "Step 3: Deploying Grafana..."
kubectl apply -f "${MONITORING_DIR}/grafana-configmap.yaml"
kubectl apply -f "${MONITORING_DIR}/grafana-dashboards.yaml"
kubectl apply -f "${MONITORING_DIR}/grafana-deployment.yaml"
echo "✓ Grafana deployed"
echo ""

# Wait for deployments
echo "Waiting for deployments to be ready..."
kubectl rollout status deployment/prometheus -n monitoring --timeout=120s
kubectl rollout status deployment/grafana -n monitoring --timeout=120s
echo "✓ All deployments ready"
echo ""

# Print access information
echo "=============================================="
echo "   Deployment Complete!"
echo "=============================================="
echo ""
echo "To access Prometheus:"
echo "  kubectl port-forward svc/prometheus 9090:9090 -n monitoring"
echo "  Open: http://localhost:9090"
echo ""
echo "To access Grafana:"
echo "  kubectl port-forward svc/grafana 3000:3000 -n monitoring"
echo "  Open: http://localhost:3000"
echo "  Default credentials: admin / admin123"
echo ""
echo "Verify pods are running:"
echo "  kubectl get pods -n monitoring"
echo ""
echo "=============================================="
