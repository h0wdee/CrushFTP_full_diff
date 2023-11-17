/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.components.jce.CertificateExtension;
import com.maverick.util.ByteArrayReader;
import com.maverick.util.ByteArrayWriter;
import java.io.IOException;

public class StringCertificateExtension
extends CertificateExtension {
    public StringCertificateExtension(String name, String value, boolean known) {
        this.setName(name);
        this.setKnown(known);
        try (ByteArrayWriter writer = new ByteArrayWriter();){
            writer.writeString(value);
            this.setStoredValue(writer.toByteArray());
        }
        catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    StringCertificateExtension(String name, byte[] value, boolean known) {
        this.setName(name);
        this.setStoredValue(value);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public String getValue() {
        try (ByteArrayReader reader = new ByteArrayReader(this.getStoredValue());){
            String string = reader.readString();
            return string;
        }
        catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}

