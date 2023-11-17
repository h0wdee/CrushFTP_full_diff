/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.smbj.paths;

import com.hierynomus.msdfsc.DFSException;
import com.hierynomus.msdfsc.DFSPath;
import com.hierynomus.msdfsc.DomainCache;
import com.hierynomus.msdfsc.ReferralCache;
import com.hierynomus.msdfsc.messages.SMB2GetDFSReferralRequest;
import com.hierynomus.msdfsc.messages.SMB2GetDFSReferralResponse;
import com.hierynomus.mserref.NtStatus;
import com.hierynomus.mssmb2.SMB2Header;
import com.hierynomus.mssmb2.SMB2Packet;
import com.hierynomus.mssmb2.messages.SMB2IoctlResponse;
import com.hierynomus.protocol.commons.buffer.Buffer;
import com.hierynomus.protocol.commons.concurrent.Futures;
import com.hierynomus.protocol.transport.TransportException;
import com.hierynomus.smb.SMBBuffer;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.common.SmbPath;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.io.BufferByteChunkProvider;
import com.hierynomus.smbj.paths.PathResolveException;
import com.hierynomus.smbj.paths.PathResolver;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.Share;
import com.hierynomus.smbj.share.StatusHandler;
import java.io.IOException;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DFSPathResolver
implements PathResolver {
    private static final Logger logger = LoggerFactory.getLogger(DFSPathResolver.class);
    private static final long FSCTL_DFS_GET_REFERRALS = 393620L;
    private static final long FSCTL_DFS_GET_REFERRALS_EX = 393648L;
    private final StatusHandler statusHandler;
    private final PathResolver wrapped;
    private ReferralCache referralCache = new ReferralCache();
    private DomainCache domainCache = new DomainCache();

    public DFSPathResolver(final PathResolver wrapped) {
        this.wrapped = wrapped;
        this.statusHandler = new StatusHandler(){

            @Override
            public boolean isSuccess(long statusCode) {
                return statusCode == NtStatus.STATUS_PATH_NOT_COVERED.getValue() || wrapped.statusHandler().isSuccess(statusCode);
            }
        };
    }

    @Override
    public SmbPath resolve(Session session, SMB2Packet responsePacket, SmbPath smbPath) throws PathResolveException {
        if (smbPath.getPath() != null && ((SMB2Header)responsePacket.getHeader()).getStatusCode() == NtStatus.STATUS_PATH_NOT_COVERED.getValue()) {
            logger.info("DFS Share {} does not cover {}, resolve through DFS", (Object)smbPath.getShareName(), (Object)smbPath);
            SmbPath target = SmbPath.parse(this.resolve(session, smbPath.toUncPath()));
            logger.info("DFS resolved {} -> {}", (Object)smbPath, (Object)target);
            return target;
        }
        if (smbPath.getPath() == null && NtStatus.isError(((SMB2Header)responsePacket.getHeader()).getStatusCode())) {
            logger.info("Attempting to resolve {} through DFS", (Object)smbPath);
            return SmbPath.parse(this.resolve(session, smbPath.toUncPath()));
        }
        return this.wrapped.resolve(session, responsePacket, smbPath);
    }

    @Override
    public StatusHandler statusHandler() {
        return this.statusHandler;
    }

    @Override
    public SmbPath resolve(Session session, SmbPath smbPath) throws PathResolveException {
        SmbPath target = SmbPath.parse(this.resolve(session, smbPath.toUncPath()));
        if (!smbPath.equals(target)) {
            logger.info("DFS resolved {} -> {}", (Object)smbPath, (Object)target);
            return target;
        }
        return this.wrapped.resolve(session, smbPath);
    }

    private String resolve(Session session, String uncPath) throws PathResolveException {
        logger.info("Starting DFS resolution for {}", (Object)uncPath);
        DFSPath dfsPath = new DFSPath(uncPath);
        ResolveState state = new ResolveState(dfsPath);
        DFSPath resolved = this.step1(session, state);
        return resolved.toPath();
    }

    private DFSPath step1(Session session, ResolveState state) throws DFSException {
        logger.trace("DFS[1]: {}", (Object)state);
        if (state.path.hasOnlyOnePathComponent() || state.path.isIpc()) {
            return this.step12(state);
        }
        return this.step2(session, state);
    }

    private DFSPath step2(Session session, ResolveState state) throws DFSException {
        logger.trace("DFS[2]: {}", (Object)state);
        ReferralCache.ReferralCacheEntry lookup = this.referralCache.lookup(state.path);
        if (lookup == null || lookup.isExpired() && lookup.isRoot()) {
            return this.step5(session, state);
        }
        if (lookup.isExpired()) {
            return this.step9(session, state, lookup);
        }
        if (lookup.isLink()) {
            return this.step4(session, state, lookup);
        }
        return this.step3(session, state, lookup);
    }

    private DFSPath step3(Session session, ResolveState state, ReferralCache.ReferralCacheEntry lookup) {
        logger.trace("DFS[3]: {}", (Object)state);
        state.path = state.path.replacePrefix(lookup.getDfsPathPrefix(), lookup.getTargetHint().getTargetPath());
        state.isDFSPath = true;
        return this.step8(session, state, lookup);
    }

    private DFSPath step4(Session session, ResolveState state, ReferralCache.ReferralCacheEntry lookup) throws DFSException {
        logger.trace("DFS[4]: {}", (Object)state);
        if (state.path.isSysVolOrNetLogon()) {
            return this.step3(session, state, lookup);
        }
        if (lookup.isInterlink()) {
            return this.step11(session, state, lookup);
        }
        return this.step3(session, state, lookup);
    }

    private DFSPath step5(Session session, ResolveState state) throws DFSException {
        logger.trace("DFS[5]: {}", (Object)state);
        String potentialDomain = state.path.getPathComponents().get(0);
        DomainCache.DomainCacheEntry domainCacheEntry = this.domainCache.lookup(potentialDomain);
        if (domainCacheEntry == null) {
            state.hostName = potentialDomain;
            state.resolvedDomainEntry = false;
            return this.step6(session, state);
        }
        if (domainCacheEntry.getDCHint() == null || domainCacheEntry.getDCHint().isEmpty()) {
            String bootstrapDC = session.getAuthenticationContext().getDomain();
            ReferralResult result = this.sendDfsReferralRequest(DfsRequestType.DC, bootstrapDC, session, state.path);
            if (!NtStatus.isSuccess(result.status)) {
                return this.step13(session, state, result);
            }
            domainCacheEntry = result.domainCacheEntry;
        }
        if (state.path.isSysVolOrNetLogon()) {
            return this.step10(session, state, domainCacheEntry);
        }
        state.hostName = domainCacheEntry.getDCHint();
        state.resolvedDomainEntry = true;
        return this.step6(session, state);
    }

    private DFSPath step6(Session session, ResolveState state) throws DFSException {
        logger.trace("DFS[6]: {}", (Object)state);
        ReferralResult result = this.sendDfsReferralRequest(DfsRequestType.ROOT, state.path.getPathComponents().get(0), session, state.path);
        if (NtStatus.isSuccess(result.status)) {
            return this.step7(session, state, result.referralCacheEntry);
        }
        if (state.resolvedDomainEntry) {
            return this.step13(session, state, result);
        }
        if (state.isDFSPath) {
            return this.step14(session, state, result);
        }
        return this.step12(state);
    }

    private DFSPath step7(Session session, ResolveState state, ReferralCache.ReferralCacheEntry lookup) throws DFSException {
        logger.trace("DFS[7]: {}", (Object)state);
        if (lookup.isRoot()) {
            return this.step3(session, state, lookup);
        }
        return this.step4(session, state, lookup);
    }

    private DFSPath step8(Session session, ResolveState state, ReferralCache.ReferralCacheEntry lookup) {
        logger.trace("DFS[8]: {}", (Object)state);
        return state.path;
    }

    private DFSPath step9(Session session, ResolveState state, ReferralCache.ReferralCacheEntry lookup) throws DFSException {
        logger.trace("DFS[9]: {}", (Object)state);
        DFSPath rootPath = new DFSPath(state.path.getPathComponents().subList(0, 2));
        ReferralCache.ReferralCacheEntry rootReferralCacheEntry = this.referralCache.lookup(rootPath);
        if (rootReferralCacheEntry == null) {
            throw new IllegalStateException("Could not find referral cache entry for " + rootPath);
        }
        ReferralResult result = this.sendDfsReferralRequest(DfsRequestType.LINK, rootReferralCacheEntry.getTargetHint().getTargetPath(), session, state.path);
        if (!NtStatus.isSuccess(result.status)) {
            return this.step14(session, state, result);
        }
        if (result.referralCacheEntry.isRoot()) {
            return this.step3(session, state, result.referralCacheEntry);
        }
        return this.step4(session, state, result.referralCacheEntry);
    }

    private DFSPath step10(Session session, ResolveState state, DomainCache.DomainCacheEntry domainCacheEntry) throws DFSException {
        logger.trace("DFS[10]: {}", (Object)state);
        ReferralResult r = this.sendDfsReferralRequest(DfsRequestType.SYSVOL, domainCacheEntry.getDCHint(), session, state.path);
        if (NtStatus.isSuccess(r.status)) {
            return this.step3(session, state, r.referralCacheEntry);
        }
        return this.step13(session, state, r);
    }

    private DFSPath step11(Session session, ResolveState state, ReferralCache.ReferralCacheEntry lookup) throws DFSException {
        logger.trace("DFS[11]: {}", (Object)state);
        state.path = state.path.replacePrefix(lookup.getDfsPathPrefix(), lookup.getTargetHint().getTargetPath());
        state.isDFSPath = true;
        return this.step2(session, state);
    }

    private DFSPath step12(ResolveState state) {
        logger.trace("DFS[12]: {}", (Object)state);
        return state.path;
    }

    private DFSPath step13(Session session, ResolveState state, ReferralResult result) throws DFSException {
        logger.trace("DFS[13]: {}", (Object)state);
        throw new DFSException(result.status, "Cannot get DC for domain '" + state.path.getPathComponents().get(0) + "'");
    }

    private DFSPath step14(Session session, ResolveState state, ReferralResult result) throws DFSException {
        logger.trace("DFS[14]: {}", (Object)state);
        throw new DFSException(result.status, "DFS request failed for path " + state.path);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private ReferralResult sendDfsReferralRequest(DfsRequestType type, String hostName, Session session, DFSPath path) throws DFSException {
        Session dfsSession = session;
        if (!hostName.equals(session.getConnection().getRemoteHostname())) {
            Connection connection;
            AuthenticationContext auth = session.getAuthenticationContext();
            Connection oldConnection = session.getConnection();
            try {
                connection = oldConnection.getClient().connect(hostName);
            }
            catch (IOException e) {
                throw new DFSException(e);
            }
            dfsSession = connection.authenticate(auth);
        }
        try (Share dfsShare = dfsSession.connectShare("IPC$");){
            ReferralResult referralResult = this.getReferral(type, dfsShare, path);
            return referralResult;
        }
        catch (Buffer.BufferException | IOException e) {
            throw new DFSException(e);
        }
    }

    private ReferralResult getReferral(DfsRequestType type, Share share, DFSPath path) throws TransportException, Buffer.BufferException {
        SMB2GetDFSReferralRequest req = new SMB2GetDFSReferralRequest(path.toPath());
        SMBBuffer buffer = new SMBBuffer();
        req.writeTo(buffer);
        Future<SMB2IoctlResponse> ioctl = share.ioctlAsync(393620L, true, new BufferByteChunkProvider(buffer));
        SMB2IoctlResponse response = Futures.get(ioctl, TransportException.Wrapper);
        return this.handleReferralResponse(type, response, path);
    }

    private ReferralResult handleReferralResponse(DfsRequestType type, SMB2IoctlResponse response, DFSPath originalPath) throws Buffer.BufferException {
        ReferralResult result = new ReferralResult(((SMB2Header)response.getHeader()).getStatusCode());
        if (result.status == NtStatus.STATUS_SUCCESS.getValue()) {
            SMB2GetDFSReferralResponse resp = new SMB2GetDFSReferralResponse(originalPath.toPath());
            resp.read(new SMBBuffer(response.getOutputBuffer()));
            switch (type) {
                case DC: {
                    this.handleDCReferralResponse(result, resp);
                    break;
                }
                case DOMAIN: {
                    throw new UnsupportedOperationException((Object)((Object)DfsRequestType.DOMAIN) + " not used yet.");
                }
                case SYSVOL: 
                case ROOT: 
                case LINK: {
                    this.handleRootOrLinkReferralResponse(result, resp);
                    break;
                }
                default: {
                    throw new IllegalStateException("Encountered unhandled DFS RequestType: " + (Object)((Object)type));
                }
            }
        }
        return result;
    }

    private void handleRootOrLinkReferralResponse(ReferralResult result, SMB2GetDFSReferralResponse response) {
        if (response.getReferralEntries().isEmpty()) {
            result.status = NtStatus.STATUS_OBJECT_PATH_NOT_FOUND.getValue();
            return;
        }
        ReferralCache.ReferralCacheEntry referralCacheEntry = new ReferralCache.ReferralCacheEntry(response, this.domainCache);
        logger.info("Got DFS Referral result: {}", (Object)referralCacheEntry);
        this.referralCache.put(referralCacheEntry);
        result.referralCacheEntry = referralCacheEntry;
    }

    private void handleDCReferralResponse(ReferralResult result, SMB2GetDFSReferralResponse response) {
        if (response.getVersionNumber() < 3) {
            return;
        }
        DomainCache.DomainCacheEntry domainCacheEntry = new DomainCache.DomainCacheEntry(response);
        this.domainCache.put(domainCacheEntry);
        result.domainCacheEntry = domainCacheEntry;
    }

    private static class ReferralResult {
        long status;
        ReferralCache.ReferralCacheEntry referralCacheEntry;
        DomainCache.DomainCacheEntry domainCacheEntry;

        private ReferralResult(long status) {
            this.status = status;
        }

        private ReferralResult(ReferralCache.ReferralCacheEntry referralCacheEntry) {
            this.referralCacheEntry = referralCacheEntry;
        }

        private ReferralResult(DomainCache.DomainCacheEntry domainCacheEntry) {
            this.domainCacheEntry = domainCacheEntry;
        }
    }

    private static class ResolveState {
        DFSPath path;
        boolean resolvedDomainEntry = false;
        boolean isDFSPath = false;
        String hostName = null;

        ResolveState(DFSPath path) {
            this.path = path;
        }

        public String toString() {
            return "ResolveState{path=" + this.path + ", resolvedDomainEntry=" + this.resolvedDomainEntry + ", isDFSPath=" + this.isDFSPath + ", hostName='" + this.hostName + '\'' + '}';
        }
    }

    private static enum DfsRequestType {
        DOMAIN,
        DC,
        SYSVOL,
        ROOT,
        LINK;

    }
}

