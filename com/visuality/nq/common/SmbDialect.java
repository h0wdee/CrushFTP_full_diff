/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.common;

import com.visuality.nq.common.NqException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public final class SmbDialect {
    private static Map smbDialects = new LinkedHashMap();
    public static int SMB2ANY_DIALECTREVISION;

    public short getDialectCount() {
        short count = 0;
        for (DialectEnvelop dialect : smbDialects.values()) {
            if (dialect.code == 256 || dialect.code == 767 || !dialect.isEnabled) continue;
            count = (short)(count + 1);
        }
        return count;
    }

    public short[] getSmbDialectList() {
        short[] dialectList = new short[this.getDialectCount()];
        Iterator it = smbDialects.values().iterator();
        int i = 0;
        while (it.hasNext()) {
            DialectEnvelop dialect = (DialectEnvelop)it.next();
            if (dialect.code == 256 || dialect.code == 767 || !dialect.isEnabled) continue;
            dialectList[i++] = dialect.code;
        }
        return dialectList;
    }

    public static short[] getSmbDialectList(int minDialect, int maxDialect) {
        ArrayList<Integer> dialectList = new ArrayList<Integer>();
        for (DialectEnvelop dialect : smbDialects.values()) {
            if (!dialect.isEnabled || 0 < minDialect && dialect.code < minDialect || 0 < maxDialect && maxDialect < dialect.code) continue;
            dialectList.add(Integer.valueOf(dialect.code));
        }
        short[] dialectArray = new short[dialectList.size()];
        int i = 0;
        for (Integer dialectItem : dialectList) {
            dialectArray[i++] = dialectItem.shortValue();
        }
        return dialectArray;
    }

    public boolean hasSmb(short code) throws NqException {
        DialectEnvelop dialect = (DialectEnvelop)smbDialects.get(code);
        if (null == dialect) {
            throw new NqException("Unknown dialect: " + code, -20);
        }
        return dialect.isEnabled;
    }

    public boolean supportSmb2() throws NqException {
        boolean res = false;
        short[] dialects = new short[]{514, 528, 768, 770, 785};
        for (int i = 0; i < dialects.length; ++i) {
            short curDialect = dialects[i];
            DialectEnvelop dialect = (DialectEnvelop)smbDialects.get(curDialect);
            if (null == dialect) {
                throw new NqException("Unknown dialect: " + curDialect, -20);
            }
            if (!dialect.isEnabled) continue;
            res = true;
            break;
        }
        return res;
    }

    public void enableDialect(short code, boolean enable) throws NqException {
        DialectEnvelop dialect = (DialectEnvelop)smbDialects.get(code);
        if (null == dialect) {
            throw new NqException("Unknown dialect: " + code, -20);
        }
        dialect.isEnabled = enable;
    }

    static {
        smbDialects.put((short)256, new DialectEnvelop(256, "NT LM 0.12", true));
        smbDialects.put((short)514, new DialectEnvelop(514, "SMB 2.002", true));
        smbDialects.put((short)528, new DialectEnvelop(528, "SMB 2.100", true));
        smbDialects.put((short)768, new DialectEnvelop(768, "SMB 3.0.0", true));
        smbDialects.put((short)770, new DialectEnvelop(770, "SMB 3.0.2", true));
        smbDialects.put((short)785, new DialectEnvelop(785, "SMB 3.1.1", true));
    }

    private static class DialectEnvelop {
        private short code;
        private String name;
        private boolean isEnabled;
        private boolean serverEnabled;

        protected DialectEnvelop(short code, String name, boolean isEnabled) {
            this.code = code;
            this.name = name;
            this.isEnabled = isEnabled;
        }
    }
}

