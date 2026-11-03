# Android Auto fechando sozinho e "PhoneLink está conectado" na central multimídia — correção (AC8227L / Jancar / CPlink)

**Sua central multimídia chinesa tem Android Auto sem fio que fecha sozinho, a música Bluetooth para de tocar
depois do Android Auto, ou aparece "PhoneLink está conectado, a operação atual não é compatível!"?**
Este repositório explica **por que** isso acontece e traz um **app gratuito** que resolve com um toque,
**sem root e sem trocar firmware**, além do **passo a passo para ativar o ADB** na central e instalar pela rede.

> 🌐 **Página do projeto:** https://snox002.github.io/central-multimidia-android-auto-fix/ · 📦 **[Baixar o APK](https://github.com/SNOX002/central-multimidia-android-auto-fix/releases/latest)**
>
> 🇺🇸 [English summary](#english) · 🇪🇸 [Resumen en español](#español)

Dois ícones para a central multimídia chinesa com chip **Autochips AC8227L** e sistema **Jancar**
(a do app **CPlink** / PhoneLink, comum em kits "Android 12/13/15" de 7, 9 e 10 polegadas), que resolvem
os problemas do **Android Auto sem fio** de fábrica:

| Ícone | Quando usar | O que faz |
|---|---|---|
| 🔵 **Destravar BT** | A música BT não toca, ou aparece *"PhoneLink está conectado, a operação atual não é compatível!"* | Fecha o Android Auto e o CPlink, desfaz as travas que o AA deixou, confere a música BT por 5 s e, se ela não voltou, reinicia o Bluetooth da central uma vez. Fecha sozinho. |
| 🟢 **Reiniciar AA** | O Android Auto começou a fechar sozinho | Fecha o AA travado e o CPlink, desfaz as travas, reinicia o Bluetooth para o celular reconectar e o AA sem fio recomeçar do zero, e abre o Android Auto. |

Os dois ficam num app só (`br.central.destravarbt`, ~37 KB).

---

## Sintomas que este projeto resolve

- **Android Auto fecha sozinho** do nada: mostra o mapa, toca o Spotify e some.
- **A música Bluetooth não toca** depois de usar o Android Auto, ou o celular conecta só para chamadas.
- Ao dar play na Música BT aparece:
  - 🇧🇷 `PhoneLink está conectado, a operação atual não é compatível!`
  - 🇺🇸 `PhoneLink is connected, the current operation is not supported!`
  - 🇪🇸 `PhoneLink está conectado, la operación actual no es compatible.`
- O **Bluetooth "só entra depois de muita luta"**, ou não reconecta nem reiniciando a central.
- **CPlink e Android Auto ficam abrindo sozinhos** e continuam rodando mesmo depois de limpar a multitarefa.
- Pendrive e Bluetooth brigando pelo som.

## O problema

O app de Android Auto que vem na central (`com.google.android.projection.sink`, versão `O.250702`)
**trava toda vez que a sessão sem fio termina**:

```
FATAL EXCEPTION: Pcm-5
java.lang.IllegalStateException: play() called on uninitialized AudioTrack.
    at com.google.android.projection.sink.audio.AudioPlayer$PcmPlayThread.run(AudioPlayer.java:489)
```

Como trava, ele nunca desfaz o que mudou ao conectar, e deixa **três travas**:

1. **Música BT bloqueada na central.** Ao conectar, o AA grava `settings global bluetooth_a2dp_src_priority_<MAC do celular> = 0`
   (o som passa a ir pelo Wi-Fi). Travado, ele não volta o valor, e a central recusa a música BT daquele celular
   (`Incoming A2DP rejected`). **Fica gravado: reiniciar a central não resolve.**
2. **"PhoneLink" preso.** A Jancar grava `settings global phoneMode = auto_wireless` e, enquanto ele estiver assim,
   bloqueia o play da Música BT com o aviso do PhoneLink.
3. **"Áudio" desligado no celular.** O próprio celular pode desligar a mídia para a central no Bluetooth dele.
   Essa trava está no celular, e o app não alcança; ele só lembra de religar.

### Android Auto fechando assim que a música começa

Outro motivo, com o mesmo erro (`play() called on uninitialized AudioTrack`), apareceu depois de a central ficar
**12 dias sem reiniciar**: o AA fecha **no instante em que a música começa a tocar por ele** (o mapa funciona).
O log mostra que o Android **recusa o canal de som** da música:

```
AudioFlinger: no more track names available
AudioTrack: AudioFlinger could not create track, status: -12
```

O **rádio da Jancar** (`com.jancar.radio`, app persistente, não dá para fechar) **vaza canais de som**: estava com
13 canais abertos e parados (`dumpsys media.audio_flinger`). Com o misturador lotado, o AA não consegue o canal da
música. **Reiniciar a central libera tudo**, e por um tempo funciona. Por isso o app, a partir da v1.7,
**conta os canais** e, se passarem de 11, avisa e oferece o botão **"Reiniciar a central"** (só reinicia se você tocar).

Outros fatos úteis desta central:

- A tela diz "Android 12/15", mas o sistema real é **Android 8.1 (SDK 27)**.
- **Wi-Fi e Bluetooth são o mesmo chip, só 2,4 GHz.** No AA sem fio a central vira **ponto de acesso**
  (`ap0 192.168.43.1`) para o celular. **Não ligue o Wi-Fi da central com o AA ativo**: isso derruba o AA.
- O **adb pela rede já vem ligado de fábrica** na porta 5555 (`service.adb.tcp.port=5555`).
- A porta USB funciona **só como host** (pendrive/iPhone); o adb por cabo normalmente não funciona.

Testado em: `JCAC10003-OC2-V1.1.71R7-250731_1740`, MCU `JCMM30`, AC8227L 2 GB/32 GB, tela 1024x600.
Em outras versões de firmware os nomes dos menus e as senhas podem mudar.

---

## ⚠️ Antes de começar

- Tudo aqui é **reversível**: nada de root, nada de apagar arquivos do sistema.
- **Nunca** grave firmware "Android 12/13/14" da internet nem atualize o MCU com arquivo de fora: é falso e
  pode deixar a central inutilizável. Nada de `fastboot` nem desbloqueio de bootloader.
- O adb da central fica aberto na rede **sem senha** para quem já foi autorizado. Conecte a central só em
  redes suas (o Wi-Fi de casa ou o roteador do seu celular).

---

## Passo a passo

### 1. Preparar o computador (Windows)

Abra o PowerShell e instale o adb:

```powershell
winget install Google.PlatformTools
```

Feche e abra o PowerShell de novo e confira:

```powershell
adb version
```

### 2. Liberar as Opções do desenvolvedor na central

O "Sobre o dispositivo" da Jancar é uma tela própria: tocar 7 vezes na versão **não funciona** nela.
É preciso abrir as **Configurações originais do Android**:

1. Na central: **Configurações > Configurações de fábrica**.
2. Digite a senha **`5678`**: ela abre as Configurações do Android (com "Apps e notificações", "Sistema"...).
   - `1234` abre o menu de fábrica do carro (CAN-bus, volante). Não mexa nele.
   - Se o `5678` não funcionar, teste: `56789`, `123456`, `7890` + hora atual (ex.: `789014`), a data
     (`AAAA.MM.DD`), `8888`, `000000`.
   - **Não teste** códigos de um dígito nem `4321`: em algumas centrais eles atualizam o MCU, resetam o rádio
     ou apagam arquivos.
3. Nas Configurações do Android: **Sistema > Sobre o tablet** e toque **7 vezes em "Número de compilação"**
   até aparecer "Você agora é um desenvolvedor".
4. Volte em **Sistema > Avançado > Opções do desenvolvedor** e ligue **Depuração USB**.
   Não precisa ligar "Desbloqueio de OEM" (é só para desbloquear bootloader; deixe desligado).

### 3. Conectar a central pela rede

1. Ligue a central e o computador **no mesmo Wi-Fi** (o de casa, com o carro na garagem, ou o roteador do celular).
2. Descubra o IP da central: **Configurações > Wi-Fi > toque na rede conectada**.
   - Ou procure pela rede com o script deste repositório (ele lista quem tem a porta 5555 aberta):
     ```powershell
     Set-ExecutionPolicy -Scope Process Bypass
     .\scripts\1-conectar.ps1 -Procurar
     ```
     TVs Android também abrem a 5555. Para ter certeza de qual é a central, compare o MAC com o que aparece
     em **Sobre o tablet > Status > Endereço MAC do Wi-Fi**.
3. Conecte:
   ```powershell
   adb connect 192.168.1.XX:5555
   adb devices
   ```
   Tem que aparecer `192.168.1.XX:5555   device`. Se aparecer `unauthorized`, aceite o
   "Permitir depuração USB?" na tela da central (marque "Sempre permitir").
4. Confira o Android real (`27` = Android 8.1):
   ```powershell
   adb shell getprop ro.build.version.sdk
   ```
   O script `.\scripts\1-conectar.ps1 -Ip 192.168.1.XX` faz os passos 3 e 4 e mostra modelo, CPU e RAM.

### 4. Fazer um backup (recomendado)

Antes de qualquer mudança, guarde o estado da central (só lê, não altera nada):

```powershell
.\scripts\2-backup.ps1
```

Ele salva em `scripts\backups\<data_hora>\` a lista de apps, `getprop`, `settings` (system/secure/global),
tela e memória. Essa pasta tem dados do seu carro e **não vai para o Git** (`.gitignore`).

### 5. Instalar os apps pela rede

```powershell
adb install -r release\DestravarBT.apk
```

Deve responder `Success`. Os dois ícones (🔵 **Destravar BT** e 🟢 **Reiniciar AA**) aparecem na lista de
aplicativos da central.

### 6. Primeiro uso

Toque em um dos ícones. Na primeira vez a central pode mostrar **"Permitir depuração USB?"** para a chave
do app: marque **"Sempre permitir"** e toque em **OK**. Isso não se repete.

A partir daí:
- **Saiu do Android Auto e a música BT não toca?** Toque em 🔵 **Destravar BT**.
- **O Android Auto começou a fechar sozinho?** Toque em 🟢 **Reiniciar AA** e espere uns 20 s.
- Se a música ainda não voltar: no celular, **Bluetooth > (nome da central) > ligue "Áudio"** e reconecte.

### 7. Remover

```powershell
adb uninstall br.central.destravarbt
```

Para desfazer as travas à mão, sem o app (troque o MAC pelo do seu celular):

```powershell
adb shell settings put global bluetooth_a2dp_src_priority_AA:BB:CC:DD:EE:FF -1
adb shell "settings put global phoneMode ''"
```

---

## Como funciona

- Um app comum não pode fechar apps do sistema nem mexer em `settings global`. Por isso o app **conecta no adb
  da própria central** (`127.0.0.1:5555`) com uma chave RSA própria e roda os comandos como o `adb shell`
  (`app/src/br/central/destravarbt/AdbLocal.java`, cliente do protocolo adb em Java puro, sem bibliotecas).
- Comandos usados: `am force-stop` (AA e CPlink), `settings put global bluetooth_a2dp_src_priority_* -1` só onde
  estiver `0`, e `settings put global phoneMode ''`.
- O **reinício do Bluetooth** é feito pelo próprio app (`BluetoothAdapter.disable()/enable()`, permissão
  `BLUETOOTH_ADMIN`), porque o `adb shell` desta central **não** tem permissão para isso. Ao religar,
  a Jancar reconecta o último celular sozinha. Com ligação em andamento, o app não reinicia o Bluetooth.
- **Canais de som (v1.7):** os dois ícones leem `dumpsys media.audio_flinger`. Com 11 ou mais canais ocupados no
  misturador principal, mostram o aviso de "som lotado" e o botão **Reiniciar a central** (o mesmo que
  `adb reboot`, pelo adb local). Nada é reiniciado sem o toque no botão.
- **Espião opcional:** se houver um diagnóstico instalado em `/sdcard/espiao/` (scripts próprios, não incluídos
  aqui), o app o religa quando a central liga e quando um ícone é tocado. Sem essa pasta, isso não faz nada.

### O que não dá para fazer sem root

- Impedir o Android Auto e o CPlink de abrirem sozinhos (o Android bloqueia `pm disable` de componentes pelo adb).
- Fechá-los ao limpar a multitarefa (rodam com a identidade do sistema, uid 1000).
- Mandar à Jancar o aviso "AA desconectado" (`action.android.auto.send`): o firmware bloqueia esse envio pelo adb.

---

## Compilar a partir do código

Não usa Gradle nem Android Studio: `aapt2 + javac + d8 + zipalign + apksigner`.

1. Instale o JDK 17 e o Android SDK:
   ```powershell
   winget install EclipseAdoptium.Temurin.17.JDK
   ```
   Baixe as *command line tools* em <https://developer.android.com/studio#command-tools>, extraia em
   `%LOCALAPPDATA%\Android\Sdk\cmdline-tools\latest\` e instale:
   ```powershell
   & "$env:LOCALAPPDATA\Android\Sdk\cmdline-tools\latest\bin\sdkmanager.bat" --licenses
   & "$env:LOCALAPPDATA\Android\Sdk\cmdline-tools\latest\bin\sdkmanager.bat" "build-tools;34.0.0" "platforms;android-28"
   ```
2. Compile:
   ```powershell
   .\app\compilar.ps1                          # cria uma chave nova em app\chave\ na primeira vez
   .\app\compilar.ps1 -PastaChave C:\...\chave # ou usa uma chave que você já tem
   ```
   O APK sai em `release\DestravarBT.apk`.

**Guarde a pasta da chave** (`destravar.jks` + `senha.txt`). Ela nunca vai para o Git, e sem ela não dá para
instalar uma versão nova por cima: seria preciso desinstalar o app e instalar de novo (e aceitar o
"Permitir depuração" outra vez).

---

## Perguntas frequentes

**Funciona na minha central?**
Foi feito e testado numa AC8227L com firmware Jancar `JCAC10003-OC2`. Se a sua tem o app **CPlink**, mostra o aviso
do **PhoneLink** e o `adb shell getprop ro.build.version.sdk` responde `27`, as chances são boas.
O app só mexe em configurações que já existem; se não servir, é só desinstalar.

**Precisa de root ou trocar o firmware?** Não. Tudo é feito pelo ADB que já vem na central.

**Preciso de cabo USB?** Não. A porta USB dessas centrais normalmente é só para pendrive; o ADB é pela rede Wi-Fi.

**Qual a senha das configurações de fábrica da central AC8227L?**
Para abrir as Configurações do Android (e as Opções do desenvolvedor) a senha que funcionou foi `5678`.
Veja outras no [passo 2](#2-liberar-as-opções-do-desenvolvedor-na-central).

**Por que o Android Auto da central fecha sozinho?**
Defeito do app de Android Auto de fábrica (versão `O.250702`): ele trava ao encerrar a sessão sem fio
(`play() called on uninitialized AudioTrack`). O app deste repositório não corrige o Android Auto em si
(isso só com atualização do fabricante), mas desfaz o estado que ele deixa e recomeça a sessão.

**Por que a música BT para depois do Android Auto?**
Porque o Android Auto desliga a música BT do celular (`bluetooth_a2dp_src_priority = 0`) e, ao travar,
não religa. O **Destravar BT** volta esse valor.

**Dá para impedir o CPlink e o Android Auto de abrirem sozinhos?**
Sem root, não: o sistema bloqueia. Veja [o que não dá para fazer sem root](#o-que-não-dá-para-fazer-sem-root).

---

## English

**Chinese Android head unit (Autochips AC8227L, Jancar firmware, CPlink / PhoneLink app): wireless Android Auto
keeps closing by itself, Bluetooth music stops working after Android Auto, and the head unit shows
"PhoneLink is connected, the current operation is not supported!".**

Cause: the stock Android Auto receiver (`com.google.android.projection.sink`, version `O.250702`) crashes every time
the wireless session ends (`IllegalStateException: play() called on uninitialized AudioTrack`). Because it crashes,
it never reverts what it changed: it leaves `settings global bluetooth_a2dp_src_priority_<phone MAC> = 0`
(the head unit rejects A2DP from your phone, `Incoming A2DP rejected`, even after reboot) and
`settings global phoneMode = auto_wireless` (Jancar blocks BT music with the PhoneLink warning).

Fix (no root, no firmware flashing): install `release/DestravarBT.apk` over Wi-Fi ADB. It adds two icons:
**Destravar BT** (unlock BT music: closes AA and CPlink, restores both settings, restarts Bluetooth once if
music doesn't come back) and **Reiniciar AA** (restart Android Auto from scratch). The app talks to the head
unit's own ADB daemon on `127.0.0.1:5555` with a tiny pure-Java ADB client.

Enable ADB: Settings > Factory settings > password `5678` (opens stock Android settings) > System > About >
tap "Build number" 7× > Developer options > USB debugging. Wi-Fi ADB on port 5555 is already on in this firmware:
`adb connect <head-unit-ip>:5555` then `adb install -r release/DestravarBT.apk`.

## Español

**Radio Android chino (Autochips AC8227L, firmware Jancar, app CPlink / PhoneLink): Android Auto inalámbrico se
cierra solo, la música Bluetooth deja de funcionar después de Android Auto y aparece
"PhoneLink está conectado, la operación actual no es compatible".**

Causa: el Android Auto de fábrica se cuelga al terminar la sesión inalámbrica y deja la prioridad A2DP del
teléfono en `0` y `phoneMode = auto_wireless`. Solución sin root: activar ADB (Configuración > Configuración de
fábrica > clave `5678` > Sistema > Acerca de > tocar 7 veces "Número de compilación" > Depuración USB),
conectar por Wi-Fi (`adb connect IP:5555`) e instalar `release/DestravarBT.apk`. Íconos: **Destravar BT**
(desbloquear música BT) y **Reiniciar AA** (reiniciar Android Auto).

---

## Estrutura

```
app/
  AndroidManifest.xml      dois ícones: MainActivity (Destravar BT) e ReiniciarAAActivity (Reiniciar AA)
  compilar.ps1             monta, assina e verifica o APK
  res/                     ícones e textos
  src/br/central/destravarbt/
    AdbLocal.java          cliente do protocolo adb (127.0.0.1:5555)
    TelaBase.java          janela comum, limpeza das travas, reinício do Bluetooth
    MainActivity.java      Destravar BT
    ReiniciarAAActivity.java  Reiniciar AA
docs/
  index.html               página do projeto (GitHub Pages)
release/
  DestravarBT.apk          pronto para instalar
scripts/
  _comum.ps1               funções usadas pelos scripts
  1-conectar.ps1           conecta (-Ip), procura na rede (-Procurar) e mostra o que a central é
  2-backup.ps1             fotografa o estado da central antes de mudanças
```
