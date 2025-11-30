package dev.educore.eduxel.security;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Minimal placeholder crypto helper. For now we simply Base64-encode/decode
 * the secret to avoid storing it in plain text. Can be replaced with
 * proper OS-keystore backed encryption later.
 */
public final class CryptoUtils {
    private CryptoUtils() {}

    public static String encryptToBase64(String plain) {
        if (plain == null) return "";
        return Base64.getEncoder().encodeToString(plain.getBytes(StandardCharsets.UTF_8));
    }

    public static String decryptFromBase64(String encoded) {
        if (encoded == null || encoded.isBlank()) return "";
        byte[] bytes = Base64.getDecoder().decode(encoded);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
