#!/bin/bash

# ============================================================================
# Secure File Delivery API - Complete Test Setup
# ============================================================================
# This script automates:
#   1. Building common-api-starter-parent modules
#   2. Building secure-file-delivery-api
#   3. Starting Docker containers (PostgreSQL, RabbitMQ, Azurite)
#   4. Running database migrations
#   5. Obtaining JWT token via Postman (auto or manual)
#   6. Testing the API endpoints
# ============================================================================

set -e  # Stop on error

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_step() { echo -e "\n${BLUE}========================================${NC}"; echo -e "${GREEN}$1${NC}"; echo -e "${BLUE}========================================${NC}\n"; }
print_success() { echo -e "${GREEN}✅ $1${NC}"; }
print_error() { echo -e "${RED}❌ $1${NC}"; }
print_warning() { echo -e "${YELLOW}⚠️  $1${NC}"; }
print_info() { echo -e "${BLUE}ℹ️  $1${NC}"; }

# ============================================================================
# Configuration
# ============================================================================
PARENT_DIR="${PARENT_DIR:-../common-api-starter-parent}"
API_URL="http://localhost:8093"
TOKEN_FILE=".jwt_token"

# ============================================================================
# Prerequisites Check
# ============================================================================
print_step "Step 1: Checking Prerequisites"

check_command() {
    if ! command -v $1 &> /dev/null; then
        print_error "$1 is not installed"
        print_info "Please install $1 first"
        exit 1
    fi
}

check_command "docker"
check_command "java"
check_command "mvn"
check_command "curl"

print_success "All prerequisites satisfied"

# ============================================================================
# Build Parent Modules
# ============================================================================
print_step "Step 2: Building Common Starter Parent Modules"

if [ -d "$PARENT_DIR" ]; then
    print_info "Building parent modules from: $PARENT_DIR"
    cd "$PARENT_DIR"
    mvn clean install -DskipTests -Dcheckstyle.skip=true -B
    
    if [ $? -ne 0 ]; then
        print_error "Failed to build parent modules"
        exit 1
    fi
    print_success "Parent modules built and installed"
    cd - > /dev/null
else
    print_warning "Parent directory not found at: $PARENT_DIR"
    print_info "Assuming parent modules are already installed in .m2 repository"
fi

# ============================================================================
# Build Main API
# ============================================================================
print_step "Step 3: Building Secure File Delivery API"

mvn clean package -DskipTests -Dcheckstyle.skip=true -B

if [ $? -ne 0 ]; then
    print_error "Failed to build API"
    exit 1
fi
print_success "API built successfully"

# ============================================================================
# Setup Environment Variables
# ============================================================================
print_step "Step 4: Configuring Environment"

if [ ! -f ".env" ]; then
    if [ -f ".env.example" ]; then
        cp .env.example .env
        print_info "Created .env from .env.example"
    else
        # Create default .env file
        cat > .env << 'EOF'
# PostgreSQL
POSTGRES_DB=statementdb
POSTGRES_USER=statement_user
POSTGRES_PASSWORD=statement_secret_2026
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/statementdb
SPRING_DATASOURCE_USERNAME=statement_user
SPRING_DATASOURCE_PASSWORD=statement_secret_2026

# RabbitMQ
RABBITMQ_DEFAULT_USER=rabbitmq_user
RABBITMQ_DEFAULT_PASS=rabbitmq_secret_2026
SPRING_RABBITMQ_HOST=localhost
SPRING_RABBITMQ_PORT=5672
SPRING_RABBITMQ_USERNAME=rabbitmq_user
SPRING_RABBITMQ_PASSWORD=rabbitmq_secret_2026

# Events
EVENTS_EXCHANGE_NAME=statement.events.exchange
EVENTS_QUEUE_NAME=statement.events.queue
EVENTS_ROUTING_KEY=statement.event
EVENTS_ROUTING_KEY_STATEMENT_UPLOADED=statement.uploaded
EVENTS_ROUTING_KEY_STATEMENT_DELETED=statement.deleted
EVENTS_ROUTING_KEY_STATEMENT_DOWNLOADED=statement.downloaded
EVENTS_ROUTING_KEY_DOWNLOAD_LINK_CREATED=download-link.created
EVENTS_ROUTING_KEY_DOWNLOAD_LINK_EXPIRED=download-link.expired
EVENTS_ROUTING_KEY_DOWNLOAD_LINK_REVOKED=download-link.revoked

# Azure Storage (Azurite)
AZURE_BLOB_STORAGE_URL=http://localhost:10000/devstoreaccount1
AZURE_STORAGE_CONNECTION_STRING=DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;BlobEndpoint=http://localhost:10000/devstoreaccount1;

# Download Links
DOWNLOAD_HMAC_SECRET=test-secret-key-for-hmac-32-chars-long
DOWNLOAD_BASE_URL=http://localhost:8093/api/v1/statements/download

# OAuth2 - Personal Microsoft Account
OAUTH2_ISSUER_URI=https://sts.windows.net/a7048452-8647-45be-a810-e2f665115655/
OAUTH2_AUDIENCE=api://a3704500-c79d-43b4-a2bd-30b1437e819a

# Sentry (disabled for testing)
SENTRY_ENABLED=false
SPRING_PROFILES_ACTIVE=docker
SERVER_PORT=8093
EOF
        print_info "Created default .env file"
    fi
fi

print_success "Environment configured"

# ============================================================================
# Start Docker Containers
# ============================================================================
print_step "Step 5: Starting Docker Containers"

# Check if docker-compose.yml exists
if [ ! -f "docker-compose.yml" ]; then
    print_error "docker-compose.yml not found"
    exit 1
fi

# Stop any existing containers
docker compose down 2>/dev/null

# Start containers
docker compose up -d --build

if [ $? -ne 0 ]; then
    print_error "Failed to start Docker containers"
    exit 1
fi
print_success "Docker containers started"

# ============================================================================
# Wait for Services
# ============================================================================
print_step "Step 6: Waiting for Services to be Ready"

wait_for_service() {
    local service=$1
    local url=$2
    local max_attempts=30
    local attempt=1
    
    print_info "Waiting for $service..."
    while [ $attempt -le $max_attempts ]; do
        if curl -s -f "$url" > /dev/null 2>&1; then
            print_success "$service is ready"
            return 0
        fi
        sleep 2
        attempt=$((attempt + 1))
    done
    print_warning "$service may not be ready"
    return 1
}

# Wait for PostgreSQL
wait_for_service "PostgreSQL" "http://localhost:5432" || true

# Wait for API
wait_for_service "API" "$API_URL/actuator/health"

print_success "Services are ready"

# ============================================================================
# Run Database Schema (via JPA)
# ============================================================================
print_step "Step 7: Database Schema Setup"

# JPA will auto-create tables, but wait a bit
sleep 5
print_success "Database schema ready (managed by JPA)"

# ============================================================================
# Obtain JWT Token
# ============================================================================
print_step "Step 8: Obtaining JWT Token"

# Check if Newman is installed
if ! command -v newman &> /dev/null; then
    print_warning "Newman (Postman CLI) not installed"
    print_info "Attempting to install Newman via npm..."
    
    if command -v npm &> /dev/null; then
        npm install -g newman
        print_success "Newman installed"
    else
        print_warning "npm not found. Manual token setup required."
    fi
fi

# Create Postman environment file
cat > postman-environment.json << 'EOF'
{
  "id": "statement-api-env",
  "name": "Statement API - Test",
  "values": [
    { "key": "base_url", "value": "http://localhost:8093", "type": "default", "enabled": true },
    { "key": "api_path", "value": "/api/v1/statements", "type": "default", "enabled": true },
    { "key": "tenant_id", "value": "a7048452-8647-45be-a810-e2f665115655", "type": "default", "enabled": true },
    { "key": "client_id", "value": "a3704500-c79d-43b4-a2bd-30b1437e819a", "type": "default", "enabled": true },
    { "key": "scope_prefix", "value": "api://a3704500-c79d-43b4-a2bd-30b1437e819a", "type": "default", "enabled": true },
    { "key": "bearer_token", "value": "", "type": "secret", "enabled": true },
    { "key": "statement_id", "value": "", "type": "default", "enabled": true },
    { "key": "download_token", "value": "", "type": "default", "enabled": true }
  ]
}
EOF

print_info "Postman environment created"

# Instructions for obtaining token
echo ""
echo "============================================================================"
echo "🔐 TOKEN SETUP"
echo "============================================================================"
echo ""
echo "To obtain a JWT token for testing:"
echo ""
echo "Method 1 - Using Postman Desktop (Recommended):"
echo "  1. Open Postman"
echo "  2. Import 'postman-collection.json' and 'postman-environment.json'"
echo "  3. In Authorization tab, select 'OAuth 2.0'"
echo "  4. Configure:"
echo "     - Grant Type: Authorization Code (with PKCE)"
echo "     - Callback URL: https://oauth.pstmn.io/v1/callback"
echo "     - Auth URL: https://login.microsoftonline.com/a7048452-8647-45be-a810-e2f665115655/oauth2/v2.0/authorize"
echo "     - Access Token URL: https://login.microsoftonline.com/a7048452-8647-45be-a810-e2f665115655/oauth2/v2.0/token"
echo "     - Client ID: a3704500-c79d-43b4-a2bd-30b1437e819a"
echo "     - Scope: api://a3704500-c79d-43b4-a2bd-30b1437e819a/statement.read api://a3704500-c79d-43b4-a2bd-30b1437e819a/statement.create api://a3704500-c79d-43b4-a2bd-30b1437e819a/statement.delete offline_access"
echo "  5. Click 'Get New Access Token'"
echo "  6. Log in with your Microsoft account (skohliso@outlook.com)"
echo "  7. Click 'Use Token'"
echo ""
echo "Method 2 - Using curl (if token already available):"
echo "  export BEARER_TOKEN=\"your-token-here\""
echo ""

# Attempt to get token via curl if user wants
read -p "Do you have a JWT token ready? (y/n): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    read -p "Paste your JWT token: " USER_TOKEN
    echo "$USER_TOKEN" > "$TOKEN_FILE"
    print_success "Token saved"
    
    # Test the token
    echo ""
    print_step "Step 9: Testing API with Token"
    RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" "$API_URL/api/v1/statements?customerId=CUST-001&limit=10&offset=0" \
        -H "Authorization: Bearer $USER_TOKEN")
    
    if [ "$RESPONSE" = "200" ]; then
        print_success "Token is valid! API responded with $RESPONSE"
    else
        print_warning "Token test returned $RESPONSE"
    fi
else
    print_warning "Please obtain a token using Postman and save it"
    print_info "Once you have the token, run: echo 'your-token' > $TOKEN_FILE"
fi

# ============================================================================
# Test Commands
# ============================================================================
print_step "Step 10: Test Commands"

if [ -f "$TOKEN_FILE" ]; then
    TOKEN=$(cat "$TOKEN_FILE")
    
    echo ""
    echo "============================================================================"
    echo "📋 TESTING API ENDPOINTS"
    echo "============================================================================"
    echo ""
    
    # Test 1: Health Check
    echo "1️⃣ Health Check:"
    curl -s "$API_URL/actuator/health" | jq . || curl -s "$API_URL/actuator/health"
    echo ""
    
    # Test 2: List Statements
    echo ""
    echo "2️⃣ List Statements:"
    curl -s "$API_URL/api/v1/statements?customerId=CUST-001&limit=10&offset=0" \
        -H "Authorization: Bearer $TOKEN" | jq . || echo "No statements or error"
    echo ""
    
    # Test 3: Upload Statement (if test file exists)
    if [ -f "test-statement.pdf" ]; then
        echo ""
        echo "3️⃣ Upload Statement:"
        UPLOAD_RESPONSE=$(curl -s -X POST "$API_URL/api/v1/statements" \
            -H "Authorization: Bearer $TOKEN" \
            -F "file=@test-statement.pdf" \
            -F "customerId=CUST-001" \
            -F "statementDate=2026-05-01T00:00:00" \
            -F "statementType=MONTHLY" \
            -F "accountNumber=1234567890")
        
        echo "$UPLOAD_RESPONSE" | jq . || echo "$UPLOAD_RESPONSE"
        
        # Extract statement ID
        STATEMENT_ID=$(echo "$UPLOAD_RESPONSE" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
        if [ -n "$STATEMENT_ID" ]; then
            echo ""
            echo "✅ Uploaded Statement ID: $STATEMENT_ID"
            
            # Test 4: Find by ID
            echo ""
            echo "4️⃣ Find Statement by ID:"
            curl -s "$API_URL/api/v1/statements/$STATEMENT_ID" \
                -H "Authorization: Bearer $TOKEN" | jq .
            
            # Test 5: Generate Download Link
            echo ""
            echo "5️⃣ Generate Download Link:"
            LINK_RESPONSE=$(curl -s -X POST "$API_URL/api/v1/statements/$STATEMENT_ID/download-links" \
                -H "Authorization: Bearer $TOKEN" \
                -H "Content-Type: application/json" \
                -d '{"expiresInHours":24,"maxDownloads":3}')
            echo "$LINK_RESPONSE" | jq .
            
            # Extract download token
            DOWNLOAD_URL=$(echo "$LINK_RESPONSE" | grep -o '"downloadUrl":"[^"]*"' | cut -d'"' -f4)
            if [ -n "$DOWNLOAD_URL" ]; then
                echo ""
                echo "6️⃣ Download PDF (no auth required):"
                curl -s -I "$DOWNLOAD_URL" | head -1
            fi
        fi
    else
        print_warning "test-statement.pdf not found in current directory"
    fi
fi

# ============================================================================
# Summary
# ============================================================================
print_step "Setup Complete!"

echo ""
echo "============================================================================"
echo "✅ API is running at: $API_URL"
echo "✅ Swagger UI: $API_URL/swagger-ui/index.html"
echo "✅ Health Check: $API_URL/actuator/health"
echo ""
echo "📌 Useful Commands:"
echo "   docker compose logs -f statement-api    # View API logs"
echo "   docker compose logs -f postgres         # View database logs"
echo "   docker compose down                     # Stop all containers"
echo "   docker compose up -d                    # Start containers again"
echo ""
echo "📌 To test with your token:"
echo "   export TOKEN=\$(cat $TOKEN_FILE)"
echo "   curl -H \"Authorization: Bearer \$TOKEN\" $API_URL/api/v1/statements?customerId=CUST-001"
echo ""
echo "============================================================================"