/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lw.bouncycastle.bcpg.ArmoredOutputStream
 *  lw.bouncycastle.openpgp.PGPException
 *  lw.bouncycastle.openpgp.PGPPublicKeyRing
 *  lw.bouncycastle.openpgp.PGPSecretKeyRing
 */
package com.didisoft.pgp.bc;

import com.didisoft.pgp.PGPException;
import com.didisoft.pgp.SecureRandomSource;
import com.didisoft.pgp.bc.DefaultSecureRandomSource;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import lw.bouncycastle.bcpg.ArmoredOutputStream;
import lw.bouncycastle.openpgp.PGPPublicKeyRing;
import lw.bouncycastle.openpgp.PGPSecretKeyRing;

public class IOUtil {
    private static SecureRandomSource randomSource = new DefaultSecureRandomSource();

    public static void closeStream(InputStream inputStream) {
        if (inputStream != null) {
            try {
                inputStream.close();
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
    }

    public static void closeStream(OutputStream outputStream) {
        if (outputStream != null) {
            try {
                outputStream.flush();
                outputStream.close();
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
    }

    private static boolean isPossiblyBase64(String string) {
        if (string == null || "".equals(string.trim())) {
            return false;
        }
        for (int i = 0; i < string.length(); ++i) {
            if (IOUtil.isPossiblyBase64(string.charAt(i))) continue;
            return false;
        }
        return true;
    }

    static boolean isPossiblyBase64(int n) {
        return n >= 65 && n <= 90 || n >= 97 && n <= 122 || n >= 48 && n <= 57 || n == 43 || n == 47 || n == 13 || n == 10;
    }

    public static InputStream readFileOrAsciiString(String string, String string2) throws IOException {
        if (string != null && !"".equals(string.trim())) {
            if (string.indexOf("BEGIN") != -1 && string.indexOf("PGP") != -1 || string.length() >= 256 && IOUtil.isPossiblyBase64(string)) {
                return new ByteArrayInputStream(string.getBytes("ASCII"));
            }
            if (string.indexOf("\n") != -1 || string.indexOf("\r") != -1) {
                return new ByteArrayInputStream(string.getBytes("UTF-8"));
            }
            if (new File(string).exists()) {
                return new BufferedInputStream(new FileInputStream(string));
            }
            throw new FileNotFoundException("File not found at the specified location: " + string);
        }
        throw new IllegalArgumentException(string2);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void exportPublicKeyRing(PGPPublicKeyRing pGPPublicKeyRing, String string, boolean bl, String string2) throws IOException {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(string);
            IOUtil.exportPublicKeyRing(pGPPublicKeyRing, fileOutputStream, bl, string2);
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(fileOutputStream);
            throw throwable;
        }
        IOUtil.closeStream(fileOutputStream);
    }

    public static void exportPublicKeyRing(PGPPublicKeyRing pGPPublicKeyRing, OutputStream outputStream, boolean bl, String string) throws IOException {
        OutputStream outputStream2 = null;
        try {
            if (bl) {
                outputStream2 = outputStream;
                outputStream = new ArmoredOutputStream(outputStream2);
                ((ArmoredOutputStream)outputStream).setHeader("Version", string);
            }
            pGPPublicKeyRing.encode(outputStream);
        }
        catch (IOException iOException) {
            throw iOException;
        }
        finally {
            if (bl) {
                IOUtil.closeStream(outputStream);
            }
        }
    }

    public static void exportPrivateKey(PGPSecretKeyRing pGPSecretKeyRing, String string, boolean bl, String string2) throws IOException {
        FileOutputStream fileOutputStream = null;
        FileOutputStream fileOutputStream2 = null;
        try {
            fileOutputStream = new FileOutputStream(string);
            if (bl) {
                fileOutputStream2 = fileOutputStream;
                fileOutputStream = new ArmoredOutputStream((OutputStream)fileOutputStream2);
                ((ArmoredOutputStream)fileOutputStream).setHeader("Version", string2);
            }
            pGPSecretKeyRing.encode((OutputStream)fileOutputStream);
        }
        catch (IOException iOException) {
            try {
                throw iOException;
            }
            catch (Throwable throwable) {
                IOUtil.closeStream(fileOutputStream);
                IOUtil.closeStream(fileOutputStream2);
                throw throwable;
            }
        }
        IOUtil.closeStream(fileOutputStream);
        IOUtil.closeStream(fileOutputStream2);
    }

    public static void exportPrivateKey(PGPSecretKeyRing pGPSecretKeyRing, OutputStream outputStream, boolean bl, String string) throws IOException {
        OutputStream outputStream2 = null;
        try {
            if (bl) {
                outputStream2 = outputStream;
                outputStream = new ArmoredOutputStream(outputStream2);
                ((ArmoredOutputStream)outputStream).setHeader("Version", string);
            }
            pGPSecretKeyRing.encode(outputStream);
        }
        catch (IOException iOException) {
            throw iOException;
        }
        finally {
            if (bl) {
                IOUtil.closeStream(outputStream);
            }
        }
    }

    public static PGPException newPGPException(lw.bouncycastle.openpgp.PGPException pGPException) {
        if (pGPException instanceof PGPException) {
            return (PGPException)pGPException;
        }
        return new PGPException(pGPException.getMessage() + (pGPException.getUnderlyingException() != null ? " : " + pGPException.getUnderlyingException().getMessage() : ""), pGPException.getUnderlyingException());
    }

    public static SecureRandom getSecureRandom() {
        return IOUtil.getRandomSource().getSecureRandom();
    }

    public static SecureRandomSource getRandomSource() {
        return randomSource;
    }

    public static void setRandomSource(SecureRandomSource secureRandomSource) {
        if (secureRandomSource == null) {
            throw new IllegalArgumentException("Parameter randomSource cannot be null!");
        }
        randomSource = secureRandomSource;
    }
}

