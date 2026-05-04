# =============================================================================
# deploy.ps1 — Secure File Delivery API
# =============================================================================
# One-command deployment: builds Docker image, starts all services, and
# optionally deploys to Kubernetes.
#
# Usage:
#   .\deploy.ps1                  # Docker Compose only (default)
#   .\deploy.ps1 -Target k8s     # Docker build + deploy to Kubernetes
#   .\deploy.ps1 -Target all     # Docker Compose + Kubernetes
#   .\deploy.ps1 -Down           # Tear down Docker Compose stack
# =============================================================================
param(
    [ValidateSet("docker", "k8s", "all")]
    [string]$Target = "docker",
    [switch]$Down
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$ProjectRoot = $PSScriptRoot
$ServiceName = "secure-file-delivery-api"
$K8sNamespace = "statement-api"
$DockerImage = "statement-api:latest"
$ApiPort = 8093

# -----------------------------------------------------------------------------
# Helper Functions
# -----------------------------------------------------------------------------
function Write-Step { param([string]$Message) Write-Host "`n==> $Message" -ForegroundColor Cyan }
function Write-Ok   { param([string]$Message) Write-Host "    [OK] $Message" -ForegroundColor Green }
function Write-Fail { param([string]$Message) Write-Host "    [FAIL] $Message" -ForegroundColor Red }

function Test-Command {
    param([string]$Command)
    $null -ne (Get-Command $Command -ErrorAction SilentlyContinue)
}

function Wait-ForHealthCheck {
    param([string]$Url, [int]$TimeoutSeconds = 120)
    $elapsed = 0
    while ($elapsed -lt $TimeoutSeconds) {
        try {
            $response = Invoke-RestMethod -Uri $Url -TimeoutSec 5 -ErrorAction Stop
            if ($response.status -eq "UP") { return $true }
        } catch { }
        Start-Sleep -Seconds 5
        $elapsed += 5
        Write-Host "    Waiting for health check... ($elapsed s)" -ForegroundColor DarkGray
    }
    return $false
}

# -----------------------------------------------------------------------------
# Tear Down
# -----------------------------------------------------------------------------
if ($Down) {
    Write-Step "Tearing down $ServiceName Docker Compose stack"
    Push-Location $ProjectRoot
    docker compose down -v
    Pop-Location
    Write-Ok "Stack removed"
    exit 0
}

# -----------------------------------------------------------------------------
# Prerequisites Check
# -----------------------------------------------------------------------------
Write-Step "Checking prerequisites"
if (-not (Test-Command "docker")) { Write-Fail "Docker is not installed"; exit 1 }
Write-Ok "Docker found"

if ($Target -in @("k8s", "all")) {
    if (-not (Test-Command "kubectl")) { Write-Fail "kubectl is not installed"; exit 1 }
    Write-Ok "kubectl found"
}

# -----------------------------------------------------------------------------
# Generate .env
# -----------------------------------------------------------------------------
Write-Step "Configuring environment"
Push-Location $ProjectRoot

if (-not (Test-Path ".env")) {
    Copy-Item ".env.example" ".env"
    Write-Ok "Created .env from .env.example"
} else {
    Write-Ok ".env already exists — keeping current values"
}

# -----------------------------------------------------------------------------
# Docker Compose
# -----------------------------------------------------------------------------
if ($Target -in @("docker", "all")) {
    Write-Step "Building and starting Docker Compose stack"
    docker compose up -d --build
    if ($LASTEXITCODE -ne 0) { Write-Fail "Docker Compose failed"; Pop-Location; exit 1 }
    Write-Ok "Containers started"

    Write-Step "Waiting for API health check (http://localhost:$ApiPort/actuator/health)"
    if (Wait-ForHealthCheck "http://localhost:$ApiPort/actuator/health") {
        Write-Ok "API is healthy"
    } else {
        Write-Fail "API did not become healthy within timeout"
        docker compose logs api --tail 50
        Pop-Location; exit 1
    }

    Write-Host ""
    Write-Host "  =================================================" -ForegroundColor Green
    Write-Host "  Secure File Delivery API is running!" -ForegroundColor Green
    Write-Host "  =================================================" -ForegroundColor Green
    Write-Host "  API:          http://localhost:$ApiPort/api/v1/statements" -ForegroundColor White
    Write-Host "  Swagger UI:   http://localhost:$ApiPort/swagger-ui/index.html" -ForegroundColor White
    Write-Host "  Health:       http://localhost:$ApiPort/actuator/health" -ForegroundColor White
    Write-Host "  RabbitMQ UI:  http://localhost:15672" -ForegroundColor White
    Write-Host "  =================================================" -ForegroundColor Green
}

# -----------------------------------------------------------------------------
# Kubernetes Deployment
# -----------------------------------------------------------------------------
if ($Target -in @("k8s", "all")) {
    Write-Step "Building Docker image for Kubernetes"
    docker build -t $DockerImage .
    if ($LASTEXITCODE -ne 0) { Write-Fail "Docker build failed"; Pop-Location; exit 1 }
    Write-Ok "Image built: $DockerImage"

    Write-Step "Deploying to Kubernetes namespace: $K8sNamespace"
    $k8sFiles = @(
        "k8s/namespace.yml",
        "k8s/secret.yml",
        "k8s/configmap.yml",
        "k8s/postgres.yml",
        "k8s/rabbitmq.yml",
        "k8s/azurite.yml",
        "k8s/api-deployment.yml"
    )
    foreach ($file in $k8sFiles) {
        Write-Host "    Applying $file" -ForegroundColor DarkGray
        kubectl apply -f $file
        if ($LASTEXITCODE -ne 0) { Write-Fail "Failed to apply $file"; Pop-Location; exit 1 }
    }
    Write-Ok "All K8s manifests applied"

    Write-Step "Waiting for rollout"
    kubectl rollout status deployment/statement-api -n $K8sNamespace --timeout=180s
    if ($LASTEXITCODE -ne 0) {
        Write-Fail "Rollout did not complete"
        kubectl get pods -n $K8sNamespace
        Pop-Location; exit 1
    }
    Write-Ok "Deployment rolled out successfully"

    Write-Host ""
    Write-Host "  =================================================" -ForegroundColor Green
    Write-Host "  Kubernetes deployment complete!" -ForegroundColor Green
    Write-Host "  =================================================" -ForegroundColor Green
    Write-Host "  Namespace:  $K8sNamespace" -ForegroundColor White
    Write-Host "  Ingress:    https://statement-api.nonprod.local" -ForegroundColor White
    Write-Host "  =================================================" -ForegroundColor Green
    kubectl get pods -n $K8sNamespace
}

Pop-Location
Write-Host "`nDone." -ForegroundColor Green
