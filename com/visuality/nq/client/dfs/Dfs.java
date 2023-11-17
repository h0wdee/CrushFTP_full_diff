/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.client.dfs;

import com.visuality.nq.auth.Credentials;
import com.visuality.nq.client.ClientException;
import com.visuality.nq.client.ClientSmb;
import com.visuality.nq.client.ClientUtils;
import com.visuality.nq.client.Mount;
import com.visuality.nq.client.MountParams;
import com.visuality.nq.client.Server;
import com.visuality.nq.client.Share;
import com.visuality.nq.client.dfs.DfsCache;
import com.visuality.nq.client.dfs.DfsEntry;
import com.visuality.nq.client.dfs.DfsReferral;
import com.visuality.nq.client.dfs.Result;
import com.visuality.nq.common.BufferReader;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.Resolver;
import com.visuality.nq.common.TraceLog;
import com.visuality.nq.config.Config;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Dfs {
    static final int DFS_ROOT_TARGET = 1;
    static final int DFS_LINK_TARGET = 0;
    static boolean dfsOn = true;
    private List referralList = null;
    public int counter;
    public int lastError;
    public DfsReferral referral;

    static void setDfsOn(boolean on) {
        dfsOn = on;
    }

    static boolean isDfsOn() {
        return dfsOn;
    }

    public Dfs(int count) {
        this.counter = count;
    }

    public static String resolveHost(String host) throws NqException {
        TraceLog.get().enter(300);
        if (null == host) {
            TraceLog.get().exit(300);
            return null;
        }
        if (!dfsOn) {
            TraceLog.get().exit(300);
            return host;
        }
        DfsEntry entry = DfsCache.findDomain(host.toLowerCase());
        if (null != entry && null != entry.referrals && !entry.referrals.isEmpty()) {
            DfsReferral referral = entry.referrals.get(0);
            if (null == referral.netPath) {
                TraceLog.get().exit(300);
                return null;
            }
            TraceLog.get().exit(300);
            return referral.netPath;
        }
        String dcName = new Resolver().getDCNameByDomain(host);
        if (null == dcName) {
            DfsCache.addDomain(host.toLowerCase(), null);
            TraceLog.get().exit(300);
            return null;
        }
        DfsCache.addDomain(host.toLowerCase(), dcName);
        TraceLog.get().exit(300);
        return dcName;
    }

    public void setIsGood(boolean isGood) {
        DfsEntry entry = DfsCache.findPath(this.referral.dfsPath);
        if (null == entry) {
            TraceLog.get().message("Null entry in setIsGood=" + isGood);
            return;
        }
        List<DfsReferral> ll = entry.referrals;
        for (DfsReferral ref : ll) {
            if (!ref.netPath.equals(this.referral.netPath)) continue;
            ref.isGood = isGood;
            TraceLog.get().message("Setting referral field isGood to true; ", ref, 2000);
        }
    }

    protected static Result createResultPath(String path, int numPathConsumed, String newPath, Result res) {
        TraceLog.get().enter(300);
        if (TraceLog.get().canLog(1000)) {
            TraceLog.get().message("path=" + ClientUtils.filePathStripNull(path) + ", numPathConsumed=" + numPathConsumed + ", newPath=" + newPath + ", res=", res, 1000);
        }
        path = ClientUtils.filePathStripNull(path);
        if (null != newPath && res.share != null) {
            String from;
            String string = from = path.length() > numPathConsumed ? path.substring(numPathConsumed) : null;
            if (null != from) {
                res.path = newPath;
                if (res.path.charAt(res.path.length() - 1) != '\\' && from.charAt(0) != '\\') {
                    res.path = res.path.concat("\\");
                }
                res.path = res.path.concat(from);
            } else {
                res.path = newPath;
            }
            res.server = res.share.getUser().getServer();
        } else if (res.share == null) {
            res = null;
        }
        TraceLog.get().exit(300);
        return res;
    }

    public Result resolvePath(Mount mount, MountParams mountParams, Share share, String fileName, Server originalServer) throws NqException {
        TraceLog.get().enter(300);
        if (TraceLog.get().canLog(1000)) {
            TraceLog.get().message("fileName=" + fileName + "; originalServer=" + originalServer + "mount=" + mount + "; share=" + share, 1000);
        }
        if (!dfsOn) {
            TraceLog.get().exit(300);
            return null;
        }
        Result res = new Result();
        String path = ClientUtils.composeDfsRemotePathToFile(share.getUser().getServer().getName(), share.getName(), fileName);
        if (null != this.referral && this.lastError != -1073741225) {
            DfsReferral contextReferral = this.referral;
            DfsReferral referral = null;
            contextReferral.isIOPerformed = true;
            contextReferral.lastIOStatus = this.lastError;
            NqException savedException = null;
            if (null != this.referralList) {
                Iterator iterator = this.referralList.iterator();
                while (iterator.hasNext() && res.share == null) {
                    referral = (DfsReferral)iterator.next();
                    TraceLog.get().message("Working on referral ", referral, 2000);
                    if (referral == contextReferral) {
                        referral = null;
                        continue;
                    }
                    if (referral.isIOPerformed) continue;
                    Credentials credentials = share.getUser().getCredentials().cloneCred();
                    try {
                        res.share = Share.connectShareInternally(referral.netPath, mount, mountParams, credentials, false, originalServer, share);
                        savedException = null;
                    }
                    catch (NqException e) {
                        savedException = e;
                        referral.isGood = false;
                        referral = null;
                    }
                    if (res.share == null) continue;
                    this.referral = referral;
                    break;
                }
                if (null != savedException) {
                    TraceLog.get().error("No valid referral found; ", savedException, 300, savedException.getErrCode());
                    TraceLog.get().caught(savedException, 300);
                    throw savedException;
                }
            }
            if (null == referral) {
                TraceLog.get().exit(300);
                return null;
            }
            res = Dfs.createResultPath(path, referral != null ? referral.numPathConsumed : 0, referral != null ? referral.netPath : null, res);
            TraceLog.get().exit(300);
            return res;
        }
        if (0 == (share.getUser().getServer().capabilities & 2)) {
            TraceLog.get().exit(300);
            return null;
        }
        DfsEntry cacheEntry = DfsCache.findPath(ClientUtils.filePathStripNull(path));
        if (null != cacheEntry) {
            res = cacheEntry.isExactMatch ? this.resolvePathWithCacheEntryExactMatch(mount, mountParams, share, res, path, cacheEntry, originalServer) : this.resolvePathWithCacheEntryNotExactMatch(mount, mountParams, share, res, path, cacheEntry, originalServer);
            if (null == res) {
                TraceLog.get().message("Unable to use referrals from cache entry.  Requesting new referrals for path = ", ClientUtils.filePathStripNull(path), 300);
                DfsCache.removePath(ClientUtils.filePathStripNull(path));
                res = this.resolvePathNotInCache(mount, mountParams, share, res, path, originalServer);
            }
        } else {
            res = this.resolvePathNotInCache(mount, mountParams, share, res, path, originalServer);
        }
        TraceLog.get().exit(300);
        return res;
    }

    private Result resolvePathNotInCache(Mount mount, MountParams mountParams, Share share, Result res, String path, Server originalServer) throws NqException {
        TraceLog.get().enter(300);
        if (TraceLog.get().canLog(1000)) {
            TraceLog.get().message("mount=" + mount + "; share=" + share + "; res=" + res + "; path=" + ClientUtils.filePathStripNull(path), 1000);
        }
        NqException savedException = null;
        try {
            this.resolveReferrals(mount, mountParams, share, path, originalServer);
        }
        catch (NqException e) {
            if (!Dfs.isSkipReferralError(e.getErrCode())) {
                if (Mount.checkForSessionDeletedOrExpired(e)) {
                    TraceLog.get().caught(e, 10);
                    throw e;
                }
                savedException = e;
                if (null == path) {
                    TraceLog.get().message("DFS resolve : path is null", 700);
                } else {
                    TraceLog.get().message("DFS resolve : " + ClientUtils.filePathStripNull(path) + " failed", 700);
                }
            }
            TraceLog.get().caught(e, 2000);
        }
        DfsEntry cacheEntry = DfsCache.findPath(path);
        if (null != cacheEntry) {
            savedException = null;
            DfsReferral ref = null;
            Iterator<DfsReferral> iterator = cacheEntry.referrals.iterator();
            while (iterator.hasNext() && null != res && null == res.share) {
                ref = iterator.next();
                if (null != ref.share) {
                    res.share = ref.share;
                    res.server = res.share.getUser().getServer();
                    break;
                }
                Credentials creds = share.getUser().getCredentials().cloneCred();
                try {
                    res.share = Share.connectShareInternally(ref.netPath, mount, mountParams, creds, false, originalServer, share);
                    savedException = null;
                }
                catch (NqException e) {
                    savedException = e;
                    TraceLog.get().message("Error connecting to " + ref.netPath + "; ", e, 2000);
                    ref.isGood = false;
                    ref = null;
                }
                res.server = res.share.getUser().getServer();
            }
            if (null != savedException) {
                TraceLog.get().error("No valid referral found; ", savedException, 300, savedException.getErrCode());
                TraceLog.get().caught(savedException, 300);
                throw savedException;
            }
            if (null != res && null != res.share) {
                this.referral = ref;
            }
            if (null == ref) {
                TraceLog.get().error("No valid referral found; ref is null", 300);
                throw new NqException("No valid referral found; ref is null", -1073741203);
            }
            path = ClientUtils.filePathStripNull(path);
            res = Dfs.createResultPath(path, ref.numPathConsumed, ref.netPath, res);
            TraceLog.get().exit(300);
            return res;
        }
        if (null != savedException) {
            TraceLog.get().error("No valid referral found; " + savedException, 300);
            TraceLog.get().caught(savedException, 300);
            throw savedException;
        }
        TraceLog.get().exit(300);
        return res;
    }

    private Result resolvePathWithCacheEntryNotExactMatch(Mount mount, MountParams mountParams, Share share, Result res, String path, DfsEntry cacheEntry, Server originalServer) throws NqException {
        TraceLog.get().enter("path=" + ClientUtils.filePathStripNull(path) + "; ", res, 300);
        if (TraceLog.get().canLog(2000)) {
            TraceLog.get().message(share, 2000);
            TraceLog.get().message(cacheEntry, 2000);
            TraceLog.get().message("originalServer = ", originalServer, 2000);
            TraceLog.get().message(mount, 2000);
        }
        if (cacheEntry.isRoot) {
            Credentials creds;
            Share rootShare = null;
            DfsReferral ref = null;
            Iterator<DfsReferral> iterator = cacheEntry.referrals.iterator();
            NqException savedException = null;
            while (iterator.hasNext() && rootShare == null) {
                ref = iterator.next();
                creds = share.getUser().getCredentials().cloneCred();
                try {
                    rootShare = Share.connectShareInternally(ref.netPath, mount, mountParams, creds, false, originalServer, share);
                    savedException = null;
                }
                catch (NqException e) {
                    savedException = e;
                    ref.isGood = false;
                    ref = null;
                }
            }
            if (null != savedException) {
                TraceLog.get().error("No valid referral found; ", savedException, 300, savedException.getErrCode());
                TraceLog.get().caught(savedException, 300);
                throw savedException;
            }
            if (null != rootShare) {
                try {
                    this.resolveReferrals(mount, mountParams, rootShare, path, originalServer);
                }
                catch (NqException e) {
                    TraceLog.get().error("DFS resolve : " + ClientUtils.filePathStripNull(path) + "failed", 700, e.getErrCode());
                    TraceLog.get().caught(e, 10);
                    throw e;
                }
                cacheEntry = DfsCache.findPath(path);
                if (null != cacheEntry) {
                    ref = null;
                    iterator = cacheEntry.referrals.iterator();
                    while (iterator.hasNext() && res.share == null) {
                        ref = iterator.next();
                        creds = share.getUser().getCredentials().cloneCred();
                        if (null != ref.share) {
                            res.share = ref.share;
                            continue;
                        }
                        try {
                            res.share = Share.connectShareInternally(ref.netPath, mount, mountParams, creds, false, originalServer, share);
                        }
                        catch (NqException e) {
                            ref.isGood = false;
                            ref = null;
                        }
                    }
                    if (null == ref) {
                        TraceLog.get().exit(300);
                        return null;
                    }
                    if (null != res.share) {
                        this.referral = ref;
                    }
                    res = Dfs.createResultPath(path, ref.numPathConsumed, ref.netPath, res);
                    TraceLog.get().exit(300);
                    return res;
                }
            }
        } else {
            DfsReferral ref = null;
            boolean doExit = false;
            boolean isFirstTimeThrough = true;
            while (!doExit) {
                Iterator<DfsReferral> iterator = cacheEntry.referrals.iterator();
                while (iterator.hasNext() && res.share == null) {
                    ref = iterator.next();
                    if (isFirstTimeThrough && !ref.isGood) {
                        ref = null;
                        continue;
                    }
                    Credentials creds = share.getUser().getCredentials().cloneCred();
                    try {
                        if (!isFirstTimeThrough && ref.isIOPerformed) {
                            ref.isIOPerformed = false;
                            ref = null;
                            continue;
                        }
                        res.share = Share.connectShareInternally(ref.netPath, mount, mountParams, creds, false, originalServer, share);
                        ref.isGood = true;
                        doExit = true;
                        break;
                    }
                    catch (ClientException ce) {
                        TraceLog.get().error("ClientException = ", ce, 2000, ce.getErrCode());
                        ref.isGood = false;
                        ref.isIOPerformed = true;
                        ref = null;
                    }
                }
                if (isFirstTimeThrough) {
                    isFirstTimeThrough = false;
                    continue;
                }
                doExit = true;
            }
            if (null == ref) {
                TraceLog.get().exit(300);
                return null;
            }
            if (null != res.share) {
                this.referral = ref;
            }
            res = Dfs.createResultPath(path, ref.numPathConsumed, ref.netPath, res);
            TraceLog.get().exit(300);
            return res;
        }
        TraceLog.get().exit(300);
        return res;
    }

    protected Result resolvePathWithCacheEntryExactMatch(Mount mount, MountParams mountParams, Share share, Result res, String path, DfsEntry cacheEntry, Server originalServer) throws NqException {
        TraceLog.get().enter(300);
        if (TraceLog.get().canLog(2000)) {
            TraceLog.get().message("mount=" + mount + "; share=" + share + "; res=" + res + "; path=" + ClientUtils.filePathStripNull(path) + "; cacheEntry=" + cacheEntry, 2000);
        }
        DfsReferral ref2 = null;
        boolean doExit = false;
        boolean isFirstTimeThrough = true;
        NqException savedException = null;
        while (!doExit) {
            for (DfsReferral ref2 : cacheEntry.referrals) {
                if (isFirstTimeThrough && !ref2.isGood) {
                    ref2 = null;
                    continue;
                }
                Credentials creds = share.getUser().getCredentials().cloneCred();
                try {
                    if (!isFirstTimeThrough && ref2.isIOPerformed) {
                        ref2.isIOPerformed = false;
                        ref2 = null;
                        continue;
                    }
                    res.share = Share.connectShareInternally(ref2.netPath, mount, mountParams, creds, false, originalServer, share);
                    ref2.isGood = true;
                    doExit = true;
                    savedException = null;
                    break;
                }
                catch (ClientException ce) {
                    savedException = ce;
                    TraceLog.get().error("ClientException = ", ce, 2000, ce.getErrCode());
                    ref2.isGood = false;
                    ref2.isIOPerformed = true;
                    ref2 = null;
                }
            }
            if (isFirstTimeThrough) {
                isFirstTimeThrough = false;
                continue;
            }
            doExit = true;
        }
        if (null != savedException) {
            TraceLog.get().error("No valid referral found; ", savedException, 300, savedException.getErrCode());
            TraceLog.get().caught(savedException, 300);
            throw savedException;
        }
        if (null != ref2) {
            this.referral = ref2;
            res = Dfs.createResultPath(path, ref2.numPathConsumed, ref2.netPath, res);
            TraceLog.get().exit(300);
            return res;
        }
        TraceLog.get().exit(300);
        return null;
    }

    private void resolveReferrals(Mount mount, MountParams mountParams, Share share, String path, Server originalServer) throws NqException {
        TraceLog.get().enter("path=" + ClientUtils.filePathStripNull(path), 300);
        if (TraceLog.get().canLog(2000)) {
            TraceLog.get().message(share, 2000);
            TraceLog.get().message("originalServer = ", originalServer, 2000);
            TraceLog.get().message(mount, 2000);
        }
        List refs = Collections.synchronizedList(new LinkedList());
        Credentials credentials = share.getUser().getCredentials().cloneCred();
        String netPath = null;
        Server server = share.getUser().getServer();
        Share ipc = null;
        try {
            ipc = Share.connectIpc(server, credentials);
        }
        catch (NqException e) {
            TraceLog.get().error("Could not use IPC$, NqException = ", e, 2000);
            TraceLog.get().exit(300);
            return;
        }
        if (null != ipc) {
            server.smb.doQueryDfsReferrals(ipc, path, new Parser(refs));
            this.referralList = refs;
            Iterator iterator = refs.iterator();
            boolean searchForFirstSuccessfulReferral = true;
            Share refShare = null;
            NqException savedException = null;
            while (iterator.hasNext()) {
                DfsReferral ref = (DfsReferral)iterator.next();
                if (ClientUtils.filePathStripNull(path.toLowerCase()).equals(ref.netPath.toLowerCase())) {
                    throw new NqException("The DFS referral points to itself", -27);
                }
                if (searchForFirstSuccessfulReferral) {
                    String dcName;
                    String domain = ClientUtils.hostNameFromRemotePath(ref.netPath);
                    DfsEntry entry = DfsCache.findDomain(domain.toLowerCase());
                    if (null == entry && null != (dcName = new Resolver().getDCNameByDomain(domain))) {
                        DfsCache.addDomain(domain.toLowerCase(), dcName);
                        entry = DfsCache.findDomain(domain.toLowerCase());
                    }
                    if (null != entry && null != entry.referrals && !entry.referrals.isEmpty()) {
                        String shareNameComponent = ClientUtils.shareNameFromRemotePath(ref.netPath);
                        String pathComponent = ClientUtils.fileNameFromRemotePath(ref.netPath, true);
                        DfsReferral nextRef = entry.referrals.get(0);
                        if (!ClientUtils.composeDfsRemotePathToFile(nextRef.netPath, shareNameComponent, null).equals(path)) {
                            String domainReferral = null;
                            try {
                                domainReferral = this.getDomainReferral(nextRef.netPath, shareNameComponent, credentials, originalServer, share, mountParams);
                            }
                            catch (NqException e) {
                                TraceLog.get().message("The referral is not domain's DC, NqException = ", e, 2000);
                            }
                            if (null != domainReferral) {
                                netPath = ClientUtils.composePath(domainReferral, pathComponent);
                            }
                        }
                    }
                    if (null == netPath) {
                        netPath = ref.netPath;
                    }
                    try {
                        refShare = Share.connectShareInternally(netPath, mount, mountParams, credentials, false, originalServer, share);
                        ref.netPath = netPath;
                        ref.share = refShare;
                        ref.isGood = true;
                        searchForFirstSuccessfulReferral = false;
                        savedException = null;
                    }
                    catch (NqException e) {
                        savedException = e;
                        ref.isGood = false;
                        ref = null;
                        TraceLog.get().error("NqException = ", e, 10, e.getErrCode());
                    }
                }
                if (null != ref) {
                    DfsCache.addPath(path.toLowerCase(), ref);
                }
                netPath = null;
            }
            if (null != savedException) {
                TraceLog.get().error("No share connected internally; ", savedException, 300, savedException.getErrCode());
                TraceLog.get().caught(savedException, 300);
                throw savedException;
            }
            if (null == refShare) {
                TraceLog.get().error("No share connected internally", 300, 0);
                throw new NqException("No share connected internally", -111);
            }
        }
        TraceLog.get().exit(300);
    }

    private String getDomainReferral(String dcName, String path, Credentials credentials, Server originalServer, Share originalShare, MountParams mountParams) throws NqException {
        TraceLog.get().enter(300);
        if (TraceLog.get().canLog(1000)) {
            TraceLog.get().message("dcName=" + dcName + "; path=" + path + "; originalServer=" + originalServer, 1000);
        }
        String result = null;
        if (null == dcName) {
            TraceLog.get().exit(300);
            return null;
        }
        String ipcPath = ClientUtils.composeRemotePathToShare(dcName, "IPC$");
        Share share = Share.connectShareInternally(ipcPath, null, mountParams, credentials, false, originalServer, originalShare);
        if (null != share) {
            LinkedList refs = new LinkedList();
            String newPath = ClientUtils.composeDfsRemotePathToFile(dcName, path, null);
            share.getUser().getServer().smb.doQueryDfsReferrals(share, newPath, new Parser(refs));
            for (DfsReferral domainRef : refs) {
                credentials = share.getUser().getCredentials().cloneCred();
                Share refShare = Share.connectShareInternally(domainRef.netPath, null, mountParams, credentials, false, originalServer, share);
                if (refShare == null) continue;
                DfsCache.addPath(path.toLowerCase(), domainRef);
                result = domainRef.netPath;
                break;
            }
        }
        TraceLog.get().exit(300);
        return result;
    }

    public static boolean isDfsError(int status) {
        int[] errorCodes = new int[]{-1073741225, -1073741620, -1073741634, -1073741766, -1073741628, -1073741635, -1073741623, -1073741633, -1073741252, -1073741251, -1073741462, -1073741666, -2147483632, -1073741612, -1073741810, -1073741412, -1073741203, -1073741801, -1073741643, -1073741637, -1073741470, -1073741670, -1073741300, -1073741258, -1073741299, -1073741309, -1073741610, -1073741636, -1073741629, -1073741616, -1073741422, -1073741729, -1073741617, -1073741790, -1073741714};
        for (int i = 0; i < errorCodes.length; ++i) {
            if (errorCodes[i] != status) continue;
            return true;
        }
        return false;
    }

    public static boolean isSkipReferralError(int status) {
        int[] errorCodes = new int[]{-1073741412, -1073741275, -1073741809, -1073741810, -27};
        for (int i = 0; i < errorCodes.length; ++i) {
            if (errorCodes[i] != status) continue;
            return true;
        }
        return false;
    }

    public static Result findInCache(Mount mount, Share share, String shareRelativePath) throws NqException {
        Result res;
        TraceLog.get().enter(300);
        if (TraceLog.get().canLog(1000)) {
            TraceLog.get().message("shareRelativePath = " + shareRelativePath + "; mount = " + mount, 1000);
        }
        DfsEntry entry = null;
        DfsEntry entryMatched = null;
        DfsReferral referral = null;
        HashMap<DfsEntry, String> entriesAndPaths = new HashMap<DfsEntry, String>();
        String pathToCheck = ClientUtils.composeDfsRemotePathToFile(share.getUser().getServer().getName(), share.getName(), shareRelativePath);
        pathToCheck = ClientUtils.filePathStripNull(pathToCheck);
        int dfsNumOfRetries = 50;
        Result tmpRes = res = new Result();
        Dfs dfs = new Dfs(50);
        entry = DfsCache.findPath(pathToCheck);
        while (0 <= --dfsNumOfRetries && null != entry) {
            entryMatched = entry;
            int numberOfCharsThatMatch = 0;
            for (DfsReferral ref : entry.referrals) {
                int matchedChars = Dfs.matchPathLengths(pathToCheck, ref.dfsPath);
                if (matchedChars <= numberOfCharsThatMatch) continue;
                numberOfCharsThatMatch = matchedChars;
                referral = ref;
            }
            if (null == referral || null == referral.dfsPath) break;
            if (null == entry || null == referral) continue;
            tmpRes.share = share;
            tmpRes = Dfs.createResultPath(pathToCheck, referral.numPathConsumed, referral.netPath, tmpRes);
            if (pathToCheck.toLowerCase().equals(tmpRes.path.toLowerCase())) {
                TraceLog.get().exit(300);
                return null;
            }
            tmpRes = dfs.resolvePathWithCacheEntryExactMatch(mount, mount.getMountParams(), share, tmpRes, pathToCheck, entry, null);
            if (null == tmpRes) {
                tmpRes = res;
                continue;
            }
            res = tmpRes;
            String newPathToCheck = ClientUtils.composeDfsRemotePathToFile(res.share.getUser().getServer().getName(), res.share.getName(), ClientUtils.fileNameFromRemotePath(res.path, true));
            entry = DfsCache.findPath(newPathToCheck);
            if (null != entry) {
                if (entriesAndPaths.containsKey(entry) && ((String)entriesAndPaths.get(entry)).toLowerCase().equals(newPathToCheck.toLowerCase()) && !entry.isRoot) {
                    TraceLog.get().exit("DFS recursive configuration error: ", entry, 300);
                    throw new NqException("DFS recursive configuration error: " + entry, -25);
                }
                if (!entriesAndPaths.containsKey(entry)) {
                    entriesAndPaths.put(entry, newPathToCheck);
                }
            }
            if (ClientUtils.filePathStripNull(referral.netPath).toLowerCase().equals(pathToCheck.toLowerCase())) break;
            if (entryMatched == entry) {
                TraceLog.get().exit("Returning res = ", res, 300);
                return res;
            }
            if (null != entry) {
                pathToCheck = newPathToCheck;
                continue;
            }
            TraceLog.get().exit(300);
            return res;
        }
        TraceLog.get().exit(300);
        return null;
    }

    protected static int matchPathLengths(String a, String b) {
        int len1 = a.length();
        int len2 = b.length();
        int n = Math.min(len1, len2);
        char[] v1 = a.toLowerCase().toCharArray();
        char[] v2 = b.toLowerCase().toCharArray();
        int i = 0;
        int j = 0;
        while (n-- != 0) {
            char c2;
            char c1;
            if ((c1 = v1[i++]) == (c2 = v2[j++])) continue;
            return i;
        }
        return i;
    }

    private class Parser
    implements ClientSmb.ParseReferral {
        private List list;

        public Parser(List list) {
            this.list = list;
        }

        public void parse(BufferReader reader) throws NqException {
            short pathConsumed = reader.readInt2();
            reader.skip(4);
            for (short numRefs = reader.readInt2(); numRefs > 0; numRefs = (short)(numRefs - 1)) {
                int ttl;
                String netPath;
                String dfsPath;
                int entryStart = reader.getOffset();
                short vers = reader.readInt2();
                short size = reader.readInt2();
                short serverType = reader.readInt2();
                short flags = reader.readInt2();
                int originalTtl = Config.jnq.getInt("DFSCACHETTL");
                switch (vers) {
                    case 1: {
                        dfsPath = null;
                        netPath = reader.readString();
                        ttl = originalTtl;
                        break;
                    }
                    case 2: {
                        reader.skip(4);
                    }
                    case 3: 
                    case 4: {
                        ttl = reader.readInt4();
                        if (0 != originalTtl) {
                            ttl = originalTtl;
                        } else {
                            originalTtl = ttl;
                        }
                        short offset = reader.readInt2();
                        if (0 != (flags & 2)) {
                            short num = reader.readInt2();
                            short expOffset = reader.readInt2();
                            reader.setOffset(entryStart + offset);
                            dfsPath = reader.readString();
                            if (num == 0) {
                                netPath = dfsPath;
                                dfsPath = null;
                                break;
                            }
                            reader.setOffset(entryStart + expOffset);
                            netPath = reader.readString();
                            break;
                        }
                        reader.skip(2);
                        short nodePathOffset = reader.readInt2();
                        reader.setOffset(entryStart + offset);
                        dfsPath = reader.readString();
                        reader.setOffset(entryStart + nodePathOffset);
                        netPath = reader.readString();
                        break;
                    }
                    default: {
                        return;
                    }
                }
                DfsReferral ref = new DfsReferral();
                ref.numPathConsumed = pathConsumed / 2 + 1;
                ref.serverType = serverType;
                ref.flags = flags;
                ref.ttl = ttl;
                ref.originalTtl = originalTtl;
                if (null != dfsPath) {
                    ref.dfsPath = dfsPath.toLowerCase();
                }
                if (null != netPath) {
                    ref.netPath = netPath.toLowerCase();
                }
                ref.isConnected = false;
                ref.isIOPerformed = false;
                ref.lastIOStatus = 0L;
                this.list.add(ref);
                reader.setOffset(entryStart + size);
            }
        }
    }
}

