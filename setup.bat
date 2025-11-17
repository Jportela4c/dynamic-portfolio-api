@echo off
REM Dynamic Portfolio API - Automated Setup (Windows CMD)
REM This script installs Task and sets up the development environment

setlocal enabledelayedexpansion

echo ===============================================================
echo   Dynamic Portfolio API - Automated Setup
echo ===============================================================
echo.

REM Check if Task is already installed
where task >nul 2>&1
if %errorlevel% equ 0 (
    echo [32m[OK] Task is already installed[0m
    task --version
) else (
    echo [36m[INFO] Installing Task (cross-platform task runner)...[0m

    REM Check if Chocolatey is available
    where choco >nul 2>&1
    if %errorlevel% equ 0 (
        echo [36m[INFO] Installing via Chocolatey...[0m
        choco install go-task -y
        if %errorlevel% equ 0 (
            echo [32m[OK] Task installed via Chocolatey[0m
        ) else (
            echo [31m[ERROR] Chocolatey installation failed[0m
            goto :manual_install
        )
    ) else (
        echo [33m[WARNING] Chocolatey not found[0m
        goto :manual_install
    )
)

REM Verify installation
where task >nul 2>&1
if %errorlevel% equ 0 (
    echo.
    echo [32m[OK] Setup script completed![0m
    echo.
    echo [36m[INFO] Next steps:[0m
    echo   1. Run: [32mtask setup[0m - Complete project setup
    echo   2. Or run: [32mtask help[0m - See all available commands
    echo.
    echo [36m[INFO] Quick start:[0m
    echo   [32mtask setup[0m         - Install dependencies and start services
    echo   [32mtask docker-up[0m     - Start all Docker containers
    echo   [32mtask logs[0m          - View container logs
    echo   [32mtask status[0m        - Check container status
    echo.
    echo ===============================================================
    goto :end
) else (
    echo [31m[ERROR] Task installation failed[0m
    goto :manual_install
)

:manual_install
echo.
echo [33m[WARNING] Automatic installation not possible[0m
echo.
echo [36m[INFO] Please install Task manually using one of these methods:[0m
echo.
echo   Option 1 - Install Chocolatey (package manager):
echo     1. Open PowerShell as Administrator
echo     2. Run: Set-ExecutionPolicy Bypass -Scope Process -Force
echo     3. Run: [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072
echo     4. Run: iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
echo     5. Run: choco install go-task
echo.
echo   Option 2 - Download directly:
echo     1. Visit: https://github.com/go-task/task/releases
echo     2. Download task_windows_amd64.zip
echo     3. Extract and add to PATH
echo.
echo   Option 3 - Use Docker directly (no Task needed):
echo     Run: docker compose up -d
echo.
exit /b 1

:end
endlocal
