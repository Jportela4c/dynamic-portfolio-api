#!/bin/bash
# 2>/dev/null || goto :windows

# ============================================================================
# UNIX/Linux/macOS SECTION (Bash)
# ============================================================================

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_success() { echo -e "${GREEN}✅ $1${NC}"; }
print_error() { echo -e "${RED}❌ $1${NC}"; }
print_warning() { echo -e "${YELLOW}⚠️  $1${NC}"; }
print_info() { echo -e "${BLUE}ℹ️  $1${NC}"; }

echo -e "${BLUE}═══════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}  Dynamic Portfolio API - Setup${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════${NC}"
echo ""

# Check if Task is installed
if command -v task &> /dev/null; then
    print_success "Task is already installed: $(task --version)"
else
    print_info "Installing Task..."

    OS="$(uname -s)"
    case "$OS" in
        Linux*)
            if command -v snap &> /dev/null; then
                sudo snap install task --classic
            else
                sh -c "$(curl --location https://taskfile.dev/install.sh)" -- -d -b /usr/local/bin 2>/dev/null || \
                sh -c "$(curl --location https://taskfile.dev/install.sh)" -- -d -b ~/bin
                export PATH="$HOME/bin:$PATH"
            fi
            ;;
        Darwin*)
            if command -v brew &> /dev/null; then
                brew install go-task/tap/go-task
            else
                sh -c "$(curl --location https://taskfile.dev/install.sh)" -- -d -b ~/bin
                export PATH="$HOME/bin:$PATH"
            fi
            ;;
        MINGW*|MSYS*|CYGWIN*)
            sh -c "$(curl --location https://taskfile.dev/install.sh)" -- -d -b ~/bin
            export PATH="$HOME/bin:$PATH"
            ;;
    esac

    if command -v task &> /dev/null; then
        print_success "Task installed: $(task --version)"
    else
        print_error "Installation failed"
        echo "Install manually: https://taskfile.dev/installation/"
        exit 1
    fi
fi

echo ""
print_success "Setup complete! Starting services..."
echo ""

# Run task run to start everything
task run

exit 0

# ============================================================================
# WINDOWS SECTION (Batch)
# ============================================================================
:windows
@echo off
setlocal

echo ===============================================================
echo   Dynamic Portfolio API - Setup
echo ===============================================================
echo.

where task >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] Task is already installed
    task --version
    goto :complete
)

echo [INFO] Installing Task...

where choco >nul 2>&1
if %errorlevel% equ 0 (
    echo [INFO] Installing via Chocolatey...
    choco install go-task -y
    if %errorlevel% equ 0 (
        echo [OK] Task installed
        goto :complete
    )
)

echo.
echo [WARNING] Automatic installation failed
echo.
echo Please install Task manually:
echo.
echo Option 1 - Use Docker directly (no Task needed):
echo   docker compose up -d
echo.
echo Option 2 - Install Chocolatey, then run:
echo   choco install go-task
echo.
echo Option 3 - Download from:
echo   https://github.com/go-task/task/releases
echo.
pause
exit /b 1

:complete
echo.
echo [OK] Setup complete! Starting services...
echo.

REM Run task run to start everything
task run

pause
