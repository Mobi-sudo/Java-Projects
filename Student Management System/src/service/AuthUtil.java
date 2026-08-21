package service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;

public class AuthUtil {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int SALT_LEN = 16; // bytes
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256; // bits

    public static byte[] generateSalt() {
        byte[] salt = new byte[SALT_LEN];
        RANDOM.nextBytes(salt);
        return salt;
    }

    public static byte[] hashPassword(char[] password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = skf.generateSecret(spec).getEncoded();
            spec.clearPassword();
            return hash;
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }

    public static boolean verifyPassword(char[] attempted, byte[] salt, byte[] expectedHash) {
        byte[] attemptHash = hashPassword(attempted, salt);
        if (attemptHash.length != expectedHash.length) return false;
        int res = 0;
        for (int i = 0; i < attemptHash.length; i++) res |= (attemptHash[i] ^ expectedHash[i]);
        return res == 0;
    }
}
