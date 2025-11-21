@goto :WINDOWS 2>nul

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
print_success "Setup complete! Building projects..."
echo ""

# Check Java installation and version - INSTALL AUTOMATICALLY IF MISSING
print_info "Checking Java installation..."

# Function to install SDKMAN and Java 21
install_sdkman_and_java() {
    print_warning "Java 21 not found - installing SDKMAN and Java 21 automatically..."

    # Install SDKMAN
    if [ ! -d "$HOME/.sdkman" ]; then
        print_info "Installing SDKMAN..."
        export SDKMAN_DIR="$HOME/.sdkman"
        curl -s "https://get.sdkman.io" | bash

        if [ ! -f "$HOME/.sdkman/bin/sdkman-init.sh" ]; then
            print_error "SDKMAN installation failed"
            exit 1
        fi
        print_success "SDKMAN installed"
    else
        print_info "SDKMAN already installed"
    fi

    # Source SDKMAN
    source "$HOME/.sdkman/bin/sdkman-init.sh"

    # Install Java 21 if not present
    if [ ! -d "$HOME/.sdkman/candidates/java/21.0.8-amzn" ]; then
        print_info "Installing Java 21 (this may take a few minutes)..."
        sdk install java 21.0.8-amzn < /dev/null
        sdk default java 21.0.8-amzn
        print_success "Java 21 installed and set as default"
    else
        print_info "Java 21 already installed"
        sdk default java 21.0.8-amzn
    fi

    export JAVA_HOME="$HOME/.sdkman/candidates/java/21.0.8-amzn"
    export PATH="$JAVA_HOME/bin:$PATH"
}

# Check for Java 21 - MUST BE EXACTLY VERSION 21
JAVA_CMD=""
JAVA_VERSION=""
NEEDS_INSTALL=false

if [ -f "$HOME/.sdkman/candidates/java/21.0.8-amzn/bin/java" ]; then
    # SDKMAN Java 21 exists - USE THIS
    source "$HOME/.sdkman/bin/sdkman-init.sh" 2>/dev/null || true
    # Force use of Java 21 (in case SDKMAN default is different)
    export JAVA_HOME="$HOME/.sdkman/candidates/java/21.0.8-amzn"
    export PATH="$JAVA_HOME/bin:$PATH"
    JAVA_CMD="$JAVA_HOME/bin/java"
    print_success "Found SDKMAN Java 21"
elif [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
    # JAVA_HOME is set - check if it's Java 21
    JAVA_CMD="$JAVA_HOME/bin/java"
    JAVA_VERSION=$($JAVA_CMD -version 2>&1 | head -n 1 | awk -F '"' '{print $2}' | awk -F '.' '{print $1}')
    if [ -z "$JAVA_VERSION" ]; then
        JAVA_VERSION=$($JAVA_CMD -version 2>&1 | head -n 1 | grep -oE '[0-9]+' | head -n 1)
    fi

    if [ "$JAVA_VERSION" != "21" ]; then
        print_warning "Java $JAVA_VERSION found, but Java 21 is REQUIRED - installing via SDKMAN"
        NEEDS_INSTALL=true
    else
        print_success "Found Java 21"
    fi
elif command -v java &> /dev/null; then
    # System Java exists - check if it's Java 21
    JAVA_CMD="java"
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | awk -F '"' '{print $2}' | awk -F '.' '{print $1}')
    if [ -z "$JAVA_VERSION" ]; then
        JAVA_VERSION=$(java -version 2>&1 | head -n 1 | grep -oE '[0-9]+' | head -n 1)
    fi

    if [ "$JAVA_VERSION" != "21" ]; then
        print_warning "Java $JAVA_VERSION found, but Java 21 is REQUIRED - installing via SDKMAN"
        NEEDS_INSTALL=true
    else
        print_success "Found Java 21"
    fi
else
    # No Java at all
    print_warning "Java not found - installing Java 21 via SDKMAN"
    NEEDS_INSTALL=true
fi

# Install Java 21 via SDKMAN if needed
if [ "$NEEDS_INSTALL" = true ]; then
    install_sdkman_and_java
    JAVA_CMD="$JAVA_HOME/bin/java"
fi

# Final verification
if ! $JAVA_CMD -version &> /dev/null; then
    print_error "Java installation verification failed"
    exit 1
fi

print_success "Java ready for builds"

# Ensure JAVA_HOME is set for Maven builds
if [ -z "$JAVA_HOME" ]; then
    if [ -f "$HOME/.sdkman/candidates/java/21.0.8-amzn/bin/java" ]; then
        export JAVA_HOME="$HOME/.sdkman/candidates/java/21.0.8-amzn"
    fi
fi

print_success "Java ready for builds"
echo ""
print_info "Starting build and Docker services (Task will handle builds)..."
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

exit 0

# ============================================================================
# WINDOWS SECTION (Batch)
# ============================================================================
:WINDOWS
@echo off
setlocal EnableDelayedExpansion

echo ===============================================================
echo   Dynamic Portfolio API - Setup
echo ===============================================================
echo.

REM Check if Docker is available (REQUIRED)
where docker >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Docker is not installed or not in PATH
    echo.
    echo Please install Docker Desktop from:
    echo   https://www.docker.com/products/docker-desktop
    echo.
    pause
    exit /b 1
)

echo [OK] Docker found
docker --version
echo.

REM Check if Task is installed
set TASK_CMD=
where task >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] Task is already installed
    task --version
    set TASK_CMD=task
    goto :check_build_method
)

echo [INFO] Installing Task...

REM Try Chocolatey first
where choco >nul 2>&1
if %errorlevel% equ 0 (
    echo [INFO] Installing via Chocolatey...
    choco install go-task -y --no-progress
    if %errorlevel% equ 0 (
        echo [OK] Task installed via Chocolatey
        REM Refresh PATH
        call refreshenv >nul 2>&1
        where task >nul 2>&1
        if %errorlevel% equ 0 (
            set TASK_CMD=task
            goto :check_build_method
        )
    )
)

REM Try direct download
echo [INFO] Downloading Task directly...
if not exist "%USERPROFILE%\bin" mkdir "%USERPROFILE%\bin"
powershell -NoProfile -Command "try { Invoke-WebRequest -Uri 'https://github.com/go-task/task/releases/latest/download/task_windows_amd64.zip' -OutFile '%TEMP%\task.zip' -UseBasicParsing; Expand-Archive -Path '%TEMP%\task.zip' -DestinationPath '%USERPROFILE%\bin' -Force; Remove-Item '%TEMP%\task.zip' } catch { exit 1 }"
if %errorlevel% equ 0 (
    echo [OK] Task installed to %USERPROFILE%\bin
    set TASK_CMD=%USERPROFILE%\bin\task.exe
    goto :check_build_method
)

echo.
echo [ERROR] Task installation failed
echo.
echo Please install Task manually from:
echo   https://taskfile.dev/installation/
echo.
echo Or use Chocolatey: choco install go-task
echo.
pause
exit /b 1

:check_build_method
echo.
echo [INFO] Determining build method...

REM Check for Git Bash (enables local Maven builds)
set GIT_BASH_PATH=
if exist "C:\Program Files\Git\bin\bash.exe" set GIT_BASH_PATH=C:\Program Files\Git\bin\bash.exe
if exist "C:\Program Files (x86)\Git\bin\bash.exe" set GIT_BASH_PATH=C:\Program Files (x86)\Git\bin\bash.exe
if exist "%PROGRAMFILES%\Git\bin\bash.exe" set GIT_BASH_PATH=%PROGRAMFILES%\Git\bin\bash.exe

if not "%GIT_BASH_PATH%"=="" (
    REM Check if Java exists (for local Maven build)
    where java >nul 2>&1
    if %errorlevel% equ 0 (
        echo [OK] Git Bash + Java found - using fast local Maven builds
        echo.
        echo [INFO] Starting Docker services via Task...
        echo.
        "%GIT_BASH_PATH%" -c "task run"
        goto :done
    ) else (
        echo [INFO] Git Bash found but Java missing - using Docker multi-stage build
    )
) else (
    echo [INFO] Git Bash not found - using Docker multi-stage build
)

REM Fallback: Use Docker multi-stage build (compiles inside Docker)
echo [INFO] Building inside Docker containers ^(slower but no local tools needed^)
echo.
echo [INFO] Starting Docker services...
echo.

REM Call docker compose directly for multi-stage build
docker compose build --pull
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Docker build failed! Check output above.
    echo.
    pause
    exit /b 1
)

docker compose up -d --wait
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Container startup failed!
    echo.
    pause
    exit /b 1
)

:done
echo.
echo [OK] Services started successfully!
echo.
echo API available at: http://localhost:8080/api/v1
echo API docs at: http://localhost:8080/api/v1/swagger-ui.html
echo.
pause
