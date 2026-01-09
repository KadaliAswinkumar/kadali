#!/bin/bash
# 🦭 RUN KADALI NOW - PODMAN VERSION
# This script runs everything with Podman instead of Docker

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
MAGENTA='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m'

clear

echo -e "${CYAN}"
echo "╔════════════════════════════════════════════════════════════╗"
echo "║                                                            ║"
echo "║    🦭 KADALI DATA PLATFORM - PODMAN AUTO RUN 🦭           ║"
echo "║                                                            ║"
echo "║     Starting Your Analytics Platform with Podman!         ║"
echo "║                                                            ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo -e "${NC}"
echo ""

PROJECT_DIR="/Users/aswinkumar/Downloads/Aswin/Startups/backend-project/kadali"
cd "$PROJECT_DIR"

# Print with icon
print_step() {
    echo -e "${CYAN}[STEP]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[✓]${NC} $1"
}

print_error() {
    echo -e "${RED}[✗]${NC} $1"
}

print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_waiting() {
    echo -e "${YELLOW}[⏳]${NC} $1"
}

# Check Podman
print_step "Checking Podman..."
if ! command -v podman &> /dev/null; then
    print_error "Podman is not installed!"
    echo ""
    echo -e "${YELLOW}Please install Podman:${NC}"
    echo "  brew install podman"
    echo ""
    echo -e "${YELLOW}Then initialize Podman machine:${NC}"
    echo "  podman machine init --cpus 4 --memory 8192 --disk-size 50"
    echo "  podman machine start"
    echo ""
    echo "See PODMAN_SETUP.md for detailed instructions."
    exit 1
fi
print_success "Podman is installed!"

# Check Podman machine
print_step "Checking Podman machine..."
if ! podman machine list 2>/dev/null | grep -q "Currently running"; then
    print_error "Podman machine is not running!"
    
    # Try to start it
    if podman machine list 2>/dev/null | grep -q "podman"; then
        print_info "Starting Podman machine..."
        podman machine start
        sleep 5
    else
        print_error "No Podman machine found!"
        echo ""
        echo -e "${YELLOW}Please initialize Podman machine:${NC}"
        echo "  podman machine init --cpus 4 --memory 8192 --disk-size 50"
        echo "  podman machine start"
        echo ""
        echo "See PODMAN_SETUP.md for detailed instructions."
        exit 1
    fi
fi
print_success "Podman machine is running!"

# Check Podman connection
if ! podman ps >/dev/null 2>&1; then
    print_error "Cannot connect to Podman!"
    print_info "Try: podman machine start"
    exit 1
fi
print_success "Podman connection verified!"

# Check podman-compose
print_step "Checking podman-compose..."
if ! command -v podman-compose &> /dev/null; then
    print_error "podman-compose is not installed!"
    echo ""
    echo -e "${YELLOW}Please install podman-compose:${NC}"
    echo "  brew install podman-compose"
    echo ""
    echo "Or via pip:"
    echo "  pip3 install podman-compose"
    echo ""
    exit 1
fi
print_success "podman-compose is installed!"

echo ""
print_step "Starting infrastructure services..."
echo ""

# Start Podman Compose services
if podman-compose ps 2>/dev/null | grep -q "Up"; then
    print_info "Services already running. Checking status..."
    podman-compose ps
else
    print_info "Starting PostgreSQL, MinIO, and Hive Metastore..."
    podman-compose up -d
    
    print_waiting "Waiting 20 seconds for services to initialize..."
    for i in {20..1}; do
        echo -ne "${YELLOW}⏳ $i seconds remaining...${NC}\r"
        sleep 1
    done
    echo -e "\n"
fi

print_success "Infrastructure services are running!"
echo ""
podman-compose ps
echo ""

# Show service URLs
echo -e "${MAGENTA}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${MAGENTA}║              🌐 Infrastructure Services URLs 🌐            ║${NC}"
echo -e "${MAGENTA}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "  ${CYAN}PostgreSQL:${NC}    localhost:5432"
echo -e "                 User: postgres | Pass: postgres"
echo ""
echo -e "  ${CYAN}MinIO API:${NC}     http://localhost:9000"
echo ""
echo -e "  ${CYAN}MinIO Console:${NC} http://localhost:9001"
echo -e "                 User: minioadmin | Pass: minioadmin"
echo ""
echo -e "  ${CYAN}Metastore:${NC}     localhost:9083"
echo ""

# Build backend if needed
print_step "Checking backend build..."
if [ ! -d "target" ] || [ ! -f "target/kadali-0.0.1-SNAPSHOT.jar" ]; then
    echo ""
    print_info "Building backend (first time, ~2-3 minutes)..."
    mvn clean install -DskipTests -q
    print_success "Backend built successfully!"
else
    print_success "Backend already built!"
fi

echo ""
print_step "Starting Backend API..."
echo ""
print_info "Backend will start on: http://localhost:8080"
print_info "This will take ~20-30 seconds..."
echo ""

# Start backend in background
mvn spring-boot:run > backend.log 2>&1 &
BACKEND_PID=$!
echo $BACKEND_PID > backend.pid

print_waiting "Waiting for backend to start..."

# Wait for backend to be ready
MAX_WAIT=60
WAITED=0
while [ $WAITED -lt $MAX_WAIT ]; do
    if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo ""
        print_success "Backend API is ready!"
        break
    fi
    echo -ne "${YELLOW}⏳ Waiting... ($WAITED/$MAX_WAIT seconds)${NC}\r"
    sleep 2
    WAITED=$((WAITED + 2))
done
echo ""

if [ $WAITED -ge $MAX_WAIT ]; then
    print_error "Backend took too long to start. Check backend.log for errors."
    echo ""
    print_info "You can check logs with: tail -f backend.log"
    exit 1
fi

# Test backend
echo ""
print_step "Testing Backend API..."
HEALTH_STATUS=$(curl -s http://localhost:8080/actuator/health | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
if [ "$HEALTH_STATUS" = "UP" ]; then
    print_success "Backend health check: UP ✓"
else
    print_error "Backend health check failed!"
    exit 1
fi

# Setup frontend
echo ""
print_step "Setting up Frontend..."
cd frontend

if [ ! -d "node_modules" ]; then
    print_info "Installing frontend dependencies (~2 minutes)..."
    npm install --silent
    print_success "Frontend dependencies installed!"
else
    print_success "Frontend dependencies already installed!"
fi

echo ""
print_step "Starting Frontend Dashboard..."
echo ""
print_info "Frontend will start on: http://localhost:3000"
echo ""

# Start frontend in background
npm run dev > ../frontend.log 2>&1 &
FRONTEND_PID=$!
echo $FRONTEND_PID > ../frontend.pid

print_waiting "Waiting for frontend to start..."
sleep 5

# Check if frontend is running
if ps -p $FRONTEND_PID > /dev/null; then
    print_success "Frontend is starting!"
else
    print_error "Frontend failed to start. Check frontend.log"
    exit 1
fi

cd ..

echo ""
echo ""
echo -e "${GREEN}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║                                                            ║${NC}"
echo -e "${GREEN}║      🎉 KADALI PLATFORM IS RUNNING WITH PODMAN! 🎉        ║${NC}"
echo -e "${GREEN}║                                                            ║${NC}"
echo -e "${GREEN}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo ""
echo -e "${CYAN}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${CYAN}║                    🌐 Access Points 🌐                     ║${NC}"
echo -e "${CYAN}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "  ${MAGENTA}🎨 Dashboard:${NC}      http://localhost:3000"
echo -e "                     ${GREEN}← OPEN THIS IN YOUR BROWSER!${NC}"
echo ""
echo -e "  ${MAGENTA}🔧 Backend API:${NC}    http://localhost:8080"
echo ""
echo -e "  ${MAGENTA}❤️  Health Check:${NC}  http://localhost:8080/actuator/health"
echo ""
echo -e "  ${MAGENTA}💾 MinIO Console:${NC}  http://localhost:9001"
echo -e "                     User: minioadmin | Pass: minioadmin"
echo ""
echo ""
echo -e "${CYAN}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${CYAN}║                   📋 Quick Start Guide 📋                  ║${NC}"
echo -e "${CYAN}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "  ${YELLOW}1.${NC} Open ${CYAN}http://localhost:3000${NC} in your browser"
echo ""
echo -e "  ${YELLOW}2.${NC} Go to ${CYAN}Settings${NC} → Set Tenant ID: ${GREEN}my-startup${NC} → Save"
echo ""
echo -e "  ${YELLOW}3.${NC} Go to ${CYAN}Clusters${NC} → Click ${GREEN}Create Cluster${NC}"
echo "     • Name: test-cluster"
echo "     • Type: Interactive"
echo "     • Keep default settings"
echo "     • Click Create!"
echo ""
echo -e "  ${YELLOW}4.${NC} Go to ${CYAN}Data Explorer${NC} → Try this SQL:"
echo "     ${GREEN}SELECT 1 as test_value, 'Hello from Podman!' as message${NC}"
echo ""
echo -e "  ${YELLOW}5.${NC} Explore all features! 🚀"
echo ""
echo ""
echo -e "${CYAN}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${CYAN}║                   🧪 Test the APIs 🧪                      ║${NC}"
echo -e "${CYAN}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "  Run automated tests:"
echo -e "  ${GREEN}./test-api.sh${NC}"
echo ""
echo ""
echo -e "${CYAN}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${CYAN}║                  🛑 To Stop Everything 🛑                  ║${NC}"
echo -e "${CYAN}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "  Run this command:"
echo -e "  ${RED}./stop-kadali-podman.sh${NC}"
echo ""
echo -e "  Or manually:"
echo -e "  ${YELLOW}kill \$(cat backend.pid)${NC}"
echo -e "  ${YELLOW}kill \$(cat frontend.pid)${NC}"
echo -e "  ${YELLOW}podman-compose down${NC}"
echo ""
echo ""
echo -e "${MAGENTA}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${MAGENTA}║                    📊 Process Info 📊                      ║${NC}"
echo -e "${MAGENTA}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "  Backend PID:  ${CYAN}$BACKEND_PID${NC}"
echo -e "  Frontend PID: ${CYAN}$FRONTEND_PID${NC}"
echo ""
echo -e "  Logs:"
echo -e "  • Backend:  ${CYAN}tail -f backend.log${NC}"
echo -e "  • Frontend: ${CYAN}tail -f frontend.log${NC}"
echo ""
echo -e "  Podman containers:"
echo -e "  • Status:   ${CYAN}podman ps${NC}"
echo -e "  • Logs:     ${CYAN}podman-compose logs${NC}"
echo ""
echo ""
echo -e "${GREEN}🦭 Running with Podman - Docker-free! 🦭${NC}"
echo ""
echo -e "${YELLOW}👉 Now open: ${CYAN}http://localhost:3000${NC} 👈"
echo ""
