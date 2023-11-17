/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.sshtools.publickey;

import com.maverick.ssh.SshException;
import com.maverick.ssh.SshIOException;
import com.maverick.ssh.components.ComponentManager;
import com.maverick.ssh.components.SshPublicKey;
import com.maverick.ssh.components.jce.Ssh1RsaPublicKey;
import com.maverick.util.ByteArrayReader;
import com.sshtools.publickey.OpenSSHPublicKeyFile;
import com.sshtools.publickey.SECSHPublicKeyFile;
import com.sshtools.publickey.Ssh1RsaPublicKeyFile;
import com.sshtools.publickey.SshPublicKeyFile;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SshPublicKeyFileFactory {
    static Logger log = LoggerFactory.getLogger(SshPublicKeyFileFactory.class);
    public static final int OPENSSH_FORMAT = 0;
    public static final int SECSH_FORMAT = 1;
    public static final int SSH1_FORMAT = 2;

    public static SshPublicKey decodeSSH2PublicKey(byte[] encoded) throws IOException {
        return SshPublicKeyFileFactory.decodeSSH2PublicKey(encoded, 0, encoded.length);
    }

    public static SshPublicKey decodeSSH2PublicKey(byte[] encoded, int off, int len) throws IOException {
        try (ByteArrayReader bar = new ByteArrayReader(encoded, off, len);){
            String algorithm = bar.readString();
            try {
                SshPublicKey publickey = (SshPublicKey)ComponentManager.getInstance().supportedPublicKeys().getInstance(algorithm);
                publickey.init(encoded, 0, encoded.length);
                SshPublicKey sshPublicKey = publickey;
                return sshPublicKey;
            }
            catch (SshException ex) {
                try {
                    throw new SshIOException(ex);
                }
                catch (OutOfMemoryError ex2) {
                    throw new IOException("An error occurred parsing a public key file! Is the file corrupt?");
                }
            }
        }
    }

    public static SshPublicKey decodeSSH2PublicKey(String algorithm, byte[] encoded) throws IOException {
        try {
            SshPublicKey publickey = (SshPublicKey)ComponentManager.getInstance().supportedPublicKeys().getInstance(algorithm);
            publickey.init(encoded, 0, encoded.length);
            return publickey;
        }
        catch (SshException ex) {
            throw new SshIOException(ex);
        }
    }

    public static SshPublicKeyFile parse(byte[] formattedkey) throws IOException {
        try {
            if (SECSHPublicKeyFile.isFormatted(formattedkey, SECSHPublicKeyFile.BEGIN, SECSHPublicKeyFile.END)) {
                return new SECSHPublicKeyFile(formattedkey);
            }
            if (OpenSSHPublicKeyFile.isFormatted(formattedkey)) {
                return new OpenSSHPublicKeyFile(formattedkey);
            }
            if (Ssh1RsaPublicKeyFile.isFormatted(formattedkey)) {
                return new Ssh1RsaPublicKeyFile(formattedkey);
            }
            throw new IOException("Unable to parse key, format could not be identified");
        }
        catch (Throwable e) {
            if (e instanceof IOException) {
                throw e;
            }
            log.error("Cannot parse public key", e);
            throw new IOException("An error occurred parsing a public key file! Is the file corrupt?");
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static SshPublicKeyFile parse(InputStream in) throws IOException {
        try {
            int read;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            while ((read = in.read()) > -1) {
                out.write(read);
            }
            SshPublicKeyFile sshPublicKeyFile = SshPublicKeyFileFactory.parse(out.toByteArray());
            return sshPublicKeyFile;
        }
        finally {
            try {
                in.close();
            }
            catch (IOException iOException) {}
        }
    }

    public static SshPublicKeyFile create(SshPublicKey key, String comment, int format) throws IOException {
        return SshPublicKeyFileFactory.create(key, null, comment, format);
    }

    public static SshPublicKeyFile create(SshPublicKey key, String options, String comment, int format) throws IOException {
        switch (format) {
            case 0: {
                if (key instanceof Ssh1RsaPublicKey) {
                    return new Ssh1RsaPublicKeyFile(key);
                }
                return new OpenSSHPublicKeyFile(key, comment, options);
            }
            case 1: {
                return new SECSHPublicKeyFile(key, comment);
            }
            case 2: {
                return new Ssh1RsaPublicKeyFile(key);
            }
        }
        throw new IOException("Invalid format type specified!");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void createFile(SshPublicKey key, String comment, int format, File toFile) throws IOException {
        SshPublicKeyFile pub = SshPublicKeyFileFactory.create(key, comment, format);
        try (FileOutputStream out = new FileOutputStream(toFile);){
            out.write(pub.getFormattedKey());
            out.flush();
        }
    }

    public static void convertFile(File keyFile, int toFormat, File toFile) throws IOException {
        SshPublicKeyFile pub = SshPublicKeyFileFactory.parse(new FileInputStream(keyFile));
        SshPublicKeyFileFactory.createFile(pub.toPublicKey(), pub.getComment(), toFormat, toFile);
    }
}

