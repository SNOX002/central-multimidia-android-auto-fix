package br.central.destravarbt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Quando a central liga, religa o espião de diagnóstico (se estiver instalado e ativo). O espião roda como
 * "adb shell" e por isso morre quando a central reinicia; o app usa o adb da própria central para religá-lo.
 * O adb pode demorar a subir depois do boot, então tenta por até ~45 s.
 */
public class InicioReceiver extends BroadcastReceiver {

    private static final int TENTATIVAS = 9;
    private static final int INTERVALO_MS = 5000;

    @Override
    public void onReceive(final Context contexto, Intent intencao) {
        final PendingResult resultado = goAsync();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < TENTATIVAS; i++) {
                        AdbLocal adb = new AdbLocal(contexto.getFilesDir());
                        try {
                            adb.conectar(new AdbLocal.Aviso() {
                                @Override
                                public void mensagem(String t) {
                                }
                            });
                            adb.shell(TelaBase.LIGAR_ESPIAO);
                            return;
                        } catch (Exception e) {
                            Thread.sleep(INTERVALO_MS);
                        } finally {
                            adb.fechar();
                        }
                    }
                } catch (InterruptedException ignorado) {
                } finally {
                    resultado.finish();
                }
            }
        }).start();
    }
}
