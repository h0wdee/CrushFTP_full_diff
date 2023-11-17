/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.common;

import com.visuality.nq.common.BufferReader;
import com.visuality.nq.common.BufferWriter;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.SmbSerializable;

public class Sid
implements SmbSerializable {
    public static final Sid localGroup = new Sid();
    public int revision;
    public int auth;
    public int[] subs;
    long ttl;

    public boolean compare(Sid sid) {
        boolean res = false;
        if (this.subs.length != sid.subs.length) {
            return false;
        }
        if (this.revision == sid.revision && this.auth == sid.auth) {
            for (int i = 0; i < this.subs.length && this.subs[i] == sid.subs[i]; ++i) {
            }
            res = true;
        }
        return res;
    }

    public void read(BufferReader reader) throws NqException {
        this.revision = reader.readByte();
        byte numAuth = reader.readByte();
        reader.skip(5);
        this.auth = reader.readByte();
        this.subs = new int[numAuth];
        for (int i = 0; i < this.subs.length; ++i) {
            this.subs[i] = reader.readInt4();
        }
    }

    public int write(BufferWriter writer) throws NqException {
        int offset = writer.getOffset();
        writer.writeByte((byte)this.revision);
        writer.writeByte((byte)this.subs.length);
        writer.writeZeros(5);
        writer.writeByte((byte)this.auth);
        for (int i = 0; i < this.subs.length; ++i) {
            writer.writeInt4(this.subs[i]);
        }
        return writer.getOffset() - offset;
    }

    public String toString() {
        String result = "S";
        result = result + "-" + this.revision;
        result = result + "-" + this.auth;
        if (null != this.subs) {
            for (int i = 0; i < this.subs.length; ++i) {
                result = result + "-" + ((long)this.subs[i] & 0xFFFFFFFFL);
            }
        }
        return result;
    }

    public boolean hasRid() {
        return this.subs.length > 4;
    }

    public int getRid() {
        return this.subs[this.subs.length - 1];
    }

    public void setTtl(long ttl) {
        this.ttl = ttl;
    }

    public long getTtl() {
        return this.ttl;
    }

    static {
        Sid.localGroup.revision = 1;
        Sid.localGroup.auth = 5;
        Sid.localGroup.subs = new int[]{32};
    }
}

