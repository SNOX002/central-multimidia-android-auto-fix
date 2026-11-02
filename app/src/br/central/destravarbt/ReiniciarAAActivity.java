package br.central.destravarbt;

import android.content.Intent;

/**
 * Ícone "Reiniciar AA": para quando o Android Auto começa a fechar sozinho. Recomeça a sessão do zero:
 *   1. fecha o Android Auto travado e o CPlink;
 *   2. limpa as travas que ele deixa (PhoneLink e música BT bloqueada);
 *   3. reinicia o Bluetooth da central (sem ligação em andamento), para o celular reconectar e o AA sem fio
 *      começar uma sessão nova - o AA acorda sozinho quando o celular conecta pelo Bluetooth;
 *   4. abre a tela do Android Auto.
 */
public class ReiniciarAAActivity extends TelaBase {

    private static final String PACOTE_AA = "com.google.android.projection.sink";

    @Override
    String titulo() {
        return "Reiniciar Android Auto";
    }

    @Override
    void trabalhar(AdbLocal adb) throws Exception {
        mostrar("Fechando o Android Auto...");
        String saida = adb.shell(FECHAR_AA_CPLINK + LIMPAR_TRAVAS + "log -t REINICIAR_AA executado; echo FIM");
        StringBuilder r = new StringBuilder(resumo(saida));
        if (!saida.contains("OK_AA")) r.append("✘ Android Auto: não consegui fechar\n");

        if (emLigacao()) {
            r.append("Ligação em andamento: não reiniciei o Bluetooth.\n");
        } else {
            mostrar(r + "\nReiniciando o Bluetooth da central...");
            String falha = reiniciarBluetooth();
            r.append(falha == null ? "✔ Bluetooth reiniciado\n" : falha + "\n");
        }

        Intent aa = getPackageManager().getLaunchIntentForPackage(PACOTE_AA);
        if (aa != null) {
            aa.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(aa);
            r.append("✔ Android Auto aberto\n");
        } else {
            r.append("✘ Não achei o Android Auto para abrir\n");
        }
        mostrar(r + "\nAguarde o celular conectar (pode levar uns 20 s).");
    }
}
