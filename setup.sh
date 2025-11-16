#!/bin/bash

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Detect OS
OS="$(uname -s)"
ARCH="$(uname -m)"

echo -e "${BLUE}═══════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}  Dynamic Portfolio API - Automated Setup${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════${NC}"
echo ""

# Function to print colored messages
print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# Check if Task is already installed
if command -v task &> /dev/null; then
    print_success "Task is already installed: $(task --version)"
else
    print_info "Installing Task (cross-platform task runner)..."

    case "$OS" in
        Linux*)
            # Linux installation
            print_info "Detected Linux system"
            if command -v snap &> /dev/null; then
                print_info "Installing via snap..."
                sudo snap install task --classic
            elif command -v apt-get &> /dev/null; then
                print_info "Installing via apt..."
                sudo sh -c "$(curl --location https://taskfile.dev/install.sh)" -- -d -b /usr/local/bin
            else
                print_info "Installing to ~/bin..."
                sh -c "$(curl --location https://taskfile.dev/install.sh)" -- -d -b ~/bin
                export PATH="$HOME/bin:$PATH"
                print_warning "Add ~/bin to your PATH: echo 'export PATH=\"\$HOME/bin:\$PATH\"' >> ~/.bashrc"
            fi
            ;;
        Darwin*)
            # macOS installation
            print_info "Detected macOS system"
            if command -v brew &> /dev/null; then
                print_info "Installing via Homebrew..."
                brew install go-task/tap/go-task
            else
                print_warning "Homebrew not found. Installing to ~/bin..."
                sh -c "$(curl --location https://taskfile.dev/install.sh)" -- -d -b ~/bin
                export PATH="$HOME/bin:$PATH"
                print_warning "Add ~/bin to your PATH: echo 'export PATH=\"\$HOME/bin:\$PATH\"' >> ~/.zshrc"
            fi
            ;;
        MINGW*|MSYS*|CYGWIN*)
            # Windows (Git Bash, MSYS2, Cygwin)
            print_info "Detected Windows system"
            print_info "Installing to ~/bin..."
            sh -c "$(curl --location https://taskfile.dev/install.sh)" -- -d -b ~/bin
            export PATH="$HOME/bin:$PATH"
            print_warning "Add ~/bin to your PATH permanently in your shell configuration"
            ;;
        *)
            print_error "Unsupported operating system: $OS"
            print_info "Please install Task manually from: https://taskfile.dev/installation/"
            exit 1
            ;;
    esac

    # Verify installation
    if command -v task &> /dev/null; then
        print_success "Task installed successfully: $(task --version)"
    else
        print_error "Task installation failed"
        print_info "Please install manually from: https://taskfile.dev/installation/"
        exit 1
    fi
fi

echo ""
print_success "Setup script completed!"
echo ""
print_info "Next steps:"
echo "  1. Run: ${GREEN}task setup${NC} - Complete project setup"
echo "  2. Or run: ${GREEN}task help${NC} - See all available commands"
echo ""
print_info "Quick start:"
echo "  ${GREEN}task setup${NC}         - Install dependencies and start services"
echo "  ${GREEN}task docker-up${NC}     - Start all Docker containers"
echo "  ${GREEN}task logs${NC}          - View container logs"
echo "  ${GREEN}task status${NC}        - Check container status"
echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════${NC}"
