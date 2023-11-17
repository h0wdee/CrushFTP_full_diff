/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.client;

import com.visuality.nq.common.NqException;
import com.visuality.nq.common.Sid;
import com.visuality.nq.common.TraceLog;
import com.visuality.nq.common.Utility;
import com.visuality.nq.config.Config;
import java.util.HashMap;

public class SidCache {
    private static final long TIME_TO_LIVE_MINUTES = 1440L;
    static HashMap domainCache = new HashMap();
    static HashMap nameCache = new HashMap();

    public static Sid findSidByName(String serverName, String name) throws NqException {
        if (null == serverName || null == name) {
            throw new NqException("null serverName or name is not allowed", -20);
        }
        String key = serverName + name;
        Sid sid = (Sid)nameCache.get(key);
        TraceLog.get().message("findSidByName=" + name + "; server=" + serverName + "; sidttl=" + (null != sid ? Long.valueOf(sid.getTtl()) : "null"), 2000);
        if (null != sid && sid.getTtl() <= Utility.getCurrentTimeInSec()) {
            nameCache.remove(key);
            return null;
        }
        return sid;
    }

    public static Sid findSidByDomainName(String serverName, String name) throws NqException {
        if (null == serverName || null == name) {
            throw new NqException("null serverName or name is not allowed", -20);
        }
        String key = serverName + name;
        Sid sid = (Sid)domainCache.get(key);
        if (null != sid && sid.getTtl() <= Utility.getCurrentTimeInSec()) {
            domainCache.remove(key);
            return null;
        }
        return sid;
    }

    public static void addSidByName(String serverName, String name, Sid sid) throws NqException {
        if (null == sid || null == name || null == serverName) {
            throw new NqException("null serverName, name or sid is not allowed", -20);
        }
        TraceLog.get().message("addSidByName=" + name + "; server=" + serverName, 2000);
        SidCache.setTtl(sid);
        nameCache.put(serverName + name, sid);
    }

    public static void addSidByDomainName(String serverName, String name, Sid sid) throws NqException {
        if (null == sid || null == name || null == serverName) {
            throw new NqException("null serverName, name or sid is not allowed", -20);
        }
        SidCache.setTtl(sid);
        domainCache.put(serverName + name, sid);
    }

    private static void setTtl(Sid sid) {
        long ttl = 1440L;
        try {
            long configEntry = Config.jnq.getInt("SIDCACHETTL");
            if (0L < configEntry) {
                ttl = configEntry;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        sid.setTtl(Utility.getCurrentTimeInSec() + ttl * 60L);
    }
}

