#!/bin/bash
# 🛑 STOP KADALI - PODMAN VERSION
# This script stops all Kadali services running with Podman

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

echo ""
echo -e "${RED}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${RED}║                                                            ║${NC}"
echo -e "${RED}║           🛑 STOPPING KADALI PLATFORM (PODMAN) 🛑         ║${NC}"
echo -e "${RED}║                                                            ║${NC}"
echo -e "${RED}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

PROJECT_DIR="/Users/aswinkumar/Downloads/Aswin/Startups/backend-project/kadali"
cd "$PROJECT_DIR"

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

# Stop frontend
print_step "Stopping Frontend..."
if [ -f "frontend.pid" ]; then
    FRONTEND_PID=$(cat frontend.pid)
    if ps -p $FRONTEND_PID > /dev/null 2>&1; then
        kill $FRONTEND_PID 2>/dev/null || true
        sleep 2
        # Force kill if still running
        if ps -p $FRONTEND_PID > /dev/null 2>&1; then
            kill -9 $FRONTEND_PID 2>/dev/null || true
        fi
        print_success "Frontend stopped (PID: $FRONTEND_PID)"
    else
        print_info "Frontend was not running"
    fi
    rm -f frontend.pid
else
    print_info "No frontend PID file found"
fi

# Stop backend
print_step "Stopping Backend..."
if [ -f "backend.pid" ]; then
    BACKEND_PID=$(cat backend.pid)
    if ps -p $BACKEND_PID > /dev/null 2>&1; then
        kill $BACKEND_PID 2>/dev/null || true
        sleep 2
        # Force kill if still running
        if ps -p $BACKEND_PID > /dev/null 2>&1; then
            kill -9 $BACKEND_PID 2>/dev/null || true
        fi
        print_success "Backend stopped (PID: $BACKEND_PID)"
    else
        print_info "Backend was not running"
    fi
    rm -f backend.pid
else
    print_info "No backend PID file found"
fi

# Also kill any rogue Maven/Java processes
print_step "Cleaning up any remaining processes..."
pkill -f "spring-boot:run" 2>/dev/null || true
pkill -f "kadali-0.0.1-SNAPSHOT" 2>/dev/null || true
pkill -f "vite" 2>/dev/null || true
sleep 1
print_success "Process cleanup complete"

# Stop Podman Compose services
print_step "Stopping infrastructure services..."
if command -v podman-compose &> /dev/null; then
    if podman-compose ps 2>/dev/null | grep -q "Up"; then
        podman-compose down
        print_success "Infrastructure services stopped"
    else
        print_info "Infrastructure services were not running"
    fi
else
    print_error "podman-compose not found. Trying direct podman stop..."
    # Stop containers directly
    podman stop kadali-postgres kadali-minio kadali-metastore 2>/dev/null || true
    podman rm kadali-postgres kadali-minio kadali-metastore 2>/dev/null || true
    print_success "Containers stopped"
fi

# Clean up log files
print_step "Cleaning up log files..."
if [ -f "backend.log" ]; then
    rm -f backend.log
    print_success "Backend logs removed"
fi
if [ -f "frontend.log" ]; then
    rm -f frontend.log
    print_success "Frontend logs removed"
fi

echo ""
echo -e "${GREEN}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║                                                            ║${NC}"
echo -e "${GREEN}║            ✓ ALL SERVICES STOPPED SUCCESSFULLY ✓           ║${NC}"
echo -e "${GREEN}║                                                            ║${NC}"
echo -e "${GREEN}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${CYAN}To start again, run:${NC}"
echo -e "  ${GREEN}./run-kadali-podman.sh${NC}"
echo ""
echo -e "${CYAN}To check Podman containers:${NC}"
echo -e "  ${YELLOW}podman ps -a${NC}"
echo ""
echo -e "${CYAN}To completely clean up Podman resources:${NC}"
echo -e "  ${YELLOW}podman system prune -a --volumes -f${NC}"
echo ""
echo -e "${CYAN}To stop Podman machine:${NC}"
echo -e "  ${YELLOW}podman machine stop${NC}"
echo ""
