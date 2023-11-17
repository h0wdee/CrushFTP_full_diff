/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.client;

import com.visuality.nq.common.BufferReader;
import com.visuality.nq.common.BufferWriter;
import com.visuality.nq.common.NqException;
import java.util.Vector;

public abstract class Smb1Transaction {
    private static Object syncObj = new Object();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void createGetBackupListRequest(BufferWriter writer, int tranId) {
        writer.writeBytes(new byte[]{-1, 83, 77, 66, 37, 0, 0, 0, 0, 24, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 17, 0, 0, 6, 0, 0, 0, 0, 0, 3, 0, 0, 0, -24, 3, 0, 0, 0, 0, 0, 0, 86, 0, 6, 0, 86, 0, 3, 0, 1, 0, 1, 0, 2, 0, 23, 0, 92, 77, 65, 73, 76, 83, 76, 79, 84, 92, 66, 82, 79, 87, 83, 69, 0, 9, 10});
        Object object = syncObj;
        synchronized (object) {
            writer.writeInt2(++tranId);
        }
    }

    public static Vector parseGetBackupListReesponse(BufferReader reader) throws NqException {
        reader.skip(32);
        reader.skip(55);
        int numServers = reader.readByte();
        reader.skip(4);
        byte[] data = reader.getSrc();
        Vector<String> names = new Vector<String>();
        int nameStart = reader.getOffset();
        while (numServers-- > 0) {
            int nameEnd = nameStart;
            while (data[nameEnd] != 0) {
                ++nameEnd;
            }
            String name = new String(data, nameStart, nameEnd - nameStart);
            names.add(name);
            nameStart = nameEnd + 1;
        }
        return names;
    }
}

