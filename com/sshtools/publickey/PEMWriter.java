/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.publickey;

import com.maverick.ssh.AdaptiveConfiguration;
import com.maverick.ssh.SshException;
import com.maverick.ssh.SshIOException;
import com.maverick.ssh.components.ComponentManager;
import com.maverick.ssh.components.SshCipher;
import com.maverick.ssh.components.jce.AES128Cbc;
import com.maverick.util.Base64;
import com.sshtools.publickey.PEM;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Hashtable;

class PEMWriter
extends PEM {
    private String type;
    private Hashtable<String, String> header = new Hashtable();

    public void write(Writer w, byte[] payload) {
        PrintWriter writer = new PrintWriter(w, true);
        writer.println("-----BEGIN " + this.type + "-----");
        if (!this.header.isEmpty()) {
            Enumeration<String> e = this.header.keys();
            while (e.hasMoreElements()) {
                String key = e.nextElement();
                String value = this.header.get(key);
                writer.print(key + ": ");
                if (key.length() + value.length() + 2 > 75) {
                    int offset;
                    writer.println(value.substring(0, offset) + "\\");
                    for (offset = Math.max(75 - key.length() - 2, 0); offset < value.length(); offset += 75) {
                        if (offset + 75 >= value.length()) {
                            writer.println(value.substring(offset));
                            continue;
                        }
                        writer.println(value.substring(offset, offset + 75) + "\\");
                    }
                    continue;
                }
                writer.println(value);
            }
            writer.println();
        }
        writer.println(Base64.encodeBytes(payload, false));
        writer.println("-----END " + this.type + "-----");
    }

    public byte[] encryptPayload(byte[] payload, String passphrase) throws IOException {
        try {
            if (passphrase == null || passphrase.length() == 0) {
                return payload;
            }
            byte[] iv = new byte[16];
            ComponentManager.getInstance().getRND().nextBytes(iv);
            StringBuffer ivString = new StringBuffer(16);
            for (int i = 0; i < iv.length; ++i) {
                ivString.append(HEX_CHARS[iv[i] >>> 4 & 0xF]);
                ivString.append(HEX_CHARS[iv[i] & 0xF]);
            }
            this.header.put("DEK-Info", AdaptiveConfiguration.getProperty("privatekey.encryption", "AES-128-CBC", new String[0]) + "," + ivString);
            this.header.put("Proc-Type", "4,ENCRYPTED");
            byte[] keydata = PEMWriter.getKeyFromPassphrase(passphrase, iv, 16);
            AES128Cbc cipher = new AES128Cbc();
            ((SshCipher)cipher).init(0, iv, keydata);
            int padding = ((SshCipher)cipher).getBlockSize() - payload.length % ((SshCipher)cipher).getBlockSize();
            if (padding > 0) {
                byte[] payloadWithPadding = new byte[payload.length + padding];
                System.arraycopy(payload, 0, payloadWithPadding, 0, payload.length);
                for (int i = payload.length; i < payloadWithPadding.length; ++i) {
                    payloadWithPadding[i] = (byte)padding;
                }
                payload = payloadWithPadding;
            }
            ((SshCipher)cipher).transform(payload, 0, payload, 0, payload.length);
            return payload;
        }
        catch (SshException e) {
            throw new SshIOException(e);
        }
    }

    public Hashtable<String, String> getHeader() {
        return this.header;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String string) {
        this.type = string;
    }
}

