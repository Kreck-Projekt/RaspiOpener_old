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
        decrypt("fbd071c75ea09e05595770fa70a7d6d2faaf5002304e9f532e57e3c0ee8eb38c");
    }
    public static String decrypt(String msg) throws Exception {
        String hexKey = msg;
        byte [] encodedKey = DatatypeConverter.parseHexBinary(hexKey);
        SecretKey secretKey = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");

        String hexNonce = "1e2e0b467ac613a9909f61c1";
        byte [] byteNonce = DatatypeConverter.parseHexBinary(hexNonce);

        String hexMsgEncrypted = "4d5a57c461d97df83bebdc98237e64d19f41a70637e53fed48b8a7f87ff3bdc6f0e7e3ffdb1a809ac38c75ccdac32e11";
        byte [] encryptedText = DatatypeConverter.parseHexBinary(hexMsgEncrypted);

        String decryptedText = EncryptorAesGcm.decrypt(encryptedText, secretKey, byteNonce);
        System.out.println(decryptedText);

        return decryptedText;
    }
}

class CryptoUtils {
    // AES secret key
    public static SecretKey getAESKey(int keysize) throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(keysize, SecureRandom.getInstanceStrong());
        return keyGen.generateKey();
    }

    // Password derived AES 256 bits secret key
    public static SecretKey getAESKeyFromPassword(char[] password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        // iterationCount = 65536
        // keyLength = 256
        KeySpec spec = new PBEKeySpec(password, salt, 65536, 256);
        SecretKey secret = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
        return secret;

    }

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

    // AES-GCM needs GCMParameterSpec
    public static byte[] encrypt(byte[] pText, SecretKey secret, byte[] iv) throws Exception {

        Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);
        cipher.init(Cipher.ENCRYPT_MODE, secret, new GCMParameterSpec(TAG_LENGTH_BIT, iv));
        byte[] encryptedText = cipher.doFinal(pText);
        return encryptedText;

    }

    public static String decrypt(byte[] cText, SecretKey secret, byte[] iv) throws Exception {

        Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);
        cipher.init(Cipher.DECRYPT_MODE, secret, new GCMParameterSpec(TAG_LENGTH_BIT, iv));
        byte[] plainText = cipher.doFinal(cText);
        return new String(plainText, UTF_8);

    }

    public static byte[] int2byte(int[]src) {
        int srcLength = src.length;
        byte[]dst = new byte[srcLength << 2];

        for (int i=0; i<srcLength; i++) {
            int x = src[i];
            int j = i << 2;
            dst[j++] = (byte) ((x >>> 0) & 0xff);
            dst[j++] = (byte) ((x >>> 8) & 0xff);
            dst[j++] = (byte) ((x >>> 16) & 0xff);
            dst[j++] = (byte) ((x >>> 24) & 0xff);
        }
        return dst;
    }

    public static void main(String[] args) throws Exception {

        String OUTPUT_FORMAT = "%-30s:%s";
        System.out.println("Vorlage");
        String hexKey = "fbd071c75ea09e05595770fa70a7d6d2faaf5002304e9f532e57e3c0ee8eb38c";
        byte [] encodedKey = DatatypeConverter.parseHexBinary(hexKey);
        SecretKey secretKey = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");

        String hexNonce = "1e2e0b467ac613a9909f61c1";
        byte [] byteNonce = DatatypeConverter.parseHexBinary(hexNonce);

        String hexMsgEncrypted = "4d5a57c461d97df83bebdc98237e64d19f41a70637e53fed48b8a7f87ff3bdc6f0e7e3ffdb1a809ac38c75ccdac32e11";
        byte [] encryptedText = DatatypeConverter.parseHexBinary(hexMsgEncrypted);


        System.out.println("\n------ AES GCM Decryption ------");
        System.out.println(String.format(OUTPUT_FORMAT, "Input (hex)", CryptoUtils.hex(encryptedText)));
        System.out.println(String.format(OUTPUT_FORMAT, "Input (hex) (block = 16)", CryptoUtils.hexWithBlockSize(encryptedText, 16)));
        System.out.println(String.format(OUTPUT_FORMAT, "Key (hex)", CryptoUtils.hex(secretKey.getEncoded())));

//        String decryptedText = EncryptorAesGcm.decryptWithPrefixIV(encryptedText, secretKey);
//        System.out.println(String.format(OUTPUT_FORMAT, "Decrypted (plain text)", decryptedText));
        String decryptedText = EncryptorAesGcm.decrypt(encryptedText, secretKey, byteNonce);
        System.out.println(decryptedText);

    }
}
