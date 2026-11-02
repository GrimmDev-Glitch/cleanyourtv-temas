# Descarga lo necesario para compilar el launcher, una sola vez, en una carpeta "toolchain" junto al repo:
# Java 17 (Eclipse Temurin, ~190 MB), las herramientas del Android SDK (~150 MB), la plataforma 34 y build-tools
# 34 (~120 MB), y crea el entorno de Python de tools\venv. Gradle descarga el resto (~400 MB) al compilar.
# Al final el Android SDK te pide aceptar sus licencias: léelas y responde "y" si estás de acuerdo.
#
#   powershell -ExecutionPolicy Bypass -File tools\preparar-herramientas.ps1
$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'
$repo = Split-Path -Parent $PSScriptRoot
$t = Join-Path (Split-Path -Parent $repo) 'toolchain'
New-Item -ItemType Directory -Force "$t\dl" | Out-Null

if (-not (Test-Path "$t\jdk")) {
    Write-Host 'Descargando Java 17...'
    Invoke-WebRequest -UseBasicParsing 'https://api.adoptium.net/v3/binary/latest/17/ga/windows/x64/jdk/hotspot/normal/eclipse' -OutFile "$t\dl\jdk17.zip"
    Expand-Archive "$t\dl\jdk17.zip" "$t\jdk" -Force
}
if (-not (Test-Path "$t\sdk\cmdline-tools\latest")) {
    Write-Host 'Descargando las herramientas del Android SDK...'
    Invoke-WebRequest -UseBasicParsing 'https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip' -OutFile "$t\dl\cmdline-tools.zip"
    Expand-Archive "$t\dl\cmdline-tools.zip" "$t\sdk-tmp" -Force
    New-Item -ItemType Directory -Force "$t\sdk\cmdline-tools" | Out-Null
    Move-Item "$t\sdk-tmp\cmdline-tools" "$t\sdk\cmdline-tools\latest" -Force
    Remove-Item "$t\sdk-tmp" -Recurse -Force
}
$env:JAVA_HOME = (Get-ChildItem "$t\jdk" -Directory | Select-Object -First 1).FullName
$sm = "$t\sdk\cmdline-tools\latest\bin\sdkmanager.bat"
Write-Host 'Licencias del Android SDK (responde y para aceptarlas):'
& $sm --sdk_root="$t\sdk" --licenses
Write-Host 'Instalando la plataforma 34 y build-tools 34...'
& $sm --sdk_root="$t\sdk" 'platforms;android-34' 'build-tools;34.0.0'

if (-not (Test-Path "$repo\tools\venv")) {
    Write-Host 'Creando el entorno de Python (tools\venv)...'
    python -m venv "$repo\tools\venv"
    & "$repo\tools\venv\Scripts\python.exe" -m pip install -q -r "$repo\tools\requirements.txt"
}
Write-Host "Listo. Herramientas en $t"
