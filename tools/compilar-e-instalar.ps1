# Compila el launcher y lo instala en la TV como pantalla de inicio.
# Con la depuración activada en la TV, desde la carpeta del repo:
#   powershell -ExecutionPolicy Bypass -File tools\compilar-e-instalar.ps1 -TvHost <ip-de-tu-tv>
#
# Usa el JDK 17 y el Android SDK de la carpeta "toolchain" junto al repo (tools\preparar-herramientas.ps1 los
# descarga); si no está, los de JAVA_HOME / ANDROID_HOME o los de Android Studio. Siempre pasa -HomeKeyService,
# que hace funcionar el botón de YouTube y el engranaje del control (mantener el volumen sube de a un paso).
param([string]$TvHost = $env:TV_HOST)
$ErrorActionPreference = 'Stop'
if (-not $TvHost) { throw 'pasa -TvHost <ip-de-tu-tv>' }
$repo = Split-Path -Parent $PSScriptRoot
$t = Join-Path (Split-Path -Parent $repo) 'toolchain'
if (Test-Path "$t\jdk") {
    $env:JAVA_HOME = (Get-ChildItem "$t\jdk" -Directory | Select-Object -First 1).FullName
    $env:ANDROID_HOME = "$t\sdk"
} elseif (-not $env:JAVA_HOME -or -not $env:ANDROID_HOME) {
    $jbr = Get-ChildItem 'C:\Program Files\Android' -Directory -ErrorAction SilentlyContinue |
        ForEach-Object { Join-Path $_.FullName 'jbr' } | Where-Object { Test-Path "$_\bin\java.exe" } | Select-Object -First 1
    if (-not $env:JAVA_HOME -and $jbr) { $env:JAVA_HOME = $jbr }
    if (-not $env:ANDROID_HOME) { $env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk" }
    if (-not $env:JAVA_HOME -or -not (Test-Path $env:ANDROID_HOME)) {
        throw 'No encuentro Java 17 ni el Android SDK: ejecuta primero tools\preparar-herramientas.ps1'
    }
}
# Gradle reads the SDK from local.properties: written each time, so a moved folder still builds.
$sdk = $env:ANDROID_HOME -replace '\\', '\\'
"sdk.dir=$sdk" | Out-File -Encoding ascii (Join-Path $repo 'launcher\local.properties')
# Build without a background Gradle daemon: on Windows a daemon left running keeps the build's files locked
# and the console output open, so the next build fails or this script never returns.
Push-Location (Join-Path $repo 'launcher')
try {
    .\gradlew.bat --no-daemon assembleRelease -q --console=plain
    if ($LASTEXITCODE -ne 0) { throw 'Falló la compilación (Gradle)' }
} finally { Pop-Location }
$env:SKIP_BUILD = '1'
try { & (Join-Path $repo 'launcher\build-and-install.ps1') -TvHost $TvHost -HomeKeyService }
finally { Remove-Item env:SKIP_BUILD -ErrorAction SilentlyContinue }
