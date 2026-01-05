#!/bin/bash
# 🚀 Quick Start Script for Kadali Data Platform
# Run this in your terminal to start everything

set -e  # Exit on error

echo "╔════════════════════════════════════════════════════════════╗"
echo "║     🚀 Kadali Data Platform - Quick Start Script          ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[✓]${NC} $1"
}

print_error() {
    echo -e "${RED}[✗]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[!]${NC} $1"
}

# Check prerequisites
print_status "Checking prerequisites..."
echo ""

# Check Java
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
    print_success "Java found: $JAVA_VERSION"
else
    print_error "Java not found. Please install Java 17+"
    echo "   Download: https://adoptium.net/"
    exit 1
fi

# Check Maven
if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn -version | head -1 | awk '{print $3}')
    print_success "Maven found: $MVN_VERSION"
else
    print_error "Maven not found. Please install Maven"
    echo "   Install: brew install maven"
    exit 1
fi

# Check Docker
if command -v docker &> /dev/null; then
    DOCKER_VERSION=$(docker --version | awk '{print $3}')
    print_success "Docker found: $DOCKER_VERSION"
else
    print_error "Docker not found. Please install Docker Desktop"
    echo "   Download: https://www.docker.com/products/docker-desktop"
    exit 1
fi

# Check Node.js
if command -v node &> /dev/null; then
    NODE_VERSION=$(node --version)
    print_success "Node.js found: $NODE_VERSION"
else
    print_error "Node.js not found. Please install Node.js 18+"
    echo "   Download: https://nodejs.org/"
    exit 1
fi

echo ""
print_success "All prerequisites met!"
echo ""

# Navigate to project directory
PROJECT_DIR="/Users/aswinkumar/Downloads/Aswin/Startups/backend-project/kadali"
cd "$PROJECT_DIR"

# Step 1: Start Docker services
print_status "Step 1: Starting Docker services (PostgreSQL, MinIO, Metastore)..."
echo ""

if docker compose version &> /dev/null; then
    docker compose up -d
elif docker-compose --version &> /dev/null; then
    docker-compose up -d
else
    print_error "Docker Compose not found"
    exit 1
fi

print_success "Docker services starting..."
echo ""
print_status "Waiting 30 seconds for services to initialize..."
sleep 30

# Check Docker services
print_status "Checking service status..."
if docker compose version &> /dev/null; then
    docker compose ps
elif docker-compose --version &> /dev/null; then
    docker-compose ps
fi

echo ""
print_success "Docker services are running!"
echo ""
echo "You can access:"
echo "  • PostgreSQL: localhost:5432 (user: postgres, pass: postgres)"
echo "  • MinIO API: http://localhost:9000"
echo "  • MinIO Console: http://localhost:9001 (user: minioadmin, pass: minioadmin)"
echo ""

# Step 2: Build backend (if needed)
if [ ! -d "target" ]; then
    print_status "Step 2: Building backend for the first time (this may take 2-5 minutes)..."
    echo ""
    mvn clean install -DskipTests
    print_success "Backend built successfully!"
    echo ""
else
    print_success "Backend already built. Skipping build..."
    echo ""
fi

# Step 3: Instructions for starting backend
echo "╔════════════════════════════════════════════════════════════╗"
echo "║                   BACKEND READY TO START                   ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""
print_warning "Open a NEW terminal window and run:"
echo ""
echo "  cd $PROJECT_DIR"
echo "  mvn spring-boot:run"
echo ""
echo "Wait for: 'Started KadaliDataPlatformApplication'"
echo ""
read -p "Press ENTER when backend is running..."

# Step 4: Install frontend dependencies
print_status "Step 3: Setting up frontend..."
echo ""
cd frontend

if [ ! -d "node_modules" ]; then
    print_status "Installing frontend dependencies (2-3 minutes)..."
    npm install
    print_success "Frontend dependencies installed!"
else
    print_success "Frontend dependencies already installed!"
fi

echo ""
echo "╔════════════════════════════════════════════════════════════╗"
echo "║                  FRONTEND READY TO START                   ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""
print_warning "Open another NEW terminal window and run:"
echo ""
echo "  cd $PROJECT_DIR/frontend"
echo "  npm run dev"
echo ""
echo "Then open your browser to: http://localhost:3000"
echo ""
read -p "Press ENTER when frontend is running..."

echo ""
echo "╔════════════════════════════════════════════════════════════╗"
echo "║                 🎉 SETUP COMPLETE! 🎉                      ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""
print_success "Kadali Data Platform is now running!"
echo ""
echo "Access points:"
echo "  • Frontend Dashboard: http://localhost:3000"
echo "  • Backend API:        http://localhost:8080"
echo "  • API Health:         http://localhost:8080/actuator/health"
echo "  • MinIO Console:      http://localhost:9001"
echo ""
echo "Next steps:"
echo "  1. Open http://localhost:3000 in your browser"
echo "  2. Go to Settings → Set Tenant ID to: my-startup"
echo "  3. Go to Clusters → Create your first Spark cluster!"
echo "  4. Go to Data Explorer → Run SQL queries!"
echo ""
echo "To test the API directly:"
echo "  curl http://localhost:8080/actuator/health"
echo ""
echo "To stop everything later:"
echo "  • Press Ctrl+C in backend terminal"
echo "  • Press Ctrl+C in frontend terminal"
echo "  • Run: docker compose down"
echo ""
print_success "Happy data analyzing! 🚀"
echo ""

