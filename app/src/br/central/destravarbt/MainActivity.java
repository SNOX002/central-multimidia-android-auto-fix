package br.central.destravarbt;

/**
 * Ícone "Destravar BT": fecha o Android Auto e o CPlink e desfaz as travas que o Android Auto sem fio deixa
 * na central quando trava ao encerrar a sessão:
 *   1. bluetooth_a2dp_src_priority_<celular> = 0  -> a central recusa a música BT do celular;
 *   2. phoneMode = auto_wireless                  -> a Jancar mostra "PhoneLink está conectado...".
 * Depois confere por até 5 s se a música BT está conectada; se não estiver (e não houver ligação), reinicia o
 * Bluetooth da central uma vez, e a Jancar reconecta o último celular sozinha. Mostra o resultado e fecha.
 * Se o AA tinha sessão aberta, o celular pode soltar o Bluetooth uns 20 s depois; a Jancar também religa sozinha.
 * A terceira trava fica no celular ("Áudio" da central desligado no Bluetooth dele) e o app só lembra dela.
 */
public class MainActivity extends TelaBase {

    private static final int ESPERA_MUSICA_MS = 5000;

    @Override
    String titulo() {
        return "Destravar BT";
    }

    @Override
    void trabalhar(AdbLocal adb) throws Exception {
        mostrar("Fechando apps e destravando...");
        String saida = adb.shell(FECHAR_AA_CPLINK + LIMPAR_TRAVAS + "log -t DESTRAVAR_BT executado; echo FIM");
        StringBuilder r = new StringBuilder(resumo(saida));
        if (!saida.contains("OK_AA")) r.append("✘ Android Auto: não consegui fechar\n");
        if (!saida.contains("OK_CPLINK")) r.append("✘ CPlink: não consegui fechar\n");
        if (saida.contains("OK_PHONELINK")) r.append("\nSe o celular desconectar nos próximos segundos, a central religa sozinha.\n");

        mostrar(r + "\nConferindo o Bluetooth...");
        if (musicaConectadaEm(adb, ESPERA_MUSICA_MS)) {
            mostrar(r + "\n✔ Música BT conectada. Pode apertar play.");
        } else if (emLigacao()) {
            mostrar(r + "\nMúsica BT não conectada, mas há uma ligação em andamento.\nReconecte pelo celular depois.");
        } else {
            mostrar(r + "\nMúsica BT não conectada: reiniciando o Bluetooth da central...");
            String falha = reiniciarBluetooth();
            mostrar(r + "\n" + (falha == null
                ? "✔ Bluetooth reiniciado. A central vai reconectar o celular em alguns segundos.\n"
                    + "Se a música não voltar: no celular, Bluetooth > (nome da central) > ligue \"Áudio\"."
                : falha + " Reconecte pelo celular."));
        }
    }
}
