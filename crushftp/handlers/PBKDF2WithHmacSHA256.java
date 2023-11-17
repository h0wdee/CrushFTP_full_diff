/*
 * Decompiled with CFR 0.152.
 */
package crushftp.handlers;

import crushftp.handlers.Common;
import crushftp.handlers.Log;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PBKDF2WithHmacSHA256 {
    public static void main(String[] args) {
        System.out.println(PBKDF2WithHmacSHA256.hashpw("letmein01!", "E6FCD958D680E64CC4F3F0430ACB7175F2BD43EDFDBD198F69E31E96587A8F7657C4293F0BBB09BE96B6FA5CA89EEC44"));
    }

    public static String hashpw(String plaintext, String hashed) {
        return PBKDF2WithHmacSHA256.getEncryptedPassword(plaintext, com.crushftp.client.Common.hexToBytes(hashed.substring(0, hashed.length() - 64)), 5000, 32);
    }

    public static String buildhash(String plaintext) {
        return "PBKDF2SHA256:" + PBKDF2WithHmacSHA256.getEncryptedPassword(plaintext, Common.makeBoundary(16).getBytes(), 5000, 32);
    }

    public static String getEncryptedPassword(String password, byte[] salt, int iterations, int derivedKeyLength) {
        try {
            return String.valueOf(com.crushftp.client.Common.bytesToHex(salt)) + com.crushftp.client.Common.bytesToHex(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(new PBEKeySpec(password.toCharArray(), salt, iterations, derivedKeyLength * 8)).getEncoded());
        }
        catch (Exception e) {
            Log.log("SERVER", 1, e);
            return null;
        }
    }
}

