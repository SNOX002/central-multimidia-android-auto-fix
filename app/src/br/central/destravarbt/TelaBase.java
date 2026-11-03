package br.central.destravarbt;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Janela comum dos dois ícones: mostra o andamento, conecta no adb da própria central, roda trabalhar()
 * numa thread e fecha sozinha alguns segundos depois do resultado (a não ser que esteja oferecendo
 * reiniciar a central porque os canais de som lotaram).
 */
abstract class TelaBase extends Activity {

    /** Tudo que a central precisa ficar sem as travas do Android Auto travado. */
    static final String LIMPAR_TRAVAS =
        "for k in $(settings list global | grep '^bluetooth_a2dp_src_priority_' | grep '=0$' | cut -d= -f1); do "
        + "settings put global $k -1 && echo OK_BT ${k#bluetooth_a2dp_src_priority_}; done; "
        + "m=$(settings get global phoneMode); "
        + "if [ -n \"$m\" ] && [ \"$m\" != null ]; then settings put global phoneMode '' && echo OK_PHONELINK $m; fi; ";

    static final String FECHAR_AA_CPLINK =
        "am force-stop com.google.android.projection.sink && echo OK_AA; "
        + "am force-stop com.autochips.carplayapp && echo OK_CPLINK; ";

    static final String ESTADO_BT = "dumpsys bluetooth_manager 2>/dev/null | grep -m1 A2dpSinkStateMachine";

    /**
     * Religa o espião de diagnóstico (/sdcard/espiao, instalado pelo notebook) se ele estiver marcado como
     * ativo e tiver parado. Sem espião instalado, não faz nada.
     */
    static final String LIGAR_ESPIAO = "[ -f /sdcard/espiao/ativo ] && sh /sdcard/espiao/iniciar.sh || echo SEM_ESPIAO";

    /**
     * Canais de som abertos no misturador principal e quantos são do rádio da Jancar. O rádio (app persistente,
     * não dá para fechar) vaza canais ao longo dos dias; quando o misturador lota, o Android recusa o canal de
     * música do Android Auto ("no more track names available") e o AA trava ao tocar música.
     */
    static final String LER_CANAIS =
        "dumpsys media.audio_flinger 2>/dev/null | grep -m1 'Tracks of which'; r=$(pidof com.jancar.radio); "
        + "if [ -n \"$r\" ]; then echo RADIO=$(dumpsys media.audio_flinger 2>/dev/null | grep -cE \"^ +[0-9]+ +(yes|no) +$r \"); "
        + "else echo RADIO=0; fi";

    /** O AA travou com 14 canais ocupados e precisa de mais 3; a partir daqui o app avisa, com folga. */
    static final int LIMITE_CANAIS = 11;

    private static final int FECHAR_DEPOIS_MS = 4000;

    private final Handler ui = new Handler(Looper.getMainLooper());
    private TextView texto;
    private Button botaoReiniciar;
    private volatile boolean fecharSozinho = true;

    static final class Canais {
        int total = -1;
        int radio;

        boolean lotado() {
            return total >= LIMITE_CANAIS;
        }

        String descricao() {
            return total + " canais de som ocupados (" + radio + " presos pelo rádio)";
        }
    }

    abstract String titulo();

    /** Faz o trabalho do ícone; roda fora da thread da tela. */
    abstract void trabalhar(AdbLocal adb) throws Exception;

    @Override
    protected void onCreate(Bundle estado) {
        super.onCreate(estado);
        setFinishOnTouchOutside(false);

        LinearLayout tela = new LinearLayout(this);
        tela.setOrientation(LinearLayout.VERTICAL);
        int m = dp(24);
        tela.setPadding(m, m, m, m);

        TextView cabecalho = new TextView(this);
        cabecalho.setText(titulo());
        cabecalho.setTextSize(24);
        cabecalho.setTextColor(Color.WHITE);
        tela.addView(cabecalho);

        texto = new TextView(this);
        texto.setTextSize(18);
        texto.setTextColor(Color.WHITE);
        texto.setPadding(0, dp(12), 0, dp(12));
        texto.setText("Trabalhando...");
        tela.addView(texto, new LinearLayout.LayoutParams(dp(560), LinearLayout.LayoutParams.WRAP_CONTENT));

        botaoReiniciar = new Button(this);
        botaoReiniciar.setText("Reiniciar a central");
        botaoReiniciar.setTextSize(20);
        botaoReiniciar.setVisibility(View.GONE);
        botaoReiniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                botaoReiniciar.setEnabled(false);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        reiniciarCentral();
                    }
                }).start();
            }
        });
        tela.addView(botaoReiniciar, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(64)));

        Button botao = new Button(this);
        botao.setText("Fechar");
        botao.setTextSize(20);
        botao.setGravity(Gravity.CENTER);
        botao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tela.addView(botao, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(64)));

        setContentView(tela);
        new Thread(new Runnable() {
            @Override
            public void run() {
                executar();
            }
        }).start();
    }

    private void executar() {
        AdbLocal adb = new AdbLocal(getFilesDir());
        try {
            adb.conectar(new AdbLocal.Aviso() {
                @Override
                public void mensagem(String t) {
                    mostrar(t);
                }
            });
            trabalhar(adb);
            adb.shell(LIGAR_ESPIAO);
        } catch (Exception e) {
            mostrar("Não consegui falar com o adb da central:\n" + e
                + "\n\nO adb pela rede (porta 5555) precisa estar ligado.");
        } finally {
            adb.fechar();
        }
        if (!fecharSozinho) return;
        ui.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, FECHAR_DEPOIS_MS);
    }

    static Canais lerCanais(AdbLocal adb) throws Exception {
        Canais c = new Canais();
        for (String linha : adb.shell(LER_CANAIS).split("\n")) {
            Matcher m = Pattern.compile("(\\d+) Tracks of which").matcher(linha);
            if (m.find() && c.total < 0) c.total = Integer.parseInt(m.group(1));
            m = Pattern.compile("RADIO=(\\d+)").matcher(linha);
            if (m.find()) c.radio = Integer.parseInt(m.group(1));
        }
        return c;
    }

    /** Mostra o aviso com o botão "Reiniciar a central"; a janela deixa de fechar sozinha. */
    void oferecerReinicio(final String t) {
        fecharSozinho = false;
        ui.post(new Runnable() {
            @Override
            public void run() {
                texto.setText(t);
                botaoReiniciar.setVisibility(View.VISIBLE);
            }
        });
    }

    private void reiniciarCentral() {
        if (emLigacao()) {
            mostrar("Há uma ligação em andamento. Termine a ligação e toque de novo.");
            ui.post(new Runnable() {
                @Override
                public void run() {
                    botaoReiniciar.setEnabled(true);
                }
            });
            return;
        }
        mostrar("Reiniciando a central... ela volta em cerca de 1 minuto.");
        AdbLocal adb = new AdbLocal(getFilesDir());
        try {
            adb.conectar(new AdbLocal.Aviso() {
                @Override
                public void mensagem(String t) {
                    mostrar(t);
                }
            });
            adb.reiniciar();
        } catch (Exception e) {
            mostrar("Não consegui reiniciar a central:\n" + e
                + "\n\nReinicie pelo botão de reset ou desligando e ligando o carro.");
        } finally {
            adb.fechar();
        }
    }

    void mostrar(final String t) {
        ui.post(new Runnable() {
            @Override
            public void run() {
                texto.setText(t);
            }
        });
    }

    /** Traduz as marcas OK_* de LIMPAR_TRAVAS / FECHAR_AA_CPLINK em linhas para a tela. */
    static String resumo(String saida) {
        StringBuilder r = new StringBuilder();
        if (saida.contains("OK_AA")) r.append("✔ Android Auto fechado\n");
        if (saida.contains("OK_CPLINK")) r.append("✔ CPlink fechado\n");
        int bt = 0;
        boolean phonelink = false;
        for (String linha : saida.split("\n")) {
            if (linha.startsWith("OK_BT")) bt++;
            if (linha.startsWith("OK_PHONELINK")) {
                phonelink = true;
                r.append("✔ PhoneLink desfeito (estava ").append(linha.substring(13).trim()).append(")\n");
            }
        }
        if (!phonelink) r.append("✔ PhoneLink já estava livre\n");
        r.append(bt > 0 ? "✔ Música BT religada para " + bt + " celular(es)\n" : "✔ Música BT já estava liberada\n");
        return r.toString();
    }

    static boolean musicaConectada(String estado) {
        return estado.contains("A2dpSinkStateMachine state=Connected");
    }

    /** Confere a música BT por no máximo esperaMs. */
    static boolean musicaConectadaEm(AdbLocal adb, int esperaMs) throws Exception {
        long limite = System.currentTimeMillis() + esperaMs;
        while (System.currentTimeMillis() < limite) {
            if (musicaConectada(adb.shell(ESTADO_BT))) return true;
            Thread.sleep(1000);
        }
        return false;
    }

    boolean emLigacao() {
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        int modo = am.getMode();
        return modo == AudioManager.MODE_IN_CALL || modo == AudioManager.MODE_IN_COMMUNICATION
            || modo == AudioManager.MODE_RINGTONE;
    }

    /**
     * Desliga e religa o Bluetooth uma vez; ao religar, a Jancar reconecta o último celular sozinha.
     * Devolve null se deu certo, ou o motivo de não ter dado.
     */
    String reiniciarBluetooth() throws InterruptedException {
        BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
        if (bt == null) return "Não achei o Bluetooth da central.";
        if (bt.isEnabled()) {
            bt.disable();
            if (!esperarEstado(bt, BluetoothAdapter.STATE_OFF, 8000)) {
                bt.enable();
                return "A central não deixou desligar o Bluetooth.";
            }
        }
        bt.enable();
        if (!esperarEstado(bt, BluetoothAdapter.STATE_ON, 8000)) return "O Bluetooth ainda está ligando.";
        return null;
    }

    private static boolean esperarEstado(BluetoothAdapter bt, int estado, int esperaMs) throws InterruptedException {
        long limite = System.currentTimeMillis() + esperaMs;
        while (System.currentTimeMillis() < limite) {
            if (bt.getState() == estado) return true;
            Thread.sleep(300);
        }
        return bt.getState() == estado;
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
