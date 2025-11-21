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
    export JAVA_HOME="$HOME/.sdkman/candidates/java/21.0.8-amzn"
    JAVA_CMD="$JAVA_HOME/bin/java"
    source "$HOME/.sdkman/bin/sdkman-init.sh" 2>/dev/null || true
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

# Build both Maven projects before Docker
print_info "Building main API..."
if [ -f "./mvnw" ]; then
    ./mvnw clean package -DskipTests -q || {
        print_error "Main API build failed"
        exit 1
    }
    print_success "Main API build complete"
else
    print_error "Maven wrapper (mvnw) not found"
    exit 1
fi

print_info "Building OFB mock server..."
if [ -f "ofb-mock-server/pom.xml" ]; then
    (cd ofb-mock-server && JAVA_HOME=${JAVA_HOME:-$HOME/.sdkman/candidates/java/21.0.8-amzn} ../mvnw clean package -DskipTests -q) || {
        print_error "OFB mock server build failed"
        exit 1
    }
    print_success "OFB mock server build complete"
else
    print_warning "OFB mock server not found, skipping..."
fi

echo ""
print_success "All builds complete! Starting Docker services..."
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

REM Try Chocolatey first
where choco >nul 2>&1
if %errorlevel% equ 0 (
    echo [INFO] Installing via Chocolatey...
    choco install go-task -y >nul 2>&1
    if %errorlevel% equ 0 (
        echo [OK] Task installed via Chocolatey
        goto :verify_task
    )
)

REM Try direct download
echo [INFO] Downloading Task directly...
if not exist "%USERPROFILE%\bin" mkdir "%USERPROFILE%\bin"
powershell -Command "Invoke-WebRequest -Uri 'https://github.com/go-task/task/releases/latest/download/task_windows_amd64.zip' -OutFile '%TEMP%\task.zip'; Expand-Archive -Path '%TEMP%\task.zip' -DestinationPath '%USERPROFILE%\bin' -Force; Remove-Item '%TEMP%\task.zip'" >nul 2>&1
if %errorlevel% equ 0 (
    set "PATH=%USERPROFILE%\bin;%PATH%"
    echo [OK] Task installed to %USERPROFILE%\bin
    goto :verify_task
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

:verify_task
where task >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Task installed but not found in PATH
    echo [INFO] Please restart your terminal or add %USERPROFILE%\bin to PATH
    pause
    exit /b 1
)

:complete
echo.
echo [OK] Setup complete! Building projects...
echo.

REM Check Java installation and version - INSTALL AUTOMATICALLY IF MISSING
echo [INFO] Checking Java installation...

set NEEDS_JAVA_INSTALL=0

REM Check if Java exists
where java >nul 2>&1
if %errorlevel% neq 0 (
    set NEEDS_JAVA_INSTALL=1
    goto :install_java
)

REM Check Java version - MUST BE EXACTLY VERSION 21
for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVA_VERSION_STRING=%%g
)
set JAVA_VERSION_STRING=%JAVA_VERSION_STRING:"=%
for /f "tokens=1 delims=." %%a in ("%JAVA_VERSION_STRING%") do set JAVA_MAJOR=%%a

if not "%JAVA_MAJOR%"=="21" (
    echo [WARNING] Java %JAVA_MAJOR% detected, but Java 21 is REQUIRED - installing via Chocolatey
    set NEEDS_JAVA_INSTALL=1
    goto :install_java
)

echo [OK] Java 21 detected
goto :java_ready

:install_java
echo [WARNING] Java 21 not found - installing automatically...

REM Try SDKMAN first (if Git Bash is available)
echo [INFO] Checking for Git Bash to use SDKMAN...
set GIT_BASH_PATH=
if exist "C:\Program Files\Git\bin\bash.exe" set GIT_BASH_PATH=C:\Program Files\Git\bin\bash.exe
if exist "C:\Program Files (x86)\Git\bin\bash.exe" set GIT_BASH_PATH=C:\Program Files (x86)\Git\bin\bash.exe
if exist "%PROGRAMFILES%\Git\bin\bash.exe" set GIT_BASH_PATH=%PROGRAMFILES%\Git\bin\bash.exe

if not "%GIT_BASH_PATH%"=="" (
    echo [INFO] Git Bash found - attempting SDKMAN installation...
    "%GIT_BASH_PATH%" -c "curl -s 'https://get.sdkman.io' | bash && source ~/.sdkman/bin/sdkman-init.sh && sdk install java 21.0.8-amzn && sdk default java 21.0.8-amzn"

    REM Check if SDKMAN Java 21 is now available
    if exist "%USERPROFILE%\.sdkman\candidates\java\21.0.8-amzn\bin\java.exe" (
        echo [OK] Java 21 installed via SDKMAN
        set "JAVA_HOME=%USERPROFILE%\.sdkman\candidates\java\21.0.8-amzn"
        set "PATH=%USERPROFILE%\.sdkman\candidates\java\21.0.8-amzn\bin;%PATH%"
        goto :java_ready
    ) else (
        echo [WARNING] SDKMAN installation failed or incomplete - falling back to Chocolatey...
    )
)

REM Fallback to Chocolatey
echo [INFO] Installing via Chocolatey...
REM Check if Chocolatey is installed
set CHOCO_PATH=%ProgramData%\chocolatey\bin\choco.exe
if not exist "%CHOCO_PATH%" (
    echo [INFO] Installing Chocolatey first...
    powershell -NoProfile -ExecutionPolicy Bypass -Command "Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))"

    if %errorlevel% neq 0 (
        echo [ERROR] Chocolatey installation failed
        echo.
        echo Please install Java 21 manually from: https://adoptium.net/
        echo.
        pause
        exit /b 1
    )

    echo [OK] Chocolatey installed

    REM Update PATH for current session
    set "PATH=%ProgramData%\chocolatey\bin;%PATH%"
    set CHOCO_PATH=%ProgramData%\chocolatey\bin\choco.exe
)

REM Install Java 21 via Chocolatey
echo [INFO] Installing Java 21 ^(this may take a few minutes^)...
"%CHOCO_PATH%" install temurin21 -y --no-progress --force

if %errorlevel% neq 0 (
    echo [ERROR] Java installation failed
    echo.
    echo Please install Java 21 manually from: https://adoptium.net/
    echo.
    pause
    exit /b 1
)

echo [OK] Java 21 installed successfully

REM Update PATH to include Java (find the installed version)
for /d %%i in ("C:\Program Files\Eclipse Adoptium\jdk-21*") do (
    set "JAVA_HOME=%%i"
    set "PATH=%%i\bin;%PATH%"
)

REM Verify installation
where java >nul 2>&1
if %errorlevel% neq 0 (
    echo [WARNING] Java installed but not in PATH for this session
    echo [INFO] Please open a NEW terminal window and run this script again
    echo.
    pause
    exit /b 1
)

echo [OK] Java ready for builds

:java_ready

REM Build main API
echo [INFO] Building main API...
if exist "mvnw.cmd" (
    call mvnw.cmd clean package -DskipTests -q
    if %errorlevel% neq 0 (
        echo [ERROR] Main API build failed
        pause
        exit /b 1
    )
    echo [OK] Main API build complete
) else (
    echo [ERROR] Maven wrapper ^(mvnw.cmd^) not found
    pause
    exit /b 1
)

REM Build OFB mock server
echo [INFO] Building OFB mock server...
if exist "ofb-mock-server\pom.xml" (
    cd ofb-mock-server
    call ..\mvnw.cmd clean package -DskipTests -q
    if %errorlevel% neq 0 (
        cd ..
        echo [ERROR] OFB mock server build failed
        pause
        exit /b 1
    )
    cd ..
    echo [OK] OFB mock server build complete
) else (
    echo [WARNING] OFB mock server not found, skipping...
)

echo.
echo [OK] All builds complete! Starting Docker services...
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

pause
