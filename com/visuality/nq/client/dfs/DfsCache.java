/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.client.dfs;

import com.visuality.nq.client.ClientException;
import com.visuality.nq.client.ClientUtils;
import com.visuality.nq.client.dfs.DfsEntry;
import com.visuality.nq.client.dfs.DfsReferral;
import com.visuality.nq.client.dfs.Entry;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.TraceLog;
import com.visuality.nq.common.Utility;
import com.visuality.nq.config.Config;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public abstract class DfsCache {
    static final char BACKSLASH = '\\';
    static ConcurrentHashMap domainCache = new ConcurrentHashMap();
    static ConcurrentHashMap pathCache = new ConcurrentHashMap();

    static DfsEntry findDomain(String name) {
        TraceLog.get().enter(300);
        DfsEntry entry = (DfsEntry)domainCache.get(name);
        long currentTime = Utility.getCurrentTimeInSec();
        if (null != entry && currentTime > entry.ttl) {
            domainCache.remove(name);
            entry = null;
        }
        TraceLog.get().exit(300);
        return entry;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static DfsEntry findPath(String name) {
        TraceLog.get().enter(300);
        TraceLog.get().message("name=" + ClientUtils.filePathStripNull(name), 1000);
        DfsEntry entry = null;
        name = ClientUtils.filePathStripNull(name.toLowerCase());
        entry = (DfsEntry)pathCache.get(name);
        if (null == entry) {
            String nameTmp = ClientUtils.filePathStripLastComponent(name);
            for (DfsEntry entryTmp : pathCache.values()) {
                if (!nameTmp.startsWith(entryTmp.name) || nameTmp.length() != entryTmp.name.length() && name.charAt(entryTmp.name.length()) != '\\' || null != entry && entryTmp.name.length() <= entry.name.length()) continue;
                entry = entryTmp;
                if (nameTmp.length() != entry.name.length()) continue;
                break;
            }
            if (null != entry) {
                entry.isExactMatch = name.length() == entry.name.length();
            }
        } else {
            entry.isExactMatch = true;
        }
        long currentTime = Utility.getCurrentTimeInSec();
        if (null != entry && currentTime > entry.ttl) {
            pathCache.remove(entry.name);
            if (TraceLog.get().canLog(2000)) {
                TraceLog.get().message("Removed entry " + name + " from cache. Entry=" + entry, 2000);
            }
            entry = null;
        } else if (null != entry) {
            Iterator<DfsReferral> iterator = entry.referrals.iterator();
            List<DfsReferral> list = entry.referrals;
            synchronized (list) {
                while (iterator.hasNext()) {
                    DfsReferral referral = iterator.next();
                    if (referral.ttl >= currentTime) continue;
                    if (TraceLog.get().canLog(2000)) {
                        TraceLog.get().message("Removed referral dfsPath=" + referral.dfsPath + "; netpath=" + referral.netPath + "; ttl=" + referral.ttl, 2000);
                    }
                    iterator.remove();
                }
            }
        }
        if (TraceLog.get().canLog(2000)) {
            TraceLog.get().exit("Found entry = ", entry, 2000);
        }
        return entry;
    }

    static void removeDomain(String domain) {
        TraceLog.get().enter(300);
        TraceLog.get().message("Inside removeDomain; domain=", domain, 2000);
        DfsCache.removeReferral(domainCache, domain);
        TraceLog.get().exit(300);
    }

    static synchronized DfsEntry addDomain(String domain, String host) throws NqException {
        TraceLog.get().enter(300);
        DfsReferral ref = new DfsReferral();
        ref.netPath = host;
        ref.serverType = 1;
        ref.numPathConsumed = 0;
        long currentTime = Utility.getCurrentTimeInSec();
        ref.ttl = (long)Config.jnq.getInt("DFSCACHETTL") + currentTime;
        if (TraceLog.get().canLog(2000)) {
            TraceLog.get().message("Inside addDomain; domain=" + domain + "; ref=" + ref, 2000);
        }
        DfsEntry entry = DfsCache.addReferral(domainCache, domain, ref);
        TraceLog.get().exit(300);
        return entry;
    }

    static void removePath(String path) {
        TraceLog.get().enter(300);
        TraceLog.get().message("Inside removePath; path=", path, 2000);
        DfsCache.removeReferral(pathCache, path);
        TraceLog.get().exit(300);
    }

    static synchronized void addPath(String path, DfsReferral ref) throws NqException {
        TraceLog.get().enter(300);
        long currentTime = Utility.getCurrentTimeInSec();
        long defaultTtl = Config.jnq.getInt("DFSCACHETTL");
        ref.ttl = 0L == defaultTtl ? ref.originalTtl + currentTime : defaultTtl + currentTime;
        if (TraceLog.get().canLog(2000)) {
            TraceLog.get().message("Referral info; path=" + ClientUtils.filePathStripNull(path) + "; ref=" + ref, 2000);
        }
        DfsCache.addReferral(pathCache, ClientUtils.filePathStripNull(path), ref);
        TraceLog.get().exit(300);
    }

    private static synchronized DfsEntry addReferral(ConcurrentHashMap cache, String name, DfsReferral ref) throws ClientException {
        TraceLog.get().enter(300);
        if (TraceLog.get().canLog(2000)) {
            TraceLog.get().message("name = " + name + "; ref = " + ref + "; cache = " + cache, 2000);
            TraceLog.get().message("Cache size = " + cache.size() + ", cache type = " + (cache == pathCache ? "pathCache" : "domainCache"), 2000);
        }
        String entryKey = name;
        if (ref.numPathConsumed > 0) {
            entryKey = name.length() > ref.numPathConsumed ? name.substring(0, ref.numPathConsumed - 1) : name;
        }
        DfsEntry entry = (DfsEntry)cache.get(entryKey);
        boolean referralFound = false;
        if (null == entry) {
            entry = new DfsEntry();
            entry.isExactMatch = true;
            if (null != ref && ref.serverType == 1) {
                entry.isRoot = true;
            }
            entry.ttl = ref.ttl;
            entry.name = entryKey;
            if (TraceLog.get().canLog(2000)) {
                TraceLog.get().message("Inside addReferral, entry to be added; entryKey=" + entryKey + "; entry=" + entry, 2000);
            }
            cache.put(entryKey, entry);
        } else {
            for (DfsReferral refTmp : entry.referrals) {
                if (!refTmp.netPath.equals(ref.netPath)) continue;
                refTmp.ttl = ref.ttl;
                referralFound = true;
                break;
            }
        }
        if (!referralFound) {
            if (entry.ttl < ref.ttl) {
                entry.ttl = ref.ttl;
            }
            if (TraceLog.get().canLog(2000)) {
                TraceLog.get().message("Inside addReferral, referral to be added; entryKey=" + entryKey + "; ref=" + ref, 2000);
            }
            entry.referrals.add(ref);
        }
        TraceLog.get().exit(300);
        return entry;
    }

    private static void removeReferral(ConcurrentHashMap cache, String name) {
        TraceLog.get().enter(300);
        DfsEntry entry = (DfsEntry)cache.get(name);
        if (null != entry) {
            TraceLog.get().message("Inside removeReferral; name=", name, 2000);
            cache.remove(name.toLowerCase());
        }
        TraceLog.get().exit(300);
    }

    protected static void dumpCache(String netPath, String pathToCheck) {
        int logLevel = 2000;
        if (!TraceLog.get().canLog(2000)) {
            return;
        }
        Set keySet = pathCache.keySet();
        TraceLog.get().message("DfsCache Dump: for referral.netpath=" + netPath + "; pathToCheck=" + pathToCheck, logLevel);
        for (String name : keySet) {
            DfsEntry entry = (DfsEntry)pathCache.get(name);
            TraceLog.get().message("Dumping entry -> ", entry, logLevel);
            if (null == entry.referrals) {
                TraceLog.get().message("\tReferral list is null", logLevel);
            } else {
                Iterator<DfsReferral> iter = entry.referrals.iterator();
                while (iter.hasNext()) {
                    TraceLog.get().message("\t" + ((Object)iter.next()).toString(), logLevel);
                }
            }
            TraceLog.get().message("----", logLevel);
        }
    }

    public static Map getDfsCache() {
        return pathCache;
    }

    public static List<Entry> getReferralInfo() {
        TraceLog.get().enter(200);
        ArrayList<Entry> entryList = new ArrayList<Entry>();
        boolean dfsEnabled = true;
        try {
            dfsEnabled = Config.jnq.getBool("DFSENABLE");
        }
        catch (NqException e) {
            // empty catch block
        }
        Set keySet = DfsCache.getDfsCache().keySet();
        if (!dfsEnabled || null == keySet || keySet.size() == 0) {
            TraceLog.get().exit("Returning empty list.", 200);
            return entryList;
        }
        for (String name : keySet) {
            DfsEntry dfsEntry = (DfsEntry)DfsCache.getDfsCache().get(name);
            DfsEntry dfsEntryClone = null;
            try {
                dfsEntryClone = dfsEntry.clone();
            }
            catch (CloneNotSupportedException e) {
                TraceLog.get().error("Unable to clone Entry = ", dfsEntry, 200, 0);
            }
            if (null == dfsEntryClone) continue;
            entryList.add(dfsEntryClone);
        }
        TraceLog.get().exit(200);
        return entryList;
    }
}

