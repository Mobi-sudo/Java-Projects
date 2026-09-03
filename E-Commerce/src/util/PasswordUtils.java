package util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.security.MessageDigest;

import java.util.Base64;

public class PasswordUtils {
    private static final String ALGO = "PBKDF2WithHmacSHA256";
    private static final int SALT_LEN = 16;
    private static final int ITERATIONS = 100_000; // reasonable default
    private static final int KEY_LENGTH = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    // Stored format: base64(salt) + $ + iterations + $ + base64(hash)
    public static String hashPassword(String password) {
        if (password == null) throw new IllegalArgumentException("Password cannot be null");
        try {
            byte[] salt = new byte[SALT_LEN];
            RANDOM.nextBytes(salt);
            byte[] hash = pbkdf2(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            return Base64.getEncoder().encodeToString(salt) + "$" + ITERATIONS + "$" + Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }

    public static boolean verifyPassword(String password, String stored) {
        if (password == null || stored == null) return false;
        try {
            String[] parts = stored.split("\\$");
            if (parts.length != 3) return false;
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            int iterations = Integer.parseInt(parts[1]);
            byte[] expected = Base64.getDecoder().decode(parts[2]);
            if (salt.length != SALT_LEN || iterations < 10_000 || iterations > 10_000_000 || expected.length == 0) {
                return false;
            }
            byte[] actual = pbkdf2(password.toCharArray(), salt, iterations, expected.length * 8);
            return MessageDigest.isEqual(actual, expected);
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLength) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGO);
        return skf.generateSecret(spec).getEncoded();
    }
}
