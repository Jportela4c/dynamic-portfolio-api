#!/bin/bash

# Dynamic Portfolio API - Comprehensive Newman Test Suite
# Runs all test collections in sequence with proper reporting

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
COLLECTIONS_DIR="$PROJECT_ROOT/postman/collections"
ENV_FILE="$PROJECT_ROOT/postman/environments/local-docker.postman_environment.json"
REPORTS_DIR="$PROJECT_ROOT/reports"

echo "========================================="
echo "Dynamic Portfolio API - E2E Test Suite"
echo "========================================="
echo ""

# Create reports directory
mkdir -p "$REPORTS_DIR"

# Check if services are running
echo "Checking if services are running..."
if ! curl -sf http://localhost:8080/api/v1/actuator/health > /dev/null; then
    echo "ERROR: API service is not running at http://localhost:8080"
    echo "Please start services with: docker compose up -d"
    exit 1
fi
echo "✓ API service is healthy"
echo ""

# Test execution function
run_collection() {
    local collection_file=$1
    local collection_name=$(basename "$collection_file" .json)
    
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "Running: $collection_name"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    
    newman run "$collection_file" \
        -e "$ENV_FILE" \
        --reporters cli,json \
        --reporter-json-export "$REPORTS_DIR/${collection_name}-report.json" \
        --timeout-request 10000 \
        --bail \
        || echo "⚠ Collection had failures: $collection_name"
    
    echo ""
}

# Run collections in order
echo "Starting test execution..."
echo ""

if [ -f "$COLLECTIONS_DIR/01-oauth2-auth.json" ]; then
    run_collection "$COLLECTIONS_DIR/01-oauth2-auth.json"
fi

if [ -f "$COLLECTIONS_DIR/02-ofb-integration.json" ]; then
    run_collection "$COLLECTIONS_DIR/02-ofb-integration.json"
fi

if [ -f "$COLLECTIONS_DIR/03-security-headers.json" ]; then
    run_collection "$COLLECTIONS_DIR/03-security-headers.json"
fi

if [ -f "$COLLECTIONS_DIR/04-api-edge-cases.json" ]; then
    run_collection "$COLLECTIONS_DIR/04-api-edge-cases.json"
fi

if [ -f "$COLLECTIONS_DIR/05-api-happy-paths.json" ]; then
    run_collection "$COLLECTIONS_DIR/05-api-happy-paths.json"
fi

if [ -f "$COLLECTIONS_DIR/06-performance-benchmarks.json" ]; then
    run_collection "$COLLECTIONS_DIR/06-performance-benchmarks.json"
fi

if [ -f "$COLLECTIONS_DIR/07-token-lifecycle.json" ]; then
    run_collection "$COLLECTIONS_DIR/07-token-lifecycle.json"
fi

# Run existing collections if they exist
if [ -f "$PROJECT_ROOT/postman/Dynamic-Portfolio-API.postman_collection.json" ]; then
    echo "Running legacy comprehensive collection..."
    run_collection "$PROJECT_ROOT/postman/Dynamic-Portfolio-API.postman_collection.json"
fi

echo "========================================="
echo "Test execution complete!"
echo "========================================="
echo ""
echo "Reports saved to: $REPORTS_DIR"
echo ""

# Count results
total_reports=$(ls -1 "$REPORTS_DIR"/*.json 2>/dev/null | wc -l)
echo "Generated $total_reports test reports"
echo ""

echo "To view detailed results:"
echo "  cat $REPORTS_DIR/*-report.json | jq '.run.stats'"
