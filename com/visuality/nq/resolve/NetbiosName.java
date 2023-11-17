/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.resolve;

import com.visuality.nq.common.BufferReader;
import com.visuality.nq.common.BufferWriter;
import com.visuality.nq.common.IpAddressHelper;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.TraceLog;
import com.visuality.nq.common.Utility;
import com.visuality.nq.resolve.NetbiosException;

public class NetbiosName {
    public static final int ROLE_SERVER = 32;
    public static final int ROLE_HOST = 0;
    public static final int ROLE_MASTERBROWSER = 29;
    public static final int ROLE_DC = 28;
    static final int NAMELEN = 15;
    static final int ENCODEDNAMELEN = 32;
    static final int NAMEOFFSET = 192;
    private String name;
    private int role;
    private String scopeId = "";
    private boolean group = false;

    public boolean isGroup() {
        return this.group;
    }

    public void setGroup(boolean group) {
        this.group = group;
    }

    public NetbiosName() {
    }

    public NetbiosName(String name, int postfix) throws NetbiosException {
        TraceLog.get().enter("name=" + name + "; postfix=" + postfix, 700);
        if (null == name) {
            TraceLog.get().exit(700);
            throw new NetbiosException("Name argument is null", -20);
        }
        boolean nameIsIp = IpAddressHelper.isIpAddress(name);
        this.name = nameIsIp ? name.toUpperCase() : Utility.getNetbiosNameFromFQN(name.toUpperCase());
        this.role = postfix;
        TraceLog.get().exit(700);
    }

    public String toString() {
        return this.scopeId.length() > 0 ? this.scopeId + ":" : "" + this.name + "<" + Integer.toHexString(this.role) + ">";
    }

    public byte[] toBytes() {
        byte[] data = new byte[16];
        int len = this.name.length();
        for (int i = 0; i < data.length; ++i) {
            data[i] = i < len ? (int)this.name.charAt(i) : (i < 15 ? 32 : (byte)this.role);
        }
        return data;
    }

    public int encodeName(byte[] dest, int offset) {
        int dotIdx;
        boolean isNbt = this.name.equals("") ? false : this.name.charAt(0) == '*' && this.name.length() == 1;
        int curOff = offset;
        dest[curOff++] = 32;
        byte[] nameBytes = this.name.getBytes();
        for (int i = 0; i < 15; ++i) {
            if (i < this.name.length()) {
                dest[curOff++] = (byte)((nameBytes[i] >> 4) + 65);
                dest[curOff++] = (byte)((nameBytes[i] & 0xF) + 65);
                continue;
            }
            dest[curOff++] = (byte)(isNbt ? 65 : 67);
            dest[curOff++] = 65;
        }
        dest[curOff++] = (byte)(65 + this.role / 16);
        dest[curOff++] = (byte)(65 + this.role % 16);
        int scopeIdx = 0;
        while (true) {
            int labelLen;
            if ((labelLen = (dotIdx = this.scopeId.indexOf(46, scopeIdx)) == -1 ? this.scopeId.length() - scopeIdx : dotIdx - scopeIdx) > 0) {
                dest[curOff++] = (byte)labelLen;
                byte[] temp = this.scopeId.getBytes();
                for (int i = scopeIdx; i < scopeIdx + labelLen; ++i) {
                    dest[curOff++] = temp[i];
                }
                curOff += labelLen;
            }
            if (dotIdx == -1) break;
            scopeIdx += labelLen + 1;
        }
        if (dotIdx == -1) {
            dest[curOff++] = 0;
        }
        return curOff;
    }

    public String getName() {
        int length = this.name.length();
        String res = this.name.substring(0, Math.min(length, 15));
        while (res.endsWith(" ")) {
            res = res.substring(0, res.length() - 1);
        }
        return res;
    }

    public int encodeNamePointer(byte[] dest, int offset, int targetOffset) throws NqException {
        LabelResolver resolver = new LabelResolver(dest, targetOffset);
        int labelOff = resolver.resolveLabel();
        BufferWriter writer = new BufferWriter(dest, offset, true);
        writer.writeInt2(labelOff | 0xC000);
        return writer.getOffset();
    }

    public static int skipName(byte[] src, int offset) throws NqException {
        int result = -1;
        LabelResolver resolver = new LabelResolver(src, offset);
        int labelOff = resolver.resolveLabel();
        byte length = src[labelOff++];
        int curOff = resolver.getOffset();
        if (length != 32) {
            throw new NetbiosException("NetBIOS name is not 32 bytes long", -503);
        }
        if (curOff == offset + 2) {
            result = offset + 2;
        } else {
            while (src[curOff] != 0) {
                resolver.setOffset(curOff);
                resolver.resolveLabel();
                curOff = resolver.getOffset();
            }
            result = curOff + 1;
        }
        return result;
    }

    public int parse(byte[] src, int offset) throws NqException {
        LabelResolver resolver = new LabelResolver(src, offset);
        int labelOff = resolver.resolveLabel();
        int curOff = resolver.getOffset();
        byte length = src[labelOff++];
        byte[] nameBytes = new byte[16];
        if (length != 32) {
            throw new NetbiosException("NetBIOS name is not 32 bytes long", -508);
        }
        int nameBytesLen = 0;
        for (int i = 0; i < 15; ++i) {
            nameBytes[i] = (byte)(src[labelOff++] - 65 << 4);
            int n = i;
            nameBytes[n] = (byte)(nameBytes[n] | (byte)(src[labelOff++] - 65));
            nameBytesLen = 0 != nameBytes[i] && 32 != nameBytes[i] ? i : nameBytesLen;
        }
        this.name = new String(nameBytes, 0, nameBytesLen + 1);
        this.role = (byte)(src[labelOff++] - 65 << 4);
        this.role |= (byte)(src[labelOff++] - 65);
        this.scopeId = "";
        if (src[curOff] != 0) {
            while (src[curOff] != 0) {
                resolver.setOffset(curOff);
                labelOff = resolver.resolveLabel();
                length = src[labelOff++];
                if (this.scopeId.length() > 0) {
                    this.scopeId = this.scopeId + ".";
                }
                this.scopeId = this.scopeId + new String(src, labelOff, (int)length);
                curOff = resolver.getOffset();
            }
        }
        return ++curOff;
    }

    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }
        NetbiosName other = (NetbiosName)obj;
        return other.name.equalsIgnoreCase(this.name) && other.group == this.group && other.role == this.role;
    }

    public int hashCode() {
        TraceLog.get().error("hasCode(); is used where not desired", 5, 0);
        return 0;
    }

    public int getRole() {
        return this.role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public void setName(String name) throws NetbiosException {
        if (null == name) {
            throw new NetbiosException("name is null", -508);
        }
        this.name = name;
    }

    private static class LabelResolver {
        private byte[] src;
        private int offset;

        public LabelResolver(byte[] src, int offset) {
            this.src = src;
            this.offset = offset;
        }

        public int resolveLabel() throws NqException {
            byte length = this.src[this.offset];
            short curOff = (short)this.offset;
            if ((length & 0xC0) == 192) {
                BufferReader reader = new BufferReader(this.src, this.offset, true);
                curOff = reader.readInt2();
                curOff = (curOff & 0x2000) != 0 ? (short)(curOff | 0xC000) : (short)(curOff & 0xFFFF3FFF);
                this.offset = reader.getOffset();
            } else {
                this.offset += length + 1;
            }
            return curOff;
        }

        public int getOffset() {
            return this.offset;
        }

        public void setOffset(int offset) {
            this.offset = offset;
        }
    }
}

