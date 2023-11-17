/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.msdfsc;

import com.hierynomus.msdfsc.DFSPath;
import com.hierynomus.msdfsc.DomainCache;
import com.hierynomus.msdfsc.messages.DFSReferral;
import com.hierynomus.msdfsc.messages.SMB2GetDFSReferralResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class ReferralCache {
    private ReferralCacheNode cacheRoot = new ReferralCacheNode("<root>");

    public ReferralCacheEntry lookup(DFSPath dfsPath) {
        List<String> pathComponents = dfsPath.getPathComponents();
        ReferralCacheEntry referralEntry = this.cacheRoot.getReferralEntry(pathComponents.iterator());
        return referralEntry;
    }

    public void put(ReferralCacheEntry referralCacheEntry) {
        List<String> pathComponents = new DFSPath(referralCacheEntry.dfsPathPrefix).getPathComponents();
        this.cacheRoot.addReferralEntry(pathComponents.iterator(), referralCacheEntry);
    }

    public void clear() {
        this.cacheRoot.clear();
    }

    private static class ReferralCacheNode {
        static final AtomicReferenceFieldUpdater<ReferralCacheNode, ReferralCacheEntry> ENTRY_UPDATER = AtomicReferenceFieldUpdater.newUpdater(ReferralCacheNode.class, ReferralCacheEntry.class, "entry");
        private final String pathComponent;
        private final Map<String, ReferralCacheNode> childNodes = new ConcurrentHashMap<String, ReferralCacheNode>();
        private volatile ReferralCacheEntry entry;

        ReferralCacheNode(String pathComponent) {
            this.pathComponent = pathComponent;
        }

        void addReferralEntry(Iterator<String> pathComponents, ReferralCacheEntry entry) {
            if (pathComponents.hasNext()) {
                String component = pathComponents.next().toLowerCase();
                ReferralCacheNode referralCacheNode = this.childNodes.get(component);
                if (referralCacheNode == null) {
                    referralCacheNode = new ReferralCacheNode(component);
                    this.childNodes.put(component, referralCacheNode);
                }
                referralCacheNode.addReferralEntry(pathComponents, entry);
            } else {
                ENTRY_UPDATER.set(this, entry);
            }
        }

        ReferralCacheEntry getReferralEntry(Iterator<String> pathComponents) {
            String component;
            ReferralCacheNode referralCacheNode;
            if (pathComponents.hasNext() && (referralCacheNode = this.childNodes.get(component = pathComponents.next().toLowerCase())) != null) {
                return referralCacheNode.getReferralEntry(pathComponents);
            }
            return ENTRY_UPDATER.get(this);
        }

        void clear() {
            this.childNodes.clear();
            ENTRY_UPDATER.set(this, null);
        }
    }

    public static class ReferralCacheEntry {
        private final String dfsPathPrefix;
        private final DFSReferral.ServerType rootOrLink;
        private final boolean interlink;
        private final int ttl;
        private final long expires;
        private final boolean targetFailback;
        private final TargetSetEntry targetHint;
        private final List<TargetSetEntry> targetList;

        public ReferralCacheEntry(SMB2GetDFSReferralResponse response, DomainCache domainCache) {
            boolean interlink;
            List<DFSReferral> referralEntries = response.getReferralEntries();
            for (DFSReferral referralEntry : referralEntries) {
                if (referralEntry.getPath() != null) continue;
                throw new IllegalStateException("Path cannot be null for a ReferralCacheEntry?");
            }
            DFSReferral firstReferral = referralEntries.get(0);
            this.dfsPathPrefix = firstReferral.getDfsPath();
            this.rootOrLink = firstReferral.getServerType();
            boolean bl = interlink = response.getReferralHeaderFlags().contains(SMB2GetDFSReferralResponse.ReferralHeaderFlags.ReferralServers) && !response.getReferralHeaderFlags().contains(SMB2GetDFSReferralResponse.ReferralHeaderFlags.StorageServers);
            if (!interlink && referralEntries.size() == 1) {
                List<String> pathEntries = new DFSPath(firstReferral.getPath()).getPathComponents();
                interlink = domainCache.lookup(pathEntries.get(0)) != null;
            }
            this.interlink = interlink;
            this.ttl = firstReferral.getTtl();
            this.expires = System.currentTimeMillis() + (long)this.ttl * 1000L;
            this.targetFailback = response.getReferralHeaderFlags().contains(SMB2GetDFSReferralResponse.ReferralHeaderFlags.TargetFailback);
            ArrayList<TargetSetEntry> targetList = new ArrayList<TargetSetEntry>(referralEntries.size());
            for (DFSReferral r : referralEntries) {
                TargetSetEntry e = new TargetSetEntry(r.getPath(), false);
                targetList.add(e);
            }
            this.targetHint = (TargetSetEntry)targetList.get(0);
            this.targetList = Collections.unmodifiableList(targetList);
        }

        public boolean isExpired() {
            long now = System.currentTimeMillis();
            return now > this.expires;
        }

        public boolean isLink() {
            return this.rootOrLink == DFSReferral.ServerType.LINK;
        }

        public boolean isRoot() {
            return this.rootOrLink == DFSReferral.ServerType.ROOT;
        }

        public boolean isInterlink() {
            return this.isLink() && this.interlink;
        }

        public String getDfsPathPrefix() {
            return this.dfsPathPrefix;
        }

        public TargetSetEntry getTargetHint() {
            return this.targetHint;
        }

        public List<TargetSetEntry> getTargetList() {
            return this.targetList;
        }

        public String toString() {
            return this.dfsPathPrefix + "->" + this.targetHint.targetPath + "(" + this.rootOrLink + "), " + this.targetList;
        }
    }

    public static class TargetSetEntry {
        final String targetPath;
        final boolean targetSetBoundary;

        public TargetSetEntry(String targetPath, boolean targetSetBoundary) {
            this.targetPath = targetPath;
            this.targetSetBoundary = targetSetBoundary;
        }

        public String getTargetPath() {
            return this.targetPath;
        }

        public String toString() {
            return "TargetSetEntry[" + this.targetPath + ",targetSetBoundary=" + this.targetSetBoundary + "]";
        }
    }
}

