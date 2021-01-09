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

// bestimmt nicht einfach kopiert
public class Decryption{
    public static void main(String [] args) throws Exception {
        //decrypt("fbd071c75ea09e05595770fa70a7d6d2faaf5002304e9f532e57e3c0ee8eb38c");
    }
    public static String decrypt(String key, String nonce, String msg) throws Exception {
        String hexKey = key;
        byte [] encodedKey = DatatypeConverter.parseHexBinary(hexKey);
        SecretKey secretKey = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");

        String hexNonce = nonce;
        byte [] byteNonce = DatatypeConverter.parseHexBinary(hexNonce);

        String hexMsgEncrypted = msg;
        byte [] encryptedText = DatatypeConverter.parseHexBinary(hexMsgEncrypted);

        String decryptedText = EncryptorAesGcm.decrypt(encryptedText, secretKey, byteNonce);
        System.out.println(decryptedText);

        return decryptedText;
    }
}

class CryptoUtils {






    // hex representation
    public static String hex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    // print hex with block size split
    public static String hexWithBlockSize(byte[] bytes, int blockSize) {

        String hex = hex(bytes);

        // one hex = 2 chars
        blockSize = blockSize * 2;

        // better idea how to print this?
        List<String> result = new ArrayList<>();
        int index = 0;
        while (index < hex.length()) {
            result.add(hex.substring(index, Math.min(index + blockSize, hex.length())));
            index += blockSize;
        }
        return result.toString();
    }

}

class EncryptorAesGcm {

    private static final String ENCRYPT_ALGO = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;

    private static final Charset UTF_8 = StandardCharsets.UTF_8;



    public static String decrypt(byte[] cText, SecretKey secret, byte[] iv) throws Exception {

        Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);
        cipher.init(Cipher.DECRYPT_MODE, secret, new GCMParameterSpec(TAG_LENGTH_BIT, iv));
        byte[] plainText = cipher.doFinal(cText);
        return new String(plainText, UTF_8);

    }

}
