/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.sshtools.publickey;

import com.maverick.ssh.AdaptiveConfiguration;
import com.maverick.ssh.components.SshKeyPair;
import com.maverick.ssh.components.SshRsaPrivateCrtKey;
import com.maverick.ssh.components.jce.JCEProvider;
import com.sshtools.publickey.InvalidPassphraseException;
import com.sshtools.publickey.OpenSSHPrivateKeyFile;
import com.sshtools.publickey.Ssh1RsaPrivateKeyFile;
import com.sshtools.publickey.SshPrivateKeyFile;
import com.sshtools.publickey.SshtoolsPrivateKeyFile;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SshPrivateKeyFileFactory {
    static Logger log = LoggerFactory.getLogger(SshPrivateKeyFileFactory.class);
    public static final int OPENSSH_FORMAT = 0;
    public static final int OPENSSL_FORMAT = 4;
    @Deprecated
    public static final int SSHTOOLS_FORMAT = 1;
    @Deprecated
    public static final int SSH1_FORMAT = 3;
    static final String[] PRIVATE_KEYS = new String[]{"OpenSSHPrivateKeyFile", "SshtoolsPrivateKeyFile", "Ssh1RsaPrivateKeyFile", "PuTTYPrivateKeyFile", "SSHCOMPrivateKeyFile"};

    public static SshPrivateKeyFile parse(byte[] formattedkey) throws IOException {
        try {
            if (JCEProvider.hasBCProvider() && JCEProvider.isBCEnabled()) {
                return SshPrivateKeyFileFactory.checkAndLoad("com.sshtools.publickey.OpenSSHPrivateKeyFile" + JCEProvider.getBCProvider().getName(), formattedkey);
            }
        }
        catch (UnsupportedOperationException unsupportedOperationException) {
            // empty catch block
        }
        for (String name : PRIVATE_KEYS) {
            try {
                return SshPrivateKeyFileFactory.checkAndLoad("com.sshtools.publickey." + name, formattedkey);
            }
            catch (UnsupportedOperationException unsupportedOperationException) {
            }
        }
        throw new IOException("A suitable key format could not be found!");
    }

    private static Method getMethod(Class<?> clz, String name, Class<?> ... args) throws NoSuchMethodException, SecurityException {
        Class<?> tmp = clz;
        while (true) {
            try {
                Method m = tmp.getMethod(name, args);
                return m;
            }
            catch (NoSuchMethodException noSuchMethodException) {
                if (!(tmp = tmp.getSuperclass()).getClass().equals(Object.class)) continue;
                throw new NoSuchMethodException();
            }
            break;
        }
    }

    private static SshPrivateKeyFile checkAndLoad(String className, byte[] formattedkey) {
        try {
            Class<?> clz = Class.forName(className);
            Method is = SshPrivateKeyFileFactory.getMethod(clz, "isFormatted", byte[].class);
            Boolean result = (Boolean)is.invoke(null, new Object[]{formattedkey});
            if (result.booleanValue()) {
                Constructor<?> c = clz.getDeclaredConstructor(byte[].class);
                c.setAccessible(true);
                return (SshPrivateKeyFile)c.newInstance(new Object[]{formattedkey});
            }
        }
        catch (InvocationTargetException e) {
            if (AdaptiveConfiguration.getBoolean("verbose", false, new String[0])) {
                log.error("OpenSSHPrivateKeyFile could not load using Bouncycastle PKIX", e.getTargetException());
            }
        }
        catch (Throwable t) {
            if (AdaptiveConfiguration.getBoolean("verbose", false, new String[0])) {
                log.error("Bouncycastle PKIX not in classpath so falling back to older implementation of OpenSSHPrivateKeyFile.", t);
            }
            log.info("Bouncycastle PKIX not in classpath so falling back to older implementation of OpenSSHPrivateKeyFile.");
        }
        throw new UnsupportedOperationException();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static SshPrivateKeyFile parse(InputStream in) throws IOException {
        try {
            int read;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            while ((read = in.read()) > -1) {
                out.write(read);
            }
            SshPrivateKeyFile sshPrivateKeyFile = SshPrivateKeyFileFactory.parse(out.toByteArray());
            return sshPrivateKeyFile;
        }
        finally {
            try {
                in.close();
            }
            catch (IOException iOException) {}
        }
    }

    public static SshPrivateKeyFile create(SshKeyPair pair, String passphrase, String comment, int format) throws IOException {
        if (!(pair.getPrivateKey() instanceof SshRsaPrivateCrtKey) && format == 3) {
            throw new IOException("SSH1 format requires rsa key pair!");
        }
        switch (format) {
            case 4: {
                if (JCEProvider.isBCEnabled()) {
                    try {
                        Class<?> clz = Class.forName("com.sshtools.publickey.OpenSSHPrivateKeyFile" + JCEProvider.getBCProvider().getName());
                        Constructor<?> c = clz.getDeclaredConstructor(SshKeyPair.class, String.class);
                        c.setAccessible(true);
                        SshPrivateKeyFile f = (SshPrivateKeyFile)c.newInstance(pair, passphrase);
                        f.toKeyPair(passphrase);
                        return f;
                    }
                    catch (Throwable throwable) {
                        // empty catch block
                    }
                }
                throw new IOException("BouncyCastle PKIX dependency not present and is required to create PEM_FORMAT keys");
            }
            case 0: {
                return new OpenSSHPrivateKeyFile(pair, passphrase, comment);
            }
            case 1: {
                return new SshtoolsPrivateKeyFile(pair, passphrase, comment);
            }
            case 3: {
                return new Ssh1RsaPrivateKeyFile(pair, passphrase, comment);
            }
        }
        throw new IOException("Invalid key format!");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void createFile(SshKeyPair key, String passphrase, String comment, int format, File toFile) throws IOException {
        SshPrivateKeyFile pub = SshPrivateKeyFileFactory.create(key, passphrase, comment, format);
        try (FileOutputStream out = new FileOutputStream(toFile);){
            out.write(pub.getFormattedKey());
            out.flush();
        }
    }

    public static void convertFile(File keyFile, String passphrase, String comment, int toFormat, File toFile) throws IOException, InvalidPassphraseException {
        SshPrivateKeyFile pub = SshPrivateKeyFileFactory.parse(new FileInputStream(keyFile));
        SshPrivateKeyFileFactory.createFile(pub.toKeyPair(passphrase), passphrase, comment, toFormat, toFile);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void changePassphrase(File keyFile, String passphrase, String newPassphrase) throws IOException, InvalidPassphraseException {
        SshPrivateKeyFile pub = SshPrivateKeyFileFactory.parse(new FileInputStream(keyFile));
        pub.changePassphrase(passphrase, newPassphrase);
        try (FileOutputStream out = new FileOutputStream(keyFile);){
            out.write(pub.getFormattedKey());
            out.flush();
        }
    }
}

