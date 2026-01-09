#!/bin/bash
# 🚀 KADALI QUICKSTART - PODMAN VERSION
# One-command setup and run for Podman

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

echo -e "${MAGENTA}"
echo "╔══════════════════════════════════════════════════════════════╗"
echo "║                                                              ║"
echo "║       🚀 KADALI DATA PLATFORM - PODMAN QUICKSTART 🚀        ║"
echo "║                                                              ║"
echo "║           Docker-Free Setup for Corporate Environments       ║"
echo "║                                                              ║"
echo "╚══════════════════════════════════════════════════════════════╝"
echo -e "${NC}"
echo ""

print_header() {
    echo -e "${CYAN}══════════════════════════════════════════════════════════════${NC}"
    echo -e "${CYAN}$1${NC}"
    echo -e "${CYAN}══════════════════════════════════════════════════════════════${NC}"
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

print_warning() {
    echo -e "${YELLOW}[!]${NC} $1"
}

# Track what needs to be installed
NEEDS_PODMAN=false
NEEDS_PODMAN_COMPOSE=false
NEEDS_JAVA=false
NEEDS_MAVEN=false
NEEDS_NODE=false

print_header "STEP 1: Checking Prerequisites"
echo ""

# Check Homebrew
if ! command -v brew &> /dev/null; then
    print_error "Homebrew not found"
    echo ""
    echo -e "${YELLOW}Homebrew is required to install dependencies.${NC}"
    echo -e "Install it from: ${CYAN}https://brew.sh${NC}"
    echo ""
    echo "Run this command:"
    echo '/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"'
    echo ""
    exit 1
fi
print_success "Homebrew found: $(brew --version | head -n1)"

# Check Java
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n1 | cut -d'"' -f2)
    print_success "Java found: $JAVA_VERSION"
else
    print_error "Java not found"
    NEEDS_JAVA=true
fi

# Check Maven
if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn -version | head -n1 | awk '{print $3}')
    print_success "Maven found: $MVN_VERSION"
else
    print_error "Maven not found"
    NEEDS_MAVEN=true
fi

# Check Node.js
if command -v node &> /dev/null; then
    NODE_VERSION=$(node --version)
    print_success "Node.js found: $NODE_VERSION"
else
    print_error "Node.js not found"
    NEEDS_NODE=true
fi

# Check Podman
if command -v podman &> /dev/null; then
    PODMAN_VERSION=$(podman --version)
    print_success "Podman found: $PODMAN_VERSION"
    
    # Check Podman machine
    if podman machine list 2>/dev/null | grep -q "Currently running"; then
        print_success "Podman machine is running"
    else
        if podman machine list 2>/dev/null | grep -q "podman"; then
            print_warning "Podman machine exists but is not running"
            print_info "Will start Podman machine..."
        else
            print_warning "No Podman machine found - will create one"
        fi
    fi
else
    print_error "Podman not found"
    NEEDS_PODMAN=true
fi

# Check podman-compose
if command -v podman-compose &> /dev/null; then
    PODMAN_COMPOSE_VERSION=$(podman-compose --version 2>&1 || echo "unknown")
    print_success "podman-compose found: $PODMAN_COMPOSE_VERSION"
else
    print_error "podman-compose not found"
    NEEDS_PODMAN_COMPOSE=true
fi

echo ""

# If anything is missing, offer to install
if [ "$NEEDS_PODMAN" = true ] || [ "$NEEDS_PODMAN_COMPOSE" = true ] || [ "$NEEDS_JAVA" = true ] || [ "$NEEDS_MAVEN" = true ] || [ "$NEEDS_NODE" = true ]; then
    print_header "MISSING DEPENDENCIES"
    echo ""
    echo -e "${YELLOW}Some required tools are missing. Here's what needs to be installed:${NC}"
    echo ""
    
    if [ "$NEEDS_JAVA" = true ]; then
        echo -e "  ${RED}✗${NC} Java 17+"
    fi
    if [ "$NEEDS_MAVEN" = true ]; then
        echo -e "  ${RED}✗${NC} Maven 3.6+"
    fi
    if [ "$NEEDS_NODE" = true ]; then
        echo -e "  ${RED}✗${NC} Node.js 18+"
    fi
    if [ "$NEEDS_PODMAN" = true ]; then
        echo -e "  ${RED}✗${NC} Podman"
    fi
    if [ "$NEEDS_PODMAN_COMPOSE" = true ]; then
        echo -e "  ${RED}✗${NC} podman-compose"
    fi
    
    echo ""
    echo -e "${CYAN}Would you like to install missing dependencies now? (y/n)${NC}"
    read -r response
    
    if [[ "$response" =~ ^[Yy]$ ]]; then
        echo ""
        print_header "INSTALLING DEPENDENCIES"
        echo ""
        
        if [ "$NEEDS_JAVA" = true ]; then
            print_info "Installing Java..."
            brew install openjdk@17
            print_success "Java installed!"
        fi
        
        if [ "$NEEDS_MAVEN" = true ]; then
            print_info "Installing Maven..."
            brew install maven
            print_success "Maven installed!"
        fi
        
        if [ "$NEEDS_NODE" = true ]; then
            print_info "Installing Node.js..."
            brew install node
            print_success "Node.js installed!"
        fi
        
        if [ "$NEEDS_PODMAN" = true ]; then
            print_info "Installing Podman..."
            brew install podman
            print_success "Podman installed!"
        fi
        
        if [ "$NEEDS_PODMAN_COMPOSE" = true ]; then
            print_info "Installing podman-compose..."
            brew install podman-compose
            print_success "podman-compose installed!"
        fi
        
        echo ""
        print_success "All dependencies installed!"
    else
        echo ""
        echo -e "${RED}Cannot proceed without required dependencies.${NC}"
        echo ""
        echo "To install manually:"
        echo "  brew install openjdk@17 maven node podman podman-compose"
        echo ""
        exit 1
    fi
fi

# Setup Podman machine if needed
echo ""
print_header "STEP 2: Setting Up Podman Machine"
echo ""

if ! command -v podman &> /dev/null; then
    print_error "Podman not available"
    exit 1
fi

if ! podman machine list 2>/dev/null | grep -q "podman"; then
    print_info "Creating Podman machine..."
    echo ""
    print_info "Configuration: 4 CPUs, 8GB RAM, 50GB Disk"
    echo ""
    podman machine init --cpus 4 --memory 8192 --disk-size 50
    print_success "Podman machine created!"
    echo ""
fi

if ! podman machine list 2>/dev/null | grep -q "Currently running"; then
    print_info "Starting Podman machine..."
    podman machine start
    echo ""
    print_success "Podman machine started!"
else
    print_success "Podman machine already running!"
fi

# Verify Podman connection
if podman ps >/dev/null 2>&1; then
    print_success "Podman connection verified!"
else
    print_error "Cannot connect to Podman"
    exit 1
fi

# Make scripts executable
echo ""
print_header "STEP 3: Preparing Scripts"
echo ""

chmod +x run-kadali-podman.sh 2>/dev/null || true
chmod +x stop-kadali-podman.sh 2>/dev/null || true
chmod +x test-api.sh 2>/dev/null || true

print_success "Scripts are ready!"

# Final summary
echo ""
echo ""
echo -e "${GREEN}╔══════════════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║                                                              ║${NC}"
echo -e "${GREEN}║              ✓ ALL PREREQUISITES MET! ✓                      ║${NC}"
echo -e "${GREEN}║                                                              ║${NC}"
echo -e "${GREEN}╚══════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo ""
echo -e "${CYAN}╔══════════════════════════════════════════════════════════════╗${NC}"
echo -e "${CYAN}║                    🚀 READY TO LAUNCH 🚀                     ║${NC}"
echo -e "${CYAN}╚══════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${YELLOW}Would you like to start Kadali Platform now? (y/n)${NC}"
read -r response

if [[ "$response" =~ ^[Yy]$ ]]; then
    echo ""
    print_info "Launching Kadali Platform with Podman..."
    echo ""
    sleep 2
    ./run-kadali-podman.sh
else
    echo ""
    echo -e "${CYAN}╔══════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${CYAN}║                   📝 NEXT STEPS 📝                           ║${NC}"
    echo -e "${CYAN}╚══════════════════════════════════════════════════════════════╝${NC}"
    echo ""
    echo -e "  ${YELLOW}To start the platform:${NC}"
    echo -e "    ${GREEN}./run-kadali-podman.sh${NC}"
    echo ""
    echo -e "  ${YELLOW}To stop the platform:${NC}"
    echo -e "    ${RED}./stop-kadali-podman.sh${NC}"
    echo ""
    echo -e "  ${YELLOW}For help:${NC}"
    echo -e "    ${CYAN}cat PODMAN_SETUP.md${NC}"
    echo ""
    echo -e "  ${YELLOW}Podman commands:${NC}"
    echo -e "    ${CYAN}podman ps${NC}              # List containers"
    echo -e "    ${CYAN}podman machine list${NC}    # Check machine status"
    echo -e "    ${CYAN}podman-compose logs${NC}    # View logs"
    echo ""
fi
