package pro.datawiki.igaming.source.vbet.utils;

import java.nio.charset.StandardCharsets;

public class VbetCryptoUtils {

    /**
     * Decrypts the Digitain/Vbet API response if encrypted.
     * The response is XORed UTF-8 byte stream where the first byte XOR '[' or '{' reveals the key.
     */
    public static String decrypt(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        String trimmed = content.trim();
        if (trimmed.startsWith("[") || trimmed.startsWith("{")) {
            return trimmed; // Already plaintext JSON
        }

        byte[] bytes = trimmed.getBytes(StandardCharsets.UTF_8);
        int key = bytes[0] ^ '[';
        if (key > 127 || key <= 0) {
            key = bytes[0] ^ '{';
        }

        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) (bytes[i] ^ key);
        }

        return new String(bytes, StandardCharsets.UTF_8);
    }
}
