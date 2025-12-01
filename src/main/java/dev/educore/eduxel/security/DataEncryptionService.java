package dev.educore.eduxel.security;

import dev.educore.eduxel.config.ClientConfig;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Field-level encryption service using AES-GCM.
 *
 * - Key is derived from the client secret (ClientConfig.getSecretPlain()) via PBKDF2-HMAC-SHA256.
 * - Each value uses a random 12-byte IV and 128-bit auth tag.
 * - Output format: "enc:v1:" + Base64( IV(12) || CIPHERTEXT || TAG(16) ).
 * - Backward compatible: values without the prefix are treated as plaintext and returned as-is from decrypt().
 */
public final class DataEncryptionService {
    private static final String PREFIX = "enc:v1:";
    private static final int GCM_TAG_BITS = 128;
    private static final int IV_LEN = 12;
    private static final int PBKDF2_ITERATIONS = 120_000;
    private static final int KEY_BITS = 256;

    private static final SecureRandom RNG = new SecureRandom();

    // Cached derived key; recomputed lazily when null.
    private static volatile SecretKeySpec cachedKey;

    private DataEncryptionService() { }

    private static SecretKeySpec getKey() {
        SecretKeySpec local = cachedKey;
        if (local != null) return local;
        synchronized (DataEncryptionService.class) {
            if (cachedKey != null) return cachedKey;
            String secret = ClientConfig.load().getSecretPlain();
            if (secret == null || secret.isBlank()) {
                throw new IllegalStateException("Client secret ist nicht konfiguriert");
            }
            // Use a fixed app-specific salt string to derive a stable key from the secret.
            // This can be changed to an OS-keystore protected key in the future.
            byte[] salt = "eduxel-desktop-app-kdf-salt".getBytes(StandardCharsets.UTF_8);
            try {
                PBEKeySpec spec = new PBEKeySpec(secret.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_BITS);
                SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
                byte[] keyBytes = skf.generateSecret(spec).getEncoded();
                cachedKey = new SecretKeySpec(keyBytes, "AES");
                return cachedKey;
            } catch (Exception e) {
                throw new IllegalStateException("Konnte Schlüssel nicht ableiten: " + e.getMessage(), e);
            }
        }
    }

    public static String encryptNullable(String plain) {
        if (plain == null) return null;
        if (plain.isEmpty()) return PREFIX + Base64.getEncoder().encodeToString(new byte[0]);
        try {
            byte[] iv = new byte[IV_LEN];
            RNG.nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, getKey(), new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] cipherText = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));

            // cipherText contains both C and tag at the end (GCM). We store iv || cipherText
            ByteBuffer bb = ByteBuffer.allocate(iv.length + cipherText.length);
            bb.put(iv);
            bb.put(cipherText);
            String b64 = Base64.getEncoder().encodeToString(bb.array());
            return PREFIX + b64;
        } catch (Exception e) {
            throw new IllegalStateException("Fehler beim Verschlüsseln: " + e.getMessage(), e);
        }
    }

    public static String decryptNullable(String stored) {
        if (stored == null) return null;
        if (!stored.startsWith(PREFIX)) {
            // legacy plaintext (vor Migration) beibehalten
            return stored;
        }
        String payload = stored.substring(PREFIX.length());
        if (payload.isEmpty()) return "";
        try {
            byte[] all = Base64.getDecoder().decode(payload);
            if (all.length < IV_LEN + 16) {
                // minimal length check (IV + tag)
                throw new IllegalStateException("Ungültiges Chiffrat");
            }
            byte[] iv = new byte[IV_LEN];
            byte[] ct = new byte[all.length - IV_LEN];
            System.arraycopy(all, 0, iv, 0, IV_LEN);
            System.arraycopy(all, IV_LEN, ct, 0, ct.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, getKey(), new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] plain = cipher.doFinal(ct);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Fehler beim Entschlüsseln: " + e.getMessage(), e);
        }
    }
}
