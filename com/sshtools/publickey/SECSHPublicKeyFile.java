/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.publickey;

import com.maverick.ssh.SshException;
import com.maverick.ssh.components.SshPublicKey;
import com.sshtools.publickey.Base64EncodedFileFormat;
import com.sshtools.publickey.SshPublicKeyFile;
import com.sshtools.publickey.SshPublicKeyFileFactory;
import java.io.IOException;

public class SECSHPublicKeyFile
extends Base64EncodedFileFormat
implements SshPublicKeyFile {
    static String BEGIN = "---- BEGIN SSH2 PUBLIC KEY ----";
    static String END = "---- END SSH2 PUBLIC KEY ----";
    String algorithm;
    byte[] encoded;

    SECSHPublicKeyFile(byte[] formattedkey) throws IOException {
        super(BEGIN, END);
        this.encoded = this.getKeyBlob(formattedkey);
        this.toPublicKey();
    }

    SECSHPublicKeyFile(SshPublicKey key, String comment) throws IOException {
        super(BEGIN, END);
        try {
            this.algorithm = key.getAlgorithm();
            this.encoded = key.getEncoded();
            this.setComment(comment);
            this.toPublicKey();
        }
        catch (SshException ex) {
            throw new IOException("Failed to encode public key");
        }
    }

    @Override
    public String getComment() {
        return this.getHeaderValue("Comment");
    }

    @Override
    public SshPublicKey toPublicKey() throws IOException {
        return SshPublicKeyFileFactory.decodeSSH2PublicKey(this.encoded);
    }

    @Override
    public byte[] getFormattedKey() throws IOException {
        return this.formatKey(this.encoded);
    }

    public void setComment(String comment) {
        this.setHeaderValue("Comment", (comment.trim().startsWith("\"") ? "" : "\"") + comment.trim() + (comment.trim().endsWith("\"") ? "" : "\""));
    }

    public String toString() {
        try {
            return new String(this.getFormattedKey(), "UTF-8");
        }
        catch (IOException ex) {
            return "Invalid encoding!";
        }
    }

    @Override
    public String getOptions() {
        return null;
    }
}

