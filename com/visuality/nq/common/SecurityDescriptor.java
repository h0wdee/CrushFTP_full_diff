/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.common;

import com.visuality.nq.common.BufferReader;
import com.visuality.nq.common.BufferWriter;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.Sid;
import com.visuality.nq.common.SmbSerializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SecurityDescriptor
implements SmbSerializable {
    private static final short SD_OWNERDEFAULTED = 1;
    private static final short SD_GROUPDEFAULTED = 2;
    private static final short SD_DACLPRESENT = 4;
    private static final short SD_DACLDEFAULTED = 8;
    private static final short SD_SACLPRESENT = 16;
    private static final short SD_SACLDEFAULTED = 32;
    private static final short SD_DACLTRUSTED = 64;
    private static final short SD_SERVER_SECURITY = 128;
    private static final short SD_DACLAUTOINHERITREQ = 256;
    private static final short SD_SACLAUTOINHERITREQ = 512;
    private static final short SD_DACLAUTOINHERITED = 1024;
    private static final short SD_SACLAUTOINHERITED = 2048;
    private static final short SD_DACLPROTECTED = 4096;
    private static final short SD_SACLPROTECTED = 8192;
    private static final short SD_RM_CONTROLVALID = 16384;
    private static final short SD_SELF_RELATIVE = Short.MIN_VALUE;
    private static final int SD_RIDADMINISTRATOR = 500;
    private static final int SD_RIDGUEST = 501;
    private static final int SD_RIDGROUPADMINS = 512;
    private static final int SD_RIDGROUPUSERS = 513;
    private static final int SD_RIDGROUPGUESTS = 514;
    private static final int SD_RIDALIASADMIN = 544;
    private static final int SD_RIDALIASUSER = 545;
    private static final int SD_RIDALIASGUEST = 546;
    private static final int SD_RIDALIASACCOUNTOP = 548;
    private static final int SD_OWNER = 1;
    private static final int SD_GROUP = 2;
    private static final int SD_DACL = 4;
    private static final int SD_SACL = 8;
    private static final int SD_LABLE = 16;
    private static final int SD_RIDTYPE_USENONE = 0;
    private static final int SD_RIDTYPE_USER = 1;
    private static final int SD_RIDTYPE_DOMGRP = 2;
    private static final int SD_RIDTYPE_DOMAIN = 3;
    private static final int SD_RIDTYPE_ALIAS = 4;
    private static final int SD_RIDTYPE_WKNGRP = 5;
    private static final int SD_RIDTYPE_DELETED = 6;
    private static final int SD_RIDTYPE_INVALID = 7;
    private static final int SD_RIDTYPE_UNKNOWN = 8;
    private short revision = 1;
    private short type;
    private Dacl dacl = new Dacl();
    private Sid ownerSid;
    private Sid groupSid;
    public static SecurityDescriptor EVERYONE_RO = new SecurityDescriptor();

    public SecurityDescriptor() {
    }

    public SecurityDescriptor(BufferReader reader) throws NqException {
        this.read(reader);
    }

    public void read(BufferReader reader) throws NqException {
        int offset = reader.getOffset();
        this.revision = reader.readInt2();
        this.type = reader.readInt2();
        int ownerOffset = reader.readInt4();
        int groupOffset = reader.readInt4();
        reader.skip(4);
        int daclOffset = reader.readInt4();
        if (ownerOffset != 0) {
            reader.setOffset(offset + ownerOffset);
            this.ownerSid = new Sid();
            this.ownerSid.read(reader);
        }
        if (groupOffset != 0) {
            reader.setOffset(offset + groupOffset);
            this.groupSid = new Sid();
            this.groupSid.read(reader);
        }
        if (daclOffset != 0) {
            reader.setOffset(offset + daclOffset);
            this.dacl.revision = reader.readInt2();
            reader.skip(2);
            int numAces = reader.readInt4();
            this.dacl.aces.clear();
            try {
                for (int i = 0; i < numAces; ++i) {
                    int startOffset = reader.getOffset();
                    Ace ace = new Ace();
                    ace.allowed = reader.readByte() == 0;
                    ace.flags = reader.readByte();
                    int sz = reader.readInt2();
                    ace.access = reader.readInt4();
                    ace.sid = new Sid();
                    ace.sid.read(reader);
                    if (0 < (sz -= reader.getOffset() - startOffset)) {
                        reader.skip(sz);
                    }
                    this.dacl.aces.add(ace);
                }
            }
            catch (Exception e) {
                NqException nqe = new NqException("Ace evalutation error: " + e.getMessage(), -23);
                nqe.initCause(e);
                throw nqe;
            }
        }
    }

    public int write(BufferWriter writer) throws NqException {
        int baseOffset = writer.getOffset();
        writer.writeInt2(this.revision);
        writer.writeInt2(this.type);
        int ownerOffsetPos = writer.getOffset();
        writer.writeInt4(0);
        int groupOffsetPos = writer.getOffset();
        writer.writeInt4(0);
        writer.writeInt4(0);
        int daclOffsetPos = writer.getOffset();
        writer.writeInt4(0);
        writer.writeInt4(daclOffsetPos, writer.getOffset() - baseOffset);
        if (this.dacl != null) {
            int daclBase = writer.getOffset();
            writer.writeInt2(this.dacl.revision);
            int daclSizePos = writer.getOffset();
            writer.writeInt2(0);
            writer.writeInt4(this.dacl.aces.size());
            for (int i = 0; i < this.dacl.aces.size(); ++i) {
                Ace ace = (Ace)this.dacl.aces.get(i);
                int aceBasePos = writer.getOffset();
                writer.writeByte((byte)(!ace.allowed ? 1 : 0));
                writer.writeByte((byte)ace.flags);
                int sizePos = writer.getOffset();
                writer.writeInt2(0);
                writer.writeInt4(ace.access);
                ace.sid.write(writer);
                writer.writeInt2(sizePos, writer.getOffset() - aceBasePos);
            }
            writer.writeInt2(daclSizePos, writer.getOffset() - daclBase);
        }
        if (this.ownerSid != null) {
            writer.writeInt4(ownerOffsetPos, writer.getOffset() - baseOffset);
            this.ownerSid.write(writer);
        }
        if (this.groupSid != null) {
            writer.writeInt4(groupOffsetPos, writer.getOffset() - baseOffset);
            this.groupSid.write(writer);
        }
        return writer.getOffset() - baseOffset;
    }

    public short getRevision() {
        return this.revision;
    }

    public void setRevision(short revision) {
        this.revision = revision;
    }

    public short getType() {
        return this.type;
    }

    public void setType(short type) {
        this.type = type;
    }

    public Dacl getDacl() {
        return this.dacl;
    }

    public void setDacl(Dacl dacl) {
        this.dacl = dacl;
    }

    public Sid getOwnerSid() {
        return this.ownerSid;
    }

    public void setOwnerSid(Sid ownerSid) {
        this.ownerSid = ownerSid;
    }

    public Sid getGroupSid() {
        return this.groupSid;
    }

    public void setGroupSid(Sid groupSid) {
        this.groupSid = groupSid;
    }

    static {
        SecurityDescriptor.EVERYONE_RO.revision = 1;
        SecurityDescriptor.EVERYONE_RO.type = (short)-32764;
        SecurityDescriptor.EVERYONE_RO.ownerSid = null;
        Dacl dacl = new Dacl();
        dacl.revision = 2;
        Ace ace = new Ace();
        ace.allowed = true;
        ace.flags = 0;
        ace.access = 1179817;
        ace.sid = new Sid();
        ace.sid.revision = 1;
        ace.sid.auth = 1;
        ace.sid.subs = new int[1];
        ace.sid.subs[0] = 0;
        dacl.aces.add(ace);
        SecurityDescriptor.EVERYONE_RO.dacl = dacl;
    }

    public static class Ace {
        public boolean allowed;
        public int flags;
        public int access;
        public Sid sid;

        public String toString() {
            return "Ace [allowed=" + this.allowed + ", flags=" + this.flags + ", access=" + this.access + ", sid=" + this.sid + "]";
        }
    }

    public static class Dacl {
        public int revision = 2;
        public List aces = new ArrayList();

        public Dacl() {
        }

        public Dacl(Collection aces) {
            this.aces.addAll(aces);
        }
    }
}

