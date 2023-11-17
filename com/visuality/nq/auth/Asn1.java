/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.auth;

import com.visuality.nq.common.Blob;
import com.visuality.nq.common.BufferReader;
import com.visuality.nq.common.BufferWriter;
import com.visuality.nq.common.NqException;
import java.util.Arrays;

class Asn1 {
    public static final int ASN1_1_2 = 42;
    public static final int ASN1_1_3 = 43;
    public static final int APPLICATION = 96;
    public static final int SIMPLE = 64;
    public static final int CONTEXT = 160;
    public static final int SEQUENCE = 48;
    public static final int ENUMERATED = 10;
    public static final int OID = 6;
    public static final int BINARY = 4;
    public static final int STRING = 27;
    public static final int NOTAG = 0;

    Asn1() {
    }

    static boolean asn1ParseCompareOid(BufferReader reader, Blob oid, boolean toRevertOnMismatch) throws NqException {
        int savedCurrent = reader.getOffset();
        int dataLen = 0;
        boolean res = false;
        int[] tag_leng = Asn1.asn1ParseTag(reader, dataLen);
        dataLen = tag_leng[1];
        if (6 != tag_leng[0]) {
            if (toRevertOnMismatch) {
                reader.setOffset(savedCurrent);
            }
            return res;
        }
        if (dataLen == oid.len) {
            int savedOffset = reader.getOffset();
            byte[] tmp = new byte[dataLen];
            reader.readBytes(tmp, dataLen);
            reader.setOffset(savedOffset);
            res = Arrays.equals(oid.data, tmp);
        } else {
            res = false;
        }
        if (reader.getRemaining() < dataLen) {
            res = false;
            if (toRevertOnMismatch) {
                reader.setOffset(savedCurrent);
            }
            return res;
        }
        reader.skip(dataLen);
        if (!res && toRevertOnMismatch) {
            reader.setOffset(savedCurrent);
        }
        return res;
    }

    public static int[] asn1ParseTag(BufferReader reader, int dataLen) throws NqException {
        int[] tag_len = new int[]{0, 0};
        byte next = reader.readByte();
        tag_len[0] = 0xFF & next;
        next = reader.readByte();
        if (0 == (next & 0x80)) {
            tag_len[1] = 0xFF & next;
        } else {
            dataLen = 0;
            for (int i = next & 0x7F; i > 0; --i) {
                next = reader.readByte();
                dataLen = dataLen * 256 + (0xFF & next);
            }
            tag_len[1] = dataLen;
        }
        return tag_len;
    }

    public static int asn1PackLen(int dataLen) {
        int res;
        if (dataLen > 127) {
            res = 1;
            while (dataLen > 0) {
                dataLen /= 256;
                ++res;
            }
        } else {
            res = 1;
        }
        return res;
    }

    public static int asn1GetElementLength(int lenght) {
        return 1 + Asn1.asn1PackLen(lenght) + lenght;
    }

    public static void asn1PackTag(BufferWriter ds, int tag, int dataLen) {
        ds.writeByte((byte)tag);
        if (dataLen > 127) {
            int i;
            byte[] lenBuf = new byte[5];
            for (i = 4; i >= 0 && dataLen > 0; dataLen /= 256, --i) {
                lenBuf[i] = (byte)(dataLen % 256);
            }
            ds.writeByte((byte)(0x80 | 5 - i - 1));
            byte[] tmp = new byte[lenBuf.length - (i + 1)];
            System.arraycopy(lenBuf, i + 1, tmp, 0, tmp.length);
            ds.writeBytes(tmp, 5 - i - 1);
        } else {
            ds.writeByte((byte)dataLen);
        }
    }

    public static void asn1PackOid(BufferWriter ds, Blob oid) {
        if (null != oid) {
            Asn1.asn1PackTag(ds, 6, oid.len);
            ds.writeBytes(oid.data, oid.len);
        }
    }
}

