$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
if (!(Test-Path -LiteralPath '.env')) {
    throw 'Copie .env.example para .env e preencha as senhas do PostgreSQL.'
}
& .\mvnw.cmd '-Dspring-boot.run.profiles=dev' spring-boot:run
exit $LASTEXITCODE
