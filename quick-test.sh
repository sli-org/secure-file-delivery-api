#!/bin/bash

# Quick test script after setup
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

API_URL="http://localhost:8093"

echo "========================================="
echo "Quick API Test"
echo "========================================="

# Test 1: Health
echo -n "Health Check: "
HEALTH=$(curl -s "$API_URL/actuator/health" | grep -o '"status":"UP"')
if [ -n "$HEALTH" ]; then
    echo -e "${GREEN}✓ PASS${NC}"
else
    echo -e "${RED}✗ FAIL${NC}"
fi

# Test 2: Check if running
echo -n "API Running: "
if curl -s -f "$API_URL/actuator/health" > /dev/null 2>&1; then
    echo -e "${GREEN}✓ PASS${NC}"
else
    echo -e "${RED}✗ FAIL${NC}"
fi

# Test 3: Docker containers
echo -n "Docker Containers: "
RUNNING=$(docker ps --format "table {{.Names}}" | grep -c "statement" || echo "0")
if [ "$RUNNING" -ge 2 ]; then
    echo -e "${GREEN}✓ PASS ($RUNNING containers)${NC}"
else
    echo -e "${RED}✗ FAIL (only $RUNNING containers)${NC}"
fi

echo ""
echo "========================================="