param(
    [Parameter(Mandatory = $true, Position = 0)]
    [string]$CommandLine
)

$githubUser = Read-Host "Digite seu usuario do GitHub"

$secureToken = Read-Host "Digite seu token do GitHub" -AsSecureString
$tokenPtr = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($secureToken)
$githubToken = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($tokenPtr)

$tempDir = $env:TEMP
$settingsPath = Join-Path $tempDir "github-packages-settings.xml"
$exitCode = 1

$xml = @"
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd">
  <servers>
    <server>
      <id>github</id>
      <username>$githubUser</username>
      <password>$githubToken</password>
    </server>
  </servers>
</settings>
"@

try {
    $xml | Set-Content -Path $settingsPath -Encoding UTF8

    $finalCommand = "$CommandLine -s `"$settingsPath`""

    Write-Host ""
    Write-Host "Executando Maven com autenticacao temporaria do GitHub Packages..."
    Write-Host "Arquivo temporario: $settingsPath"
    Write-Host "Comando: $finalCommand"
    Write-Host ""

    cmd.exe /c $finalCommand
    $exitCode = $LASTEXITCODE
}
finally {
    if (Test-Path $settingsPath) {
        Remove-Item $settingsPath -Force
    }

    if ($tokenPtr -ne [IntPtr]::Zero) {
        [System.Runtime.InteropServices.Marshal]::ZeroFreeBSTR($tokenPtr)
    }

    Write-Host ""
    Write-Host "Arquivo temporario removido."
}

exit $exitCode