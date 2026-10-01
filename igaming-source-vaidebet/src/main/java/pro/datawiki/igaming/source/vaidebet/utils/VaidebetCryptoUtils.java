package pro.datawiki.igaming.source.vaidebet.utils;

import java.nio.charset.StandardCharsets;

public class VaidebetCryptoUtils {

    /**
     * Decrypts the Digitain/Vaidebet API response.
     * The response is encrypted by XORing each UTF-8 byte of the JSON string
     * with a dynamic key derived from '[' (91) or '{' (123).
     *
     * @param encrypted The encrypted payload as a String
     * @return The decoded JSON String
     */
    public static String decrypt(String encrypted) {
        if (encrypted == null || encrypted.isEmpty()) {
            return encrypted;
        }

        String content = encrypted.trim();
        if (content.isEmpty()) {
            return encrypted;
        }

        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);

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
