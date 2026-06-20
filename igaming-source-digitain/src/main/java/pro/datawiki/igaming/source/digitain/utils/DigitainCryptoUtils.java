package pro.datawiki.igaming.source.digitain.utils;

import java.nio.charset.StandardCharsets;

public class DigitainCryptoUtils {

    /**
     * Decrypts the Digitain API response.
     * The response is encrypted by XORing each UTF-8 byte of the JSON string
     * with a dynamic key. The key is derived from the fact that the resulting 
     * JSON string always begins with '[' (91) or '{' (123).
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

        // The key is found by XORing the first byte with '[' (91)
        int key = bytes[0] ^ '[';
        
        // If the key is out of expected ASCII bounds, it might be a JSON object starting with '{' (123)
        if (key > 127 || key <= 0) {
            key = bytes[0] ^ '{';
        }

        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) (bytes[i] ^ key);
        }

        return new String(bytes, StandardCharsets.UTF_8);
    }
}
