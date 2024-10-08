package CRM.project.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class EncryptionUtil {
    private static final String SECRET_KEY = "1234567812345678"; // Must match the key used in JavaScript
    private static final String IV = "1234567812345678"; // Must match the IV used in JavaScript
    private static final String ALGORITHM = "AES";

    public static String encrypt(String dataToEncrypt) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes("UTF-8"), ALGORITHM);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(IV.getBytes("UTF-8"));

        Cipher cipher = Cipher.getInstance(ALGORITHM + "/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);

        byte[] encryptedData = cipher.doFinal(dataToEncrypt.getBytes("UTF-8"));

        return Base64.getEncoder().encodeToString(encryptedData);
    }
    public static String decrypt(String encryptedData) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes("UTF-8"), ALGORITHM);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(IV.getBytes("UTF-8")); // Use the same IV

        Cipher cipher = Cipher.getInstance(ALGORITHM + "/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);

        byte[] decryptedData = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decryptedData, "UTF-8");
    }
}

