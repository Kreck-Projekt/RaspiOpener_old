import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.DatatypeConverter;

public class Decryption{
    private static final String ENCRYPT_ALGO = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;

    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    public static String decrypt(String key, String nonce, String msg) throws Exception {
        String hexKey = key;
        byte [] encodedKey = DatatypeConverter.parseHexBinary(hexKey);
        SecretKey secretKey = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");

        String hexNonce = nonce;
        byte [] byteNonce = DatatypeConverter.parseHexBinary(hexNonce);

        String hexMsgEncrypted = msg;
        byte [] encryptedText = DatatypeConverter.parseHexBinary(hexMsgEncrypted);

        Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BIT, byteNonce));
        byte[] plainText = cipher.doFinal(encryptedText);
        String decryptedText = new String(plainText, UTF_8);
        System.out.println(decryptedText);

        return decryptedText;
    }




    public static String decrypt(byte[] cText, SecretKey secret, byte[] iv) throws Exception {

        Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);
        cipher.init(Cipher.DECRYPT_MODE, secret, new GCMParameterSpec(TAG_LENGTH_BIT, iv));
        byte[] plainText = cipher.doFinal(cText);
        return new String(plainText, UTF_8);

    }
}


class EncryptorAesGcm {



}
