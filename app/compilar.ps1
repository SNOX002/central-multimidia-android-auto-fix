# compilar.ps1 - Monta o DestravarBT.apk sem Gradle (aapt2 + javac + d8 + zipalign + apksigner).
# Requer: JDK 17 (Eclipse Temurin) e Android SDK em %LOCALAPPDATA%\Android\Sdk
# (build-tools 34.0.0 e platforms;android-28).
#
# Uso:
#   .\compilar.ps1                                  chave em app\chave\ (criada na primeira vez)
#   .\compilar.ps1 -PastaChave D:\minha\chave       usa uma chave que você já tem
#
# A chave (destravar.jks + senha.txt) NUNCA vai para o Git (.gitignore). GUARDE essa pasta: o Android só aceita
# atualizar o app se a nova versão for assinada com a mesma chave. O APK sai em release\DestravarBT.apk.
param(
    [string]$PastaChave
)
$ErrorActionPreference = 'Stop'
$raiz = $PSScriptRoot
if (-not $PastaChave) { $PastaChave = Join-Path $raiz 'chave' }

$jdk = Get-ChildItem 'C:\Program Files\Eclipse Adoptium' -Directory | Where-Object Name -like 'jdk-17*' | Select-Object -First 1
if (-not $jdk) { throw 'JDK 17 não encontrado. Instale: winget install EclipseAdoptium.Temurin.17.JDK' }
$env:JAVA_HOME = $jdk.FullName
$env:Path = "$($jdk.FullName)\bin;$env:Path"
$sdk = "$env:LOCALAPPDATA\Android\Sdk"
$bt = "$sdk\build-tools\34.0.0"
$androidJar = "$sdk\platforms\android-28\android.jar"
foreach ($f in "$bt\aapt2.exe", "$bt\d8.bat", "$bt\zipalign.exe", "$bt\apksigner.bat", $androidJar) {
    if (-not (Test-Path $f)) { throw "Falta $f (instale com sdkmanager)." }
}

function Rodar([string]$exe, [string[]]$argumentos) {
    & $exe @argumentos
    if ($LASTEXITCODE -ne 0) { throw "Falhou: $exe $($argumentos -join ' ')" }
}

$b = Join-Path $raiz 'build'
if (Test-Path $b) { Remove-Item $b -Recurse -Force }
New-Item -ItemType Directory -Force "$b\gen", "$b\classes", "$b\dex" | Out-Null

Write-Host '1/6 recursos (aapt2)' -ForegroundColor Cyan
Rodar "$bt\aapt2.exe" @('compile', '--dir', "$raiz\res", '-o', "$b\res.zip")
Rodar "$bt\aapt2.exe" @('link', '-I', $androidJar, '--manifest', "$raiz\AndroidManifest.xml",
    '--min-sdk-version', '21', '--target-sdk-version', '27', '--java', "$b\gen", '-o', "$b\base.apk", "$b\res.zip")

Write-Host '2/6 Java (javac)' -ForegroundColor Cyan
$fontes = @(Get-ChildItem "$raiz\src", "$b\gen" -Recurse -Filter *.java | ForEach-Object FullName)
Rodar 'javac' (@('-encoding', 'UTF-8', '-source', '8', '-target', '8', '-Xlint:-options',
    '-bootclasspath', $androidJar, '-d', "$b\classes") + $fontes)

Write-Host '3/6 dex (d8)' -ForegroundColor Cyan
$classes = @(Get-ChildItem "$b\classes" -Recurse -Filter *.class | ForEach-Object FullName)
Rodar "$bt\d8.bat" (@('--min-api', '21', '--lib', $androidJar, '--output', "$b\dex") + $classes)

Write-Host '4/6 empacotando' -ForegroundColor Cyan
Copy-Item "$b\base.apk" "$b\sem-assinatura.apk"
Rodar 'jar' @('uf', "$b\sem-assinatura.apk", '-C', "$b\dex", 'classes.dex')
Rodar "$bt\zipalign.exe" @('-f', '-p', '4', "$b\sem-assinatura.apk", "$b\alinhado.apk")

Write-Host '5/6 assinando' -ForegroundColor Cyan
$ks = Join-Path $PastaChave 'destravar.jks'
$arqSenha = Join-Path $pastaChave 'senha.txt'
if (-not (Test-Path $ks)) {
    New-Item -ItemType Directory -Force $pastaChave | Out-Null
    $senha = -join ((48..57) + (65..90) + (97..122) | Get-Random -Count 24 | ForEach-Object { [char]$_ })
    [IO.File]::WriteAllText($arqSenha, $senha)
    Rodar 'keytool' @('-genkeypair', '-keystore', $ks, '-alias', 'destravar', '-keyalg', 'RSA', '-keysize', '2048',
        '-validity', '10000', '-storepass', $senha, '-keypass', $senha, '-dname', 'CN=Destravar BT')
}
$senha = [IO.File]::ReadAllText($arqSenha).Trim()
$pastaRelease = Join-Path (Split-Path $raiz -Parent) 'release'
New-Item -ItemType Directory -Force $pastaRelease | Out-Null
$apk = Join-Path $pastaRelease 'DestravarBT.apk'
Rodar "$bt\apksigner.bat" @('sign', '--ks', $ks, '--ks-pass', "pass:$senha", '--key-pass', "pass:$senha", '--out', $apk, "$b\alinhado.apk")

Write-Host '6/6 verificando' -ForegroundColor Cyan
Rodar "$bt\apksigner.bat" @('verify', $apk)
Write-Host "Pronto: $apk ($([math]::Round((Get-Item $apk).Length / 1KB)) KB)" -ForegroundColor Green
