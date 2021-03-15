package de.NikomitK.RaspiOpener.handler;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Decryption {
    private static final String ENCRYPT_ALGO = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;

    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    public static String decrypt(String key, String nonce, String msg) throws Exception {
        String hexKey = key;
        byte[] encodedKey = DatatypeConverter.parseHexBinary(hexKey);
        SecretKey secretKey = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");

        String hexNonce = nonce;
        byte[] byteNonce = DatatypeConverter.parseHexBinary(hexNonce);

        String hexMsgEncrypted = msg;
        byte[] encryptedText = DatatypeConverter.parseHexBinary(hexMsgEncrypted);

        Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BIT, byteNonce));
        byte[] plainText = cipher.doFinal(encryptedText);
        String decryptedText = new String(plainText, UTF_8);

        return decryptedText;
    }
}

