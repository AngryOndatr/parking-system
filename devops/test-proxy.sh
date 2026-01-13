#!/bin/bash
# ============================================================
# API Gateway Proxy Smoke Tests
# Purpose: Verify API Gateway correctly proxies requests to services
# Usage: ./test-proxy.sh
# ============================================================

set +e  # Continue on error

API_GATEWAY="http://localhost:8086"
TOKEN=""

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
GRAY='\033[0;90m'
NC='\033[0m' # No Color

echo -e "\n${CYAN}🧪 API Gateway Proxy Smoke Tests${NC}"
echo "============================================================"
echo -e "Gateway URL: ${API_GATEWAY}\n"

# ============================================================
# Step 1: Authentication
# ============================================================
echo -e "${YELLOW}🔐 Step 1: Authenticating...${NC}"

LOGIN_RESPONSE=$(curl -s -X POST "$API_GATEWAY/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"parking123"}')

TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

if [ -n "$TOKEN" ]; then
    echo -e "${GREEN}✅ Authentication successful${NC}"
    echo -e "${GRAY}   Token length: ${#TOKEN} characters${NC}\n"
else
    echo -e "${RED}❌ Authentication failed - no token received${NC}"
    exit 1
fi

# ============================================================
# Helper Function: Test Endpoint
# ============================================================
test_endpoint() {
    local method=$1
    local endpoint=$2
    local description=$3
    local body=$4

    echo -e "${CYAN}Testing: $description${NC}"
    echo -e "${GRAY}  $method $endpoint${NC}"

    if [ -n "$body" ]; then
        response=$(curl -s -w "\n%{http_code}" -X $method "$API_GATEWAY$endpoint" \
          -H "Authorization: Bearer $TOKEN" \
          -H "Content-Type: application/json" \
          -d "$body")
    else
        response=$(curl -s -w "\n%{http_code}" -X $method "$API_GATEWAY$endpoint" \
          -H "Authorization: Bearer $TOKEN" \
          -H "Content-Type: application/json")
    fi

    http_code=$(echo "$response" | tail -n1)
    body_response=$(echo "$response" | sed '$d')

    if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
        echo -e "${GREEN}  ✅ Status: $http_code OK${NC}"
        echo -e "${GRAY}  📊 Response received${NC}"
        echo ""
        return 0
    else
        echo -e "${RED}  ❌ Status: $http_code${NC}"
        echo -e "${RED}  💬 Error: $body_response${NC}"
        echo ""
        return 1
    fi
}

# ============================================================
# Step 2: Management Service Proxy Tests
# ============================================================
echo -e "\n${YELLOW}📦 Step 2: Testing Management Service Proxy${NC}"
echo "------------------------------------------------------------"

management_passed=0
management_failed=0

# Test 2.1: Get available parking spots
if test_endpoint "GET" "/api/management/spots/available" "Get available parking spots"; then
    ((management_passed++))
else
    ((management_failed++))
fi

# Test 2.2: Get available spots count
if test_endpoint "GET" "/api/management/spots/available/count" "Get available spots count"; then
    ((management_passed++))
else
    ((management_failed++))
fi

# Test 2.3: Get all parking spots
if test_endpoint "GET" "/api/management/spots" "Get all parking spots"; then
    ((management_passed++))
else
    ((management_failed++))
fi

# Test 2.4: Search spots by type and status
if test_endpoint "GET" "/api/management/spots/search?type=STANDARD&status=AVAILABLE" "Search spots by type and status"; then
    ((management_passed++))
else
    ((management_failed++))
fi

if [ $management_failed -eq 0 ]; then
    echo -e "${GREEN}Management Service Results: $management_passed passed, $management_failed failed${NC}"
else
    echo -e "${YELLOW}Management Service Results: $management_passed passed, $management_failed failed${NC}"
fi

# ============================================================
# Step 3: Reporting Service Proxy Tests
# ============================================================
echo -e "\n${YELLOW}📊 Step 3: Testing Reporting Service Proxy${NC}"
echo "------------------------------------------------------------"

reporting_passed=0
reporting_failed=0

# Test 3.1: Create Log Entry (POST)
LOG_BODY=$(cat <<EOF
{
  "timestamp": "$(date -u +"%Y-%m-%dT%H:%M:%SZ")",
  "level": "INFO",
  "service": "test-proxy-script",
  "message": "Smoke test log entry",
  "userId": 1,
  "meta": {
    "test": true,
    "timestamp": "$(date -Iseconds)"
  }
}
EOF
)

if test_endpoint "POST" "/api/reporting/log" "Create log entry" "$LOG_BODY"; then
    ((reporting_passed++))
else
    ((reporting_failed++))
fi

# Test 3.2: Get all logs
if test_endpoint "GET" "/api/reporting/logs" "Get all logs"; then
    ((reporting_passed++))
else
    ((reporting_failed++))
fi

# Test 3.3: Get logs by level
if test_endpoint "GET" "/api/reporting/logs?level=INFO" "Get logs by level (INFO)"; then
    ((reporting_passed++))
else
    ((reporting_failed++))
fi

# Test 3.4: Get logs by service
if test_endpoint "GET" "/api/reporting/logs?service=test-proxy-script" "Get logs by service"; then
    ((reporting_passed++))
else
    ((reporting_failed++))
fi

# Test 3.5: Get logs with limit
if test_endpoint "GET" "/api/reporting/logs?limit=5" "Get logs with limit"; then
    ((reporting_passed++))
else
    ((reporting_failed++))
fi

if [ $reporting_failed -eq 0 ]; then
    echo -e "${GREEN}Reporting Service Results: $reporting_passed passed, $reporting_failed failed${NC}"
else
    echo -e "${YELLOW}Reporting Service Results: $reporting_passed passed, $reporting_failed failed${NC}"
fi

# ============================================================
# Step 4: Client Service Proxy Tests (existing)
# ============================================================
echo -e "\n${YELLOW}👥 Step 4: Testing Client Service Proxy${NC}"
echo "------------------------------------------------------------"

client_passed=0
client_failed=0

# Test 4.1: Get all clients
if test_endpoint "GET" "/api/clients" "Get all clients"; then
    ((client_passed++))
else
    ((client_failed++))
fi

# Test 4.2: Get all vehicles
if test_endpoint "GET" "/api/vehicles" "Get all vehicles"; then
    ((client_passed++))
else
    ((client_failed++))
fi

if [ $client_failed -eq 0 ]; then
    echo -e "${GREEN}Client Service Results: $client_passed passed, $client_failed failed${NC}"
else
    echo -e "${YELLOW}Client Service Results: $client_passed passed, $client_failed failed${NC}"
fi

# ============================================================
# Final Summary
# ============================================================
echo -e "\n============================================================"
echo -e "${CYAN}📋 Test Summary${NC}"
echo "============================================================"

total_passed=$((management_passed + reporting_passed + client_passed))
total_failed=$((management_failed + reporting_failed + client_failed))
total_tests=$((total_passed + total_failed))

echo ""
if [ $management_failed -eq 0 ]; then
    echo -e "${GREEN}Management Service: $management_passed/4 passed${NC}"
else
    echo -e "${YELLOW}Management Service: $management_passed/4 passed${NC}"
fi

if [ $reporting_failed -eq 0 ]; then
    echo -e "${GREEN}Reporting Service:  $reporting_passed/5 passed${NC}"
else
    echo -e "${YELLOW}Reporting Service:  $reporting_passed/5 passed${NC}"
fi

if [ $client_failed -eq 0 ]; then
    echo -e "${GREEN}Client Service:     $client_passed/2 passed${NC}"
else
    echo -e "${YELLOW}Client Service:     $client_passed/2 passed${NC}"
fi

echo "------------------------------------------------------------"
if [ $total_failed -eq 0 ]; then
    echo -e "${GREEN}Total:              $total_passed/$total_tests passed${NC}"
else
    echo -e "${YELLOW}Total:              $total_passed/$total_tests passed${NC}"
fi

if [ $total_failed -eq 0 ]; then
    echo -e "\n${GREEN}✅ All proxy tests passed!${NC}"
    exit 0
else
    echo -e "\n${YELLOW}⚠️  Some tests failed. Check the output above for details.${NC}"
    exit 1
fi

