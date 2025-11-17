# Dynamic Portfolio API - Automated Setup (Windows PowerShell)
# This script installs Task and sets up the development environment

$ErrorActionPreference = "Stop"

# Colors for output
function Print-Success {
    param([string]$Message)
    Write-Host "✅ $Message" -ForegroundColor Green
}

function Print-Error {
    param([string]$Message)
    Write-Host "❌ $Message" -ForegroundColor Red
}

function Print-Warning {
    param([string]$Message)
    Write-Host "⚠️  $Message" -ForegroundColor Yellow
}

function Print-Info {
    param([string]$Message)
    Write-Host "ℹ️  $Message" -ForegroundColor Cyan
}

Write-Host "══════════════════════════════════════════════════════=" -ForegroundColor Cyan
Write-Host "  Dynamic Portfolio API - Automated Setup" -ForegroundColor Cyan
Write-Host "══════════════════════════════════════════════════════=" -ForegroundColor Cyan
Write-Host ""

# Check if Task is already installed
if (Get-Command task -ErrorAction SilentlyContinue) {
    $version = (task --version)
    Print-Success "Task is already installed: $version"
} else {
    Print-Info "Installing Task (cross-platform task runner)..."

    # Check if Chocolatey is available
    if (Get-Command choco -ErrorAction SilentlyContinue) {
        Print-Info "Installing via Chocolatey..."
        try {
            choco install go-task -y
            Print-Success "Task installed via Chocolatey"
        } catch {
            Print-Error "Chocolatey installation failed: $_"
            Print-Info "Trying alternative method..."
        }
    }

    # If still not installed, try Scoop
    if (-not (Get-Command task -ErrorAction SilentlyContinue)) {
        if (Get-Command scoop -ErrorAction SilentlyContinue) {
            Print-Info "Installing via Scoop..."
            try {
                scoop install task
                Print-Success "Task installed via Scoop"
            } catch {
                Print-Error "Scoop installation failed: $_"
            }
        } else {
            Print-Warning "Neither Chocolatey nor Scoop found."
            Print-Info "Installing Scoop package manager..."
            try {
                Set-ExecutionPolicy RemoteSigned -Scope CurrentUser -Force
                Invoke-RestMethod get.scoop.sh | Invoke-Expression
                scoop install task
                Print-Success "Task installed via Scoop"
            } catch {
                Print-Error "Automatic installation failed: $_"
                Print-Info "Please install Task manually:"
                Print-Info "  Option 1: Install Chocolatey and run: choco install go-task"
                Print-Info "  Option 2: Download from: https://github.com/go-task/task/releases"
                exit 1
            }
        }
    }

    # Verify installation
    if (Get-Command task -ErrorAction SilentlyContinue) {
        $version = (task --version)
        Print-Success "Task installed successfully: $version"
    } else {
        Print-Error "Task installation failed"
        Print-Info "Please install manually from: https://taskfile.dev/installation/"
        exit 1
    }
}

Write-Host ""
Print-Success "Setup script completed!"
Write-Host ""
Print-Info "Next steps:"
Write-Host "  1. Run: " -NoNewline
Write-Host "task setup" -ForegroundColor Green -NoNewline
Write-Host " - Complete project setup"
Write-Host "  2. Or run: " -NoNewline
Write-Host "task help" -ForegroundColor Green -NoNewline
Write-Host " - See all available commands"
Write-Host ""
Print-Info "Quick start:"
Write-Host "  " -NoNewline
Write-Host "task setup" -ForegroundColor Green -NoNewline
Write-Host "         - Install dependencies and start services"
Write-Host "  " -NoNewline
Write-Host "task docker-up" -ForegroundColor Green -NoNewline
Write-Host "     - Start all Docker containers"
Write-Host "  " -NoNewline
Write-Host "task logs" -ForegroundColor Green -NoNewline
Write-Host "          - View container logs"
Write-Host "  " -NoNewline
Write-Host "task status" -ForegroundColor Green -NoNewline
Write-Host "        - Check container status"
Write-Host ""
Write-Host "══════════════════════════════════════════════════════=" -ForegroundColor Cyan
