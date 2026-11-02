# Funções usadas por todos os scripts. Não rode este arquivo direto.

[Console]::OutputEncoding = [Text.Encoding]::UTF8

function Get-AdbPath {
    $cmd = Get-Command adb -ErrorAction SilentlyContinue
    if ($cmd) { return $cmd.Source }
    $candidatos = @(
        "$env:LOCALAPPDATA\Microsoft\WinGet\Links\adb.exe",
        "$env:LOCALAPPDATA\Microsoft\WinGet\Packages\Google.PlatformTools_Microsoft.Winget.Source_8wekyb3d8bbwe\platform-tools\adb.exe"
    )
    foreach ($c in $candidatos) { if (Test-Path $c) { return $c } }
    Write-Host 'adb não encontrado. Instale com: winget install Google.PlatformTools' -ForegroundColor Red
    exit 1
}

$Adb = Get-AdbPath
# O scrcpy traz um adb próprio; apontar para o mesmo adb evita o erro "adb server version doesn't match".
$env:ADB = $Adb

# Lista os aparelhos vistos pelo adb: objetos com Serial e Estado (device, unauthorized, offline...).
function Get-Aparelhos {
    & $Adb devices | ForEach-Object {
        if ($_ -match '^(\S+)\s+(device|unauthorized|offline|no permissions|recovery|sideload|bootloader|host)\b') {
            [pscustomobject]@{ Serial = $Matches[1]; Estado = $Matches[2] }
        }
    }
}

# Escolhe a central (ou a passada em -Serial) e fixa ANDROID_SERIAL para os próximos comandos.
function Select-Central {
    param([string]$Serial)
    if ($Serial) { $env:ANDROID_SERIAL = $Serial; return $Serial }
    $ok = @(Get-Aparelhos | Where-Object Estado -eq 'device')
    if ($ok.Count -eq 0) {
        Write-Host 'Nenhuma central conectada e autorizada. Rode primeiro o 1-conectar.ps1.' -ForegroundColor Red
        exit 1
    }
    if ($ok.Count -gt 1) {
        Write-Host 'Há mais de um aparelho conectado:' -ForegroundColor Yellow
        $ok | Format-Table -AutoSize | Out-Host
        Write-Host 'Rode de novo informando qual, por exemplo: -Serial 192.168.43.20:5555' -ForegroundColor Yellow
        exit 1
    }
    $env:ANDROID_SERIAL = $ok[0].Serial
    return $ok[0].Serial
}

function Invoke-Shell {
    & $Adb shell @args
}

# Roda um comando no shell da central e grava a saída num arquivo UTF-8.
function Save-Shell {
    param([string]$Arquivo, [string]$Comando)
    $saida = & $Adb shell $Comando
    $saida | Out-File -FilePath $Arquivo -Encoding utf8
    return $saida
}

# Nome do pacote a partir de uma linha "package:/caminho/app.apk=com.exemplo" ou "package:com.exemplo".
function ConvertTo-NomePacote {
    param([string]$Linha)
    $l = $Linha.Trim()
    if (-not $l.StartsWith('package:')) { return $null }
    $l = $l.Substring(8)
    $i = $l.LastIndexOf('=')
    if ($i -ge 0) { $l = $l.Substring($i + 1) }
    return $l
}

function Get-VersaoReal {
    param([int]$Sdk)
    $mapa = @{ 21 = '5.0'; 22 = '5.1'; 23 = '6.0'; 24 = '7.0'; 25 = '7.1'; 26 = '8.0'; 27 = '8.1'; 28 = '9';
               29 = '10'; 30 = '11'; 31 = '12'; 32 = '12L'; 33 = '13'; 34 = '14'; 35 = '15'; 36 = '16' }
    if ($mapa.ContainsKey($Sdk)) { return "Android $($mapa[$Sdk])" }
    return "SDK $Sdk (desconhecido)"
}

function Get-Carimbo { Get-Date -Format 'yyyy-MM-dd_HH-mm-ss' }
