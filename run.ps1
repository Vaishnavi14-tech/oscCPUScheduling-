$ErrorActionPreference = "Stop"

Write-Host "===================================================" -ForegroundColor Cyan
Write-Host " CPU Scheduler Simulator - Launching..." -ForegroundColor Cyan
Write-Host "===================================================" -ForegroundColor Cyan

$javafxDir = "$env:USERPROFILE\.m2\repository\org\openjfx"
if (-not (Test-Path $javafxDir)) {
    Write-Error "JavaFX dependencies not found in $javafxDir. Please ensure dependencies are downloaded."
    exit 1
}

$javafxJars = (Get-ChildItem -Path $javafxDir -Recurse -Filter "*-win.jar").FullName -join ";"

if (-not (Test-Path "target\classes")) {
    New-Item -ItemType Directory -Path "target\classes" -Force | Out-Null
}

# Compile source files if needed
$sources = (Get-ChildItem -Path "src\main\java" -Recurse -Filter "*.java").FullName
if ($sources) {
    Write-Host "Compiling sources..." -ForegroundColor Yellow
    javac --module-path $javafxJars --add-modules javafx.controls,javafx.fxml -d "target\classes" $sources
    Copy-Item "src\main\resources\*" "target\classes\" -Force -ErrorAction SilentlyContinue
}

Write-Host "Starting JavaFX Application..." -ForegroundColor Green
java --module-path $javafxJars --add-modules javafx.controls,javafx.fxml -cp "target\classes" com.cpuscheduler.CPUSchedulerApp
