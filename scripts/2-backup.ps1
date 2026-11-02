# 2-backup.ps1 - Fotografa o estado da central ANTES de qualquer mudança.
# Só lê a central, não altera nada. Grava em backups\<data_hora>\.
param([string]$Serial)

. "$PSScriptRoot\_comum.ps1"
$serialUsado = Select-Central $Serial

$pasta = Join-Path $PSScriptRoot "backups\$(Get-Carimbo)"
New-Item -ItemType Directory -Force -Path $pasta | Out-Null
Write-Host "Salvando backup em: $pasta" -ForegroundColor Cyan

$itens = [ordered]@{
    'pacotes-todos.txt'          = 'pm list packages -f'
    'pacotes-desativados.txt'    = 'pm list packages -d'
    'pacotes-usuario.txt'        = 'pm list packages -3'
    'pacotes-sistema.txt'        = 'pm list packages -s'
    'pacotes-com-removidos.txt'  = 'pm list packages -u'
    'getprop.txt'                = 'getprop'
    'settings-system.txt'        = 'settings list system'
    'settings-secure.txt'        = 'settings list secure'
    'settings-global.txt'        = 'settings list global'
    'wm-size.txt'                = 'wm size'
    'wm-density.txt'             = 'wm density'
    'meminfo.txt'                = 'dumpsys meminfo'
    'launcher-padrao.txt'        = 'cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME'
    'launchers-instalados.txt'   = 'cmd package query-activities --brief -a android.intent.action.MAIN -c android.intent.category.HOME'
}

foreach ($arquivo in $itens.Keys) {
    Write-Host ("  {0,-28} <- {1}" -f $arquivo, $itens[$arquivo])
    Save-Shell (Join-Path $pasta $arquivo) $itens[$arquivo] | Out-Null
}

$sdk = (& $Adb shell getprop ro.build.version.sdk | Out-String).Trim()
$todos = @(Get-Content (Join-Path $pasta 'pacotes-todos.txt') | ForEach-Object { ConvertTo-NomePacote $_ } | Where-Object { $_ })
$desativados = @(Get-Content (Join-Path $pasta 'pacotes-desativados.txt') | ForEach-Object { ConvertTo-NomePacote $_ } | Where-Object { $_ })
$usuario = @(Get-Content (Join-Path $pasta 'pacotes-usuario.txt') | ForEach-Object { ConvertTo-NomePacote $_ } | Where-Object { $_ })
$launcher = (Get-Content (Join-Path $pasta 'launcher-padrao.txt') | Where-Object { $_ -match '/' } | Select-Object -Last 1)

@(
    "Data: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
    "Serial: $serialUsado"
    "SDK: $sdk ($(Get-VersaoReal ([int]$sdk)))"
    "Pacotes instalados: $($todos.Count)"
    "Pacotes desativados: $($desativados.Count)"
    "Pacotes instalados pelo usuário: $($usuario.Count)"
    "Launcher padrão: $launcher"
) | Out-File (Join-Path $pasta 'resumo.txt') -Encoding utf8

Write-Host ''
Get-Content (Join-Path $pasta 'resumo.txt') | Out-Host
$vazios = @(Get-ChildItem $pasta -File | Where-Object Length -lt 4)
if ($vazios.Count -gt 0) {
    Write-Host ("Arquivos vazios (o comando pode não existir nessa versão): {0}" -f ($vazios.Name -join ', ')) -ForegroundColor Yellow
}
Write-Host 'Backup concluído.' -ForegroundColor Green
