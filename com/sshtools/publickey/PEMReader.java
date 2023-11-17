/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.publickey;

import com.maverick.ssh.SshException;
import com.maverick.ssh.SshIOException;
import com.maverick.ssh.components.ComponentManager;
import com.maverick.ssh.components.SshCipher;
import com.maverick.util.Base64;
import com.sshtools.publickey.PEM;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.Hashtable;

class PEMReader
extends PEM {
    private LineNumberReader reader;
    private String type;
    private Hashtable<String, String> header;
    private byte[] payload;

    public PEMReader(Reader r) throws IOException {
        this.reader = new LineNumberReader(r);
        this.read();
    }

    private void read() throws IOException {
        int colon;
        String line;
        while ((line = this.reader.readLine()) != null) {
            if (!(line = line.trim()).startsWith("-----") || !line.endsWith("-----")) continue;
            if (line.startsWith("-----BEGIN ")) {
                this.type = line.substring("-----BEGIN ".length(), line.length() - "-----".length());
                break;
            }
            throw new IOException("Invalid PEM boundary at line " + this.reader.getLineNumber() + ": " + line);
        }
        this.header = new Hashtable();
        block1: while ((line = this.reader.readLine()) != null && (colon = line.indexOf(58)) != -1) {
            String key = line.substring(0, colon).trim();
            if (line.endsWith("\\")) {
                String v = line.substring(colon + 1, line.length() - 1).trim();
                StringBuffer value = new StringBuffer(v);
                while ((line = this.reader.readLine()) != null) {
                    if (line.endsWith("\\")) {
                        value.append(" ").append(line.substring(0, line.length() - 1).trim());
                        continue;
                    }
                    value.append(" ").append(line.trim());
                    continue block1;
                }
                continue;
            }
            String value = line.substring(colon + 1).trim();
            this.header.put(key, value);
        }
        if (line == null) {
            throw new IOException("The key format is invalid! OpenSSH formatted keys must begin with -----BEGIN RSA or -----BEGIN DSA");
        }
        StringBuffer body = new StringBuffer(line);
        while ((line = this.reader.readLine()) != null) {
            if ((line = line.trim()).startsWith("-----") && line.endsWith("-----")) {
                if (line.startsWith("-----END " + this.type)) break;
                throw new IOException("Invalid PEM end boundary at line " + this.reader.getLineNumber() + ": " + line);
            }
            body.append(line);
        }
        this.payload = Base64.decode(body.toString());
    }

    public Hashtable<String, String> getHeader() {
        return this.header;
    }

    public byte[] getPayload() {
        return this.payload;
    }

    public String getType() {
        return this.type;
    }

    public byte[] decryptPayload(String passphrase) throws IOException {
        try {
            String dekInfo = this.header.get("DEK-Info");
            if (dekInfo != null) {
                int comma = dekInfo.indexOf(44);
                String keyAlgorithm = dekInfo.substring(0, comma);
                String ivString = dekInfo.substring(comma + 1);
                byte[] iv = new byte[ivString.length() / 2];
                for (int i = 0; i < ivString.length(); i += 2) {
                    iv[i / 2] = (byte)Integer.parseInt(ivString.substring(i, i + 2), 16);
                }
                byte[] keydata = null;
                SshCipher cipher = null;
                switch (keyAlgorithm) {
                    case "DES-EDE3-CBC": {
                        keydata = PEMReader.getKeyFromPassphrase(passphrase, iv, 24);
                        cipher = (SshCipher)ComponentManager.getInstance().supportedSsh2CiphersCS().getInstance("3des-cbc");
                        break;
                    }
                    case "AES-128-CBC": {
                        keydata = PEMReader.getKeyFromPassphrase(passphrase, iv, 16);
                        cipher = (SshCipher)ComponentManager.getInstance().supportedSsh2CiphersCS().getInstance("aes128-cbc");
                        break;
                    }
                    case "AES-256-CBC": {
                        keydata = PEMReader.getKeyFromPassphrase(passphrase, iv, 32);
                        cipher = (SshCipher)ComponentManager.getInstance().supportedSsh2CiphersCS().getInstance("aes256-cbc");
                        break;
                    }
                    default: {
                        throw new IOException("Unsupported passphrase algorithm: " + keyAlgorithm);
                    }
                }
                cipher.init(1, iv, keydata);
                byte[] plain = new byte[this.payload.length];
                cipher.transform(this.payload, 0, plain, 0, plain.length);
                return plain;
            }
            return this.payload;
        }
        catch (SshException e) {
            throw new SshIOException(e);
        }
    }
}

