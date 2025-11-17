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

# Check if port 8080 is in use and find alternative if needed
PORT=8080
if command -v lsof &> /dev/null; then
    while lsof -i :$PORT &> /dev/null; do
        print_warning "Port $PORT is already in use"
        PORT=$((PORT + 1))
        print_info "Trying port $PORT instead..."
    done
elif command -v netstat &> /dev/null; then
    while netstat -an | grep ":$PORT " | grep LISTEN &> /dev/null; do
        print_warning "Port $PORT is already in use"
        PORT=$((PORT + 1))
        print_info "Trying port $PORT instead..."
    done
fi

if [ $PORT -ne 8080 ]; then
    print_info "Using port $PORT instead of 8080"
    export SERVER_PORT=$PORT
fi

# Run task run to start everything
task run

if [ $PORT -ne 8080 ]; then
    echo ""
    print_info "API is running on http://localhost:$PORT"
    print_info "Swagger UI: http://localhost:$PORT/swagger-ui.html"
fi

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

REM Check if port 8080 is in use
set PORT=8080
:check_port
netstat -an | findstr ":%PORT% " | findstr LISTEN >nul 2>&1
if %errorlevel% equ 0 (
    echo [WARNING] Port %PORT% is already in use
    set /a PORT=%PORT%+1
    echo [INFO] Trying port %PORT% instead...
    goto :check_port
)

if not %PORT%==8080 (
    echo [INFO] Using port %PORT% instead of 8080
    set SERVER_PORT=%PORT%
)

REM Run task run to start everything
task run

if not %PORT%==8080 (
    echo.
    echo [INFO] API is running on http://localhost:%PORT%
    echo [INFO] Swagger UI: http://localhost:%PORT%/swagger-ui.html
)

pause
