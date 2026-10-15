# Arranca el backend en perfil dev (MySQL local + .env)
# Requiere .env en la raíz (copia desde .env.example si no existe)
if (-not (Test-Path -LiteralPath ".env")) {
    Write-Warning ".env no encontrado. Copiando .env.example -> .env (edita los valores CHANGEME)"
    Copy-Item -Path ".env.example" -Destination ".env"
}
$env:SPRING_PROFILES_ACTIVE = "dev"
[Environment]::SetEnvironmentVariable("SPRING_PROFILES_ACTIVE", "dev", "Process")
Write-Host "SPRING_PROFILES_ACTIVE=dev" -ForegroundColor Cyan
if (Test-Path -LiteralPath "./mvnw.cmd") {
    & ./mvnw.cmd spring-boot:run
} else {
    & ./mvnw spring-boot:run
}
