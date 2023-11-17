/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bouncycastle.crypto.generators.Argon2BytesGenerator
 *  org.bouncycastle.crypto.params.Argon2Parameters$Builder
 */
package crushftp.handlers;

import com.crushftp.client.Base64;
import com.crushftp.client.Common;
import com.crushftp.client.File_S;
import crushftp.handlers.Log;
import crushftp.server.ServerStatus;
import java.io.IOException;
import java.math.BigInteger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;

public class DesEncrypter {
    Cipher ecipher;
    Cipher dcipher;

    public DesEncrypter(String key, boolean base64) {
        try {
            key = Common.getHash(key, base64, "SHA", "", "", ServerStatus.BG("sha3_keccak"));
            this.doInit(key);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public DesEncrypter(String key) {
        try {
            key = Common.getHash(key, false, "SHA", "", "", ServerStatus.BG("sha3_keccak"));
            this.doInit(key);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public void doInit(String key) throws Exception {
        while ((float)key.length() / 8.0f != (float)(key.length() / 8)) {
            key = String.valueOf(key) + "Z";
        }
        DESKeySpec desKeySpec = new DESKeySpec(key.getBytes());
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
        this.ecipher = Cipher.getInstance("DES");
        this.dcipher = Cipher.getInstance("DES");
        this.ecipher.init(1, secretKey);
        this.dcipher.init(2, secretKey);
    }

    public String encrypt(String str, String method, boolean base64, String salt) {
        if (ServerStatus.thisObj != null) {
            if (ServerStatus.SG("password_salt_location") != null) {
                if (!ServerStatus.SG("password_salt_location").equals("")) {
                    if (!new File_S(ServerStatus.SG("password_salt_location")).exists()) {
                        Log.log("SERVER", 0, "Password SALT file could not be located, logins may fail...:" + ServerStatus.SG("password_salt_location"));
                    }
                }
            }
        }
        if (method.equals("SHA") || str.startsWith("SHA:") || method.equals("SHA512") || method.equals("SHA256") || str.startsWith("SHA512:") || str.startsWith("SHA256:") || method.equals("SHA3") || str.startsWith("SHA3:") || method.equals("MD5") || str.startsWith("MD5:") || str.startsWith("MD5S2:") || method.equals("MD5S2") || method.equals("MD4") || str.startsWith("MD4:") || method.equals("SHA3_KECCAK") || str.startsWith("SHA3_KECCAK:")) {
            try {
                if (str.startsWith("SHA:") || str.startsWith("SHA512:") || str.startsWith("SHA256:") || str.startsWith("SHA3:") || str.startsWith("MD5:") || str.startsWith("MD5S2:") || str.startsWith("MD4:") || str.startsWith("CRYPT3:") || str.startsWith("BCRYPT:") || str.startsWith("MD5CRYPT:") || str.startsWith("PBKDF2SHA256:") || str.startsWith("SHA512CRYPT:") || str.startsWith("SHA3_KECCAK:")) {
                    return str;
                }
                if (base64) {
                    return String.valueOf(method) + ":" + Common.getHash(str, base64, method, ServerStatus.SG("password_salt_location"), salt, ServerStatus.BG("sha3_keccak")).trim();
                }
                return String.valueOf(method) + ":" + Base64.encodeBytes(Common.getHash(str, base64, method, ServerStatus.SG("password_salt_location"), salt, ServerStatus.BG("sha3_keccak")).getBytes()).trim();
            }
            catch (Exception exception) {
                return null;
            }
        }
        if (method.equals("CRYPT3") || str.startsWith("CRYPT3:")) {
            try {
                if (str.startsWith("CRYPT3:")) {
                    return str;
                }
                if (base64) {
                    return "SHA:" + Common.getHash(str, base64, "SHA", ServerStatus.SG("password_salt_location"), salt, ServerStatus.BG("sha3_keccak")).trim();
                }
                return "SHA:" + Base64.encodeBytes(Common.getHash(str, base64, "SHA", ServerStatus.SG("password_salt_location"), salt, ServerStatus.BG("sha3_keccak")).getBytes()).trim();
            }
            catch (Exception exception) {
                return null;
            }
        }
        if (method.equals("BCRYPT") || str.startsWith("BCRYPT:")) {
            try {
                if (str.startsWith("BCRYPT:")) {
                    return str;
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            return null;
        }
        if (method.equals("MD5CRYPT") || str.startsWith("MD5CRYPT:")) {
            try {
                if (str.startsWith("MD5CRYPT:")) {
                    return str;
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            return null;
        }
        if (method.equals("PBKDF2SHA256") || str.startsWith("PBKDF2SHA256:")) {
            try {
                if (str.startsWith("PBKDF2SHA256:")) {
                    return str;
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            return null;
        }
        if (method.equals("SHA512CRYPT") || str.startsWith("SHA512CRYPT:")) {
            try {
                if (str.startsWith("SHA512CRYPT:")) {
                    return str;
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            return null;
        }
        if (method.startsWith("ARGOND:") || str.startsWith("ARGOND:")) {
            try {
                if (str.startsWith("ARGOND:")) {
                    return str;
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            try {
                Argon2Parameters.Builder builder = new Argon2Parameters.Builder(0).withVersion(19).withIterations(Integer.parseInt(method.split(":")[2])).withMemoryPowOfTwo(Integer.parseInt(method.split(":")[1])).withParallelism(Integer.parseInt(method.split(":")[3])).withSalt(salt.getBytes("UTF8"));
                Argon2BytesGenerator gen = new Argon2BytesGenerator();
                gen.init(builder.build());
                byte[] result = new byte[Integer.parseInt(method.split(":")[4])];
                gen.generateBytes(str.toCharArray(), result, 0, result.length);
                String hash = new BigInteger(1, result).toString(16).toLowerCase();
                while (hash.length() != hash.length() / 2 * 2) {
                    hash = "0" + hash;
                }
                return "ARGOND:" + hash;
            }
            catch (Exception e) {
                Log.log("SERVER", 1, e);
                return null;
            }
        }
        try {
            byte[] utf8 = str.getBytes("UTF8");
            byte[] enc = this.ecipher.doFinal(utf8);
            return Base64.encodeBytes(enc);
        }
        catch (BadPaddingException badPaddingException) {
        }
        catch (IllegalBlockSizeException illegalBlockSizeException) {
        }
        catch (IOException iOException) {
            // empty catch block
        }
        return null;
    }

    public String decrypt(String str) {
        if (str.startsWith("SHA:")) {
            return str;
        }
        if (str.startsWith("SHA512:")) {
            return str;
        }
        if (str.startsWith("SHA256:")) {
            return str;
        }
        if (str.startsWith("SHA3:")) {
            return str;
        }
        if (str.startsWith("MD5:")) {
            return str;
        }
        if (str.startsWith("MD5S2:")) {
            return str;
        }
        if (str.startsWith("MD4:")) {
            return str;
        }
        if (str.startsWith("CRYPT3:")) {
            return str;
        }
        if (str.startsWith("BCRYPT:")) {
            return str;
        }
        if (str.startsWith("MD5CRYPT:")) {
            return str;
        }
        if (str.startsWith("PBKDF2SHA256:")) {
            return str;
        }
        if (str.startsWith("SHA512CRYPT:")) {
            return str;
        }
        if (str.startsWith("ARGOND:")) {
            return str;
        }
        try {
            byte[] dec = Base64.decode(str);
            byte[] utf8 = this.dcipher.doFinal(dec);
            return new String(utf8, "UTF8");
        }
        catch (BadPaddingException badPaddingException) {
        }
        catch (IllegalBlockSizeException illegalBlockSizeException) {
        }
        catch (IOException iOException) {
            // empty catch block
        }
        return null;
    }

    public static byte[] blockEncrypt(byte[] b, byte[] ch) throws Exception {
        Cipher encrypt_des = Cipher.getInstance("DES");
        byte[] encrypted_bytes = new byte[24];
        encrypt_des.init(1, new SecretKeySpec(DesEncrypter.keyMaker(b, 0), 0, 8, "DES"));
        System.arraycopy(encrypt_des.doFinal(ch), 0, encrypted_bytes, 0, 8);
        encrypt_des.init(1, new SecretKeySpec(DesEncrypter.keyMaker(b, 7), 0, 8, "DES"));
        System.arraycopy(encrypt_des.doFinal(ch), 0, encrypted_bytes, 8, 8);
        encrypt_des.init(1, new SecretKeySpec(DesEncrypter.keyMaker(b, 14), 0, 8, "DES"));
        System.arraycopy(encrypt_des.doFinal(ch), 0, encrypted_bytes, 16, 8);
        return encrypted_bytes;
    }

    public static byte[] keyMaker(byte[] b, int loc) {
        byte[] key = new byte[]{(byte)(b[loc + 0] >> 1), (byte)((b[loc + 0] & 1) << 6 | (b[loc + 1] & 0xFF) >> 2), (byte)((b[loc + 1] & 3) << 5 | (b[loc + 2] & 0xFF) >> 3), (byte)((b[loc + 2] & 7) << 4 | (b[loc + 3] & 0xFF) >> 4), (byte)((b[loc + 3] & 0xF) << 3 | (b[loc + 4] & 0xFF) >> 5), (byte)((b[loc + 4] & 0x1F) << 2 | (b[loc + 5] & 0xFF) >> 6), (byte)((b[loc + 5] & 0x3F) << 1 | (b[loc + 6] & 0xFF) >> 7), (byte)(b[loc + 6] & 0x7F)};
        int i = 0;
        while (i < 8) {
            key[i] = (byte)(key[i] << 1);
            ++i;
        }
        return key;
    }
}

