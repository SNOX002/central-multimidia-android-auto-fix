# 1-conectar.ps1 - Reinicia o adb, encontra a central e mostra o que ela realmente é.
#
# Uso:
#   .\1-conectar.ps1                     conexão pelo cabo USB
#   .\1-conectar.ps1 -AtivarWifi         (com o cabo ligado) liga o adb pela rede na porta 5555
#   .\1-conectar.ps1 -Ip 192.168.43.20   conecta pela rede (central com adb na porta 5555)
#   .\1-conectar.ps1 -Procurar           procura na rede atual quem tem a porta 5555 aberta (não conecta)
param(
    [string]$Serial,
    [string]$Ip,
    [switch]$AtivarWifi,
    [switch]$Procurar
)

. "$PSScriptRoot\_comum.ps1"

if ($Procurar) {
    $redes = @(Get-NetIPConfiguration | Where-Object { $_.IPv4DefaultGateway -and $_.NetAdapter.Status -eq 'Up' })
    if ($redes.Count -eq 0) { Write-Host 'O notebook não está em nenhuma rede. Conecte-o ao mesmo Wi-Fi da central.' -ForegroundColor Red; exit 1 }
    $achados = @()
    foreach ($r in $redes) {
        $meuIp = @($r.IPv4Address)[0].IPAddress
        $base = ($meuIp -split '\.')[0..2] -join '.'
        Write-Host "Procurando a porta 5555 em $base.1-254 (rede '$($r.NetProfile.Name)', notebook $meuIp)..." -ForegroundColor Cyan
        $tentativas = foreach ($n in 1..254) {
            $alvo = "$base.$n"
            if ($alvo -eq $meuIp) { continue }
            $cli = New-Object Net.Sockets.TcpClient
            [pscustomobject]@{ Ip = $alvo; Cli = $cli; Op = $cli.BeginConnect($alvo, 5555, $null, $null) }
        }
        Start-Sleep -Milliseconds 2000
        foreach ($t in $tentativas) {
            if ($t.Cli.Connected) {
                $mac = (Get-NetNeighbor -IPAddress $t.Ip -ErrorAction SilentlyContinue | Select-Object -First 1).LinkLayerAddress
                $achados += [pscustomobject]@{ Ip = $t.Ip; MAC = $mac }
            }
            $t.Cli.Close()
        }
    }
    Write-Host ''
    if ($achados.Count -eq 0) {
        Write-Host 'Ninguém com a porta 5555 aberta nesta rede.' -ForegroundColor Yellow
        Write-Host ' - Confira se a central está mesmo no MESMO Wi-Fi que o notebook (e com IP na faixa acima).'
        Write-Host ' - Procure no menu de fábrica / Opções do desenvolvedor algo como "ADB por rede", "ADB Wi-Fi" ou "ADB debug".'
        Write-Host ' - Se não houver, o adb pela rede está desligado e só o cabo (ou -AtivarWifi via cabo) liga.'
        exit 1
    }
    Write-Host 'Aparelhos com a porta 5555 aberta:' -ForegroundColor Green
    $achados | Format-Table -AutoSize | Out-Host
    Write-Host 'Confira na central (Wi-Fi > detalhes) qual desses é o IP dela e rode:  .\1-conectar.ps1 -Ip <IP>' -ForegroundColor Green
    Write-Host '(TVs Android também costumam abrir a 5555; não conecte num IP que não seja o da central.)' -ForegroundColor DarkGray
    exit 0
}

Write-Host "adb: $Adb" -ForegroundColor DarkGray
Write-Host 'Reiniciando o servidor adb...' -ForegroundColor Cyan
& $Adb kill-server | Out-Null
& $Adb start-server | Out-Null

if ($Ip) {
    $alvo = if ($Ip -match ':') { $Ip } else { "${Ip}:5555" }
    Write-Host "Conectando em $alvo pela rede..." -ForegroundColor Cyan
    & $Adb connect $alvo
    $Serial = $alvo
}

Write-Host ''
Write-Host 'Aparelhos encontrados:' -ForegroundColor Cyan
& $Adb devices -l
$aparelhos = @(Get-Aparelhos)
if ($Serial) { $aparelhos = @($aparelhos | Where-Object Serial -eq $Serial) }

if ($aparelhos.Count -eq 0) {
    Write-Host ''
    Write-Host 'NENHUM APARELHO APARECEU. Confira, nesta ordem:' -ForegroundColor Red
    Write-Host ' 1. Menu de fábrica da central: USB em modo "device" (selecione de novo mesmo que já esteja marcado).'
    Write-Host '    Em algumas centrais só vale depois de reiniciar a central.'
    Write-Host ' 2. Porta certa: normalmente a USB 1 do chicote (a de dados). Teste a outra porta se houver duas.'
    Write-Host ' 3. Cabo USB-A macho/macho QUE PASSA DADOS (muitos só carregam). Evite hub e extensor.'
    Write-Host ' 4. No notebook, teste outra porta USB (de preferência uma USB 2.0, a de cor preta).'
    Write-Host ' 5. Driver: abra o Gerenciador de Dispositivos (devmgmt.msc). Se houver um "Android" ou'
    Write-Host '    dispositivo desconhecido com ponto de exclamação: Atualizar driver > Procurar no computador >'
    Write-Host '    Permitir que eu escolha > "ADB Interface" ou "Android Composite ADB Interface".'
    Write-Host ' 6. Se o notebook mostrar a central como pendrive/MTP, ela ainda não está em modo "device" para adb.'
    Write-Host ''
    Write-Host 'Dispositivos USB que o Windows está vendo agora (procure Android/ADB/MediaTek/Autochips):' -ForegroundColor Yellow
    Get-PnpDevice -PresentOnly -ErrorAction SilentlyContinue |
        Where-Object { $_.FriendlyName -match 'android|adb|mtp|mediatek|mtk|autochips|composite|desconhecido|unknown' -or $_.Status -ne 'OK' } |
        Select-Object Status, Class, FriendlyName | Format-Table -AutoSize | Out-Host
    exit 1
}

$naoAutorizados = @($aparelhos | Where-Object Estado -eq 'unauthorized')
if ($naoAutorizados.Count -gt 0) {
    Write-Host ''
    Write-Host 'Aparece "unauthorized": a central viu o notebook, mas não liberou o acesso.' -ForegroundColor Yellow
    Write-Host ' 1. Olhe a tela da central: deve haver "Permitir depuração USB?". Marque "Sempre permitir" e toque em OK.'
    Write-Host ' 2. Se a janela não aparecer: desconecte e reconecte o cabo e rode este script de novo.'
    Write-Host ' 3. Se existir "Opções do desenvolvedor" na central: "Revogar autorizações de depuração USB" e reconecte.'
}
$offline = @($aparelhos | Where-Object Estado -eq 'offline')
if ($offline.Count -gt 0) {
    Write-Host ''
    Write-Host 'Aparece "offline": desconecte e reconecte o cabo; se continuar, reinicie a central.' -ForegroundColor Yellow
}

$prontos = @($aparelhos | Where-Object Estado -eq 'device')
if ($prontos.Count -eq 0) { exit 1 }
if ($prontos.Count -gt 1 -and -not $Serial) {
    Write-Host ''
    Write-Host 'Mais de um aparelho pronto. Rode de novo com -Serial <número> para escolher.' -ForegroundColor Yellow
    exit 1
}
$env:ANDROID_SERIAL = $prontos[0].Serial

function P([string]$nome) { (& $Adb shell getprop $nome | Out-String).Trim() }

$sdk = [int](P 'ro.build.version.sdk')
$memLinhas = & $Adb shell cat /proc/meminfo
$cpuLinhas = & $Adb shell cat /proc/cpuinfo
$nucleos = @($cpuLinhas | Where-Object { $_ -cmatch '^processor\s*:' }).Count
$modeloCpu = ($cpuLinhas | Where-Object { $_ -match '^(model name|Processor)\s*:' } | Select-Object -First 1) -replace '^[^:]+:\s*', ''
$hardware = ($cpuLinhas | Where-Object { $_ -match '^Hardware\s*:' } | Select-Object -First 1) -replace '^[^:]+:\s*', ''
function MemMB([string]$chave) {
    $l = $memLinhas | Where-Object { $_ -match "^${chave}:" } | Select-Object -First 1
    if ($l -match '(\d+)\s*kB') { return [math]::Round([int64]$Matches[1] / 1024) }
    return '?'
}

Write-Host ''
Write-Host "=== CENTRAL CONECTADA: $($prontos[0].Serial) ===" -ForegroundColor Green
Write-Host ("Fabricante / modelo : {0} / {1} ({2})" -f (P 'ro.product.manufacturer'), (P 'ro.product.model'), (P 'ro.product.device'))
Write-Host ("Android na tela     : {0}" -f (P 'ro.build.version.release'))
Write-Host ("Android REAL (sdk)  : {0} = {1}" -f $sdk, (Get-VersaoReal $sdk)) -ForegroundColor Green
Write-Host ("Build               : {0}" -f (P 'ro.build.display.id'))
Write-Host ("Plataforma          : {0} / {1}" -f (P 'ro.board.platform'), (P 'ro.hardware'))
Write-Host ("CPU                 : {0} núcleo(s), {1} {2}" -f $nucleos, $modeloCpu, $hardware)
Write-Host ("ABI                 : {0}" -f (P 'ro.product.cpu.abilist'))
Write-Host ("RAM total / livre   : {0} MB / {1} MB (disponível: {2} MB)" -f (MemMB 'MemTotal'), (MemMB 'MemFree'), (MemMB 'MemAvailable'))
Write-Host ("Modo low-RAM (Go)   : {0}" -f (P 'ro.config.low_ram'))
Write-Host ("Tela                : {0}" -f ((& $Adb shell wm size | Out-String).Trim() -replace '\s+', ' '))
Write-Host ("Densidade           : {0}" -f ((& $Adb shell wm density | Out-String).Trim() -replace '\s+', ' '))

if ($AtivarWifi) {
    Write-Host ''
    $ipLinha = & $Adb shell ip -f inet addr show wlan0 | Where-Object { $_ -match 'inet\s+(\d+\.\d+\.\d+\.\d+)' } | Select-Object -First 1
    if (-not ($ipLinha -match 'inet\s+(\d+\.\d+\.\d+\.\d+)')) {
        Write-Host 'A central não tem IP no Wi-Fi (wlan0). Conecte-a a uma rede (ex.: roteador do celular) e tente de novo.' -ForegroundColor Red
        exit 1
    }
    $ipCentral = $Matches[1]
    Write-Host "Ligando o adb pela rede (porta 5555). IP da central: $ipCentral" -ForegroundColor Cyan
    Write-Host 'Atenção: qualquer aparelho na mesma rede pode se conectar enquanto isso estiver ligado.' -ForegroundColor Yellow
    Write-Host 'Use só numa rede sua. Desliga sozinho quando a central reinicia.' -ForegroundColor Yellow
    & $Adb tcpip 5555
    Start-Sleep -Seconds 3
    Write-Host "Agora pode tirar o cabo e rodar:  .\1-conectar.ps1 -Ip $ipCentral" -ForegroundColor Green
}
