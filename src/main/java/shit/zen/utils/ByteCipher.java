package shit.zen.utils;

import java.security.SecureRandom;
import java.util.Arrays;

public class ByteCipher {
    private final byte[] key;

    public ByteCipher(byte[] byArray) {
        this.key = Arrays.copyOf(byArray, byArray.length);
    }

    public byte[] encrypt(byte[] byArray, int n) {
        byte[] byArray2 = new byte[byArray.length];
        for (int i = 0; i < byArray.length; ++i) {
            byArray2[i] = (byte)(byArray[i] + this.key[i % this.key.length] + n);
        }
        return byArray2;
    }

    public byte[] decrypt(byte[] byArray, int n) {
        byte[] byArray2 = new byte[byArray.length];
        for (int i = 0; i < byArray.length; ++i) {
            byArray2[i] = (byte)(byArray[i] - this.key[i % this.key.length] - n);
        }
        return byArray2;
    }

    static {
        new SecureRandom();
    }
}