/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.components.Utils;
import com.maverick.ssh.components.jce.EncodedExtension;
import com.maverick.util.ByteArrayReader;
import com.maverick.util.ByteArrayWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CriticalOption
extends EncodedExtension {
    public static final String FORCE_COMMAND = "force-command";
    public static final String SOURCE_ADDRESS = "source-address";

    public CriticalOption(String name, byte[] value, boolean known) {
        this.setName(name);
        this.setKnown(known);
        this.setStoredValue(value);
    }

    public CriticalOption(String name, String value, boolean known) {
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

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public String getStringValue() {
        try (ByteArrayReader reader = new ByteArrayReader(this.getStoredValue());){
            String string = reader.readString();
            return string;
        }
        catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static CriticalOption createKnownOption(String name, byte[] value) {
        switch (name) {
            case "force-command": 
            case "source-address": {
                return new CriticalOption(name, value, true);
            }
        }
        return new CriticalOption(name, value, false);
    }

    public static class Builder {
        List<CriticalOption> tmp = new ArrayList<CriticalOption>();

        public Builder createCustomOption(String name, String value) {
            this.tmp.add(new CriticalOption(name, value, false));
            return this;
        }

        public Builder createCustomOption(String name, byte[] value) {
            this.tmp.add(new CriticalOption(name, value, false));
            return this;
        }

        public Builder forceCommand(String cmd) {
            this.tmp.add(new CriticalOption(CriticalOption.FORCE_COMMAND, cmd, true));
            return this;
        }

        public Builder sourceAddress(String ... addresses) {
            this.tmp.add(new CriticalOption(CriticalOption.SOURCE_ADDRESS, Utils.csv(addresses), true));
            return this;
        }

        public List<CriticalOption> build() {
            return this.tmp;
        }
    }
}

