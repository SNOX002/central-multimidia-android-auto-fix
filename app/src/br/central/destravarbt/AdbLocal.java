package br.central.destravarbt;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import android.util.Base64;

/**
 * Cliente mínimo do protocolo adb, para o app rodar comandos de shell no adb da própria central
 * (127.0.0.1:5555). Assim o app tem as mesmas permissões do "adb shell" sem precisar de root.
 * Na primeira conexão a central pode pedir "Permitir depuração?" para a chave deste app.
 */
final class AdbLocal {

    interface Aviso {
        void mensagem(String texto);
    }

    private static final int CNXN = 0x4e584e43;
    private static final int AUTH = 0x48545541;
    private static final int OPEN = 0x4e45504f;
    private static final int OKAY = 0x59414b4f;
    private static final int CLSE = 0x45534c43;
    private static final int WRTE = 0x45545257;

    private static final int AUTH_TOKEN = 1;
    private static final int AUTH_SIGNATURE = 2;
    private static final int AUTH_RSAPUBLICKEY = 3;

    private static final int VERSAO = 0x01000000;
    private static final int MAX_DADOS = 4096;

    // Prefixo DigestInfo do SHA-1: o adbd verifica a assinatura do token como se ele fosse um hash SHA-1.
    private static final byte[] PREFIXO_SHA1 = {
        0x30, 0x21, 0x30, 0x09, 0x06, 0x05, 0x2b, 0x0e, 0x03, 0x02, 0x1a, 0x05, 0x00, 0x04, 0x14
    };

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private final File pastaChave;
    private int proximoId = 1;
    private Socket socket;
    private DataInputStream entrada;
    private OutputStream saida;

    AdbLocal(File pastaChave) {
        this.pastaChave = pastaChave;
    }

    void conectar(Aviso aviso) throws Exception {
        KeyPair chave = carregarOuCriarChave();
        socket = new Socket();
        socket.connect(new InetSocketAddress("127.0.0.1", 5555), 3000);
        socket.setSoTimeout(10000);
        entrada = new DataInputStream(socket.getInputStream());
        saida = socket.getOutputStream();

        enviar(CNXN, VERSAO, MAX_DADOS, "host::\0".getBytes(UTF8));
        boolean assinou = false;
        while (true) {
            Mensagem m = receber();
            if (m.comando == CNXN) {
                socket.setSoTimeout(15000);
                return;
            }
            if (m.comando != AUTH || m.arg0 != AUTH_TOKEN) {
                throw new IOException("Resposta inesperada do adb: " + Integer.toHexString(m.comando));
            }
            if (!assinou) {
                enviar(AUTH, AUTH_SIGNATURE, 0, assinar(chave.getPrivate(), m.dados));
                assinou = true;
            } else {
                // A central não conhece esta chave: manda a chave pública e espera o "Permitir" na tela.
                aviso.mensagem("Na tela da central, toque em PERMITIR\n(marque \"Sempre permitir deste computador\").");
                enviar(AUTH, AUTH_RSAPUBLICKEY, 0, chavePublicaAdb((RSAPublicKey) chave.getPublic()));
                socket.setSoTimeout(60000);
            }
        }
    }

    /** Roda um comando no shell da central e devolve a saída completa. */
    String shell(String comando) throws IOException {
        final int idLocal = proximoId++;
        enviar(OPEN, idLocal, 0, ("shell:" + comando + "\0").getBytes(UTF8));
        ByteArrayOutputStream texto = new ByteArrayOutputStream();
        int idRemoto = 0;
        while (true) {
            Mensagem m = receber();
            if (m.arg1 != idLocal) continue; // resto de um comando anterior
            if (m.comando == OKAY) {
                idRemoto = m.arg0;
            } else if (m.comando == WRTE) {
                idRemoto = m.arg0;
                texto.write(m.dados);
                enviar(OKAY, idLocal, idRemoto, new byte[0]);
            } else if (m.comando == CLSE) {
                enviar(CLSE, idLocal, idRemoto, new byte[0]);
                return new String(texto.toByteArray(), UTF8);
            }
        }
    }

    /** Reinicia a central (o mesmo que "adb reboot"). A conexão cai logo em seguida. */
    void reiniciar() throws IOException {
        enviar(OPEN, proximoId++, 0, "reboot:\0".getBytes(UTF8));
        try {
            receber();
        } catch (IOException esperado) {
            // A central começa a desligar e derruba a conexão.
        }
    }

    void fechar() {
        try {
            if (socket != null) socket.close();
        } catch (IOException ignorado) {
        }
    }

    // --- protocolo ---

    private static final class Mensagem {
        int comando, arg0, arg1;
        byte[] dados;
    }

    private void enviar(int comando, int arg0, int arg1, byte[] dados) throws IOException {
        ByteBuffer cabecalho = ByteBuffer.allocate(24).order(ByteOrder.LITTLE_ENDIAN);
        int soma = 0;
        for (byte b : dados) soma += b & 0xff;
        cabecalho.putInt(comando).putInt(arg0).putInt(arg1).putInt(dados.length).putInt(soma).putInt(~comando);
        saida.write(cabecalho.array());
        saida.write(dados);
        saida.flush();
    }

    private Mensagem receber() throws IOException {
        byte[] cab = new byte[24];
        entrada.readFully(cab);
        ByteBuffer b = ByteBuffer.wrap(cab).order(ByteOrder.LITTLE_ENDIAN);
        Mensagem m = new Mensagem();
        m.comando = b.getInt();
        m.arg0 = b.getInt();
        m.arg1 = b.getInt();
        int tamanho = b.getInt();
        m.dados = new byte[tamanho];
        entrada.readFully(m.dados);
        return m;
    }

    // --- chave RSA do app ---

    private KeyPair carregarOuCriarChave() throws Exception {
        File priv = new File(pastaChave, "adbkey.pk8");
        File pub = new File(pastaChave, "adbkey.x509");
        KeyFactory kf = KeyFactory.getInstance("RSA");
        if (priv.exists() && pub.exists()) {
            return new KeyPair(
                kf.generatePublic(new X509EncodedKeySpec(ler(pub))),
                kf.generatePrivate(new PKCS8EncodedKeySpec(ler(priv))));
        }
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair par = gen.generateKeyPair();
        gravar(priv, par.getPrivate().getEncoded());
        gravar(pub, par.getPublic().getEncoded());
        return par;
    }

    private static byte[] assinar(PrivateKey chave, byte[] token) throws Exception {
        byte[] bloco = new byte[PREFIXO_SHA1.length + token.length];
        System.arraycopy(PREFIXO_SHA1, 0, bloco, 0, PREFIXO_SHA1.length);
        System.arraycopy(token, 0, bloco, PREFIXO_SHA1.length, token.length);
        Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        c.init(Cipher.ENCRYPT_MODE, chave);
        return c.doFinal(bloco);
    }

    /** Chave pública no formato do adb (struct RSAPublicKey do mincrypt, em base64) + nome. */
    private static byte[] chavePublicaAdb(RSAPublicKey chave) {
        final int palavras = 64; // 2048 bits / 32
        BigInteger n = chave.getModulus();
        BigInteger r32 = BigInteger.ONE.shiftLeft(32);
        BigInteger n0inv = n.mod(r32).modInverse(r32).negate().mod(r32);
        BigInteger rr = BigInteger.ONE.shiftLeft(2048).modPow(BigInteger.valueOf(2), n);

        ByteBuffer b = ByteBuffer.allocate(4 + 4 + 256 + 256 + 4).order(ByteOrder.LITTLE_ENDIAN);
        b.putInt(palavras);
        b.putInt(n0inv.intValue());
        b.put(littleEndian(n, 256));
        b.put(littleEndian(rr, 256));
        b.putInt(chave.getPublicExponent().intValue());
        String texto = Base64.encodeToString(b.array(), Base64.NO_WRAP) + " destravar-bt@central\0";
        return texto.getBytes(UTF8);
    }

    private static byte[] littleEndian(BigInteger valor, int tamanho) {
        byte[] be = valor.toByteArray();
        byte[] le = new byte[tamanho];
        for (int i = 0; i < tamanho && i < be.length; i++) {
            le[i] = be[be.length - 1 - i];
        }
        return le;
    }

    private static byte[] ler(File f) throws IOException {
        InputStream in = new FileInputStream(f);
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            return out.toByteArray();
        } finally {
            in.close();
        }
    }

    private static void gravar(File f, byte[] dados) throws IOException {
        FileOutputStream out = new FileOutputStream(f);
        try {
            out.write(dados);
        } finally {
            out.close();
        }
    }
}
