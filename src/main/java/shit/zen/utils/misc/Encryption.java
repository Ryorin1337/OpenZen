package shit.zen.utils.misc;

import shit.zen.utils.ByteCipher;

public class Encryption {
    public enum Algorithm {
        AES(new byte[16]),
        DES(new byte[8]);

        public final byte[] iv;

        Algorithm(byte[] iv) {
            this.iv = iv;
        }
    }

    private final ByteCipher cipher;
    public final Algorithm algo;
    public int keyLength = 0;

    public Encryption(Algorithm algorithm) {
        this.algo = algorithm;
        this.cipher = new ByteCipher(algorithm.iv);
    }

    public String encryptString(String s) throws Exception {
        return s;
    }

    public byte[] encryptBytes(byte[] b) throws Exception {
        return b;
    }

    public String decryptString(String s) throws Exception {
        return s;
    }

    public byte[] decryptBytes(byte[] b) throws Exception {
        return b;
    }
}
